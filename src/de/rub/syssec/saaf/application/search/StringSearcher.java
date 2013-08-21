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
package de.rub.syssec.saaf.application.search;

import java.util.Vector;

import de.rub.syssec.saaf.misc.ByteUtils;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;

/**
 * Searches for all Strings in all smali files.
 */
public class StringSearcher {
	
	/**
	 * Searches for all Strings which are enclosed in double quotes
	 * inside all codelines in all SMALI files.
	 * 
	 * @param app the application
	 * @return a Vector containing all found strings and corresponding codelines 
	 */
	public static Vector<FoundString> searchString(ApplicationInterface app) {
		
		Vector<FoundString> foundStringsVec = new Vector<FoundString>();
		
		for (ClassInterface smaliClass : app.getAllSmaliClasss(false)) {
			//ClassInterface smaliClass = app.getSmaliClass(f);
			for (CodeLineInterface cl : smaliClass.getAllCodeLines()) {
				if (cl.isEmpty()) continue;
				byte[] codeline = cl.getLine();
				int indexOfOpeningQuote = -1;
				for (int i=0; i<codeline.length; i++) {
					if (codeline[i] == '"' && (i-1 < 0 || codeline[i-1] != '\\')) {
						/* array index out of bounds should not occur on i-1, as the line should never begin with a '"',
						 * but the file may be corrupted
						 */
						if (indexOfOpeningQuote < 0) indexOfOpeningQuote = i;
						else {
							// found a closing one
							String s = new String(ByteUtils.subbytes(codeline, indexOfOpeningQuote, i+1));
							FoundString fs = new FoundString(cl, s);
							foundStringsVec.add(fs);
							indexOfOpeningQuote = -1; // reset, there might be more strings in one line
						}
					}
				}
			}
		}
		
		return foundStringsVec;
	}
	
	
	/**
	 * A small helper class to store CodeLines and corresponding Strings.
	 */
	public static class FoundString {
		private final CodeLineInterface cl;
		private final String string;
		
		public FoundString(CodeLineInterface cl, String string) {
			this.cl = cl;
			this.string = string;
		}
		
		public CodeLineInterface getCodeLine() {
			return cl;
		}
		
		public String getFoundString() {
			return string;
		}
	}
}
