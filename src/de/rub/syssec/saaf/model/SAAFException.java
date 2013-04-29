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
package de.rub.syssec.saaf.model;

import de.rub.syssec.saaf.model.analysis.AnalysisInterface;

/**
 * Extends a Java Exception so we can store it along with the analysis.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class SAAFException extends Exception implements Entity {

	/**
	 * 
	 */
	public SAAFException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public SAAFException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public SAAFException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = -5968093893079356433L;
	private AnalysisInterface analysis;
	private boolean changed=true;
	private int id;
	
	
	/**
	 * @param message
	 * @param cause
	 */
	public SAAFException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
	
	public SAAFException(String message, Throwable e, AnalysisInterface analysis2) {
		this(message,e);
		this.analysis=analysis2;
	}

	public AnalysisInterface getAnalysis()
	{
		return this.analysis;
	}
	
	public void setAnalysis(AnalysisInterface analysis)
	{
		this.analysis=analysis;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
		
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
