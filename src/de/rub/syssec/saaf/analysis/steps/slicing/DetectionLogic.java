/* SAAF: A static analyzer for APK files.
 * Copyright (C) 2013  syssec.rub.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.rub.syssec.saaf.analysis.steps.slicing;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.analysis.steps.slicing.TodoList.ClassContentTracker;
import de.rub.syssec.saaf.analysis.steps.slicing.TodoList.RegisterSearch;
import de.rub.syssec.saaf.application.instructions.Constant;
import de.rub.syssec.saaf.application.instructions.Instruction;
import de.rub.syssec.saaf.application.methods.BasicBlock;
import de.rub.syssec.saaf.application.methods.Method;
import de.rub.syssec.saaf.misc.ByteUtils;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.BasicBlockInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.ClassOrMethodNotFoundException;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.ConstantInterface;
import de.rub.syssec.saaf.model.application.DetectionLogicError;
import de.rub.syssec.saaf.model.application.FieldInterface;
import de.rub.syssec.saaf.model.application.MethodInterface;
import de.rub.syssec.saaf.model.application.SyntaxException;
import de.rub.syssec.saaf.model.application.ConstantInterface.VariableType;
import de.rub.syssec.saaf.model.application.instruction.InstructionInterface;
import de.rub.syssec.saaf.model.application.instruction.InstructionType;

/**
 * This class works with SmaliClasss and is able to search for constants in the smali
 * code which are passed as parameters to defined functions. This is basically the
 * program slicing algorithm.
 * 
 * @author Johannes Hoffmann <johannes.hoffmann@rub.de>
 *
 */
public class DetectionLogic {
	
	private static final boolean DEBUG=Boolean.parseBoolean(System.getProperty("debug.slicing","false"));
	private final ApplicationInterface app;
	private SlicingCriterion backtrackRequest;
	private TodoList todoList = new TodoList();
	
	/**
	 * Each Constant which is found for one specific Codeline has the same searchId.
	 * This is useful to find Constant which are part of one method invocation but
	 * are used as different method parameters. The same method invocation in different
	 * parts of the code will have a different searchId.  
	 * 
	 * Eg: sentTextMessage(nr, ..., text, ...)
	 */
	private int searchId = 0;
	private final HashMap<CodeLineInterface, Integer> searchIdMap = new HashMap<CodeLineInterface, Integer>();
	private int globalSearchId = 0;
	
	private static final int MAX_ITERATIONS = Integer.MAX_VALUE;
	
	private static final Logger LOGGER = Logger.getLogger(DetectionLogic.class);
	
	private static final byte[] P0_THIS = "p0".getBytes();
	
		
	/**
	 * Create a new DetectionLogic for some given application.
	 * @param app the application to be analyzed
	 */
	public DetectionLogic(ApplicationInterface app) {
		this.app = app;
	}
	
	/**
	 * This holds the class, the method and its parameters we're currently searching for
	 */
	private byte[][] cmp = new byte[3][];

	/**
	 * Search the application code for a calls/invokes to a given class and method and determine all constants which can be assigned to
	 * a given parameter.
	 * 
	 * Errors which occur during the search will be logged but will not abort all sub-searches, only the currently failing one!
	 * 
	 * This method will build the def-use chains (slices).
	 * 
	 * WARNING: This method is not threadsafe!
	 * 
	 * @param breq the request to search for, results will be added to this request
	 * @throws DetectionLogicError if the register index is not appropriate or if the search does not seem to terminate
	 */
	public void search(SlicingCriterion backtrackRequest) throws DetectionLogicError {
		cmp = backtrackRequest.getClassAndMethodAndParameter();
		int regToTrack = backtrackRequest.getParameterIndex();
		// clean up, it may be called multiple times
		this.backtrackRequest = backtrackRequest;
		
		
		if (regToTrack < 0 || regToTrack > 65535) {
			throw new DetectionLogicError("Register index must not be negative or too big");
		}
		
		/*
		 * Get a list of all invokes and start a search for each one. Each search gets a unique searchId which is assigned to all
		 * constants which are found for the corresponding register/invoke.
		 */
		LinkedList<RegisterSearch> rsList = findInvokes(cmp, regToTrack, 0, new LinkedList<BasicBlockInterface>()); // new empty path list
		if (DEBUG) LOGGER.debug("Found "+rsList.size()+" INVOKES for "+new String(cmp[0])+"."+new String(cmp[1])+", paraIndex="+regToTrack);
		for (RegisterSearch rs : rsList) {
			todoList.addRegisterToTrack(rs);
			/*
			 * Set up searchid. Each Codeline where a search begins gets the same id.
			 * This is useful to match Constants which are found for the same
			 * invocation but for different arguments.
			 */
//			// FIXME
////			System.out.println(" bb: "+rs.getBB().);
//			LOGGER.debug("\n\n\nFound "+rsList.size()+" INVOKES for "+new String(cmp[0])+"."+new String(cmp[1])+", paraIndex="+regToTrack);
//			for (CodeLineInterface cl : rs.getBB().getCodeLines()) {
//				System.out.println(" bb: "+cl);
//			}
//			System.out.println("#cl: "+rs.getBB().getCodeLines().size());
//			System.out.println(" #i: "+rs.getIndex());
//			System.out.println(" cl: "+rs.getBB().getCodeLines().get(rs.getIndex()));
//			
//			// FIXME
			CodeLineInterface cl = rs.getBB().getCodeLines().get(rs.getIndex());
			Integer mapId = searchIdMap.get(cl);
			if (mapId == null) {
				searchId = globalSearchId;
				searchIdMap.put(cl, searchId);
				globalSearchId++;
			}
			else {
				searchId = mapId;
			}
			
			startSearch(); // track the register for this invoke
			todoList = new TodoList(); // clean up
		}
	}
	
	
	
