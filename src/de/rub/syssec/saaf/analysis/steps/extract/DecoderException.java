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
package de.rub.syssec.saaf.analysis.steps.extract;

/**
 * Exception to wrap problems in the process of decoding the apk.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class DecoderException extends Exception {

	private static final long serialVersionUID = -5369770179809077223L;

	/**
	 * @param message
	 */
	public DecoderException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public DecoderException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DecoderException(String message, Throwable cause) {
		super(message, cause);
	}
}
