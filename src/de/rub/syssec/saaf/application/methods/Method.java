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
package de.rub.syssec.saaf.application.methods;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.application.Field;
import de.rub.syssec.saaf.misc.ByteUtils;
import de.rub.syssec.saaf.misc.Highlight;
import de.rub.syssec.saaf.misc.KMP;
import de.rub.syssec.saaf.model.application.BasicBlockInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.DetectionLogicError;
import de.rub.syssec.saaf.model.application.FieldInterface;
import de.rub.syssec.saaf.model.application.InstructionInterface;
import de.rub.syssec.saaf.model.application.InstructionType;
import de.rub.syssec.saaf.model.application.MethodInterface;
import de.rub.syssec.saaf.model.application.SmaliClassError;
import de.rub.syssec.saaf.model.application.SyntaxException;

public class Method implements MethodInterface {
	private String name;
	private String parameters = null;
	private String returnValueString = null;
	private byte[] rawParameters;
	private byte[] returnValue;
	private boolean hasUnlinkedBBs = false;
	private LinkedList<CodeLineInterface> codeLines;
	private LinkedList<FieldInterface> localFieldList = null;
	private static final byte[] CONSTRUCTOR_NAME = "<init>".getBytes();
	public static final byte[] STATIC_CONSTRUCTOR_NAME = "<clinit>".getBytes();
	private METHOD_TYPE methodType;
	private ClassInterface smaliClass; // the SMALI file this method belongs to
	private LinkedList<BasicBlockInterface> bbList = new LinkedList<BasicBlockInterface>();
	private int id = -1; // ID from the table in db
	private boolean isStatic = false;
	private String fuzzyHash = null;
	private int label;
	private static final Logger LOGGER = Logger.getLogger(Method.class);
	private boolean isCurrentLineInSwitch = false;

	public enum METHOD_TYPE {
		CONSTRUCTOR, STATIC_CONSTRUCTOR, // static { ... } block
		METHOD; // all other "normal" methods
	}

	public Method(LinkedList<CodeLineInterface> codeLines,
			ClassInterface smaliClass, int label) {
		this.codeLines = codeLines;
		this.smaliClass = smaliClass;
		this.label = label;
		parseNameAndType();
		// parseBB();
		this.changed=true;
	}

	/**
	 * Does this method contain anything else besides an empty declaration?
	 * 
	 * @return true if the method is "empty", false otherwise
	 */
	public boolean isEmpty() {
		return bbList.isEmpty() ? true : false;
	}

	private void parseNameAndType() {
		// ex: .method public constructor <init>(Landroid/content/Context;)V
		byte[] firstLine = codeLines.getFirst().getLine();
		int openingParenthesisIndex = ByteUtils.indexOf(firstLine, '(');
		int closingParenthesisIndex = ByteUtils.indexOf(firstLine, ')');
		int spaceBeforeIndex = ByteUtils.indexOfReverse(firstLine, ' ',
				openingParenthesisIndex);
		byte[] n = ByteUtils.subbytes(firstLine, spaceBeforeIndex + 1,
				openingParenthesisIndex); // cut the space
		if (Arrays.equals(n, CONSTRUCTOR_NAME))
			methodType = METHOD_TYPE.CONSTRUCTOR;
		else if (Arrays.equals(n, STATIC_CONSTRUCTOR_NAME))
			methodType = METHOD_TYPE.STATIC_CONSTRUCTOR;
		else
			methodType = METHOD_TYPE.METHOD;
		name = new String(n);

		// Check if the method is static, only use the first part b/c the method
		// could have the word static in its name
		byte[] lineTillSpace = ByteUtils.subbytes(firstLine, 0,
				spaceBeforeIndex);
		if (ByteUtils.contains(lineTillSpace, "static".getBytes()))
			isStatic = true;

		// get the parameters
		rawParameters = ByteUtils.subbytes(firstLine,
				openingParenthesisIndex + 1, closingParenthesisIndex);
		
		// return value
		returnValue = ByteUtils.subbytes(firstLine,	closingParenthesisIndex + 1);
	}

	// FIXME: crude hack to replace the autogeneration in parseNameAndType(),
	// could perhaps still always be generated in parseNameAndType (for safety)
	// and use this just for explicitly generating bbs anew
	public void generateBBs() throws DetectionLogicError, SmaliClassError {
		bbList = generateBlocksNew();
		DFS dfs = new DFS();
		dfs.labelAllBB(this);
	}

	private LinkedList<BasicBlockInterface> generateBlocksNew() throws SmaliClassError {
		// CHANGE ALL PARAMS TO PASS THE LABELS
		// possibly change everything to index in codeline array instead of cls

		// still have to fix problem that switchtable will be part of the last
		// BB
		LinkedList<BasicBlockInterface> blocks = null;
		LinkedList<TryBlock> tries = null;
		HashMap<String, CodeLineInterface> labels = getLabelLines();
		LinkedList<Link> links = findLeadersNew();
		LinkedList<Integer> leaders = new LinkedList<Integer>();
		links = findGotoTargetsNew(links, labels, leaders);
		links = findIfTargetsNew(links, labels);// instruction.getType() ==
												// InstructionType.LABEL and or
												// isCode
		links = findSwitchTargetsNew(links, labels);
		tries = findTryTargetsNew(labels, links);
		// build blocks from tries + links
		blocks = buildBlocks(links, tries, leaders);

		blocks = linkNew(blocks, links, tries);

		return blocks;
	}
	
