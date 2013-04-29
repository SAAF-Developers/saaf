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
package de.rub.syssec.saaf.db.dao.exceptions;

/**
 * An Exception that indicates that an Entity does not exist.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class NoSuchEntityException extends Exception {

	private static final long serialVersionUID = -4891538002518492559L;

	/**
	 * 
	 */
	public NoSuchEntityException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NoSuchEntityException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public NoSuchEntityException(Throwable cause) {
		super(cause);
	}

}
