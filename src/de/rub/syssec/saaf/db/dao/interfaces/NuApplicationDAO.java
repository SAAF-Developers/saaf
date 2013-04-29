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
package de.rub.syssec.saaf.db.dao.interfaces;

import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.model.application.ApplicationInterface;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public interface NuApplicationDAO extends GenericDAO<ApplicationInterface> {

	/**
	 * Return the id of the application with the given hash.
	 * 
	 * @param hash
	 * @return the id of the application, 0 if not found
	 * @throws DAOException 
	 */
	public abstract int findByMD5Hash(String hash) throws DAOException;

	/**
	 * Return the name of the application with the given id.
	 * @param id
	 * @return DAOException
	 */
	public abstract String findNameById(int id) throws DAOException;
}