	/**
	 * Start the search. Search until the TodoList is empty. It may be "refilled" during each run. 
	 * @throws DetectionLogicError 
	 */
	private void startSearch() throws DetectionLogicError {
		if (DEBUG) LOGGER.debug("Starting search...");
		
		int loopCnt = 0;
		while (!todoList.isFinished()) {
			// Sanity check ;)
			loopCnt++;
			if (loopCnt == MAX_ITERATIONS) throw new DetectionLogicError("We're probably stuck in an endless loop while working through the TODO list. Aborting!");
			
			if (todoList.getFinishedRsCount() > TodoList.MAX_RS_COUNT) {
				throw new DetectionLogicError("Reached maximum RS limit ("+TodoList.MAX_RS_COUNT+")");
			}
			
			try {
				if (todoList.getRemainingRegistersToTrack() > 0) {
					RegisterSearch rs = todoList.getNextRegisterToTrack();
					backtrackRegister(rs);
				}
				else if (todoList.getRemainingReturnValuesFromMethods() > 0) {
					ClassContentTracker ctt =  todoList.getNextReturnValuesFromMethod();
					try {
						MethodInterface m = app.getMethodByClassAndName(new String(ctt.getCi()[0]), new String(ctt.getCi()[1]), ctt.getCi()[2], ctt.getCi()[3]);
						addAllReturnedRegistersFromMethod(m, ctt); // parse all return values
					}
					catch (ClassOrMethodNotFoundException e) {
						if (DEBUG) LOGGER.debug("Lost Track: "+e.getMessage());
					}
					
				}
				else if (todoList.getRemainingFieldsToTrack() > 0) {
					ClassContentTracker ctt = todoList.getNextField();
					backTrackField(ctt);
				}
				else if (todoList.getRemainingArraysToTrack() > 0) {
					ClassContentTracker ctt = todoList.getNextCaToTrack(); // ctt contains classname, arrayname etc
					findArrayGets(ctt); // find all codelines where an array is accessed after it was loaded
					findArrayPuts(ctt); // find all codelines where an array is created and accessed and later stored
				}
			}
			catch (SyntaxException e) {
				LOGGER.error("Syntax Error (Search continues)",e);
				backtrackRequest.logException(e);
			}
			catch (DetectionLogicError e) {
				LOGGER.error("Logic Error (Search continues)",e);
				backtrackRequest.logException(e);
			}
		}
		if (DEBUG) LOGGER.debug("Search finished.");
	}
	
	
	/**
	 * This method does the normal backtracking of a register in a BasicBlock. It will search through the BasicBlock and track access
	 * to the given register. It will act appropriate to the found opcodes.
	 * @throws SyntaxException 
	 * @throws DetectionLogicError 
	 */
	private void backtrackRegister(final RegisterSearch rs) throws SyntaxException, DetectionLogicError {
		byte[] register = rs.getRegister();
		BasicBlockInterface bb = rs.getBB();
		int actualLine = rs.getIndex();
		if (DEBUG) LOGGER.debug("RS Backtracking, reg="+new String(register)+", actualLine="+actualLine);
		/*
		 * Failsafe:
		 * If the method is not static and p0 is tracked, abort tracking. p0 is the this-reference and
		 * this will most likely mess things up, as every called method on the corresponding class etc
		 * will be tracked.
		 */
		if (!bb.getMethod().isStatic() && ByteUtils.equals(P0_THIS, register)) {
			if (DEBUG) LOGGER.debug(" Will not track p0 in non-static method ("+bb.getMethod().getName()+")");
			return;
		}
		
//		int fuzzyLevel = rs.getFuzzyLevel();
		/*
		 * Another failsafe is located in TodoList.java. If the fuzzy level is too high, we skip
		 * the request as the results get way too bloated.
		 */
		
		
		final LinkedList<BasicBlockInterface> path = rs.getPath();
		if (path.isEmpty() || !(path.getLast() == bb)) {
			// add the current BB to the path but no duplicates (at the end)
			path.addLast(bb); 
		}
		
		/*
		 * Check all opcodes in the BB. Actual line points to the CL which was found in the
		 * previous search. The next opcode of interest any previous opcode which relates
		 * to the tracked register. If the beginning of a BB is reached, this case is
		 * handled after this loop.
		 */
		while (actualLine > 0) { // look at all code lines in this BB
			CodeLineInterface cl = bb.getCodeLines().get(--actualLine); // look at the previous instruction
			
			// skip all non-code lines
			if (!cl.isCode()) continue;
			
			InstructionInterface i = cl.getInstruction(); // get the instruction and work with it
			if (DEBUG) LOGGER.debug(" Checking cl "+cl);
			
			switch (i.getType()) {
			
			case NEW_ARRAY:
			case NEW_INSTANCE:
				if (doesRegisterMatch(i, register)) {
					// TODO: We could also add this as a found constant, we know at least the class-type then
					// end here b/c the reg is created here and before this opcode it will contain unrelated data
					if (DEBUG) LOGGER.debug(" Lost track (new-instance/array)");
					return;
				}
				break;
			
			case JMP:
				// we do not care about JMPs etc b/c they only start/end BasicBlocks
				continue;
				
			case INVOKE_STATIC: // same as INVOKE
			case INVOKE:
				// Check if this invoke involves our currently searched method, if so, we do not need to investigate anything else
				if (Arrays.equals(i.getCalledClassAndMethodWithParameter()[0], cmp[0])
						&& Arrays.equals(i.getCalledClassAndMethodWithParameter()[1], cmp[1])
						&& (
								cmp[2] == null // See findInvokesAndAddToTodoList for more info for an empty method signature 
								|| Arrays.equals(i.getCalledClassAndMethodWithParameter()[2], cmp[2])
							)) {
					continue;
				}
				// Check if our register is involved in this invoke opcode
				// A method can also be invoked on an object, eg, StringBuilder.append(...), therefore start at ii = 0
				boolean found = false;
				for (int ii = 0; ii < i.getInvolvedRegisters().size(); ii++) {
					byte[] reg = i.getInvolvedRegisters().get(ii);
					if (Arrays.equals(register, reg)) {
						found = true;
						break;
					}
				}
				if (found) {
					/*
					 * We now track all return values from this method. In order to not track too many constants
					 * which might be unrelated, we set a large offset to the fuzzy value. This way, the fuzzy
					 * value in the results will be "normal", but if the fuzzyValue+offset gets too large, further
					 * searches will be cancelled. Since we do not know what happens in the methode, we do not
					 * want to track into methods which call the found found method and so on. We therefore set
					 * the offset to the maximum-2.
					 */
					int offset = rs.getFuzzyLevel();
					if (offset < TodoList.MAX_FUZZY_LEVEL-2) {
						offset = TodoList.MAX_FUZZY_LEVEL-2;
					}
					handleInvoke(bb, actualLine, register, false, rs.getFuzzyLevel(), offset, new LinkedList<BasicBlockInterface>(path)); // +1 b/c we already decremented it but need the current line
				}
				continue;
				
			case PUT:
				/*
				 * We can ignore PUTs as our tracked register is only copied to a field. If this field matters,
				 * at will get tracked later on (or was already) if this field shows up somewhere else where
				 * it is loaded (GET).  
				 */
				continue;
				
			case APUT:
				/*
				 * Ignore, as they are searched when we occur a AGET  
				 */
				if (doesRegisterMatch(i, register)) {
					/*
					 * We are backtracking an array object and something is written to this array. We therefore also backtrack
					 * the register which is APUTted into our array and continue our search. The current search will likely end
					 * when a new-array instruction is found with our register being the new array register.
					 */
					if (DEBUG) LOGGER.debug("Found an APUT. Seems we are backtracking an array, will also backtrack the putted register!");
					RegisterSearch rs2 = new RegisterSearch(i.getInvolvedRegisters().getFirst(), bb, actualLine, rs.getFuzzyLevel(), rs.getFuzzyOffset(), new LinkedList<BasicBlockInterface>(path));
					todoList.addRegisterToTrack(rs2);
					// continue the search
				}
				else {
					// ignore it
					continue;
				}				
				
			case MATH_1: // unary operations w/ only 1 target and 1 source reg
				if (doesRegisterMatch(i, register)) {
					byte[] involvedReg  = i.getInvolvedRegisters().getFirst(); // track the register which got applied to our old register
					if (!Arrays.equals(register, involvedReg)) {
						if (DEBUG) LOGGER.debug(" 2nd register is different from target, now tracking: "+new String(involvedReg));
						register = involvedReg;
					}
					else {
						if (DEBUG) LOGGER.debug(" 2nd register is the same as the tracked one, keep on tracking "+new String(register));
					}
				}
				continue; 
				
			case MATH_2: // binary operations w/ 1 target and 2 sources
				if (doesRegisterMatch(i, register)) {
					byte[] involvedReg1 = i.getInvolvedRegisters().getFirst();
					byte[] involvedReg2 = i.getInvolvedRegisters().getLast();
					// Check if only one or both register are different
					if (Arrays.equals(register, involvedReg1)) {
						involvedReg1 = null;
					}
					if (Arrays.equals(register, involvedReg2)) {
						involvedReg2 = null;
					}
					/*
					 * Track one register directly and add the second one as a new RS.
					 */
					RegisterSearch rs2;
					if (involvedReg1 != null && involvedReg2 != null) {
						if (DEBUG) LOGGER.debug(" 2nd register is different from target, now tracking: "+new String(involvedReg1));
						register = involvedReg1;
						rs2 = new RegisterSearch(involvedReg2, bb, actualLine, rs.getFuzzyLevel(), rs.getFuzzyOffset(), new LinkedList<BasicBlockInterface>(path)); // first and only operand register
						todoList.addRegisterToTrack(rs2); // Keep on track with this register
						if (DEBUG) LOGGER.debug(" Adding RS for 3rd register: "+new String(involvedReg2));
					}
					else if (involvedReg1 != null) {
						if (DEBUG) LOGGER.debug(" Only 2nd register is different from tracked one, keep on tracking "+new String(involvedReg1));
						rs2 = new RegisterSearch(involvedReg1, bb, actualLine, rs.getFuzzyLevel(), rs.getFuzzyOffset(), new LinkedList<BasicBlockInterface>(path)); // first and only operand register
						todoList.addRegisterToTrack(rs2); // Keep on track with this register
					}
					else if (involvedReg2 != null) {
						if (DEBUG) LOGGER.debug(" Only 3rd register is different from tracked one, keep on tracking "+new String(involvedReg2));
						rs2 = new RegisterSearch(involvedReg2, bb, actualLine, rs.getFuzzyLevel(), rs.getFuzzyOffset(), new LinkedList<BasicBlockInterface>(path)); // first and only operand register
						todoList.addRegisterToTrack(rs2); // Keep on track with this register
					}
				}	
				continue; 
				
			case MATH_2C: // binary operations w/ 1 target, 1 source and 1 constant instead of register
				if (doesRegisterMatch(i, register)) {
					ConstantInterface c = new Constant(cl, rs.getFuzzyLevel(), new LinkedList<BasicBlockInterface>(path), searchId);
					backtrackRequest.addFoundConstant(c);
					if (DEBUG) LOGGER.debug(" Found MATH const! cl="+cl);
					byte[] involvedReg = i.getInvolvedRegisters().getFirst();
					if (!Arrays.equals(register, involvedReg)) {
//						RegisterSearch rs3 = new RegisterSearch(i.getInvolvedRegisters().getFirst(), bb, actualLine, fuzzyLevel, new LinkedList<BasicBlockInterface>(path)); // first and only operand register
//						todoList.addRegisterToTrack(rs3); // Keep on track with this register
						if (DEBUG) LOGGER.debug(" 2nd register is different from target, now tracking: "+new String(involvedReg));
						register = involvedReg;
					}
					else {
						if (DEBUG) LOGGER.debug(" 2nd register is the same as the tracked one, keep on tracking "+new String(register));
					}
				}	
				continue; 
				
			case CONST:
				if (doesRegisterMatch(i, register)) {
					ConstantInterface c = new Constant(cl, rs.getFuzzyLevel(), new LinkedList<BasicBlockInterface>(path), searchId);
					backtrackRequest.addFoundConstant(c);
					if (DEBUG) LOGGER.debug("Found const! cl="+cl);
					return;
				}	
				break;
			
			case GET: // some field is loaded into our register
				if (doesRegisterMatch(i, register)) {
					// parse the fieldname+class
					byte[][] cf = Instruction.parseClassAndField(i.getInvolvedFields().getFirst());
					todoList.addField(cf, rs.getFuzzyLevel(), rs.getFuzzyOffset(), new LinkedList<BasicBlockInterface>(path));
					if (DEBUG) LOGGER.debug(" GET case, will later backtrack cf="+new String(cf[0])+"."+new String(cf[1]));
					return; // we're done, track the field later
				}
				continue;
				
			case AGET:
				if (Arrays.equals(i.getResultRegister(), register)) {
					// entering arraymode
					byte[] arrayReg = i.getInvolvedRegisters().getFirst();
					if (DEBUG) LOGGER.debug(" AGET case, entering ARRAY mode, arrayReg="+new String(arrayReg));
					arrayMode(arrayReg, actualLine, rs.getFuzzyLevel(), rs.getFuzzyOffset(), new LinkedList<BasicBlockInterface>(path));
					if (DEBUG) LOGGER.debug(" Finished ARRAY mode");
					return; // abort here, backtracking array accesses etc which were not found in arrayMode-method the will be done later on
				}
				break;
				
			case FILL_ARRAY_DATA:
				if (Arrays.equals(register, i.getResultRegister())) {
					ConstantInterface c = new Constant(cl, rs.getFuzzyLevel(), new LinkedList<BasicBlockInterface>(path), searchId);
					backtrackRequest.addFoundConstant(c);
					if (DEBUG) LOGGER.debug(" Found a FILL_ARRAY_DATA constant! "+cl);
					return; // stop
				}
				break;
				
			case MOVE:
				if (doesRegisterMatch(i, register)) {
					register = i.getInvolvedRegisters().getFirst(); // track the register which got moved to our old register
					if (DEBUG) LOGGER.debug(" MOVE: now tracking "+new String(register));
				}
				continue;
				
			case MOVE_RESULT:
				/*
				 * MOVE_RESULT are moving results from INVOKES or FILLED_NEW_ARRAY opcodes into a register. If we have a MOVE_RESULT
				 * opcode, we need to find the previous opcode and handle it appropriately.
				 */
				if (doesRegisterMatch(i, register)) {
					if (DEBUG) LOGGER.debug(" MOVE_RESULT case");
					BasicBlock.FoundCodeLine fcl = null;
					fcl = getPreviousOpcode(bb, cl); 
						
					// We have to fix the path if we found it in another BB
					LinkedList<BasicBlockInterface> fixedPath = new LinkedList<BasicBlockInterface>(path);
					if (fixedPath.getLast() != fcl.getBasicBlock()) {
						fixedPath.addLast(fcl.getBasicBlock());
					}
					
					InstructionInterface ii = fcl.getCodeLine().getInstruction();
					if (ii.getType() == InstructionType.INVOKE || ii.getType() == InstructionType.INVOKE_STATIC) {
						if (DEBUG) LOGGER.debug("  Found INVOKE");
						handleInvoke(fcl.getBasicBlock(), fcl.getIndex(), register, true, rs.getFuzzyLevel(), rs.getFuzzyOffset(), fixedPath);
						return;
					}
					else if (ii.getType() == InstructionType.FILLED_NEW_ARRAY) {
						if (DEBUG) LOGGER.debug("  Found FILLED_NEW_ARRAY");
						handleFilledNewArray(fcl.getBasicBlock(), fcl.getCodeLine(), fcl.getIndex(), rs.getFuzzyLevel(), rs.getFuzzyLevel(), fixedPath);
						return;
					}
					else {
						throw new DetectionLogicError("MOVE_RESULT: The previous opcode was not invoke-x nor filled-new-array! cl="+fcl.getCodeLine());
					}
				}
				continue;
				
			case IGNORE:
				// ignore, see JMP
				continue;
			
			case INTERNAL_SMALI_OPCODE:
				/*
				 * This could be a valid operation, if, eg, array-length is used in a weird way or if an Exception is used in some
				 * tracked method etc. We have to stop here, but we should nevertheless log the occurrence of this.
				 */
				if (doesRegisterMatch(i, register)) {
					LOGGER.info("Found an internal method which overwrote our register "+new String(register)+". Adding as constant! cl="+cl);
					ConstantInterface c = new Constant(cl, rs.getFuzzyLevel(), new LinkedList<BasicBlockInterface>(path), searchId);
					backtrackRequest.addFoundConstant(c);
					return;
				}
				continue;
			
			case NOP:
				// nothing to do here :) Should be used to align code
				continue;
			
			case RETURN:
				/*
				 * If we find a return in the middle of the BB the following might have happened:
				 * 
				 * 1) Someone patched the code and premature returns something. This if fine
				 * and the dex parser/optimizer will not complain about this. But this
				 * yields to dead code in at least the patched BB.
				 * 
				 * 2) We find a return as the last statement in a block (no dead code) and the
				 * current BB is a try block and the last BB where we are coming from is a
				 * catch block, this is also fine. Although the return will not throw any
				 * exception, the code is fine. Normally the BB would reside in another BB
				 * with appropriate goto statements and labels.
				 * 
				 * 3) A return normally ends the BB and no outgoing edge should be there such
				 * that we cannot find this BB by a backwards search. This is the normal case.
				 */
				if (!bb.hasDeadCode()) {
					boolean error = true;
					if (path.size() > 1) {
						int index = path.size()-2;
						BasicBlockInterface lastBB = path.get(index);
						if (lastBB.isCatchBlock() && bb.isTryBlock()) {
							/*
							 * We are in a try block, the last one is a catch block and the return is the last opcode.
							 * The search continues here.
							 */
							error = false;
							if (DEBUG) LOGGER.debug("Ignoring RETURN, try/catch and no dead code.");
//							throw new DetectionLogicError("Ignoring RETURN, try/catch and no dead code.");
						}
					}
					if (error) {
						// this should not happen
						throw new DetectionLogicError("Found unexpected RETURN opcode! cl="+cl/*+"in file:"+cl.getSmaliClass().getFile().getAbsolutePath()+" in method:"+cl.getMethod()*/);
					}
					
				}
				else {
					// Some patched a return?
					LOGGER.info("Found a RETURN opcode and dead code. Method patched/cracked?! Aborting this search.");
					return;
				}
				
			default:
				if (DEBUG) LOGGER.debug(" Did not handle opcode "+i.getType()+"/"+new String(i.getOpCode())+" (default)");
				continue;
			}
		} // while end
		/*
		 * We reached the beginning of the BB, now check if we also
		 * reached the beginning of the method. If so and if we are
		 * tracking a pX register, we will backtrack access to this
		 * (the current) method w/ the corresponding parameter
		 * index.
		 */
		LinkedList<BasicBlockInterface> previousBBs = bb.getPreviousBB();
		if (previousBBs == null || previousBBs.isEmpty()) { // We reached the beginning of a method
			if (DEBUG) LOGGER.debug("Reached end of BB and are in the first BB of the method.");
			if (ByteUtils.startsWith(register, new byte[] { 'p' })) {
				 /*
				  * We have a parameter index, pX. We now need to look at calls for this method w/ the
				  * corresponding parameter.
				  */
				int parameterIndex = Integer.parseInt(new String(register).substring(1)); // cut the 'p'
				if (!bb.getMethod().isStatic()) {
					/*
					 * p0 is the class instance if the method is not static, otherwise
					 * it is the first parameter. Method parameters start at 0, so we have to
					 * subtract 1 if we are not in a static method, b/c of p0 and therefore p1
					 * being the first parameter.
					 */
					parameterIndex--;
					if (DEBUG) LOGGER.debug(" We're NOT inside a STATIC method and are searching "+new String(register)+": decreasing parameter index to "+parameterIndex);
				}
				byte[][] method = bb.getMethod().getCmp();
				if (DEBUG) LOGGER.debug(" Searching for INVOKES to method "+new String(method[0])+"."+new String(method[1])+", paraIndex="+parameterIndex);
				// Increase the fuzzy value by 1 in order to not get exceptional long paths over different methods
				int foundInvokes = findInvokesAndAddToTodoList(method, parameterIndex, rs.getFuzzyLevel()+1, rs.getFuzzyOffset(), new LinkedList<BasicBlockInterface>(path));
				if (DEBUG) LOGGER.debug(" Found "+foundInvokes+" INVOKES for our method "+new String(method[0])+"."+new String(method[1])+", paraIndex="+parameterIndex);
				/*
				 * If the method is never invoked, it is likely called over java.lang.reflect or is some entry point from, eg, the Android
				 * framework as android.content.BroadcastReceiver.onReceive(..) would be. We store this info and treat it as a constant.
				 */
				if (foundInvokes == 0) {
					if (DEBUG) LOGGER.debug("   Saving this method as a CONSTANT b/c no invokes were found.");
					// store the class and the method with the signature etc as the value, also save the register, eg, p1
					String value;
					value = bb.getMethod().getReadableJavaName()+", parameterIndex="+parameterIndex;
					ConstantInterface c = new Constant(bb.getMethod().getCodeLines().getFirst(), rs.getFuzzyLevel(), path, searchId, VariableType.UNCALLED_METHOD, value);
					backtrackRequest.addFoundConstant(c);
				}
			}
			else {
				if (DEBUG) LOGGER.debug(" Lost track of reg "+new String(register)+", no more BBs available (reached method beginning?)");
			}
			
		}
		else {
			/*
			 * Search for the register in all BBs "above" this one b/c we reached the beginning of the actual BB. Do this w/ a new
			 * RS object b/c that way we do not look into a BB more than once for the same register.
			 * 
			 */
			if (DEBUG) LOGGER.debug("Reached end of BB, adding RS for all previous blocks.");
			for (BasicBlockInterface bbb : previousBBs) {
				// bbb.getCodeLines().size() is an invalid index, but it will be decremented in the main loop before anything happens
				RegisterSearch rs2 = new RegisterSearch(register, bbb, bbb.getCodeLines().size(), rs.getFuzzyLevel(), rs.getFuzzyOffset(), new LinkedList<BasicBlockInterface>(path));
				todoList.addRegisterToTrack(rs2); // search in this BB (add to todo-list)
			}
		}
	}
		
	
	/**
	 * Find all PUTs to a given field from a given class. All PUTs are added to the TodoList as a RegisterSearch object.
	 * This way, all values are found which are written to a given field.
	 * @param cf the class and field
	 * @param fuzzyLevel
	 * @throws SyntaxException if a Constant could not be created for a Field
	 */
	private void backTrackField(ClassContentTracker ctt) throws SyntaxException {
		byte[][] cf = ctt.getCi();
		if (DEBUG) LOGGER.debug("Backtracking field "+new String(cf[0])+"."+new String(cf[1]));
		File ff = new File(app.getBytecodeDirectory(), new String(cf[0])+".smali");
		ClassInterface sf;
		if (!ff.exists() || ((sf =  app.getSmaliClass(ff)) == null)) {
			if (DEBUG) LOGGER.debug(" Lost track, FIELD (or class) not available, file="+ff.getAbsolutePath());
			return;
		}
		for (FieldInterface f : sf.getAllFields()) {
			if (DEBUG) LOGGER.debug(" Checking field "+f.getFieldName()+" in "+sf.getFile().getName());
			if (f.getFieldName().equals(new String(cf[1]))) {
				if (DEBUG) LOGGER.debug("  ...it matches our pattern!");
				// found it
				
				boolean isFinalAndStatic = f.isFinal() && f.isStatic();
				if (isFinalAndStatic) {
					// we already have the constant b/c it is assigned in the field declaration
					ConstantInterface c = new Constant(f.getCodeLine(), ctt.getFuzzyLevel(), ctt.getPath(), searchId);
					if (c.getValue() != null) { // if null, something gets assigned in <clinit>
						backtrackRequest.addFoundConstant(c);
						if (DEBUG) LOGGER.debug("  ...added to result list b/c it is static and final w/ value! We're done.");
						return; // we're done
					}
					
				}
				if (isFinalAndStatic) if (DEBUG) LOGGER.debug("  ..found a static and final constant w/o any assigned value, need to parse <clinit>!");
				else if (DEBUG) LOGGER.debug("  ...not constant and static, checking field access!");
				/*
				 * If the field is final, it will get its value assigned in the constructor (<init>). We could
				 * be smart and parse only the constructor, but we are lazy and search for all PUTs
				 * which use this field, see below.
				 * 
				 * Additionally, if the field is only static and has a value set, a corresponding PUT is to be
				 * found in the static constructor (<clinit>).
				 * 
				 * We will now search all IPUTs and SPUTs which operate on this field and put them as a
				 * RegisterSearch into the TodoList.
				 */
				for (ClassInterface sf2 : app.getAllSmaliClasss(false)) {
					for (MethodInterface m : sf2.getMethods()) {
						if (isFinalAndStatic && !Method.STATIC_CONSTRUCTOR_NAME.equals(m.getName())) continue; // we only need to look into <clinit>.
						for (BasicBlockInterface bb : m.getBasicBlocks()) {
							for (int i=0; i<bb.getCodeLines().size(); i++) { // look at all put opcodes in all BBs
								CodeLineInterface cl = bb.getCodeLines().get(i);
								InstructionInterface instr = cl.getInstruction();
								if (instr.getType() == InstructionType.PUT) {
									// check the classname and fieldname
									if (Arrays.equals(cf[0], instr.getResultField()[0])	&& Arrays.equals(cf[1], instr.getResultField()[1])) {
										// we have a PUT opcode which puts into our field
										if (DEBUG) LOGGER.debug("    Found a valid xPUT in "+sf2.getFile().getName()+"."+m.getName()+"(...), adding reg "+new String(instr.getInvolvedRegisters().getFirst())+" to TodoList, cl="+cl);
										LinkedList<BasicBlockInterface> newPath = new LinkedList<BasicBlockInterface>(ctt.getPath());
										newPath.addLast(bb); // add the found BB to the path, TODO: indicate that Field accesses are searched instead of a normal BB search?
										RegisterSearch rs = new RegisterSearch(instr.getInvolvedRegisters().getFirst(), bb, i, ctt.getFuzzyLevel(), ctt.getFuzzyOffset(), newPath); // there is only one register involved
										todoList.addRegisterToTrack(rs); // add this reg to our todolist in order to continue search later on
									}
									else {
//										if (DEBUG) LOGGER.debug("    ...fieldname ("+new String(instr.getResultField()[1])+") did not match :(");
									}
								}
							}
						}
					}
				}
			}
			else if (DEBUG) LOGGER.debug(" Field does not match our search pattern!");
		}
	}
	
