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
package de.rub.syssec.saaf.analysis.steps.heuristic;

import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.analysis.HPatternInterface;
import de.rub.syssec.saaf.model.analysis.HResultInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;


/**
 * This object contains all information for one found heuristic pattern 
 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
 */
public class HResult implements HResultInterface {
	private AnalysisInterface analysis;
	private HPatternInterface pattern;
	private ClassInterface file;
	private CodeLineInterface cl;
	private String methodName;
	private int id = -1;		//ID from the table in db
	private boolean changed;

	public HResult(AnalysisInterface analysis, HPatternInterface pattern, CodeLineInterface cl) {
		this.analysis = analysis;
		this.pattern = pattern;
		this.cl = cl;
		file = cl.getSmaliClass();
		if (cl.getMethod() == null) {
			methodName = null;
		}
		else {
			methodName = cl.getMethod().getName();
		}	
		changed = true;
	}

	/**
	 * Constructor only for pattern which do not contain a codeline.
	 * The result will not contain a codeline, file and methodname.
	 * 
	 * @param analysis
	 * @param pattern
	 */
	public HResult(AnalysisInterface analysis, HPatternInterface pattern) {
		this.analysis = analysis;
		this.pattern = pattern;
		cl = null;
		file = null;
		methodName = null;
		changed = true;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public AnalysisInterface getAnalysis() {
		return analysis;
	}

	@Override
	public HPatternInterface getPattern() {
		return pattern;
	}

	@Override
	public ClassInterface getFile() {
		return file;
	}

	@Override
	public CodeLineInterface getCodeline() {
		return cl;
	}

	@Override
	public String toString() {
		String result = "HResult [" +
				"id=" + id + ", " +
				"analysis=" + analysis.getId() + ", " +
				"patternID=" + pattern.getId() + ", " +
				"patternHvalue=" + pattern.getHvalue() + ", ";
		if (cl != null) result +=
				"class=" + file.getFullClassName(true) + ", " +
				"method=" + methodName + ", \n" +
				"cl=" + cl.getNrAndLine();
		return result
				+ "]";
	}

	@Override
	public void setAnalysis(AnalysisInterface analysis) {
		this.analysis=analysis;
		setChanged(true);
	}

	@Override
	public void setPattern(HPatternInterface pattern) {
		this.pattern=pattern;
		setChanged(true);
	}

	@Override
	public void setFile(ClassInterface file) {
		this.file=file;
		setChanged(true);
	}

	@Override
	public void setCodeline(CodeLineInterface codeline) {
		this.cl=codeline;
		setChanged(true);
	}

	@Override
	public void setChanged(boolean changed) {
		this.changed=changed;
	}

	@Override
	public boolean isChanged() {
		return this.changed;
	}
}
