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
package de.rub.syssec.saaf.model.analysis;

import de.rub.syssec.saaf.model.Entity;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;

public interface HResultInterface extends Entity {

	/**
	 * @return the ana
	 */
	public abstract AnalysisInterface getAnalysis();

	public abstract void setAnalysis(AnalysisInterface analysis);

	/**
	 * @return the pat
	 */
	public abstract HPatternInterface getPattern();

	public abstract void setPattern(HPatternInterface pattern);

	/**
	 * @return the file
	 */
	public abstract ClassInterface getFile();

	public abstract void setFile(ClassInterface file);

	/**
	 * @return the cl
	 */
	public abstract CodeLineInterface getCodeline();

	public abstract void setCodeline(CodeLineInterface codeline);

}