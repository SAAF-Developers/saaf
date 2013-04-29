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

import de.rub.syssec.saaf.model.Entity;

public interface MethodInterface extends Entity {

	/**
	 * Does this method contain anything else besides an empty declaration?
	 * @return true if the method is "empty", false otherwise
	 */
	public abstract boolean isEmpty();
	
	public abstract void setEmpty(boolean isEmpty);

	//FIXME: crude hack to replace the autogeneration in parseNameAndType(), could perhaps still always be generated in parseNameAndType (for safety) and use this just for explicitly generating bbs anew
	public abstract void generateBBs() throws DetectionLogicError, SmaliClassError;

	/**
	 * Get the unparsed parameters of this method.
	 * .method public constructor <init>(Landroid/content/Context;)V
	 * would return Landroid/content/Context;
	 * @return the parameter declaration
	 */
	public abstract byte[] getParameters();
	
	/**
	 * Get the unparsed return value of this method. .method public constructor
	 * <init>(Landroid/content/Context;)V would return V.
	 * 
	 * @return the return value
	 */
	public byte[] getReturnValue();
	
	/**
	 * Get thre return value of this method as as string.
	 * 
	 * @return
	 */
	public String getReturnValueString();

	/**
	 * Get the first BB.
	 * @return the first BB or null if none is available
	 */
	public abstract BasicBlockInterface getFirstBasicBlock();

	public abstract LinkedList<BasicBlockInterface> getBasicBlocks();
	
	public abstract void setBasicBlocks(LinkedList<BasicBlockInterface> blocks);

	public abstract String getName();
	
	public abstract void setName(String name);

	public abstract LinkedList<FieldInterface> getLocalFields();

	public abstract void setLocalFields(LinkedList<FieldInterface> localFields);
	
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
	public abstract boolean contains(byte[] instruction);

	/**
	 * FIXME das ist langsam weil es aus byte[] nen string macht und dann viel
	 * replace
	 * 
	 * @return
	 */
	public abstract String getInstructionsForDot();

	/**
	 * Calculate the percentage of artithmetic operations in this function.
	 * 
	 * @return
	 */
	public abstract float arithOps();

	public abstract LinkedList<CodeLineInterface> getCodeLines();
	
	public abstract void setCodeLines(LinkedList<CodeLineInterface> lines);

	/**
	 * 
	 * @return the SMALI file this method belongs to
	 */
	public abstract ClassInterface getSmaliClass();
	
	public abstract void setSmaliClass(ClassInterface smaliClass);

	//################# getter and setter ##################################

	public abstract boolean isStatic();
	
	public abstract void isStatic(boolean isStatic);

	/**
	 * Return the class with full path, the method name and the (raw) parameters as byte arrays.
	 * @return [ classname, name, parameters ]
	 */
	public abstract byte[][] getCmp();

	/**
	 * 
	 * @return
	 */
	public abstract String getParameterString();
	
	public abstract void setParameterString(String params);

	/**
	 * Get the unique label of this Method within a SmaliClass.
	 * @return
	 */
	public abstract int getLabel();
	
	public abstract void setLabel(int label);

	public abstract String getUniqueLabel();
	
	public abstract void  setUniqueLabel(String ulabel);

	/**
	 * TODO and FIXME:
	 * convert the .method... line to a more java syntax, such as private void full-class-name.methodname(String s);
	 * Right now this method will only return the classpath and the .method name. It would be best to overwrite this
	 * method which will append the full-class-name and one method which does not do it (real java syntax)
	 * @return
	 */
	public abstract String getReadableJavaName();
	
	public abstract void setReadableJavaName(String javaName);

	public abstract void setHasUnlinkedBlocks(boolean b);
	
	/**
	 * Returns true if at least one BB is not linked for this method.
	 * @return
	 */
	public abstract boolean hasUnlinkedBBs();

	/**
	 * If the method contains unlinked Basic Blocks, this might
	 * indicate some nop'ed out instructions. If all BBs are
	 * reachable, this method checks if at least one BB contains
	 * unreachable code, eg, after a return statement. If neither
	 * is true, false is returned.
	 * 
	 * @return true if the method might be patched
	 */
	boolean isProbablyPatched();


}