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

public interface ConstantInterface {

	/**
	 * FIELD_CONSTANT: a normal (class) field
	 * LOCAL_VARIABLE: some variable inside a method
	 * MATH_OPCODE_CONSTANT: some constant inside a math opcode
	 * ARRAY: if this is set, all values from an array are aggregated inside one constant
	 * LOCAL_UNREFERENCED_CONSTANT: some constant which are not assigned to any variable, eg, i = i+1.
	 * EXTERNAL_METHOD: An unknown/external (api) method was invoked and the result was written to the backtracked register, methods will only be threated this way if the search is not fuzzy
	 * INTERNAL_BYTECODE_OP: Something unusual, if, eg, some exception got moved to our backtracked register!
	 * UNCALLED_METHOD: A method which is never directly invoked but has parameters linked to a tracked register, eg, android.content.BroadcastReceiver.receive(..). Might also be dead code.
	 */
	public enum VariableType {
		FIELD_CONSTANT, // a normal (class) field
		LOCAL_VARIABLE, // some variable inside a method
		MATH_OPCODE_CONSTANT, // some constant inside a math opcode
		ARRAY, // if this is set, all values from an array are aggregated inside one constant 
		LOCAL_ANONYMOUS_CONSTANT, // some constant which are not assigned to any variable, eg, i = i+1. This may not always be correct.
		EXTERNAL_METHOD, // an unknown/external (api) method was invoked and the result was written to the backtracked register, methods will only be threated this way if the search is not fuzzy
		INTERNAL_BYTECODE_OP, // something unusual, if, eg, some exception got moved to our backtracked register!
		UNCALLED_METHOD; // a method which is never directly invoked but has parameters linked to a tracked register, eg, android.content.BroadcastReceiver.receive(..). Might also be dead code.
	}

	/**
	 * The different primitive types in Java and some "special" types:
	 * "String", "Math_OP", "Other_Class" (for classes) and "Unknown".
	 */
	public enum Type {
		//Never change the order of the different types, because it is also used in DB. Only add new types to the end.
		BOOLEAN("boolean"),
	    BYTE("byte"),
	    SHORT("short"),
	    CHAR("char"),
	    INTEGER("int"),
	    LONG("long"),
	    FLOAT("float"),
	    DOUBLE("double"),
	    STRING("String"),
	    MATH_OP("Math-Operator"), // // we do not know the type of the value, at least we do not parse it back!
	    UNKNOWN("Unknown"), // if it can somehow not be parsed
	    OTHER_CLASS("Other-Class"), // if the type is some non-primitive type, as eg, com/example/Blah
	    ARRAY("Array"); // an array of any type
		
		
	    private final String text;
	
	    private Type(String text) {
	    	this.text = text;
	    }
	
	    @Override
	    public String toString() {
	    	return text;
	    }
	}

	/**
	 * The type of the constant as a String, class names will be resolved.
	 * See {@linkplain Type} for more info.
	 * @return
	 */
	public abstract String getTypeDescription();

	public abstract int getArrayDimension();

	/**
	 * The type of the constant. Non-primitive types are not resolved.
	 * See {@linkplain Type} for more info.
	 * @return
	 */
	public abstract Type getType();

	/**
	 * The name of the variable, may be null.
	 * @return
	 */
	public abstract String getIdentifier();

	/**
	 * Return the parsed value. May be null if no value was assignet.
	 * @return
	 */
	public abstract String getValue();

	/**
	 * @return Codeline Object where the constant come from
	 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
	 */
	public abstract CodeLineInterface getCodeLine();

	/**
	 * Get the level of fuzziness.
	 * @return 0 if the constant was found during a non-fuzzy search. Values higher than 0 indicate more fuzziness the higher they are.
	 */
	public abstract int getFuzzyLevel();

	/**
	 * Determines whether the found constant was found during
	 * a fuzzy (inaccurate) search
	 * @return true if the search was funny (fuzzyLevel > 0)
	 */
	public abstract boolean wasFuzzySearch();

	/**
	 * Get the path in which the this constant was found.
	 * @return the path, the last entry contains the found constant
	 */
	public abstract String getPath();

	/**
	 * Check whether this constant was found inside an ad framework package path.
	 * @return true if it is
	 */
	public abstract boolean isInAdFrameworkPackage();

	/**
	 * Get the searchId which all Constants should have in common which were found during one run of the
	 * DetectionLogic for one tracked invoke.
	 * @return the searchId
	 */
	public abstract int getSearchId();
	
	/**
     * Get the type of the found variable or constant.
     * @return the type
     */
    public VariableType getVariableType();

}