	private LinkedList<BasicBlockInterface> linkNew(
			LinkedList<BasicBlockInterface> blocks, LinkedList<Link> links,
			LinkedList<TryBlock> tries) {
		// first add all links
		for (Link l : links) {
			for (BasicBlockInterface start : blocks) {
				
				if (/*!start.hasReturn() && !start.hasThrow() &&*///added for return in block followed by a goto, which is most likely patched into the code
					!start.getCodeLines().isEmpty()
					&& start.getCodeLines().getLast().getLineNr()
					>= (l.getFrom().getLineNr())
					&& start.getCodeLines().getFirst().getLineNr()
					<= (l.getFrom().getLineNr())
				) {
					for (BasicBlockInterface target : blocks) {
						// could also be done via line number
						if (
							!target.getCodeLines().isEmpty()
							&& target.getCodeLines().getFirst().getLineNr()
							<= (l.getTo().getLineNr())
							&& target.getCodeLines().getLast().getLineNr()
							>= (l.getTo().getLineNr())
						) {
							//linkBBs(start, target);
								start.addNextBB(target);
								target.addPreviousBB(start);
								break;
						}
					}
					break;
				}
			}
		}
		
		
		//add all try code
		for(TryBlock t:tries){
			for(BasicBlockInterface start:blocks){
				//found a block within the try
				if(
					!start.getCodeLines().isEmpty() && // TODO Fix for issue 54
					start.getCodeLines().getLast().getLineNr()>=t.getBegin()&&start.getCodeLines().getLast().getLineNr()<=t.getEnd()){
					//for every block, check if it is one of the catches
					for(CodeLineInterface c:t.getCatches()){
						//check all catches
						for(BasicBlockInterface target:blocks){
							if(
									!target.getCodeLines().isEmpty() && // TODO Fix for issue 54
									target.getCodeLines().getFirst().getLineNr()<=(c.getLineNr())&&
									target.getCodeLines().getLast().getLineNr()>=(c.getLineNr())){


									//linkBBs(start, target);
									start.addNextBB(target);
									target.addPreviousBB(start);
									start.setIsTryBlock(true);
									target.setIsCatchBlock(true);
									

								break;
							}
						}
					}
				}
			}
		}
		
		// link all the default cases
		// there is always a fall through, except for 3 cases:
		// goto, return, throw
		Iterator<BasicBlockInterface> iter = blocks.iterator();
		BasicBlockInterface first = null;
		BasicBlockInterface next = null;
		if (iter.hasNext())
			first = iter.next();
		while (iter.hasNext()) {
			next = iter.next();
			try {

				CodeLineInterface lastLine = BasicBlock.getLastCodeLine(
						first).getCodeLine();
				
				//!(hasReturn() && !hasDeadCode()) && !(hasth)
				//TODO: check again if we need the add !first.hasDeadCode() to this default fall through case in BB
				if (/*!first.hasDeadCode() && */!first.hasGoto() && lastLine.getInstruction().getType() != InstructionType.RETURN
						&& !(KMP.indexOf(lastLine.getLine(), "throw".getBytes()) == 0)/*&& !first.hasReturn() &&! first.hasThrow()*/){
					//linkBBs(first, next);
					first.addNextBB(next);
					next.addPreviousBB(first);
				}
				first = next;
			} catch (SyntaxException e) {
				// this just happens if there is no code in the bb, so error is
				// no error...
				// it can happen if there is a BB just consisting of a single
				// label,
				// so link the bb to the next and previous on
				//linkBBs(first, next);
				first.addNextBB(next);
				next.addPreviousBB(first);

				first = next;
			}
		}
		
		return blocks;
	}

	
//	/**
//	 * this links two BBs 
//	 * @param first the starting block (this does first.addNextBB() )
//	 * @param next the target block (this does next.addPreviousBB() )
//	 */
//	private void linkBBs(BasicBlockInterface first, BasicBlockInterface next) {
//		first.addNextBB(next);
//		next.addPreviousBB(first);
//	}

