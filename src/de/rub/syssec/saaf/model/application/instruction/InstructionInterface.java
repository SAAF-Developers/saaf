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
package de.rub.syssec.saaf.model.application.instruction;

import java.util.LinkedList;

import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.SyntaxException;


public interface InstructionInterface {

	/**
	 * Attempts to parse the opcode from the line of the SMALI file. This should
	 * only be called from SmaliClass.java!
	 * 
	 * FIXME: should be protected
	 */
	public abstract void parseOpCode();

	/**
	 * The type. UNKNOWN if the line could not be parsed and NOT_YET_PARSED if
	 * it has not been parsed until now.
	 * 
	 * @return
	 */
	public abstract InstructionType getType();

	/**
	 * The opcode, which is the first string in each line.
	 * 
	 * @return
	 */
	public abstract byte[] getOpCode();

	/**
	 * Get the register where the result is copied into
	 * 
	 * @return
	 */
	public abstract byte[] getResultRegister();

	/**
	 * Get the field where the result is copied to. The first index is the class
	 * name, the second the field name.
	 * 
	 * @return
	 */
	public abstract byte[][] getResultField();

	/**
	 * Get all registers which are involved in a method call or all relevant
	 * registers for eg, MATH opcodes, PUTs, RETURN-x etc.
	 * 
	 * @return
	 */
	public abstract LinkedList<byte[]> getInvolvedRegisters();

	/**
	 * Get all fields which are involved in a method call or a GET opcode etc.
	 * 
	 * @return
	 */
	public abstract LinkedList<byte[]> getInvolvedFields();

	/**
	 * Get the class, the method and its parameters for a call/invoke.
	 * 
	 * @return cm[0] is the class, cm[1] the method and cm[3] the unparsed
	 *         parameters.
	 */
	public abstract byte[][] getCalledClassAndMethodWithParameter();

	/**
	 * TODO: Use getCalledClassAndMethodWithParameter instead! Get the class,
	 * the method and its parameters for a call/invoke.
	 * 
	 * @return cm[0] is the class, cm[1] the method and cm[3] the unparsed
	 *         parameters.
	 * @deprecated
	 */
	public abstract byte[][] getCalledClassAndMethod();

	/**
	 * Debug only. Dump all parsed data to stdout.
	 */
	public abstract void dump();

	public abstract CodeLineInterface getCodeLine();

	/**
	 * Some opcodes contain a label, eg, fill-array-data.
	 * 
	 * @return
	 */
	public abstract byte[] getLabel();

	public abstract boolean hasConstant();

	/**
	 * Get the constant which is assigned to a register, is used in a
	 * mathematical operation or all constant from an initialized array. May be
	 * null if the opcode does not use a constant value.
	 * 
	 * @return the parsed constant value, depends on the codeline
	 * @throws SyntaxException
	 *             if the constant can not be parsed
	 */
	public abstract String getConstantValue() throws SyntaxException;

}