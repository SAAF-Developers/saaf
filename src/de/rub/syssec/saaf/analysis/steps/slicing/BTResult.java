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

import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.analysis.BTPatternInterface;
import de.rub.syssec.saaf.model.analysis.BTResultInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.ConstantInterface;

/**
 * This object contains all information for one found BackTrack pattern
 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
 */
public class BTResult implements BTResultInterface {
	private AnalysisInterface analysis;
	private BTPatternInterface pattern;
	private ClassInterface file;
	private ConstantInterface constant;
	private int argument;		//the number of the argument of the function which was backTracked
	private CodeLineInterface cl;
	private String methodName;
	private int id = -1;		//ID from the table in db
	private boolean changed;

	public BTResult(AnalysisInterface analysis, BTPatternInterface pattern, ConstantInterface constant, int argument) {
		this.analysis = analysis;
		this.pattern = pattern;
		this.constant = constant;
		this.argument = argument;
		this.cl = constant.getCodeLine();
		this.file = cl.getSmaliClass();
		if (cl.getMethod() == null)
			this.methodName = null;
		else
			this.methodName = cl.getMethod().getName();
		this.changed=true;
	}
	//################# getter and setter ##################################

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.analysis.BTResultInterface#getAnalysis()
	 */
	@Override
	public AnalysisInterface getAnalysis() {
		return analysis;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.analysis.BTResultInterface#getPattern()
	 */
	@Override
	public BTPatternInterface getPattern() {
		return pattern;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.analysis.BTResultInterface#getFile()
	 */
	@Override
	public ClassInterface getFile() {
		return file;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.analysis.BTResultInterface#getCodeline()
	 */
	@Override
	public CodeLineInterface getCodeline() {
		return cl;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.analysis.BTResultInterface#getConstant()
	 */
	@Override
	public ConstantInterface getConstant() {
		return constant;
	}
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.analysis.BTResultInterface#getArgument()
	 */
	@Override
	public int getArgument() {
		return argument;
	}
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.analysis.BTResultInterface#getCl()
	 */
	@Override
	public CodeLineInterface getCl() {
		return cl;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = "BTResult [" +
				"id=" + id + ", " +
				"analysis=" + analysis.getId() + ", " +
				"patternID=" + pattern.getId() + ", " +
				"searchID=" + constant.getSearchId() + ", " +
				"fuzzyLevel=" + constant.getFuzzyLevel() + ", " +
				"patternMethod=" + pattern.getMethodName() + ", " +
				"result=" + constant.getValue() + ", ";
		if (cl != null) result +=
				"file=" + file.getClassName() + ", " + //"file=" + file.getClass().getName() + ", " +
				"method=" + methodName + ", \n" +
				"cl=" + cl.getNrAndLine();
		return result
				+ "]";
	}

	@Override
	public void setAnalysis(AnalysisInterface analysis) {
		this.analysis = analysis;
		setChanged(true);
	}

	@Override
	public void setPattern(BTPatternInterface pattern) {
		this.pattern = pattern;
		setChanged(true);
	}

	@Override
	public void setFile(ClassInterface file) {
		this.file = file;
		setChanged(true);
	}

	@Override
	public void setCodeline(CodeLineInterface codeline) {
		this.cl=codeline;
		setChanged(true);
	}

	@Override
	public void setMethodName(String name) {
		this.methodName=name;
		setChanged(true);
	}

	@Override
	public void setConstant(ConstantInterface constant) {
		this.constant=constant;
		setChanged(true);
	}

	@Override
	public void setArgument(int argument) {
		this.argument=argument;
		setChanged(true);
	}

	@Override
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	@Override
	public boolean isChanged() {
		return this.changed;
	}

}
