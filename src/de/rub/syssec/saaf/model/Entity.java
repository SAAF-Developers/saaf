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

/**
 * Defines methods for all objects in the data-model that should be persisted.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public interface Entity {

	/**
	 * Obtain the entity's ID in the storage database.
	 * 
	 * @return the id (positive integer &gt 0 if persisted -1 otherwise)
	 */
	public abstract int getId();
	/**
	 * Set the ID used to reference the entity in the database.
	 * @param id
	 */
	public abstract void setId(int id);
	/**
	 * Mark the entity as changed.
	 * 
	 * When an entity as changed since compared
	 * to its last write. This function is called
	 * by the persistence layer to mark that the
	 * changes have been written to database.
	 * 
	 * @param changed true if entity should be written to database.
	 */
	public abstract void setChanged(boolean changed);
	
	public abstract boolean isChanged();

}