	/**
	 * Search for all RETURN opcodes in all BasicBlocks from a method and add the returned register as a RegisterSearch to the TodoList.
	 * @param m the method to search through
	 * @param ctt the ctt with additional information such as the fuzzy level
	 * @throws DetectionLogicError if the returned register in the return-opcode cannot be parsed
	 */
	private void addAllReturnedRegistersFromMethod(MethodInterface m, ClassContentTracker ctt) throws DetectionLogicError {
		LinkedList<BasicBlockInterface> path;
		for (BasicBlockInterface bb : m.getBasicBlocks()) {
			
			for (int i=0; i<bb.getCodeLines().size(); i++) {
				CodeLineInterface cl = bb.getCodeLines().get(i);
				InstructionInterface ii = cl.getInstruction();
				if (ii.getType() == InstructionType.RETURN) {
					/*
					 * We found a RETURN and will track the returned register later.
					 * 
					 * Some smali code uses return-void in non void method.
					 * Will this simply return null? Nevertheless, just stop
					 * here and handle the Nullpointer as no register is
					 * returned!
					 * 
					 * Example from:
					 * Lcom/nd/net/netengine/BufferData; (md5: e3acc3a60...)
					 * 
					 * # virtual methods
					 * .method public getByteBuffer()[B
					 * .locals 1
					 * .prologue
					 * return-void
					 * .end method
					 * 
					 * .method public getFileName()Ljava/lang/String;
					 * .locals 1
					 * .prologue
					 * return-void
					 * .end method
					 */ 
					// add found BB to path
					path = new LinkedList<BasicBlockInterface>(ctt.getPath());
					path.addLast(bb);
					if (ii.getInvolvedRegisters().size() > 0) { // Prevent NP
						RegisterSearch rs = new RegisterSearch(ii.getInvolvedRegisters().getFirst(), bb, i, ctt.getFuzzyLevel(), ctt.getFuzzyOffset(), path);
						todoList.addRegisterToTrack(rs);
					}
					else if (ii.getCodeLine().contains("return-void")) {
						if (DEBUG) LOGGER.debug("Found a non-void method returning w/ return-void!");
						continue;
					}
					else {
						throw new DetectionLogicError("Cannot parse returned register: "+cl);
					}
				}
			}
		}	
	}
	