	private LinkedList<BasicBlockInterface> buildBlocks(LinkedList<Link> links,
			LinkedList<TryBlock> tries, LinkedList<Integer> gotoLeaders) throws SmaliClassError {
		// leaders = lines (or ints) which start a block, thus all targets of
		// the links + first line + all targets of tries
		// get the position of the leader codeline in this methods codeline
		// array and put it into a treeset to sort all leaders
		// TODO: getting the position is quite inefficient, so instead of
		// codelines save the int (or both)
		TreeSet<Integer> leaders = new TreeSet<Integer>();
		LinkedList<CodeLineInterface> returnList = new LinkedList<CodeLineInterface>();
		// first line of the code is always a leader
		leaders.add(0);
		leaders.addAll(gotoLeaders);
		
		boolean hasThrow;
		boolean hasReturn;
		boolean hasGoto;
		
		hasThrow = false;
		hasReturn = false;
		hasGoto = false;
		// TODO: if ints are saved these lockups become unnecessary
		// TODO: could be deleted from here until next TODO (if ints used)
		for (Link l : links) {
			CodeLineInterface current = codeLines.getFirst();
			int index = 0;
			while (current.getLineNr() != l.getTo().getLineNr()
					&& index < codeLines.size() - 1) {
				index++;
				current = codeLines.get(index);
			}
			if (index < codeLines.size())
				leaders.add(index);
			else {
				throw new SmaliClassError("Could not link BasicBlocks in method: "
						+ this.getName() + " from File: "
						+ this.getSmaliClass().getFile().getAbsolutePath());
			}

		}
		

		for (TryBlock t : tries) {
			CodeLineInterface current = codeLines.getFirst();
			// special case which should never ever happen
			if (t.getBlockEnd() != null
					&& t.getBlockEnd().getLineNr() == current.getLineNr()) {
				leaders.add(1);
			}
			LinkedList<CodeLineInterface> targets = t.getCatches();
			for (CodeLineInterface c : targets) {
				int index = 0;
				while (current.getLineNr() != c.getLineNr()
						&& index < codeLines.size() - 1) {
					index++;
					current = codeLines.get(index);
					if (t.getBlockEnd() != null
							&& t.getBlockEnd().getLineNr() == current
									.getLineNr()
							&& index < codeLines.size() - 1) {
						leaders.add(index + 1);
					}

				}
				if (index < codeLines.size())
					leaders.add(index);
				else {
					throw new SmaliClassError("Could not link BasicBlocks in  method: "
							+ this.getName() + " from File: "
							+ this.getSmaliClass().getFile().getAbsolutePath());
				}
			}
		}
		// TODO: delete if ints used, up to here

		// build all the blocks, based on the leaders
		// a block starts at a leader and extends until on line before the next
		// leader

		// now that we know the leaders
		// start building + filling BBs

		// 25.01.2013 added some control structures for cosmetic reasons (like
		// not showing the switchtables)

		// TODO: speed up the switch hiding, could be done by changing that
		// triple if construct
		// or by passing lines to hide as argument (and building a TreeSet
		// beforehand (in findSwitchTargets) containing
		// the lines to hide, so it ends up being just an if comparing line
		// numbers)
		LinkedList<BasicBlockInterface> blocks = new LinkedList<BasicBlockInterface>();
		Iterator<Integer> iter = leaders.iterator();
		Integer leader = iter.next();
		int nextLeader = -1;
		if (iter.hasNext()) {
			nextLeader = iter.next();
		}
		// case if nextLeader == -1, just 1 block
		if (nextLeader == -1) {
			LinkedList<CodeLineInterface> lines = new LinkedList<CodeLineInterface>();
			for (int i = leader; i < codeLines.size(); i++) {
				CodeLineInterface c = codeLines.get(i);
				if (isSwitchStart(c))
					continue;
				if (isSwitchEnd(c))
					continue;
				if (isInSwitch())
					continue;
				if(c.getInstruction().getType() == InstructionType.GOTO){
					hasGoto = true;
				}
				if(c.getInstruction().getType() == InstructionType.RETURN){
					hasReturn = true;
					returnList.add(c);
				}
				if((KMP.indexOf(c.getLine(), "throw".getBytes()) == 0)){
					hasThrow = true;
				}
				lines.add(c);
			}
			BasicBlockInterface block = new BasicBlock(lines, this);
			block.setHasReturn(hasReturn);
			block.setHasThrow(hasThrow);
			block.setHasGoto(hasGoto);
			
			if(block.hasReturn()){
				try {
					CodeLineInterface lastCodeLine = BasicBlock.getLastCodeLine(block).getCodeLine();

					if (returnList.size()>1 || (returnList.size()==1 && lastCodeLine.getLineNr()!= returnList.getFirst().getLineNr())){
						block.setHasDeadCode(true);
					}
				} catch (SyntaxException e) {
					LOGGER.error("Could not find last Codeline, while searching for return codes");

				}
			}
			
			returnList.clear();

			hasReturn = false;
			hasThrow = false;
			hasGoto = false;
			
			blocks.add(block);
			
		} else {
			// else

			// add first block
			LinkedList<CodeLineInterface> lines = new LinkedList<CodeLineInterface>();
			boolean justDotComment = true;
			for (int i = leader; i < nextLeader; i++) {
				CodeLineInterface c = codeLines.get(i);

				if (c.isCode()) {
					justDotComment = false;
				}
				if (isSwitchStart(c))
					continue;
				if (isSwitchEnd(c))
					continue;
				if (isInSwitch())
					continue;
				if(c.getInstruction().getType() == InstructionType.GOTO){
					hasGoto = true;
				}
				if(c.getInstruction().getType() == InstructionType.RETURN){
					hasReturn = true;
					returnList.add(c);
				}
				if((KMP.indexOf(c.getLine(), "throw".getBytes()) == 0)){
					hasThrow = true;
				}
				lines.add(c);

			}
			if (!justDotComment) {
				BasicBlockInterface block = new BasicBlock(lines, this);
				block.setHasReturn(hasReturn);
				block.setHasThrow(hasThrow);
				block.setHasGoto(hasGoto);
				
				if(block.hasReturn()){
					try {
						CodeLineInterface lastCodeLine = BasicBlock.getLastCodeLine(block).getCodeLine();

						if (returnList.size()>1 || (returnList.size()==1 && lastCodeLine.getLineNr()!= returnList.getFirst().getLineNr())){
							block.setHasDeadCode(true);
						}
					} catch (SyntaxException e) {
						LOGGER.error("Could not find last Codeline, while searching for return codes");

					}
				}
				
				returnList.clear();	
				
				hasReturn = false;
				hasThrow = false;
				hasGoto = false;

				blocks.add(block);
			}

			// add all intermediate blocks
			while (iter.hasNext()) {
				if (!justDotComment)
					lines = new LinkedList<CodeLineInterface>();
				justDotComment = true;

				// this while is used to merge blocks, which just consist of dot
				// comments with the next block
				while (justDotComment && iter.hasNext()) {
					leader = nextLeader;
					nextLeader = iter.next();

					for (int i = leader; i < nextLeader; i++) {
						CodeLineInterface c = codeLines.get(i);
						
						
						if (c.isCode()) {
							justDotComment = false;
						}
						if (isSwitchStart(c))
							continue;
						if (isSwitchEnd(c))
							continue;
						if (isInSwitch())
							continue;
						if(c.getInstruction().getType() == InstructionType.GOTO){
							hasGoto = true;
						}
						if(c.getInstruction().getType() == InstructionType.RETURN){
							hasReturn = true;
							returnList.add(c);
						}
						if((KMP.indexOf(c.getLine(), "throw".getBytes()) == 0)){
							hasThrow = true;
						}
						lines.add(c);

					}
				}
				// just add the block if the while stopped because of no more
				// dot comment
				// if it stopped because there is no following block, the code
				// handling the last block
				// will add all this code aswell, because all the previous code
				// was just .comment
				if (!justDotComment) {
					BasicBlockInterface block = new BasicBlock(lines, this);
					block.setHasReturn(hasReturn);
					block.setHasThrow(hasThrow);
					block.setHasGoto(hasGoto);
					
					if(block.hasReturn()){
					
						try {
							CodeLineInterface lastCodeLine = BasicBlock.getLastCodeLine(block).getCodeLine();

							if (returnList.size()>1 || (returnList.size()==1 && lastCodeLine.getLineNr()!= returnList.getFirst().getLineNr())){
								block.setHasDeadCode(true);
							}
						} catch (SyntaxException e) {
							LOGGER.error("3 "+"Could not find last Codeline, while searching for return codes");

						}
					}
					
					returnList.clear();
					
					hasReturn = false;
					hasThrow = false;
					hasGoto = false;
					
					blocks.add(block);
				}
			}
			//add last block
			//TODO: maybe switch check
			leader=nextLeader;//System.out.println("lastBlock");
			//just reset if we left the intermediate part by adding a block (and not when we still have "buffered"
			//codelines in the list, because of .comment merging 

			
			if(!justDotComment)
				lines=new LinkedList<CodeLineInterface>();
			justDotComment = true;
			for(int i=leader;i<codeLines.size();i++){
				CodeLineInterface c = codeLines.get(i);

				if(isSwitchStart(c)){
					continue;	
				}
				if(isSwitchEnd(c)){
					continue;
				}
				if (isInSwitch()){
					continue;
				}
				if(c.getInstruction().getType() == InstructionType.GOTO){
					hasGoto = true;
				}
				if(c.getInstruction().getType() == InstructionType.RETURN){
					hasReturn = true;
					returnList.add(c);
				}
				if((KMP.indexOf(c.getLine(), "throw".getBytes()) == 0)){
					hasThrow = true;
				}

				if (c.isCode()) {
					justDotComment = false;
				}
				lines.add(c);
				
			}
			if (!justDotComment) {
				BasicBlockInterface block = new BasicBlock(lines, this);
				block.setHasReturn(hasReturn);
				block.setHasThrow(hasThrow);
				block.setHasGoto(hasGoto);
				
				if(block.hasReturn()){
				
					try {
						CodeLineInterface lastCodeLine = BasicBlock.getLastCodeLine(block).getCodeLine();

						if (returnList.size()>1 || (returnList.size()==1 && lastCodeLine.getLineNr()!= returnList.getFirst().getLineNr())){
							block.setHasDeadCode(true);
						}
					} catch (SyntaxException e) {
						LOGGER.error("Could not find last Codeline, while searching for return codes");

					}
				}
				
				returnList.clear();
				
				hasReturn = false;
				hasThrow = false;
				hasGoto = false;
				blocks.add(block);
			}else {
				LinkedList<CodeLineInterface> linesPreviousBlock = blocks.getLast().getCodeLines();
				linesPreviousBlock.addAll(lines);
				BasicBlockInterface block = new BasicBlock(linesPreviousBlock, this);
				blocks.removeLast();
				blocks.add(block);
			}
		}

		return blocks;
	}

