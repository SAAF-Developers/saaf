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

import java.util.EnumSet;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.analysis.steps.obfuscation.Entropy;
import de.rub.syssec.saaf.misc.ByteUtils;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.FieldInterface;
import de.rub.syssec.saaf.model.application.SyntaxException;


/**
 * This class describes a field in SMALI code. It is able to parse its contents.
 * 
 * @author Johannes Hoffmann <johannes.hoffmann@rub.de>
 *
 */
public class Field implements FieldInterface {
	
	private final EnumSet<Modifier> modifierSet = EnumSet.noneOf(Modifier.class);
	
	private final CodeLineInterface cl;
	
	private int arrayDimension = 0;
	
	private String fieldName = null;

	private boolean obfuscated;

	private Entropy entropy;
	
	private static final Logger LOGGER = Logger.getLogger(Field.class);
	
	protected Field(CodeLineInterface cl) throws SyntaxException {
		// parse the line and add it to the set
		for (Modifier modifier : Modifier.values()) {
			if (cl.contains(modifier.getBytePresentation())) {
				// FIXME: public int privateBlah = 0; would yield public and private!
				modifierSet.add(modifier);
			}
		}
		this.cl = cl;
	}
	
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.FieldInterface#isArray()
	 */
	@Override
	public boolean isArray() {
		if (arrayDimension > 0) return true;
		else return false;
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.FieldInterface#getArrayDimension()
	 */
	@Override
	public int getArrayDimension() {
		return arrayDimension;
	}
	
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.FieldInterface#hasModifier(de.rub.syssec.saaf.application.Field.Modifier)
	 */
	@Override
	public boolean hasModifier(Modifier modifier) {
		return modifierSet.contains(modifier);
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.FieldInterface#isStatic()
	 */
	@Override
	public boolean isStatic() {
		return hasModifier(Modifier.STATIC);
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.FieldInterface#isFinal()
	 */
	@Override
	public boolean isFinal() {
		return hasModifier(Modifier.FINAL);
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.FieldInterface#getAllModifiers()
	 */
	@Override
	public EnumSet<Modifier> getAllModifiers() {
		return modifierSet;
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.FieldInterface#getFieldName()
	 */
	@Override
	public String getFieldName() {
		if (fieldName == null) {
			int colonIndex = ByteUtils.indexOf(cl.getLine(), ':');
			int spaceBeforeColonPos = ByteUtils.indexOfReverse(cl.getLine(), ' ', colonIndex);
			fieldName = new String(ByteUtils.subbytes(cl.getLine(), spaceBeforeColonPos+1, colonIndex));
		}
		return fieldName;
		
	}

	
	/**
	 * Parse all Fields from the given CodeLines.
	 * 
	 * TODO: Catch exception to not abort whole parsing?
	 * @param codeLines
	 * @return
	 */
	public static LinkedList<FieldInterface> parseAllFields(LinkedList<CodeLineInterface> codeLines) {
		LinkedList<FieldInterface> fieldList = new LinkedList<FieldInterface>();
		for (CodeLineInterface codeLine : codeLines) {
			if (codeLine.startsWith(FIELD)) {
				try {
					Field f = new Field(codeLine);
					fieldList.add(f);
				}
				catch (SyntaxException e) {
					LOGGER.error("Could not parse field ",e);
				}
				
			}
		}
		return fieldList;
	}
	
	/**
	 * Print the field and its description.
	 * TODO: Format to plain java syntax.
	 */
	public String toString() {
		return cl.toString();
	}
	
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.FieldInterface#getCodeLine()
	 */
	@Override
	public CodeLineInterface getCodeLine() {
		return cl;
	}


	@Override
	public void setObfuscated(boolean b) {
		this.obfuscated=b;
	}


	@Override
	public boolean isObfuscated() {
		return this.obfuscated;
	}


	@Override
	public void setEntropy(Entropy entropy) {
		this.entropy=entropy;
	}


	@Override
	public Entropy getEntropy() {
		return this.entropy;
	}
}
