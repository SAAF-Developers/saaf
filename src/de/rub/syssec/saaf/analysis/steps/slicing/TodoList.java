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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.application.methods.BasicBlock;
import de.rub.syssec.saaf.misc.ByteUtils;
import de.rub.syssec.saaf.model.application.BasicBlockInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.DetectionLogicError;
import de.rub.syssec.saaf.model.application.SyntaxException;

/**
 * 
 * This class is a helper class for the DetectionLogic and holds "jobs" which need to be processed
 * at a later time in the program slicing process. 
 * 
 * @author Johannes Hoffmann <johannes.hoffmann@rub.de>
 *
 */
public class TodoList {
	
	private static final boolean DEBUG=Boolean.parseBoolean(System.getProperty("debug.slicing","false"));
	private HashSet<ClassContentTracker> returnMap = new HashSet<ClassContentTracker>();
	private HashSet<ClassContentTracker> returnMapDone = new HashSet<ClassContentTracker>();
	
	private static final Logger LOGGER = Logger.getLogger(TodoList.class);
	
	// TODO: Make it configurable
	protected static final int MAX_FUZZY_LEVEL = 10;
	protected static final int MAX_RS_COUNT = 100000;
	
	
	/**
	 * Add new method to search.
	 * @param cm
	 * @param fuzzyLevel
	 * @param fuzzyLevelOffset
	 * @param path
	 * @return true if this was not yet searched through, create a new object, you'll get inconsistencies if you'll reuse the same object
	 */
	public boolean addReturnValuesFromMethod(byte[][] cm, int fuzzyLevel, int fuzzyLevelOffset, LinkedList<BasicBlockInterface> path) {
		if (DEBUG) LOGGER.debug(" -> Add RETURN VALUE: "+new String(cm[0])+"."+new String(cm[1])+"\tfuzzy="+fuzzyLevel+"/"+fuzzyLevelOffset);
		if (fuzzyLevel+fuzzyLevelOffset > MAX_FUZZY_LEVEL) {
			if (DEBUG) LOGGER.debug("    Maximum fuzzy level reached ("+MAX_FUZZY_LEVEL+"): aborting.");
			return false;
		}
		ClassContentTracker ctt = new ClassContentTracker(cm, fuzzyLevel, fuzzyLevelOffset, path);
		if (returnMap.contains(ctt)) return false;
		if (returnMapDone.contains(ctt)) return false;
		returnMap.add(ctt);
		return true;
	}
	
	
	public ClassContentTracker getNextReturnValuesFromMethod() {
		ClassContentTracker ctt = null;
		for (ClassContentTracker ctt2 : returnMap) { // get one item "at random"
			ctt = ctt2;
			break;
		}
		returnMap.remove(ctt);
		returnMapDone.add(ctt);
		if (DEBUG) LOGGER.debug("\n\n-> TRACKING RETURN VALUE: "+new String(ctt.getCi()[0])+"."+new String(ctt.getCi()[1])+"\tfuzzy="+ctt.getFuzzyLevel()+"/"+ctt.getFuzzyOffset());
		return ctt;
	}
	
