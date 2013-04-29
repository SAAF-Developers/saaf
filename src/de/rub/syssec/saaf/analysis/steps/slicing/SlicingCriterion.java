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

import java.util.LinkedList;

import de.rub.syssec.saaf.model.application.ConstantInterface;

/**
 * This class describes the entry point for the program slicing algorithm.
 * It defines a method within in a class and a parameter which is then
 * backtracked in order to build def-use chains and find used constants
 * which are used as input for the defined parameter. 
 * 
 * @author Johannes Hoffmann <johannes.hoffmann@rub.de>
 *
 */
public class SlicingCriterion {
	
	private final byte[][] cmp = new byte[3][];
	private final int parameterIndex;
	private final String description;
	private final LinkedList<ConstantInterface> resultList = new LinkedList<ConstantInterface>();
	private final LinkedList<Throwable> exceptionList = new LinkedList<Throwable>();
	
	/**
	 * Search for a method of a specific class w/ specific parameters and track a given
	 * parameter of that method. You do not need to care about if it is a static call or
	 * not. Just set the full classpath, the method name, its parameters and the parameter
	 * index of parameter to track, starting at zero for the first parameter.
	 * @param classname the classname, you may specify '*' as a wildcard for all classes
	 * @param methodname the method name, <> method are also supported, eg, <init>
	 * @parameter parameter must be in Android-syntax, eg, Ljava/lang/String;
	 * @param parameterIndex the index of the parameter to track
	 */
	public SlicingCriterion(String classname, String methodname, byte[] parameter, int parameterIndex) {
		this(classname, methodname, parameter, parameterIndex, null);
	}	
	
	
	/**
	 * Search for a method of a specific class w/ specific parameters and track a given
	 * parameter of that method. You do not need to care about if it is a static call or
	 * not. Just set the full classpath, the method name, its parameters and the parameter
	 * index of parameter to track, starting at zero for the first parameter.
	 * @param classname the classname, you may specify '*' as a wildcard for all classes
	 * @param methodname the method name, <> method are also supported, eg, <init>
	 * @param parameter must be in Android-syntax, eg, Ljava/lang/String;
	 * @param parameterIndex the index of the parameter to track
	 * @param description, may be null
	 */
	public SlicingCriterion(String classname, String methodname, byte[] parameter, int parameterIndex, String description) {
		this.cmp[0] = classname.getBytes();
		this.cmp[1] = methodname.getBytes();
		this.cmp[2] = parameter;
		this.parameterIndex = parameterIndex;
		this.description = description;
	}
	
	
	public byte[][] getClassAndMethodAndParameter() {
		return cmp;
	}
	
	public int getParameterIndex() {
		return parameterIndex;
	}
	
	/**
	 * The description
	 * @return null if unset
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Add a new Constant. Duplicates are ignored.
	 * @param c
	 */
	protected void addFoundConstant(ConstantInterface c) {
		if (!resultList.contains(c)) {
			resultList.addLast(c);
		}
	}
	
	
	public LinkedList<ConstantInterface> getResults() {
		return resultList;
	}
	
	
	/**
	 * Log an exception which occurred during the backtrack analysis. 
	 * @param t the exception or error
	 */
	public void logException(Throwable t) {
		exceptionList.addLast(t);
	}
	
	/**
	 * Get all logged exception
	 * @return the (empty) list of exceptions
	 */
	public LinkedList<Throwable> getExceptionList() {
		return exceptionList;
	}
	
	/**
	 * See if an exception occurred during analysis
	 * @return true if any occurred, false otherwise
	 */
	public boolean isCleanAnalysis() {
		return exceptionList.isEmpty();
	}

}
