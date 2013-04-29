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

import java.util.EnumSet;


public interface FieldInterface {

	/**
	 * All possible modifiers.
	 */
	public enum Modifier {
	
	    PUBLIC("public".getBytes()),
	    PROTECTED("protected".getBytes()),
	    PRIVATE("private".getBytes()),
	    STATIC("static".getBytes()),
	    ABSTRACT("abstract".getBytes()),
	    SYNCHRONIZED("synchronized".getBytes()),
	    TRANSIENT("transient".getBytes()),
	    VOLATILE("volatile".getBytes()),
	    FINAL("final".getBytes()),
	    NATIVE("native".getBytes());
	
	    private byte[] text;
	
	    private Modifier(byte[] text) {
	    	this.text = text;
	    }
	    
	    public byte[] getBytePresentation() {
	    	return text;
	    }
	
	    @Override
	    public String toString() {
	    	return new String(text);
	    }
	}

	public static final byte[] FIELD = ".field ".getBytes();

	public abstract boolean isArray();

	public abstract int getArrayDimension();

	public abstract boolean hasModifier(Modifier modifier);

	public abstract boolean isStatic();

	public abstract boolean isFinal();

	public abstract EnumSet<Modifier> getAllModifiers();

	public abstract String getFieldName();

	//	
	//	public Type getType() {
	//		return constant.getType();
	//	}
	//	
	//	public String getTypeDescription() {
	//		return constant.getTypeDescription();
	//	}
	//	
	//	/**
	//	 * Get the parsed value from this field if any is defined.
	//	 * The value is parsed/converted to a String in decimal representation for short, int, long, double and float.
	//	 * If the conversation fails, the original value is returned.
	//	 * @return
	//	 */
	//	public String getValue() {
	//		return constant.getValue();
	//	}

	public abstract CodeLineInterface getCodeLine();

}