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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.db.dao.exceptions.DuplicateEntityException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;

/**
 * A generic interface that provides methods for CRUD.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public interface GenericDAO<T> {
	
	public static final String SQL_ERROR_DUPLICATE = "23000";

	/**
	 * Creates a new entity for the first time
	 * 
	 * @param entity
	 * @return the id of the entity on successfull creation
	 * @throws DAOException when trying to create an object twice
	 *  or general problem in the underlying persistence mechanism
	 */
	public abstract int create(T entity) throws DuplicateEntityException, DAOException;
	/**
	 * Reads an existing entity from the underlying datastore.
	 * 
	 * @param id the id of the existing entity (returned by create())
	 * @return the entity, null no object by that id exists
	 * @throws DAOException on general problems in the underlying persistence mechanism
	 */
	public abstract T read(int id) throws DAOException;
	
	/**
	 * Read all instances of entity.
	 * 
	 * @return a list of all entities in the  database or empty list.
	 * @throws DAOException
	 */
	public abstract List<T> readAll() throws DAOException;

	/**
	 * Updates an existing entity.
	 * 
	 * @param entity the entity to update
	 * @return true if update was successful, false otherwise
	 * @throws NoSuchEntityException if the entity we werer trying to delete does not exist
	 * @throws DAOException on general problems in the underlying persistence mechanism
	 */
	public abstract boolean update(T entity) throws NoSuchEntityException, DAOException;
	
	/**
	 * Deletes an existing entity.
	 * 
	 * @param entity the entity to delete
	 * @return true if update was successful, false otherwise 
	 * @throws NoSuchEntityException if the entity we werer trying to delete does not exist
	 * @throws DAOException on general problems in the underlying persistence mechanism
	 */
	public abstract boolean delete(T entity) throws NoSuchEntityException, DAOException;
	
	/**
	 * Delete all entities of this type.
	 * 
	 * @return the number of affected records
	 * @throws DAOException on general problems in the underlying persistence mechanism
	 */
	public abstract int deleteAll() throws DAOException;
	
	/**
	 * Searches for an entity with the same and returns the id.
	 * 
	 * This is useful in occasions where an entity has been instantiated
	 * and not yet saved but a record with the same attributes already
	 * exists in the datastore preventing you from creating another one.
	 * 
	 * @return the id of the first entity with the same attributes
	 * @throws DAOException 
	 */
	public abstract int findId(T candidate) throws DAOException;
}
