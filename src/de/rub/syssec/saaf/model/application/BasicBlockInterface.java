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
package de.rub.syssec.saaf.model.application;

import java.util.LinkedList;

public interface BasicBlockInterface {

	/**
	 * Return previous BBs.
	 */
	public abstract LinkedList<BasicBlockInterface> getPreviousBB();

	public abstract LinkedList<BasicBlockInterface> getNextBB();

	public abstract void addPreviousBB(BasicBlockInterface bb);

	public abstract void addNextBB(BasicBlockInterface bb);

	public abstract boolean containsLineNr(int nr);

	public abstract boolean isSwitchTable();

	public abstract LinkedList<CodeLineInterface> getCodeLines();

	/**
	 * @return the method this BB belongs to
	 */
	public abstract MethodInterface getMethod();

	public abstract void setSwitchTable(boolean b);

	public abstract boolean containsLine(String target);

	public abstract int getLabel();

	/**
	 * Get the unique label of this BB within a Method.
	 * @return
	 */
	public abstract void setLabel(int label);

	public abstract String getUniqueId();
	
	
	
	public abstract boolean hasReturn();
	
	public abstract boolean hasThrow();
	
	public abstract boolean hasGoto();
	
	public abstract void setHasReturn(boolean hasReturn);
	
	public abstract void setHasThrow(boolean hasThrow);
	
	public abstract void setHasGoto(boolean hasGoto);
	
	public abstract void setHasDeadCode(boolean hasDeadCode);
	
	/**
	 * Checks whether the BB contains dead code. This most likely results when someone
	 * patches a program in order to return something and skip real code. BBs are build
	 * after jmp/goto label/targets and this should not occur in "normal" apps.
	 * 
	 * @return true if the BB contains a return opcode which is not the last opcode.
	 */
	public abstract boolean hasDeadCode();
	
	public abstract void setIsTryBlock(boolean isTryBlock);
	
	public abstract void setIsCatchBlock(boolean isCatchBlock);
	
	public abstract boolean isTryBlock();
	
	public abstract boolean isCatchBlock();

}