	private boolean isSwitchStart(CodeLineInterface cl) {
		if (KMP.indexOf(cl.getLine(), ":sswitch_data".getBytes()) == 0
				|| KMP.indexOf(cl.getLine(), ":pswitch_data".getBytes()) == 0) {
			isCurrentLineInSwitch = true;
			return true;
		}
		return false;
	}

	private boolean isInSwitch() {
		return isCurrentLineInSwitch;
	}

	private boolean isSwitchEnd(CodeLineInterface cl) {
		if (KMP.indexOf(cl.getLine(), ".end sparse-switch".getBytes()) == 0
				|| KMP.indexOf(cl.getLine(), ".end packed-switch".getBytes()) == 0) {
			isCurrentLineInSwitch = false;
			return true;
		}
		return false;//changed from isCurrentLineInSwitch  to false similar error to issue 54
	}

//	private boolean inSwitch(CodeLineInterface cl) {
//		if (isCurrentLineInSwitch) {
//			if (KMP.indexOf(cl.getLine(), ".end sparse-switch".getBytes()) == 0
//					|| KMP.indexOf(cl.getLine(),
//							".end packed-switch".getBytes()) == 0) {
//				isCurrentLineInSwitch = false;// TODO: maybe change on first
//												// line after switch???
//				return true;
//			}
//			return isCurrentLineInSwitch;
//		}
//		if (KMP.indexOf(cl.getLine(), ":sswitch_data".getBytes()) == 0
//				|| KMP.indexOf(cl.getLine(), ":pswitch_data".getBytes()) == 0) {
//			isCurrentLineInSwitch = true;
//		}
//		return isCurrentLineInSwitch;
//	}