	/**
	 * FIXME name
	 */
	public int getRemainingReturnValuesFromMethods() {
		return returnMap.size();
	}
	
	
	//
	//
	
	
	private HashSet<ClassContentTracker> fieldMap = new HashSet<ClassContentTracker>();
	private HashSet<ClassContentTracker> fieldMapDone = new HashSet<ClassContentTracker>();
	
	
	/**
	 * Add new field to search.
	 * @param cf class, field
	 * @param fuzzyLevel
	 * @param fuzzyLevelOffset
	 * @param linkedList
	 * @return true if this was not yet searched through, create a new object, you'll get inconsistencies if you'll reuse the same object
	 */
	public boolean addField(byte[][] cf, int fuzzyLevel, int fuzzyLevelOffset, LinkedList<BasicBlockInterface> linkedList) {
		if (DEBUG) LOGGER.debug(" -> Add FIELD: "+new String(cf[0])+"."+new String(cf[1])+"\tfuzzy="+fuzzyLevel+"/"+fuzzyLevelOffset);
		if (fuzzyLevel+fuzzyLevelOffset > MAX_FUZZY_LEVEL) {
			if (DEBUG) LOGGER.debug("    Maximum fuzzy level reached ("+MAX_FUZZY_LEVEL+"): aborting.");
			return false;
		}
		if (fuzzyLevelOffset < MAX_FUZZY_LEVEL-2) {
			if (DEBUG) LOGGER.debug("    Setting fuzzy offset to "+(MAX_FUZZY_LEVEL-2));
			fuzzyLevelOffset = MAX_FUZZY_LEVEL-2;
		}
		ClassContentTracker ctt = new ClassContentTracker(cf, fuzzyLevel, fuzzyLevelOffset, linkedList);
		if (fieldMap.contains(ctt)) return false;
		if (fieldMapDone.contains(ctt)) return false;
		fieldMap.add(ctt);
		return true;
	}
	
	
	public ClassContentTracker getNextField() {
		ClassContentTracker ctt = null;
		for (ClassContentTracker ctt2 : fieldMap) { // get one item "at random"
			ctt = ctt2;
			break;
		}
		fieldMap.remove(ctt);
		fieldMapDone.add(ctt);
		if (DEBUG) LOGGER.debug("\n\n-> TRACKING FIELD: "+new String(ctt.getCi()[0])+"."+new String(ctt.getCi()[1])+"\tfuzzy="+ctt.getFuzzyLevel()+"/"+ctt.getFuzzyOffset());
		return ctt;
	}
	
	
	public int getRemainingFieldsToTrack() {
		return fieldMap.size();
	}
	
	
	//
	//
	
	
	public static class RegisterSearch {
		private final byte[] register;
		private BasicBlockInterface bb;
		private final int index;
		private final int fuzzyLevel;
		private LinkedList<BasicBlockInterface> path;
		private int fuzzyOffset;
		
	
		/**
		 * A helper class to backtrack a register.
		 * @param register the registername to backtrack, eg, v0.
		 * @param bb2 the {@linkplain BasicBlock} to backtrack
		 * @param index the index where to start backtracking inside the {@linkplain BasicBlock}
		 * @param fuzzyLevel set this to >1 if the search gets noisy, eg, if you are backtracking into the blue for unknown method calls and are interested in the parameters from such a call
		 * @param fuzzyOffset
		 * @param path the path through the BBs of this search, create a new object, you'll get inconsistencies if you'll reuse the same object 
		 */
		public RegisterSearch(byte[] register, BasicBlockInterface bb, int index, int fuzzyLevel, int fuzzyOffset, LinkedList<BasicBlockInterface> path) {
			this.register = register;
			this.bb = bb;
			this.index = index;
			this.fuzzyLevel = fuzzyLevel;
			this.fuzzyOffset = fuzzyOffset;
			this.path = path;
		}
		
		/**
		 * The register to track backwards.
		 * @return
		 */
		public byte[] getRegister() {
			return register;
		}

		/**
		 * The BB which holds all instructions
		 */
		public BasicBlockInterface getBB() {
			return bb;
		}


		/**
		 * The index of the opcode from which the register is tracked backwards.
		 * The index points to the previously found codeline where the search
		 * 'stopped' and this RS was created.
		 * @return
		 */
		public int getIndex() {
			return index;
		}
		
		/**
		 * Was this a fuzzy search. If so, the Results may be (very) inaccurate.
		 * @return 0 for a non-fuzzy search. Higher values means more fuzziness
		 */
		public int getFuzzyLevel() {
			return fuzzyLevel;
		}
		
		
		/**
		 * The path through the program for this search. 
		 * @return the path
		 */
		public LinkedList<BasicBlockInterface> getPath() {
			return path;
		}
		
		/**
		 * This method sets an offset to the fuzzy value. If the offset
		 * plus the fuzzy is too high, the search will be aborted.
		 * @param offset
		 */
		public void setFuzzyOffset(int offset) {
			fuzzyOffset = offset;
		}
		