	/**
 	 * Handle an array which was found in an AGET opcode.
	 * 
	 * This method does the following:
	 *  
	 * Do a backward search:
	 * 
	 * 1) If we find an APUT into arrayReg, backtrack the value register.
	 * 2) If we find a NEW-ARRAY which puts a new array in arrayReg, stop.
	 *  2a) If we find a FILLED_NEW_ARRAY which fills our arrayReg, we're done but we will backtrack the parameter registers.
	 * 3) If we find a xGET-x, we will backtrack this array "at the end" (findArrayGets)
	 * 4) If we find another opcode which overwrites arrayReg, we're screwed and something is wrong (or we're too stupid).
	 *  
	 * @param arrayReg the array-register
	 * @param codeLineIndex the index of the codeline in the BB
	 * @param fuzzyLevel is the search fuzzy (inaccurate)
	 * @param fuzzyLevelOffset the offset of the fuzzy value
	 * @param path the current path
	 * @throws SyntaxException
	 * @throws DetectionLogicError
	 */
	public void arrayMode(byte[] arrayReg, int codeLineIndex, int fuzzyLevel, int fuzzyLevelOffset, LinkedList<BasicBlockInterface> path) throws SyntaxException, DetectionLogicError {
		if (DEBUG) LOGGER.debug("\nEntering array mode");
		BasicBlockInterface bb; // will be assigned to the last blocked added to the path b/c it is already added		
		BBList bbl = new BBList(path, arrayReg);
		boolean firstRun = true;
		while ((bb = bbl.getNextBb()) != null) {
//			System.out.println(bbl);
//			System.out.println(" current bb: "+bb.getLabel());
			arrayReg = bbl.getState();
			CodeLineInterface cl;
			InstructionInterface i;
			if (!firstRun) codeLineIndex = bb.getCodeLines().size()-1; // go through the complete block, except the first
			else firstRun = false;
			boolean abort = false;
			while (!abort && codeLineIndex >= 0) { // search backwards
				cl = bb.getCodeLines().get(codeLineIndex--);
				if (DEBUG) LOGGER.debug(" array mode: array="+new String(arrayReg)+" cl="+cl);
				i = cl.getInstruction();
				switch (i.getType()) {
				case APUT:
					// found APUT, check if it stores something in our array, if so, backtrack the register put into our array
					if (Arrays.equals(arrayReg, i.getResultRegister())) {
						if (DEBUG) LOGGER.debug("Found a valid APUT");
						// the value is stored into our array
						byte[] regPutIntoArray = i.getInvolvedRegisters().getFirst();
						LinkedList<BasicBlockInterface> p = new LinkedList<BasicBlockInterface>(bbl.getPathForLastBB());
						RegisterSearch rs = new RegisterSearch(regPutIntoArray, bb, codeLineIndex, fuzzyLevel, fuzzyLevelOffset, p);
						todoList.addRegisterToTrack(rs);
					}
					break;
					
				case NEW_ARRAY:
					// This check can be fooled by smalicode which somehow reuses used arrays and move them to temporary arrays and so on...
					if (Arrays.equals(i.getResultRegister(), arrayReg)) {
						if (DEBUG) LOGGER.debug("Found a valid NEW_ARRAY, stop.");
						// we found the creation of the array and can therefore end our search
						bbl.removeLastBBFromList();
						abort = true;
					}
					break;
					
				case FILL_ARRAY_DATA:
					if (Arrays.equals(i.getResultRegister(), arrayReg)) {
						ConstantInterface c = new Constant(cl, fuzzyLevel-fuzzyLevelOffset, path, searchId);
						backtrackRequest.addFoundConstant(c);
						// we found the initialization of the array and can therefore end our search
						bbl.removeLastBBFromList();
						abort = true;
						if (DEBUG) LOGGER.debug("Found fill-array-data instruction, will store as constant and stop search in current BB.");
					}
					break;
					
				case GET:
					// check if something is copied into our array register
					if (Arrays.equals(i.getResultRegister(), arrayReg)) { // it is
						if (DEBUG) LOGGER.debug("Found a valid xGET-x, adding to later search. cl="+cl);
						byte[][] ca = Instruction.parseClassAndField(i.getInvolvedFields().getFirst());
						LinkedList<BasicBlockInterface> p = new LinkedList<BasicBlockInterface>(bbl.getPathForLastBB());
						todoList.addArrayFieldToTrack(ca, fuzzyLevel, fuzzyLevelOffset, p);
						bbl.removeLastBBFromList(); // we're done for this path
						abort = true;
					}
					break;
					
				case MOVE_RESULT:
					if (doesRegisterMatch(i, arrayReg)) {
						if (cl.getInstruction().getType() == InstructionType.MOVE_RESULT) { // it is
							// now check if the previous is either a FILLED_NEW_ARRAY or an INVOKE
							BasicBlock.FoundCodeLine fcl = getPreviousOpcode(bb, cl);
							// We have to fix the path if we found it in another BB
							LinkedList<BasicBlockInterface> fixedPath = bbl.getPathForLastBB();
							if (fixedPath.getLast() != fcl.getBasicBlock()) {
								fixedPath.addLast(fcl.getBasicBlock());
							}
							if (fcl.getCodeLine().getInstruction().getType() == InstructionType.FILLED_NEW_ARRAY) {
								if (DEBUG) LOGGER.debug("  Found a FILLED_NEW_ARRAY opcode, will handle it.");
								// parse the involved registers and track them back
								handleFilledNewArray(bb, fcl.getCodeLine(), fcl.getIndex(), fuzzyLevel, fuzzyLevelOffset, fixedPath);
							}
							else if (fcl.getCodeLine().getInstruction().getType() == InstructionType.INVOKE
									|| fcl.getCodeLine().getInstruction().getType() == InstructionType.INVOKE_STATIC) {
								// some array is returned from a method
								if (DEBUG) LOGGER.debug("  Found an INVOKE opcode, will handle it.");
								handleInvoke(fcl.getBasicBlock(), fcl.getIndex(), arrayReg, false, fuzzyLevel, fuzzyLevelOffset, fixedPath); // array reg is the currently backtracked register
							}
							else {
								// this is unexpected, what did we find?
								throw new SyntaxException("Found unexpected opcode in arraymode! cl="+fcl);
							}
						}
						bbl.removeLastBBFromList(); // we're done for this path
						abort = true;
					}
					break;
					
				case MOVE:
					// check if something is moved into the tracked register, if so, track the moved register
					if (doesRegisterMatch(i, arrayReg)) { // not all lines are opcodes or have a target register 
						arrayReg = i.getInvolvedRegisters().getFirst(); // there is only one register
						if (DEBUG) LOGGER.debug("  ArrayReg MOVED, new="+new String(arrayReg));
						bbl.setNewStateforCurrentBB(arrayReg);
					}
					/*
					 * TODO: Let's assume array reg is v50 and we find this:
					 * move-object/from16 v0, v50
					 * Our array is now also in v0. Should we also track v0 now?
					 * If so, we have to do it FORWARDs, not BACKWARDs!
					 * Therefore use method forwardFindAPuts(...).
					 */
					break;
					
				case PUT:
					// check if our array is copied to some field and add it to the todolist if this is the case
					if (Arrays.equals(i.getInvolvedRegisters().getFirst(), arrayReg)) {
						LinkedList<BasicBlockInterface> p = new LinkedList<BasicBlockInterface>(bbl.getPathForLastBB());
						todoList.addArrayFieldToTrack(i.getResultField(), fuzzyLevel, fuzzyLevelOffset, p);
					}
					break;

				default:
					// Check if all other opcodes overwrite our array register, if so, we're screwed, if not, we're fine
					if (cl.isCode() && i.getResultRegister() != null && doesRegisterMatch(i, arrayReg)) { // not all lines are opcodes or have a target register
						if ((i.getType() == InstructionType.CONST) && ("0".equals(i.getConstantValue()) || "0x0".equals(i.getConstantValue()))) {
							// If this happens we assume that the array was initialized with the "null value"
							if (DEBUG) LOGGER.debug("Found an 0x0 const, array probably NULL'ed. Stopping search.");
							bbl.removeLastBBFromList(); // we're done for this path
							abort = true;
						}
						else if (i.getType() == InstructionType.AGET) {
							/*
							 * Handle special case (multidimensional arrays):
							 * iget-object v0, v0, Lcom/tapjoy/TapjoyVideoObject;->buttonData:[[Ljava/lang/String; <= our multidimensional array v0
							 * const/4 v1, 0x0
							 * aget-object v0, v0, v1 <= Overwriting v0 w/ an array from an array, this would crash otherwise
							 * const/4 v1, 0x1
							 * aget-object v0, v0, v1 <= Array mode, array is v0
							 * invoke-static {v0}, Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri; <= Tracking v0
							 */
							arrayReg = i.getInvolvedRegisters().get(0); // set the array to the other array (in this case also v0)
							bbl.setNewStateforCurrentBB(arrayReg);
							if (DEBUG) LOGGER.debug("Multidimensional array GET. Array reg now: "+new String(arrayReg));
						}
						else {
							LOGGER.error("Found opcode overwriting our array! Aborting, but search continues! "+cl);
							throw new DetectionLogicError("Found opcode overwriting our array! Aborting, but search continues! "+cl);
						}
					}
					break;
				}
			}
		}
	}
	