	// TODO: give this everywhere as parameter, so we dont need to do it in
	// every function again and again
	private HashMap<String, CodeLineInterface> getLabelLines() {
		HashMap<String, CodeLineInterface> labels = new HashMap<String, CodeLineInterface>();
		// boolean inSwitchTable=false;
		for (int currentLine = 0; currentLine < codeLines.size(); currentLine++) {
			CodeLineInterface cl = codeLines.get(currentLine);

			if (ByteUtils.startsWith(cl.getLine(), ":".getBytes())) {
				String label = new String(cl.getLine());
				if (!labels.containsKey(label))
					labels.put(label, cl);
			}

		}
		return labels;
	}

	private LinkedList<TryBlock> findTryTargetsNew(
			HashMap<String, CodeLineInterface> labels, LinkedList<Link> links) {
		LinkedList<TryBlock> tryList = new LinkedList<TryBlock>();
		TryBlock block = null;
		int begin = -1;
		int end = -1;
		int endOfTryCode = -1;
		boolean findCatches = false;
		for (int currentLine = 0; currentLine < codeLines.size(); currentLine++) {
			CodeLineInterface cl = codeLines.get(currentLine);
			if (findCatches) {
				if (KMP.indexOf(cl.getLine(), ".catch".getBytes()) == 0) {
					// we want the actual CodeLineInterface, so we read the
					// label of the catch block and then look up which CodeLine
					// that is
					CodeLineInterface currentCatchTarget = labels
							.get(new String(
									ByteUtils.subbytes(
											cl.getLine(),
											ByteUtils.indexOfReverse(
													cl.getLine(), ':'))));
					block.addCatch(currentCatchTarget);
					end = cl.getLineNr();
					continue;
				} else {
					findCatches = false;
					block.setEnd(end);

					block.setBlockEnd(codeLines.get(currentLine - 1));
					tryList.add(block);
					block = null;
					
					//add fall through link here?
					//System.out.println("Fall through try link: ");
					if(currentLine < codeLines.size()-1 && endOfTryCode != -1){
						Link link = new Link(codeLines.get(endOfTryCode), codeLines.get(currentLine));
						//System.out.println("link...   from: "+link.getFrom()+" to: "+link.getTo());
						links.add(link);
					}
				}

			}

			if (KMP.indexOf(cl.getLine(), ":try_start_".getBytes()) == 0) {
				begin = cl.getLineNr();
				endOfTryCode = -1;
			}

			if (KMP.indexOf(cl.getLine(), ":try_end_".getBytes()) == 0) {
				block = new TryBlock(begin, end);
				findCatches = true;
				endOfTryCode = currentLine -1;
			}

		}
		return tryList;
	}

	private LinkedList<Link> findIfTargetsNew(LinkedList<Link> links,
			HashMap<String, CodeLineInterface> labels) {

		for (int currentLine = 0; currentLine < codeLines.size(); currentLine++) {
			CodeLineInterface cl = codeLines.get(currentLine);

			if (cl.getInstruction().getType() == InstructionType.JMP) {
				String label = new String(cl.getInstruction().getLabel());

				CodeLineInterface to = labels.get(label);
				Link link = new Link(cl, to);
				if (!links.contains(link))
					links.add(link);
				
				//fallthrough
				//need to check if next is switch? probably not, because there is always a fallthrough so 
				//compiler should prevent fall to switchtable (would probably result in an error)
				if(currentLine < codeLines.size()-1){
					to = codeLines.get(currentLine+1);
					link = new Link(cl, to);
					if (!links.contains(link))
						links.add(link);
				}
			}
		}
		return links;
	}

	private LinkedList<Link> findGotoTargetsNew(LinkedList<Link> links,
			HashMap<String, CodeLineInterface> labels, LinkedList<Integer> leaders) {

		for (int currentLine = 0; currentLine < codeLines.size(); currentLine++) {
			CodeLineInterface cl = codeLines.get(currentLine);

			if (cl.getInstruction().getType() == InstructionType.GOTO) {
				String label = new String(cl.getInstruction().getLabel());

				CodeLineInterface to = labels.get(label);
				Link link = new Link(cl, to);
				if (!links.contains(link))
					links.add(link);
				if(currentLine<codeLines.size()-1){
					leaders.add(currentLine+1);
				}
			}
		}

		return links;
	}

