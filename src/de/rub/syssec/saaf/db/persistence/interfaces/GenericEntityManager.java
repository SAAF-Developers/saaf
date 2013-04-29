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
package de.rub.syssec.saaf.db.persistence.interfaces;

import java.util.List;

import de.rub.syssec.saaf.db.persistence.exceptions.InvalidEntityException;
import de.rub.syssec.saaf.db.persistence.exceptions.PersistenceException;
import de.rub.syssec.saaf.model.Entity;

/**
 * The GenericEntityManager connects the business logic an the datbase layer.
 * 
 * It provides simplified front-end method that deal with saving an entity.
 * <ul>
 * <li>validates the entity according to business logic</li>
 * <li>decide whether to create or update the entity in the back-end</li>
 * <li>decide in what order to persist the entity and related objects
 * </ul>
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public interface GenericEntityManager<T extends Entity> {

	/**
	 * Save the entity in the storage back-end.
	 * @param entity
	 * @return boolean true if entity was saved successfully
	 * @throws InvalidEntityException
	 * @throws PersistenceException
	 */
	public abstract boolean save(T entity) throws InvalidEntityException,
			PersistenceException;

	/**
	 * Deletes the entity from the storage back-end.
	 * 
	 * @param entity the entity that should be saved
	 * @return true if the entity was saved successfully
	 * @throws InvalidEntityException if the entity is not valid according to business rules
	 * @throws PersistenceException if a general error occured during saving
	 */
	public abstract boolean delete(T entity)
			throws InvalidEntityException, PersistenceException;

	/**
	 * Validate the entity according the business-rules.
	 * 
	 * @param entity
	 * @return true if the entity is valid according to business rules
	 * @throws InvalidEntityException if the entity was found invalid
	 */
	public abstract boolean validate(T entity)
			throws InvalidEntityException;

	public abstract List<?> readAll(Class<?> entitClass) throws PersistenceException;
	
	public abstract List<T> readAll() throws PersistenceException;
	
	public abstract boolean saveAll(List<T> entities) throws PersistenceException, InvalidEntityException;

	/**
	 * Closes connections.
	 * @throws PersistenceException 
	 */
	public abstract void shutdown() throws PersistenceException;
}
