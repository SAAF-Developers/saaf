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
package de.rub.syssec.saaf.analysis.steps.reporting;


/**
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class ReportingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8651846810670726517L;

	public ReportingException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ReportingException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public ReportingException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public ReportingException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	
}
