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

import java.util.LinkedList;

import de.rub.syssec.saaf.application.methods.BasicBlock.FoundCodeLine;
import de.rub.syssec.saaf.model.application.BasicBlockInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.SyntaxException;

public class BasicBlock implements BasicBlockInterface {

	/**
	 * A small helper class which we should get rid of.
	 */
	public static class FoundCodeLine {
		private final CodeLineInterface cl;
		private final int index;
		private final BasicBlockInterface bb;
		
		public FoundCodeLine(CodeLineInterface cl, BasicBlockInterface bb, int index) {
			this.cl = cl;
			this.index = index;
			this.bb = bb;
		}
		
		/**
		 * 
		 * @return the cl found in the BB
		 */
		public CodeLineInterface getCodeLine() {
			return cl;
		}
		
		/**
		 * @return the index of the cl in the BB where it was found
		 */
		public int getIndex() {
			return index;
		}
		
		/**
		 * Get the BB where the found CL is located to.
		 * @return the corresponding BB
		 */
		public BasicBlockInterface getBasicBlock() {
			return bb;
		}
	}

	private boolean hasReturn = false;
	private boolean hasThrow = false;
	private boolean hasGoto = false;
	private boolean hasDeadCode = false;
	private boolean isTryBlock = false;
	private boolean isCatchBlock = false;


	private final LinkedList<CodeLineInterface> codeLines;
	private final Method method;
	private boolean isSwitchTable = false;
	private LinkedList<BasicBlockInterface> nextBlocks = new LinkedList<BasicBlockInterface>();
	private LinkedList<BasicBlockInterface> previousBlocks = new LinkedList<BasicBlockInterface>();

	private LinkedList<Link> nextLinkBlocks = new LinkedList<Link>();
	private LinkedList<Link> previousLinkBlocks = new LinkedList<Link>();

	// The label assigned by a DFS
	private int label = -1;

	public LinkedList<Link> getNextLinkBlocks() {
		return nextLinkBlocks;
	}

	public void addNextLinkBlocks(Link link) {
		if (!nextLinkBlocks.contains(link))
			nextLinkBlocks.add(link);
	}

	public LinkedList<Link> getPreviousLinkBlocks() {
		return previousLinkBlocks;
	}

	public void addPreviousLinkBlocks(Link link) {
		if (!previousLinkBlocks.contains(link))
			previousLinkBlocks.add(link);
	}

	/**
	 * A BasicBlock.
	 * 
	 * @param codeLines
	 *            for this BB
	 * @param method
	 *            the method where this BB belongs to
	 */
	public BasicBlock(LinkedList<CodeLineInterface> codeLines, Method method) {
		this.codeLines = codeLines;
		this.method = method;
	}

	/**
	 * Return previous BBs.
	 */
	public LinkedList<BasicBlockInterface> getPreviousBB() {
		return previousBlocks;
	}

	/**
	 * 
	 * @return the list of BBs reachable from this BB
	 */
	public LinkedList<BasicBlockInterface> getNextBB() {
		return nextBlocks;
	}

	/**
	 * This method adds a bb to the list of previous BBs
	 * 
	 * @param bb
	 *            - the bb to add to the list of previous BBs
	 */
	public void addPreviousBB(BasicBlockInterface bb) {
		if (!previousBlocks.contains(bb))
			previousBlocks.add(bb);
	}

	/**
	 * This method adds a bb to the list of following BBs
	 * 
	 * @param bb
	 *            - the bb to add to the list of BBs following this BB
	 */
	public void addNextBB(BasicBlockInterface bb) {
		// multiple following BBs are possible (case, if-else, exceptions, ...)
		if (!(nextBlocks.contains(bb)))
			nextBlocks.add(bb);
	}

	/**
	 * This method determines if a line is in the scope of this BB
	 * 
	 * @param nr
	 *            - the line number to try
	 * @return true if the given line nr. is in this BB
	 */
	public boolean containsLineNr(int nr) {
		if (this.getCodeLines().getLast().getLineNr() >= nr
				&& this.getCodeLines().getFirst().getLineNr() <= nr)
			return true;
		else
			return false;
	}

	/**
	 * 
	 * @return true if this BB is a switchTable
	 */
	public boolean isSwitchTable() {
		return isSwitchTable;
	}

	/**
	 * 
	 * @return the codelines this BB consists of
	 */
	public LinkedList<CodeLineInterface> getCodeLines() {
		return codeLines;
	}