	private LinkedList<Link> findSwitchTargetsNew(LinkedList<Link> links,
			HashMap<String, CodeLineInterface> labels) {

		HashMap<String, LinkedList<Target>> switchTablesNew = new HashMap<String, LinkedList<Target>>();
		HashMap<String, CodeLineInterface> switchInstructions = new HashMap<String, CodeLineInterface>();
		LinkedList<Target> tmpList = new LinkedList<Target>();
		String switchName = null;
		boolean inSwitchTable = false;

		for (int currentLine = 0; currentLine < codeLines.size(); currentLine++) {
			CodeLineInterface cl = codeLines.get(currentLine);

			// switch start found
			// build association between the switch-statement and the
			// corresponding switch-table
			if (cl.getInstruction().getType() == InstructionType.SWITCH) {
				String tableName = new String(cl.getInstruction().getLabel());


				if (!switchInstructions.containsKey(tableName))
					switchInstructions.put(tableName, cl);
			}
			// switch table BEGIN found
			// if(ByteUtils.startsWith(cl.getLine(), ":switch ".getBytes())){
			// label of current switch tables
			// Table starts in the following line

			// name of the switch table
			// next line is a .sparse/packed_switch, after that , the switches
			// are listed, containing the initial value too compare too
			if (KMP.indexOf(cl.getLine(), ":sswitch_data".getBytes()) == 0
					|| KMP.indexOf(cl.getLine(), ":pswitch_data".getBytes()) == 0) {

				switchName = new String(cl.getLine());

				continue;
			}
			// the initial value... in hex
			// example : .packed_switch 0x0
			// -> first one taken if value = 0, second if value 2 etc.
			// TODO: might be used for labels, but will be ignored at the moment
			if (KMP.indexOf(cl.getLine(), ".sparse-switch".getBytes()) == 0
					|| KMP.indexOf(cl.getLine(), ".packed-switch".getBytes()) == 0) {
				// initialvalue = end of line (from hex to decimal)
				inSwitchTable = true;
				continue;
			}

			// read table
			// determine which switch label belongs to which switch table
			// condition shoudl be in front of : , target = :blabla
			if (inSwitchTable) {
				// check of end of table first

				// switch table END found
				// end of switch table
				if (KMP.indexOf(cl.getLine(), ".end sparse-switch".getBytes()) == 0
						|| KMP.indexOf(cl.getLine(),
								".end packed-switch".getBytes()) == 0) {

					// put links into table
					switchTablesNew.put(switchName, tmpList);
					// ini new list
					tmpList = new LinkedList<Target>();
					switchName = null;

					inSwitchTable = false;
					continue;
				}

				// if not end of table, make links and put them into the list
				// use switchName for map association
				int start = KMP.indexOf(cl.getLine(), ":".getBytes());
				// targets are starting with a :
				String switchTarget = new String(ByteUtils.subbytes(
						cl.getLine(), start));
				CodeLineInterface switchTargetLine = labels.get(switchTarget);
				// TODO: add label
				tmpList.add(new Target(switchTargetLine));
				continue;

			}
		}

		// now that we have seen all the switches, make all the links based on
		// the instructions and corresponding tables

		for (Map.Entry<String, CodeLineInterface> entry : switchInstructions
				.entrySet()) {
			String key = entry.getKey();
			CodeLineInterface value = entry.getValue();
			LinkedList<Target> targets = switchTablesNew.get(key);
			for (Target t : targets) {
				links.add(new Link(value, t.getTo()));
			}
		}

		// return all the links
		return links;
	}

	private LinkedList<Link> findLeadersNew() {
		// all leaders of the BBs
		LinkedList<Link> links = new LinkedList<Link>();
		// list of BBs
		// LinkedList<BasicBlock> blockList = new LinkedList<BasicBlock>();
		// nur durchrutsch bedingungen
		for (int currentLine = 0; currentLine < codeLines.size() - 1; currentLine++) {
			CodeLineInterface cl = codeLines.get(currentLine);
			Link link = new Link(cl, codeLines.get(currentLine + 1));
			if (isLeaderNew(cl) && !links.contains(link)) {
				links.add(link);
			}
		}

		return links;
	}

	/**
	 * Get the unparsed parameters of this method. .method public constructor
	 * <init>(Landroid/content/Context;)V would return Landroid/content/Context;
	 * 
	 * @return the parameter declaration
	 */
	public byte[] getParameters() {
		return rawParameters;
	}
	
	/**
	 * Get the unparsed return value of this method. .method public constructor
	 * <init>(Landroid/content/Context;)V would return V.
	 * 
	 * @return the return value
	 */
	public byte[] getReturnValue() {
		return returnValue;
	}

	/**
	 * Get the first BB.
	 * 
	 * @return the first BB or null if none is available
	 */
	public BasicBlockInterface getFirstBasicBlock() {
		if (bbList.isEmpty())
			return null;
		return bbList.getFirst(); // firstBasicBlock;
	}

	public LinkedList<BasicBlockInterface> getBasicBlocks() {
		return bbList;
	}

	public String getName() {
		return name;
	}

	public LinkedList<FieldInterface> getLocalFields() {
		if (localFieldList == null)
			localFieldList = Field.parseAllFields(codeLines);
		return localFieldList;
	}

//	/**
//	 * Check if the line is the end of a BB, so the next Line is a leader TODO:
//	 * rename
//	 * 
//	 * @param line
//	 * @return
//	 */
//	private boolean isLeader(CodeLineInterface cl) {
//		if (cl.getInstruction().getType() == InstructionType.JMP
//				|| cl.getInstruction().getType() == InstructionType.GOTO
//				|| cl.getInstruction().getType() == InstructionType.SWITCH)
//			return true;
//		return false;
//	}

	/**
	 * Check if the line is the end of a BB, so the next Line is a leader TODO:
	 * rename
	 * 
	 * @param line
	 * @return
	 */
	private boolean isLeaderNew(CodeLineInterface cl) {
		if (cl.getInstruction().getType() == InstructionType.JMP
				|| cl.getInstruction().getType() == InstructionType.SWITCH // für
																			// den
																			// default
																			// fall
																			// TODO:
																			// maybe
																			// remove
																			// and
																			// handle
																			// in
																			// switch
		)
			return true;
		return false;
	}

