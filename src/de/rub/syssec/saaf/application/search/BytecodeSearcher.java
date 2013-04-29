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

import java.io.File;
import java.util.LinkedList;
import java.util.Vector;

import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.ClassInterface.SearchType;


/**
 * A class to search for patterns in smali bytecode.
 *
 */
public class BytecodeSearcher {

	/**
	 * Search for a given pattern in all SMALI files in one application
	 *  
	 * @param app the application
	 * @param pattern the pattern to search for
	 * @return all codelines matching the pattern
	 */
	public static Vector<CodeLineInterface> searchPattern(ApplicationInterface app, String pattern) {

	Vector<CodeLineInterface> resultVec = new Vector<CodeLineInterface>();
	
		final byte[] p = pattern.getBytes();
		for (File f : app.getAllRawSmaliFiles(true)) {
			ClassInterface smaliClass = app.getSmaliClass(f);
			LinkedList<CodeLineInterface> matchedLines = smaliClass.searchPattern(p, SearchType.INSTRUCTIONS_AND_NON_INSTRUCTIONS);
			resultVec.addAll(matchedLines);
		}

		return resultVec;
	}
}
