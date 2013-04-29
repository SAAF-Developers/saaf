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
package de.rub.syssec.saaf.db.persistence.exceptions;

/**
 * Indicates that an entity was not satisfiy the rules enforced by EnttiyManager.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class InvalidEntityException extends Exception {

	private static final long serialVersionUID = -5067381869159457728L;


	/**
	 * @param message
	 */
	public InvalidEntityException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidEntityException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidEntityException(String message, Throwable cause) {
		super(message, cause);
	}

}