	/**
	 * Check if a given instruction is within the methods code lines.
	 * 
	 * TODO: methode einbauen die auch noch die enstprechenden CLS als
	 * linkedList zurueckgibt?
	 * 
	 * @param instruction
	 *            the instruction, or parts of it
	 * @return true if the instruction is found inside the instruction, comments
	 *         etc are not searched.
	 */
	public boolean contains(byte[] instruction) {
		for (CodeLineInterface cl : codeLines) {
			if (cl.isCode() && cl.contains(instruction))
				return true;
		}
		return false;
	}

	@Override
	public String toString(){
		return name+"("+getParameterString()+")"+getReturnValueString();
	}
	
	/**
	 * Can be used to print all CodeLines and some
	 * other data to stdout.
	 * @return the "contents" of the method
	 */
	public String dump() {
		StringBuilder sb = new StringBuilder();

		sb.append("Methodname: " + name);
		sb.append("\nMethodtype: ");
		sb.append(methodType);
		sb.append("\n");
		for (FieldInterface f : getLocalFields()) {
			sb.append(f);
			sb.append("\n");
		}
		sb.append("Code: ");
		sb.append("\n");

		for (CodeLineInterface cl : codeLines) {
//			if (!cl.isCode()) continue;
			sb.append(cl);
			sb.append("\n");
		}
		return sb.toString();
	}


	private final static String DOT_BR = "<BR ALIGN=\"LEFT\"/>";
	private String dotInstructions = null;

	/**
	 * FIXME das ist langsam weil es aus byte[] nen string macht und dann viel
	 * replace
	 * 
	 * @return
	 */
	public String getInstructionsForDot() {
		Pattern p = Pattern.compile("[vp][0-9]+");// pattern to match everything
													// begin with v or p and
													// havin at least one digit
													// after that
		if (dotInstructions == null) { // create it
			StringBuilder sb = new StringBuilder();
			String name = this.getName().replace(">", "").replace("<", "")
					.replace("&", "").replace("$", "");
			// int count = 0;
			for (BasicBlockInterface bb : bbList) {
				sb.append(name);// TODO: Add better replacement (real escaping)
				sb.append("_");
				sb.append(bb.getCodeLines().getFirst().getLineNr());
				sb.append("[shape=box, label=<");
				// dot.append(method.getInstructionsForDot());
				sb.append(bb.getUniqueId());
				sb.append(DOT_BR);
				sb.append(System.getProperty("line.separator"));
				for (CodeLineInterface cl : bb.getCodeLines()) {
					if (cl.isEmpty())
						continue; // skip empty lines
					String inst = cl.getNrAndLine().replace("&", "&amp;")
							.replace(">", "&gt;").replace("<", "&lt;");// TODO:
																		// need
																		// to
																		// replace
																		// $
																		// here
																		// aswell?
					inst = syntaxhighlighting(inst, p);
					sb.append(inst);
					sb.append(DOT_BR);
					sb.append(System.getProperty("line.separator"));
				}
				sb.append(">];\n");

				for (BasicBlockInterface next : bb.getNextBB()) {
					sb.append(name + "_"
							+ bb.getCodeLines().getFirst().getLineNr() + " -> "
							+ name + "_"
							+ next.getCodeLines().getFirst().getLineNr()
							+ ";\n");
				}
			}

			dotInstructions = sb.toString();
		}
		return dotInstructions;
	}

	private String syntaxhighlighting(String zeile, Pattern p) {

		// commands
		Vector<String> commands = Highlight.OP_CODES;

		for (String cmd : commands) {
			zeile = zeile.replace(cmd, "<FONT COLOR=\"red\">" + cmd
					+ "</FONT> ");
		}


		StringTokenizer st = new StringTokenizer(zeile, " ,}{", true);
		String reg = new String();
		while (st.hasMoreTokens()) {
			String temp = st.nextToken();
			Matcher m = p.matcher(temp);
			if (m.matches())
				reg += "<FONT COLOR=\"blue\">" + temp + "</FONT>";
			else
				reg += temp;
		}
		zeile = reg;

		// replace
		zeile = zeile.trim().replace("-&gt;",
				"<FONT COLOR=\"blue\">-&gt;</FONT>");

		// jumps
		Vector<String> jumps = new Vector<String>();

		jumps.add(":cond_");
		jumps.add(":goto_");
		jumps.add(":catch_");
		jumps.add(":catchall_");

		for (int i = 0; i < jumps.size(); i++) {
			for (int z = 100; z >= 0; z--) {
				zeile = zeile.trim().replace(
						jumps.get(i) + z + "",
						"<FONT COLOR=\"#009900\">" + jumps.get(i) + z
								+ "</FONT>");
			}
		}

		// Annotations
		if (zeile.trim().startsWith(".") || zeile.trim().startsWith("#")
				|| zeile.trim().startsWith(":")) {
			zeile = "<FONT COLOR=\"#5F5F5F\">" + zeile + "</FONT>";
		}

		return zeile;
	}

	private Float arithOpsAmount = null;
	private boolean changed;
	private boolean obfuscated;
	private double entropy;