		public int getFuzzyOffset() {
			return fuzzyOffset;
		}
	}
	
	
	private static boolean checkRsForEquality(RegisterSearch rs1, RegisterSearch rs2) {
		if (!Arrays.equals(rs1.getRegister(), rs2.getRegister())) return false;
		if (rs1.getBB() != rs2.getBB()) return false;
		if (rs1.getIndex() != rs2.getIndex()) return false;
		return true;
	}
	
	
	LinkedList<RegisterSearch> regList = new LinkedList<RegisterSearch>();
	LinkedList<RegisterSearch> regListDone = new LinkedList<RegisterSearch>();
	
	
	/**
	 * @param rs
	 * @return
	 * @throws DetectionLogicError 
	 */
	public boolean addRegisterToTrack(RegisterSearch rs) {
		if (DEBUG) LOGGER.debug(" -> Add REGISTER: "+new String(rs.getRegister())+", "+rs.getBB().getMethod().getName()+":"+rs.getIndex()+"\tfuzzy="+rs.getFuzzyLevel()+"/"+rs.getFuzzyOffset()+", bb="+rs.getBB().getUniqueId());
		if (rs.getFuzzyLevel()+rs.getFuzzyOffset() > MAX_FUZZY_LEVEL) {
			if (DEBUG) LOGGER.debug("    Maximum fuzzy level reached ("+MAX_FUZZY_LEVEL+"): aborting.");
			return false;
		}
		
		for (RegisterSearch rs2 : regListDone) {
			if (checkRsForEquality(rs, rs2)) {
				if (DEBUG) LOGGER.debug("     Already searched this RS. It will be ignored! (This is ok)");
				return false;
			}
		}
		boolean found = false;
		for (RegisterSearch rs2 : regList) {
			if (checkRsForEquality(rs, rs2)) {
				found = true;
				break;
			}
		}
		if (!found) regList.addFirst(rs); // this way we first work up actual stuff
		else if (DEBUG) LOGGER.debug("     Duplicate RS added. It will be ignored! (This is ok)");
		return true;	
	}
	
	
	public RegisterSearch getNextRegisterToTrack() {
		if (regList.size() == 0) return null;
		else {
			RegisterSearch rs = regList.getFirst();
			regList.removeFirst();
			regListDone.addLast(rs);
			// debug only: FIXME
			int i = rs.getIndex();
			if (i<0) i = 0;
			else if (i>=rs.getBB().getMethod().getCodeLines().size()) {
				i = rs.getBB().getMethod().getCodeLines().size()-1;
			}
			CodeLineInterface cl =  rs.getBB().getMethod().getCodeLines().get(i);
			if (DEBUG) LOGGER.debug("\n\n-> TRACKING REGISTER: "+new String(rs.getRegister())+", "+rs.getBB().getMethod().getSmaliClass().getFullClassName(true)+"."+rs.getBB().getMethod().getName()+":"+cl.getLineNr()+"\tfuzzy="+rs.getFuzzyLevel()+"/"+rs.getFuzzyOffset());
			// /debug only
			return rs;
		}
	}
	
	
	public int getRemainingRegistersToTrack() {
		return regList.size();
	}
	
	
	//
	//
	
	private HashSet<ClassContentTracker> arrayMap = new HashSet<ClassContentTracker>();
	private HashSet<ClassContentTracker> arrayMapDone = new HashSet<ClassContentTracker>();
	
	
	/**
	 * A helper class to identify content of a given class. This may be
	 * an array name, a method or a field name.
	 */
	public static class ClassContentTracker {
		private final byte[][] ci;
		private final int fuzzyLevel;
		private final int fuzzyLevelOffset;
		private LinkedList<BasicBlockInterface> path;
		
		/**
		 * This is a helper class to wrap some content. It can contain arbitrary data,
		 * but the first entry of the first parameter must always be the full class
		 * name and the second one the identifier of a method, array or field.
		 * Further entries are optional and can contain, eg, the parameters for a
		 * method and it's return value.
		 * @param ci [class, identifier, more optional]
		 * @param fuzzyLevel
		 * @param fuzzyLevelOffset the offset to the fuzzyLevel
		 * @param create a new object, you'll get inconsistencies if you'll reuse the same object
		 */
		public ClassContentTracker(byte[][] ci, int fuzzyLevel, int fuzzyLevelOffset, LinkedList<BasicBlockInterface> path2) {
			this.ci = ci;
			this.fuzzyLevel = fuzzyLevel;
			this.fuzzyLevelOffset = fuzzyLevelOffset;
			this.path = path2;
		}
		
		/**
		 * Get the class and identifier of the class content (array, method or field name)
		 * and other optional data.
		 * @return ci, ci[0]=full class name, ci[1]=identifier (ci[2+] are optional and can contain more data) 
		 */
		public byte[][] getCi() {
			return ci;
		}
		
		/**
		 * Is the search fuzzy, that means inaccurate?
		 * @return 0 if it is not fuzzy, or a number > 0 for a fuzzy search
		 */
		public int getFuzzyLevel() {
			return fuzzyLevel;
		}
		
		public int getFuzzyOffset() {
			return fuzzyLevelOffset;
		}
		

