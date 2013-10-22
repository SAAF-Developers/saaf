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
package de.rub.syssec.saaf.application;

import java.util.Arrays;

import de.rub.syssec.saaf.application.instructions.Instruction;
import de.rub.syssec.saaf.application.manifest.permissions.Permission;
import de.rub.syssec.saaf.misc.ByteUtils;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.InstructionInterface;
import de.rub.syssec.saaf.model.application.InstructionType;
import de.rub.syssec.saaf.model.application.MethodInterface;

/**
 * This class represents one line of code. The codeline will be stripped of all leading
 * and trailing whitespace and unprintable bytes in the range of 0 to 32.
 */
public class CodeLine implements CodeLineInterface {
	
	
	private final byte[] line;
	private final int lineNr;
	private final InstructionInterface instruction;	
	private final ClassInterface sf;
	private Permission permission = null;
	/**
	 * A reference to the method where this cl comes from, may be null!
	 */
	private MethodInterface method = null; 
	
	/**
	 * This class represent one line from a SmaliClass.
	 * @param line the bytes from the line from the original SmaliClass
	 * @param lineNr the line number in the file
	 * @param sf a reference to the SmaliClass
	 */
	protected CodeLine(byte[] line, int lineNr, ClassInterface sf) {
		this.line = trim(line);
		this.lineNr = lineNr;
		this.sf = sf;
		instruction = new Instruction(this);
	}
	
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.CodeLineInterface#getLine()
	 */
	@Override
	public byte[] getLine() {
		return line;
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.CodeLineInterface#getLineNr()
	 */
	@Override
	public int getLineNr() {
		return lineNr;
	}
	
	StringBuilder clSb;
	@Override
	public String toString() {
		if (clSb == null) { // init
			clSb = new StringBuilder();
			clSb.append(lineNr);
			clSb.append(":   ");
			clSb.append(new String(line));
		}
		return clSb.toString();
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.CodeLineInterface#getNrAndLine()
	 */
	@Override
	public String getNrAndLine(){
		return lineNr+" "+new String(line);
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.CodeLineInterface#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		if (line.length == 0) return true;
		else return false;
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.CodeLineInterface#isCode()
	 */
	@Override
	public boolean isCode() {
		if (		instruction.getType() == InstructionType.NOT_YET_PARSED || instruction.getType() == InstructionType.SMALI_DOT_COMMENT
				||	instruction.getType() == InstructionType.EMPTY_LINE || instruction.getType() == InstructionType.LABEL
				||	instruction.getType() == InstructionType.SMALI_HASH_KEY_COMMENT
				||	instruction.getType() == InstructionType.UNKNOWN
				||	instruction.getType() == InstructionType.NOP) return false;
		else return true;
	}
	
	
	/**
	 * Deletes all whitespace and non printable bytes (bytes <= 32) from the beginning and the end of the byte array
	 * @param line
	 * @return
	 */
	private static byte[] trim(byte[] line) {
		int begin = 0;
		while (begin < line.length && line[begin] <= 32) { begin++;	}
		
		int end = line.length-1;
		while (end >= 0 && line[end] <= 32) { end--; }
		
		if (end < begin) return new byte[0];
		else return Arrays.copyOfRange(line, begin, end+1);
	}
	
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.CodeLineInterface#startsWith(byte[])
	 */
	@Override
	public boolean startsWith(byte[] pattern) {
		return ByteUtils.startsWith(line, pattern);
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.CodeLineInterface#startsWith(java.lang.String)
	 */
	@Override
	@Deprecated
	public boolean startsWith(String pattern) {
		return ByteUtils.startsWith(line, pattern.getBytes());
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.CodeLineInterface#contains(byte[])
	 */
	@Override
	public boolean contains(byte[] pattern) {
		return ByteUtils.contains(line, pattern);
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.CodeLineInterface#contains(java.lang.String)
	 */
	@Override
	@Deprecated
	public boolean contains(String pattern) {
		return ByteUtils.contains(line, pattern.getBytes());
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.CodeLineInterface#getInstruction()
	 */
	@Override
	public InstructionInterface getInstruction() {
		return instruction;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(line);
		result = prime * result + lineNr;
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
		CodeLineInterface other = (CodeLineInterface) obj;
		if (!Arrays.equals(line, other.getLine()))
			return false;
		if (lineNr != other.getLineNr())
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.CodeLineInterface#getSmaliClass()
	 */
	@Override
	public ClassInterface getSmaliClass() {
		return sf;
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.CodeLineInterface#getMethod()
	 */
	@Override
	public MethodInterface getMethod() {
		return method;
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.CodeLineInterface#setMethod(de.rub.syssec.saaf.application.Method)
	 */
	@Override
	public void setMethod(MethodInterface method) {
		this.method = method;
	}
	
	public void setPermission(Permission perm){
		this.permission = perm;
	}
	
	public Permission getPermission(){
		return permission;
	}
}