	/**
	 * Calculate the percentage of artithmetic operations in this function.
	 * 
	 * @return
	 */
	public float arithOps() {
		if (arithOpsAmount != null)
			return arithOpsAmount;
		// else
		float arithOps = 0F;
		float allOps = 0F;

		for (CodeLineInterface cl : codeLines) {

			// don't count comments and annotations
			if (cl.isCode())
				allOps++;
			// arithmetic operations
			InstructionInterface i = cl.getInstruction();
			if (i.getType() == InstructionType.MATH_1
					|| i.getType() == InstructionType.MATH_2
					|| i.getType() == InstructionType.MATH_2C) {
				arithOps++;
			}
		}

		if (allOps == 0) {
			arithOps = 0F;
		} else {
			arithOps = (arithOps / allOps);
		}
		arithOpsAmount = arithOps;
		return arithOps;
	}

	public LinkedList<CodeLineInterface> getCodeLines() {
		return codeLines;
	}

	/**
	 * 
	 * @return the SMALI file this method belongs to
	 */
	public ClassInterface getSmaliClass() {
		return smaliClass;
	}

	// ################# getter and setter ##################################

	public boolean isStatic() {
		return isStatic;
	}

	/**
	 * Return the class with full path, the method name and the (raw) parameters
	 * as byte arrays.
	 * 
	 * @return [ classname, name, parameters ]
	 */
	public byte[][] getCmp() {
		byte[][] cmp = new byte[3][];
		cmp[0] = getSmaliClass().getFullClassName(false).getBytes();
		cmp[1] = name.getBytes();
		cmp[2] = rawParameters;
		return cmp;
	}

	/**
	 * This gives the parameters in their short form.
	 * see: https://code.google.com/p/smali/wiki/TypesMethodsAndFields  for more information
	 * @return the parameters of this method in their short letter form
	 */	
	public String getParameterString(){
		if(parameters == null) {
			parameters = new String(this.getParameters());
			parameters = parameters.replaceAll("L.[^;]*;", "L");
		}

		return parameters;
	}

	/**
	 * 
	 * @param hash
	 */
	public void setFuzzyHash(String hash) {
		fuzzyHash = hash;
	}

	/**
	 * @return the fuzzy hash of this method
	 */
	public String getFuzzyHash() {
		return fuzzyHash;
	}

	/**
	 * Get the unique label of this Method within a SmaliClass.
	 * 
	 * @return
	 */
	public int getLabel() {
		return label;
	}

	public String getUniqueLabel() {
		StringBuffer sb = new StringBuffer();
		sb.append(getSmaliClass().getUniqueId());
		sb.append(',');
		sb.append(label);
		return sb.toString();
	}

	/**
	 * TODO and FIXME: convert the .method... line to a more java syntax, such
	 * as private void full-class-name.methodname(String s); Right now this
	 * method will only return the classpath and the .method name. It would be
	 * best to overwrite this method which will append the full-class-name and
	 * one method which does not do it (real java syntax)
	 * 
	 * @return
	 */
	public String getReadableJavaName() {
		return getSmaliClass().getFullClassName(true) + ": "
				+ new String(getCodeLines().getFirst().getLine());
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public void setEmpty(boolean isEmpty) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBasicBlocks(LinkedList<BasicBlockInterface> blocks) {
		this.bbList = blocks;
		setChanged(true);
	}

	@Override
	public void setName(String name) {
		this.name = name;
		setChanged(true);
	}

	@Override
	public void setLocalFields(LinkedList<FieldInterface> localFields) {
		this.localFieldList = localFields;
		setChanged(true);
	}

	@Override
	public void setCodeLines(LinkedList<CodeLineInterface> lines) {
		this.codeLines = lines;
		setChanged(true);
	}

	@Override
	public void setSmaliClass(ClassInterface smaliClass) {
		this.smaliClass = smaliClass;
		setChanged(true);
	}

	@Override
	public void isStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	@Override
	public void setParameterString(String params) {
		throw new IllegalArgumentException("Params cannot be set as they are parsed from the smali file/class.");
	}

	@Override
	public void setLabel(int label) {
		this.label = label;
		setChanged(true);
	}

	@Override
	public void setUniqueLabel(String ulabel) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setReadableJavaName(String javaName) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setChanged(boolean changed) {
		this.changed = changed;		
	}

	@Override
	public boolean isChanged() {
		return this.changed;
	}

	@Override
	public String getReturnValueString() {
		if(returnValueString == null) {
			returnValueString = new String(this.returnValue);
			returnValueString = returnValueString.replaceAll("L.[^;]*;", "L");
		}

		return returnValueString;
	}

	@Override
	public void setHasUnlinkedBlocks(boolean hasUnlinkedBBs) {
		this.hasUnlinkedBBs = hasUnlinkedBBs;
	}
	
	@Override
	public boolean hasUnlinkedBBs() {
		return hasUnlinkedBBs;
	}
	
	@Override
	public boolean isProbablyPatched() {
		if (hasUnlinkedBBs) return true;
		else {
			for (BasicBlockInterface bb : getBasicBlocks()) {
				if (bb.hasDeadCode()) return true;
			}
		}
		return false;
	}

	@Override
	public void setObfuscated(boolean b) {
		this.obfuscated=b;
		
	}

	@Override
	public boolean isObfuscated() {
		return this.obfuscated;
	}

	@Override
	public void setEntropy(double entropy) {
		this.entropy=entropy;
	}

	@Override
	public double getEntropy() {
		return this.entropy;
	}
}