	/**
	 * @return the method this BB belongs to
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * 
	 * @param b
	 *            - indicate if this BB is a switch table or not
	 */
	public void setSwitchTable(boolean b) {
		isSwitchTable = b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((codeLines == null) ? 0 : codeLines.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BasicBlockInterface other = (BasicBlockInterface) obj;
		if (codeLines == null) {
			if (other.getCodeLines() != null)
				return false;
		} else if (!codeLines.equals(other.getCodeLines()))
			return false;
		return true;
	}

	/**
	 * 
	 * @param target
	 *            - the target CodeLine to search for
	 * @return true, if the target line is contained within this BB
	 */
	public boolean containsLine(String target) {
		for (CodeLineInterface cl : codeLines) {
			if (new String(cl.getLine()).equals(target))
				return true;
		}
		return false;
	}

	/**
	 * Get the unique label of this BB within a Method.
	 * 
	 * @return the label of this BB
	 */
	public int getLabel() {
		return label;
	}

	/**
	 * Set the unique label of this BB within a Method.
	 * 
	 * @param label
	 *            - the label of this BB
	 */
	public void setLabel(int label) {
		this.label = label;
	}

	public String getUniqueId() {
		StringBuilder sb = new StringBuilder();
		sb.append(getMethod().getSmaliClass().getUniqueId());
		sb.append(',');
		sb.append(getMethod().getLabel());
		sb.append(',');
		sb.append(label);
		return sb.toString();
	}
	
	public boolean hasReturn(){
		return hasReturn;
	}
	
	public boolean hasThrow(){
		return hasThrow;
	}
	
	public boolean hasGoto(){
		return hasGoto;
	}
	
	public void setHasReturn(boolean hasReturn){
		this.hasReturn = hasReturn;
	}
	
	public void setHasThrow(boolean hasThrow) {
		this.hasThrow = hasThrow;
	}

	public void setHasGoto(boolean hasGoto) {
		this.hasGoto = hasGoto;
	}
	
	public void setHasDeadCode(boolean hasDeadCode){
		this.hasDeadCode = hasDeadCode;
	}
	
	/**
	 * Checks whether the BB contains dead code. This most likely results when someone
	 * patches a program in order to return something and skip real code. BBs are build
	 * after jmp/goto label/targets and this should not occur in "normal" apps.
	 * 
	 * @return true if the BB contains a return opcode which is not the last opcode.
	 */
	public boolean hasDeadCode() {
		return hasDeadCode; 
	}
	
	@Override
	public boolean isTryBlock(){
		return isTryBlock;
	}
	
	@Override
	public boolean isCatchBlock() {
		return isCatchBlock;
	}
	

	@Override
	public void setIsTryBlock(boolean isTryBlock) {
		this.isTryBlock = isTryBlock;		
	}

	@Override
	public void setIsCatchBlock(boolean isCatchBlock) {
		this.isCatchBlock = isCatchBlock;
	}

	/**
	 * Get the previous CodeLine which contains actual code (is not empty, a comment etc).
	 * @param bb the BB in which to search
	 * @param index the current index, not previous CodeLine index
	 * @return the corresponding CodeLine or a SyntaxException
	 * @throws SyntaxException if no real instruction can be found in the given BB
	 */
	public static BasicBlock.FoundCodeLine getPreviousCodeLine(BasicBlockInterface bb, int index) throws SyntaxException {
		while (index >= 1) {
			index--;
			CodeLineInterface cl = bb.getCodeLines().get(index);
			if (!cl.isCode()) continue;
			else {
				return new BasicBlock.FoundCodeLine(cl, bb, index);
			}
		}
		throw new SyntaxException("Could not find previous \"real\" instruction in given BB!"); 
	}

	/**
	 * Returns the last codeline which contains a real opcode (not comment, dot prefix etc) from the BB. 
	 * @param bb the BasicBlock to search
	 * @return the found line
	 * @throws SyntaxException if now line was found
	 */
	public static BasicBlock.FoundCodeLine getLastCodeLine(BasicBlockInterface bb) throws SyntaxException {
		return BasicBlock.getPreviousCodeLine(bb, bb.getCodeLines().size());
	}


	public String toString(){
		StringBuilder bb = new StringBuilder();
		for (CodeLineInterface cl: this.getCodeLines()){
			bb.append(cl.getNrAndLine());
			bb.append("\n");
		}
		return bb.toString();
	}
	
}
