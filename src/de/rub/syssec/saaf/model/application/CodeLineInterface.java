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

import de.rub.syssec.saaf.application.manifest.permissions.Permission;
import de.rub.syssec.saaf.model.application.instruction.InstructionInterface;

public interface CodeLineInterface {

	public abstract byte[] getLine();

	public abstract int getLineNr();

	public abstract String getNrAndLine();

	/**
	 * Is this line empty?
	 * @return true if this line does not contain at least a single byte
	 */
	public abstract boolean isEmpty();

	/**
	 * Denotes whether a codeline contains a "real operation". That means it does not begin with a '.', '#', ':' or is empty at all. If the
	 * instruction is not yet parsed and does not not start w/ any sign above, false will also be returned.
	 * 
	 * @return true if the codeline contains is a "real" opcode
	 */
	public abstract boolean isCode();

	public abstract boolean startsWith(byte[] pattern);

	@Deprecated
	public abstract boolean startsWith(String pattern);

	public abstract boolean contains(byte[] pattern);

	@Deprecated
	public abstract boolean contains(String pattern);

	/**
	 * Get the parsed instruction if it is parsed. 
	 * @return the instruction
	 */
	public abstract InstructionInterface getInstruction();

	/**
	 * Get the SmaliClass where this line originates from.
	 * @return the SmaliClass
	 */
	public abstract ClassInterface getSmaliClass();

	/**
	 * A reference to the method where this cl comes from.
	 * Null if not set b/c cl does not belong to a method.
	 * @return the method or null
	 */
	public abstract MethodInterface getMethod();

	/**
	 * Set the method where this cl comes from. Should be set
	 * when the SmaliClass is parsed.
	 * @param method
	 */
	public abstract void setMethod(MethodInterface method);
	
	public abstract void setPermission(Permission perm);
	
	public abstract Permission getPermission();

}