		/**
		 * The path through the program for this search, the list is always a new copy of the original list.
		 * Changes to the returned list will not effect the internal list!
		 * @return the path
		 */
		public LinkedList<BasicBlockInterface> getPath() {
			return new LinkedList<BasicBlockInterface>(path);
		}
		
	    @Override
	    public boolean equals(Object other)
	    {
	        if (!(other instanceof ClassContentTracker)) {
	            return false;
	        }
	        // check the arrays for equality
	        if (ci.length != ((ClassContentTracker)other).getCi().length) return false;
	        for (int i=0; i<ci.length; i++) {
	        	if (!(Arrays.equals(ci[i], ((ClassContentTracker)other).getCi()[i]))) return false;
	        }
	        return true;
	    }
	    
	    private Integer hashCode = null;

	    @Override
	    public int hashCode() {
	    	if (hashCode == null) {
	    		byte[] tmp = new byte[0];
		    	for (int i=0; i<ci.length; i++) {
		    		tmp = ByteUtils.concatAll(tmp, ci[i]);
		    	}
		        hashCode = Arrays.hashCode(tmp);
	    	}
	    	return hashCode; 
	    }
	    
	    @Override
	    public String toString() {
	    	StringBuilder sb = new StringBuilder();
	    	sb.append("ci=");
	    	for (byte[] b : ci) {
	    		sb.append(new String(b));
	    		sb.append(" ");
	    	}
	    	sb.append(", fuzzy=");
	    	sb.append(fuzzyLevel);
	    	sb.append(", pathLen=");
	    	sb.append(path.size());
	    	return sb.toString();
	    }
	    
	}
	
	/**
	 * Add a new array field instance where accesses to are searched later on.
	 * @param ci: class, array-field-name
	 * @param fuzzyLevel
	 * @param path, create a new object, you'll get inconsistencies if you'll reuse the same object
	 * @throws SyntaxException 
	 */
	public boolean addArrayFieldToTrack(byte[][] ca, int fuzzyLevel, int fuzzyLevelOffset, LinkedList<BasicBlockInterface> path) throws SyntaxException {
		if (DEBUG) LOGGER.debug(" -> Add ARRAY FIELD: "+new String(ca[0])+"."+new String(ca[1])+"\tfuzzy="+fuzzyLevel+"/"+fuzzyLevelOffset);
		if (fuzzyLevel+fuzzyLevelOffset > MAX_FUZZY_LEVEL) {
			if (DEBUG) LOGGER.debug("    Maximum fuzzy level reached ("+MAX_FUZZY_LEVEL+"): aborting.");
			return false;
		}
		if (fuzzyLevelOffset < MAX_FUZZY_LEVEL-2) {
			if (DEBUG) LOGGER.debug("    Setting fuzzy offset to "+(MAX_FUZZY_LEVEL-2));
			fuzzyLevelOffset = MAX_FUZZY_LEVEL-2;
		}
		
		ClassContentTracker ctt = new ClassContentTracker(ca, fuzzyLevel, fuzzyLevelOffset, path);
		if (arrayMap.contains(ctt)) return false;
		if (arrayMapDone.contains(ctt)) return false;
		arrayMap.add(ctt);
		return true;
	}
	
	
	public ClassContentTracker getNextCaToTrack() {
		ClassContentTracker ctt = null;
		for (ClassContentTracker ctt2 : arrayMap) { // get one item "at random"
			ctt = ctt2;
			break;
		}
		arrayMap.remove(ctt);
		arrayMapDone.add(ctt);
		if (DEBUG) LOGGER.debug("\n\n-> TRACKING ARRAY FIELD: "+new String(ctt.getCi()[0])+"."+new String(ctt.getCi()[1])+"\tfuzzy="+ctt.getFuzzyLevel()+"/"+ctt.getFuzzyOffset());
		return ctt;
	}
	
	
	
	public int getRemainingArraysToTrack() {
		return arrayMap.size();
	}
	
	
	public boolean isFinished() {
		if (	getRemainingRegistersToTrack() > 0 || getRemainingFieldsToTrack() > 0
				|| getRemainingReturnValuesFromMethods() > 0 || getRemainingArraysToTrack() > 0)
			return false;
		else return true;
	}
	
	
	/**
	 * Returns the amount of finished RS searches.
	 * @return
	 */
	public int getFinishedRsCount() {
		return regListDone.size();
	}
}