	/**
	 * Find all xGET-x which assign an array to a register v. Then search for all APUT's which put something into our array in register v.
	 * The put'ed values are then backtracked as a RegisterSearch.
	 *  
	 * @param ctt with ca: c=class, a=arrayname (the field)
	 */
	private void findArrayGets(ClassContentTracker ctt) {
		if (DEBUG) LOGGER.debug("findArrayGets, ctt="+ctt);
		for (ClassInterface sf : app.getAllSmaliClasss(false)) {
			for (MethodInterface m : sf.getMethods()) {
				for (BasicBlockInterface bb : m.getBasicBlocks()) {
					LinkedList<CodeLineInterface> codeLines = bb.getCodeLines();
					for (int i=0; i<codeLines.size(); i++ ) {
						CodeLineInterface cl = codeLines.get(i);
						InstructionInterface instruction = cl.getInstruction();
						if (instruction.getType() == InstructionType.GET) {
							// either IGET or SGET, check if the loaded field matches our array
							byte[][] instField = Instruction.parseClassAndField(instruction.getInvolvedFields().getFirst());
							if (Arrays.equals(ctt.getCi()[0], instField[0]) && Arrays.equals(ctt.getCi()[1], instField[1])) { // it does
								/*
								 * We found an opcode that loads our array of interest, ca, into a register.
								 * Now check if we find any APUTs in this array, do this search forward through the BBs!
								 */
								if (DEBUG) LOGGER.debug(" Found array-get, cl="+cl);
								LinkedList<BasicBlockInterface> path = ctt.getPath(); // add BB to the path, search begins at last BB in path
								path.addLast(bb);
								forwardFindAPuts(i, instruction.getResultRegister(), ctt.getFuzzyLevel(), ctt.getFuzzyOffset(), path);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * We have a BB (which must the last BB in the path), a codeline index and an array register arrayReg to track.
	 * Now look at the next lines and see if we find some APUTs which copies something from register v into our
	 * array arrayReg. If so, we need to backtrack v.
	 * 
	 * @param codeLineIndex the codeline index where to search from
	 * @param arrayReg the array register
	 * @param fuzzyLevel
	 * @param fuzzyLevelOffset
	 * @param path the current path INCLUDING the BB to search in as the last BB (use a new object otherwise things might go boom), path contains bb!
	 */
	private void forwardFindAPuts(int codeLineIndex, byte[] arrayReg, int fuzzyLevel, int fuzzyLevelOffset, LinkedList<BasicBlockInterface> path) {
		if (DEBUG) LOGGER.debug("ForwardFindAPuts: index="+codeLineIndex+", array reg="+new String(arrayReg));
		BasicBlockInterface bb = path.getLast();
		BBList bbl = new BBList(path, arrayReg, false); // it is a forward search
		CodeLineInterface cl;
		if (!(bb.getCodeLines().size() > codeLineIndex)) {
			/*
			 * There should be something unless the file is broken! If we end here, something is fishy.
			 * Nevertheless, use the next BBs.
			 */
			LOGGER.error("CodelineIndex is too big. "+codeLineIndex+">"+(bb.getCodeLines().size()-1));
			codeLineIndex = 0; // search the new ones from the beginning
		}
		else {
			codeLineIndex++; // use the next line
		}
		
		while ((bb = bbl.getNextBb()) != null) { // check all BBs
			for (int i=codeLineIndex; i<bb.getCodeLines().size(); i++) { // check all codelines in the BB
				// TODO: what about MOVE opcodes? 
				cl = bb.getCodeLines().get(i);
				if (DEBUG) LOGGER.debug(" cl="+cl);
				InstructionInterface instruction = cl.getInstruction();
				byte[] targetReg = instruction.getResultRegister();
				if ((!(instruction.getType() == InstructionType.APUT)) && Arrays.equals(arrayReg, targetReg)) {
					// something overwrote our array register: we're done with this path.
					bbl.removeLastBBFromList();
					break;
				}
				else if ((instruction.getType() == InstructionType.APUT) && Arrays.equals(arrayReg, targetReg)) {
					// we found an APUT and it copies into our array (register)
					byte[] regCopiedIntoArray = instruction.getInvolvedRegisters().getFirst();
					if (DEBUG) LOGGER.debug("Found an APUT for our array, backtracking reg="+new String(regCopiedIntoArray));
					// backtracking register later
					RegisterSearch rs = new RegisterSearch(regCopiedIntoArray, bb, i, fuzzyLevel, fuzzyLevelOffset, bbl.getPathForLastBB()); // bb will later be added to the path
					todoList.addRegisterToTrack(rs);
				}
				else {
					// some unrelated opcode
					continue;
				}
			}
			codeLineIndex = 0; // reset index for the next BB if there is any
		}
	}
	
	/**
	 * Find all xPUT-x opcodes which assign an array in a register av to our array field ca. We therefore search for opcodes which
	 * actually store an array in our array field ca. If we find such an PUT, we search for APUTs from this position which actually
	 * put some values in the register av where the array is assigned to. The putted values/registers are then further backtracked
	 * with a RegisterSearch. This search ends if we find a NEW_ARRAY opcode which overwrites our array register av or something
	 * overwrites av.
	 * 
	 * @param ca c=class, a=arrayname (field name)
	 * @throws SyntaxException
	 * @throws DetectionLogicError 
	 */
	private void findArrayPuts(ClassContentTracker ctt) throws SyntaxException, DetectionLogicError {
		if (DEBUG) LOGGER.debug("findArrayPuts, ctt="+ctt);
		for (ClassInterface sf : app.getAllSmaliClasss(false)) { // search through all SmaliClasses, Methods and BBs
			for (MethodInterface m : sf.getMethods()) {
				for (BasicBlockInterface bb : m.getBasicBlocks()) {
					LinkedList<CodeLineInterface> codeLines = bb.getCodeLines();
					for (int i=0; i<codeLines.size(); i++ ) {
						CodeLineInterface cl = codeLines.get(i);
						InstructionInterface instruction = cl.getInstruction();
						if (instruction.getType() == InstructionType.PUT) {
							/*
							 * Either iGET or sGET. Check if the field which gets assigned is our searched array.
							 */
							byte[][] fieldName = instruction.getResultField();
							if (Arrays.equals(ctt.getCi()[0], fieldName[0]) && Arrays.equals(ctt.getCi()[1], fieldName[1])) {
								/*
								 * We found an opcode that stores our array of interest, ca, into a field.
								 * Now check if we find any APUTs in this array, do this search backwards through the BBs!
								 */
								byte[] arrayReg = instruction.getInvolvedRegisters().getFirst(); // this is our array!
								// Search the previous opcodes
								LinkedList<BasicBlockInterface> path = ctt.getPath();
								path.addLast(bb);
								backwardFindAPuts(i, arrayReg, ctt.getFuzzyLevel(), ctt.getFuzzyOffset(), path);
								/*
								 * Also look at the following opcodes and check if our array register gets overwritten. It is possible
								 * to first create an array, assign it to a field, and put values into the array "directly through
								 * the local array object". 
								 */
								LinkedList<BasicBlockInterface> path2 = new LinkedList<BasicBlockInterface>(path); // we need a copy
								path2.add(bb);
								forwardFindAPuts(i, arrayReg, ctt.getFuzzyLevel(), ctt.getFuzzyOffset(), path2);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * We have a BB (which must be the last BB in the path), a codeline index and an array register (arrayReg) to track. Now
	 * look at the previous opcodes and see if we find some APUTs which copy something from register v into arrayReg. If so,
	 * we need to backtrack v. We stop when something overwrites our arrayReg.
	 * 
	 * FIXME: Fuzzy value and offset are currently not handled correctly, the fuzzy level always includes the offset here.
	 * This is only relevant for the results in the DB/XML.
	 * 
	 * @param codeLineIndex the codeline index in the BB
	 * @param arrayReg the array register to track
	 * @param fuzzyLevel is the search fuzzy (inaccurate)?
	 * @param fuzzyLevelOffset
	 * @param path the current path INCLUDING the BB to search in as the last BB (use a new object otherwise things might go boom), path contains bb!
	 * @throws SyntaxException
	 * @throws DetectionLogicError 
	 */
	private void backwardFindAPuts(int codeLineIndex, byte[] arrayReg, int fuzzyLevel, int fuzzyLevelOffset, LinkedList<BasicBlockInterface> path) throws SyntaxException, DetectionLogicError {
		if (DEBUG) LOGGER.debug("BackwardFindAPuts: index="+codeLineIndex+", arrayReg="+new String(arrayReg));
		CodeLineInterface cl;
		boolean firstRun = true;
		BasicBlockInterface bb = path.getLast();
		
		BBList bbl = new BBList(path, arrayReg);
		while ((bb = bbl.getNextBb()) != null) {
			if (!firstRun) {
				codeLineIndex = bb.getCodeLines().size()-1; // for new BBs begin at the end of the BB
			}
			firstRun = false;
			
			for (int i=codeLineIndex; i>=0; i--) { // check all codelines in the BB
				cl = bb.getCodeLines().get(i);
				if (DEBUG) LOGGER.debug(" Handling cl="+cl);
				InstructionInterface instruction = cl.getInstruction();
				byte[] targetReg = instruction.getResultRegister();
				if ((instruction.getType() == InstructionType.APUT) && Arrays.equals(arrayReg, targetReg)) {
					// we found an APUT and it copies into our array (register)
					byte[] regCopiedIntoArray = instruction.getInvolvedRegisters().getFirst();
					if (DEBUG) LOGGER.debug("Found an APUT for our array, backtracking reg="+new String(regCopiedIntoArray));
					// backtracking register later
					RegisterSearch rs = new RegisterSearch(regCopiedIntoArray, bb, i, fuzzyLevel, fuzzyLevelOffset, bbl.getPathForLastBB());
					todoList.addRegisterToTrack(rs);
				}
				else if ( // array creation/initialization
						( (instruction.getType() == InstructionType.FILL_ARRAY_DATA) 
						|| (instruction.getType() == InstructionType.NEW_ARRAY)
						) && Arrays.equals(arrayReg, targetReg)) {
					/*	It might be something like this:
					 *  const/4 v0, 0x3
					 *  new-array v0, v0, [I							  <-- found
					 *  sput-object v0, Ltest/android/Testcase5;->a1:[I   <-- started
					 *  Java code: private static final int[] a1 = { 0, 0, 0 };
					 */
					ConstantInterface c = new Constant(cl, fuzzyLevel, bbl.getPathForLastBB(), searchId);
					if (DEBUG) LOGGER.debug(" Found constant. c="+c);
					backtrackRequest.addFoundConstant(c);
					return;
				}
				else if ((!(instruction.getType() == InstructionType.APUT)) && Arrays.equals(arrayReg, targetReg)) {
					// something overwrote our array register: we're done.			
					if (DEBUG) LOGGER.debug(" ArrayReg overwritten. Done.");
					return;
					
				}
				else if ((instruction.getType() == InstructionType.MOVE) && Arrays.equals(arrayReg, targetReg)) {
					instruction.getInvolvedRegisters().getFirst();
					if (DEBUG) LOGGER.debug(" ArrayReg moved, old="+new String(arrayReg)+", new="+new String(instruction.getInvolvedRegisters().getFirst()));
					arrayReg = instruction.getInvolvedRegisters().getFirst();
				}
				else {
					// some unrelated opcode
					if (DEBUG) LOGGER.debug(" unrelated cl.");
					continue;
				}
			}
		}	 
	}
	
	
	/**
	 * Handle an INVOKE opcode. This method will attempt to access the invoked method. It is only able to do so if the method
	 * is somewhere in a SMALI file within the analyzed application. If the method is known, all return values are eventually
	 * later backtracked and eventually put into the ResultList. Then, all method parameters (registers) are backtracked. If the
	 * method is unknown, only the parameters are backtracked. This enabled us to, eg, see Strings which are added to a
	 * StringBuilder with its append() method.
	 * 
	 * FIXME: Fuzzy value and offset are currently not handled correctly, the fuzzy level always includes the offset here.
	 * This is only relevant for the results in the DB/XML.
	 *  
	 * @param bb the BasicBlock where the INVOKE opcode comes from
	 * @param register the currently backtracked register
	 * @param index the index of the invoke opcode within this BB
	 * @param resultWasMoved true if the result was moved to the backtracked register, this will trigger the search for return values in the invoked method
	 * @param fuzzyLevel
	 * @param fuzzyLevelOffset
	 * @param path the path (passed through)
	 * @throws DetectionLogicError 
	 */
	private void handleInvoke(BasicBlockInterface bb, int index, byte[] register, boolean resultWasMoved, int fuzzyLevel, int fuzzyLevelOffset, LinkedList<BasicBlockInterface> path) throws DetectionLogicError {
		CodeLineInterface cl = bb.getCodeLines().get(index);
		if (!(cl.getInstruction().getType() == InstructionType.INVOKE || cl.getInstruction().getType() == InstructionType.INVOKE_STATIC)) {
			throw new DetectionLogicError("Wrong instruction, need INVOKE, but got: "+cl);
		}
		boolean methodKnown = true;
		byte[][] cmp2 = cl.getInstruction().getCalledClassAndMethodWithParameter();
		try {
			app.getMethodByClassAndName(new String(cmp2[0]), new String(cmp2[1]), cmp2[2], cmp2[3]);
		}
		catch (ClassOrMethodNotFoundException e) {
			if (DEBUG) LOGGER.debug("Unable to backtrack into method, will parse parameters only. "+e.getMessage());
			methodKnown = false;
		}
		if (methodKnown && resultWasMoved) {
			if (DEBUG) LOGGER.debug("Will later backtrack RETURN values from "+new String(cmp2[0])+"."+new String(cmp2[1])+"("+new String(cmp2[2])+")");
			// search for all returns in this method if it is known
			todoList.addReturnValuesFromMethod(cmp2, fuzzyLevel, fuzzyLevelOffset, path);
		}
		else if (resultWasMoved /*&& fuzzyLevel == 0*/) {
			// treat the invoked method as a constant for our register as the result was moved to it!
			try {
				if (DEBUG) LOGGER.debug("Adding constant for cl = "+cl);
				ConstantInterface c = new Constant(cl, fuzzyLevel, path, searchId);
				backtrackRequest.addFoundConstant(c);
			} catch (SyntaxException e) {
				LOGGER.error("Could not add found method to found constants", e);
				backtrackRequest.logException(e);
			}
		}

		/*
		 * Backtrack all parameters of the invoked method.
		 * This will result in a fuzzy search b/c we do not
		 * really know what the parameters are and where they are used.
		 * Do not backtrack the actual backtracked register (again).
		 */
		boolean increaseFuzzyness;
		for (int i = 0; i< cl.getInstruction().getInvolvedRegisters().size(); i++) {
			increaseFuzzyness = true;
			byte[] reg = cl.getInstruction().getInvolvedRegisters().get(i);
			if (i == 0 && !(cl.getInstruction().getType() == InstructionType.INVOKE_STATIC)) {
				/*
				 * Skip the first parameter for non-static invokes, if it references the class object, ("this").
				 * Normally, this is register p0. Non-static invokes always have the first register referencing
				 * the object where the method gets invoked, which might be "this". If we backtrack "this", this
				 * could result in a huge "overtracking"!
				 * 
				 * But we want to track the first parameter if it refers to some other object, eg:
				 *  const-string v2, "some string"
				 *  invoke-virtual {v1, v2}, Ljava/lang/StringBuffer;->append(Ljava/lang/String;)Ljava/lang/StringBuffer;
				 *  invoke-virtual {v1}, Ljava/lang/StringBuffer;->toString()Ljava/lang/String; <-- WE WANT TO TRACK V1
				 *  move-result-object v0  <-- v0 IS TRACKED
				 * 
				 * The problem is, that p0 may be overwritten and will then not reference "this" anymore, but
				 * we simply ignore this here. Otherwise we would have to search all previous opcodes if they
				 * overwrite p0.
				 * 
				 * So for non-static invokes we ignore the first parameter/register if it is p0, otherwise we track it.
				 * Static invokes are not affected.
				 */
				if (Arrays.equals(reg, P0_THIS)) continue;
				else {
					/*
					 * Do not increment it for v1 in the example above, otherwise v2 would be tagged w/ a value of +2,
					 * which is wrong. It should be tagged w/ +1, b/c v2 is added to v1, and v1 is just a reference to
					 * some "intermediate object". This check is only performed for the first register for non-static
					 * invokes. All other found registers which are backtracked get their fuzzyness increased.
					 */
					increaseFuzzyness = false;
				}
			}
			if (!resultWasMoved && Arrays.equals(reg, register)) {
				/* Skip the register which is currently backtracked but only
				 * do it if the result was not moved. If it was moved, eg,
				 * StringBuilder.toString() we still need to track the
				 * StringBuilder object. But if it was moved, the search for
				 * this register will end (the calling method will immediately
				 * return, hence we need a new RegisterSearch for it!
				 */
				continue;
			}
			if (DEBUG) LOGGER.debug(" Adding parameterIndex/register: "+i+"/"+new String(reg));
			int fl;
			if (increaseFuzzyness) fl = fuzzyLevel+1;
			else fl = fuzzyLevel;
			/*
			 * This search is fuzzy if the register does not only reference an "intermediate object", see fl variable.
			 */
			RegisterSearch rs2 = new RegisterSearch(reg, bb, index, fl, fuzzyLevelOffset, path);
			todoList.addRegisterToTrack(rs2);
		}
	}
	
	/**
	 * Returns whether the given instruction puts the result in the requested register
	 * @param instruction
	 * @param register
	 * @return
	 */
	private boolean doesRegisterMatch(InstructionInterface instruction, byte[] register) {
		return ByteUtils.equals(instruction.getResultRegister(), register);
	}
	
	/**
	 * Get the previous CodeLine which contains actual code (is not empty, a comment etc).
	 * 
	 * The previous opcode CAN reside in another opcode if, and only if, the searched
	 * BB is part of the following try/catch construct and there is only one previous
	 * block. That means the BB is not a catch block but the block directly after the
	 * try/catch construct.
	 * 
	 * invoke-static {v1, p2}, La/b;->decrypt(String;String;)String; 		<- We want to find this, but the BB already ended!
	 * :try_end_0
	 * .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0	<- BB ends here
	 * move-result-object p2												<- p2 is currently tracked and we need the previous opcode in this BB
	 * 
	 * The BB where the previous cl real opcode was found is saved in the returned
	 * FoundCodeLine. It might be different from the parameter.
	 * 
	 * @param bb the BB in which to search
	 * @param cl the codeline from which the search starts, the search will begin before this line
	 * @return the corresponding CodeLine or a SyntaxException
	 * @throws SyntaxException if no real instruction can be found in the given BB (or the previous one)
	 */
	private static BasicBlock.FoundCodeLine getPreviousOpcode(BasicBlockInterface bb, CodeLineInterface cl) throws SyntaxException {
		int index = bb.getCodeLines().indexOf(cl);
		while (index >= 1) {
			index--;
			cl = bb.getCodeLines().get(index);
			if (!cl.isCode()) continue;
			else {
				return new BasicBlock.FoundCodeLine(cl, bb, index);
			}
		}
		
		/*
		 * If we are searching for an invoke b/c we have return opcode,
		 * the corresponding invoke can be in another BB if we are
		 * right beneath a try/catch BB and not the catch block itself.
		 */
		if (!bb.isCatchBlock() && bb.getPreviousBB().size() == 1) {
			return BasicBlock.getLastCodeLine(bb.getPreviousBB().getFirst());
		}
		if (DEBUG) LOGGER.debug("Could not find previous \"real\" instruction in given BB! In Catch BB: "+bb.isCatchBlock()+", #previousBB: "+bb.getPreviousBB().size());
		// if we can not find something we have to abort!
		throw new SyntaxException("Could not find previous \"real\" instruction in given BB!"); 
	}
	
	/**
	 * Parse the involved registers in an FILLED_NEW_ARRAY and track them back.
	 * 
	 * @param bb the basic block where the codeline is taken from
	 * @param cl the codeline with the FILLED_NEW_ARRAY
	 * @param index the index of the cl in the bb
	 * @param fuzzyLevel
	 * @param fuzzyLevelOffset
	 * @param path the path (passed through)
	 * @throws DetectionLogicError if the opcode in cl is not of type FILLED_NEW_ARRAY
	 */
	private void handleFilledNewArray(BasicBlockInterface bb, CodeLineInterface cl, int index, int fuzzyLevel, int fuzzyLevelOffset, LinkedList<BasicBlockInterface> path) throws DetectionLogicError {
		if (cl.getInstruction().getType() != InstructionType.FILLED_NEW_ARRAY) throw new DetectionLogicError("Expected FILLED_NEW_ARRAY opcode, but code cl="+cl);
		// parse the involved registers and track them back
		for (byte[] register : cl.getInstruction().getInvolvedRegisters()) {
			RegisterSearch rs = new RegisterSearch(register, bb, index, fuzzyLevel, fuzzyLevelOffset, path);
			todoList.addRegisterToTrack(rs);
		}
	}

    
    private static final byte[] WILDCARD = { '*' };
    
    /**
     * Find all invokes in all files and return all corresponding RegisterSearch objects in a list.
     * This method automatically handles static invokes. This method will add the found RS objects
     * to the Todo-List for further processing.
     * @param cmp the class, method and its parameters, the class cmp[0] may be the wildcard '*'.
     * @param parameterIndex the parameter index to track
     * @param fuzzyLevel
     * @param path the path (passed through)
     * @throws DetectionLogicError 
     * @return the number of found invokes
     */
    private int findInvokesAndAddToTodoList(byte[][] cmp, int parameterIndex, int fuzzyLevel, int fuzzyLevelOffset, LinkedList<BasicBlockInterface> path) throws DetectionLogicError {
		LinkedList<RegisterSearch> rsList = findInvokes(cmp, parameterIndex, fuzzyLevel, path);
		
		for (RegisterSearch rs : rsList) {
			rs.setFuzzyOffset(fuzzyLevelOffset);
			todoList.addRegisterToTrack(rs);
		}
		
		return rsList.size();
    }
    
    
    /**
     * Find all invokes in all files and return all corresponding RegisterSearch objects in a list.
     * This method automatically handles static invokes.
     * @param cmp the class, method and its parameters, the class cmp[0] may be the wildcard '*'.
     * @param parameterIndex the parameter index to track
     * @param fuzzyLevel
     * @param path the path (passed through)
     * @throws DetectionLogicError 
     * @return the number of found invokes
     */
    private LinkedList<RegisterSearch> findInvokes(byte[][] cmp, int parameterIndex, int fuzzyLevel, LinkedList<BasicBlockInterface> path) throws DetectionLogicError {
		LinkedList<RegisterSearch> rsList = new LinkedList<TodoList.RegisterSearch>();
		boolean noSignatureGiven = false;
		
		if (cmp[2] == null) {
			/*
			 * This "workaround" mimics the old search behavior where overloaded methods
			 * could not be handled. If no method signature is given, the given index is
			 * blindly backtracked for all methods which have the same name and where
			 * the parameter index is in range of the the actual method signature.
			 * second Part of the workaround (see down)
			 */
			LOGGER.error("No parameter signature given while searching for invoke "+new String(cmp[0])+"->"+new String(cmp[1])+"(???). Search is fuzzy. Please define the signature!");
			noSignatureGiven = true;
		}
		
		Vector<File> fvec = app.getAllRawSmaliFiles(Config.getInstance().getBooleanConfigValue(ConfigKeys.ANALYSIS_INCLUDE_AD_FRAMEWORKS));
		for (File file : fvec) {
			ClassInterface sfile = app.getSmaliClass(file);
			if (sfile == null) {
				LOGGER.error("Could not find SMALI file for raw file `"+file+"`. SmaliClass object most probably threw an exception while parsing it. Ignoring this one.");
				continue;
			}
			for (MethodInterface m : sfile.getMethods()) {
//				if (DEBUG) LOGGER.debug("Looking in method: "+file.getName()+"."+m.getName()+"()");
				for (BasicBlockInterface bb : m.getBasicBlocks()) {
					for (CodeLineInterface cl : bb.getCodeLines()) {
						InstructionInterface i = cl.getInstruction();
						if (i.getType() == InstructionType.INVOKE || i.getType() == InstructionType.INVOKE_STATIC) {
							if ( (Arrays.equals(cmp[0], WILDCARD) || Arrays.equals(i.getCalledClassAndMethodWithParameter()[0], cmp[0]) )
								&& Arrays.equals(i.getCalledClassAndMethodWithParameter()[1], cmp[1])
								&& (
									cmp[2] == null // second part of workaround (see above)
									|| Arrays.equals(i.getCalledClassAndMethodWithParameter()[2], cmp[2]))) {
								// we found the method!
								if (DEBUG) LOGGER.debug(" Found a method w/ correct invoke "+new String(cmp[0])+"->"+new String(cmp[1])+"("+new String(i.getCalledClassAndMethodWithParameter()[2])+") in line "+cl.getLineNr());
								boolean isStaticCall = false;
								if (i.getType() == InstructionType.INVOKE_STATIC) {
									isStaticCall = true;
									if (DEBUG) LOGGER.debug("  Invoke is static");
								}
								LinkedList<byte[]> regs = i.getInvolvedRegisters();
								
								//  Found a method w/ correct invoke
								// 	invoke-direct {v0}, Landroid/content/IntentFilter;-><init>()V
								byte[] register;
								int realParameterIndex = parameterIndex;
								if (!isStaticCall) {
									realParameterIndex++;
								}
								if (noSignatureGiven && realParameterIndex >= regs.size()) {
									// this will not work, we skip this invoke. It is always better to define the method signature
									LOGGER.info("Could not backtrack, parameterIndex "+realParameterIndex+" is out of range! No method signature defined. cl="+cl);
									continue;
								}
								else if (realParameterIndex >= regs.size() || realParameterIndex < 0) {
									// This should not happen!
									throw new DetectionLogicError("Could not backtrack, parameterIndex "+realParameterIndex+" is out of range! cl="+cl);
								}
								register = regs.get(realParameterIndex);
								int index = bb.getCodeLines().indexOf(cl); // FIXME make if better and faster

								int lvl = fuzzyLevel;
								if (noSignatureGiven) lvl++;
								RegisterSearch rs = new RegisterSearch(register, bb, index, lvl, 0, path);
								rsList.addLast(rs);
							}
//							else { /* invoke did not match */ }
						}
					}
				}
				
			}
		}
		return rsList;
    }
}
