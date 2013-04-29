
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
package de.rub.syssec.saaf.model.application;

import de.rub.syssec.saaf.model.Entity;

/**
 * A permission that belongs to the set of known permissions.
 * 
 * The idea of having two interfaces PermissionInterface and
 * PermissionRequestInterface stems from the fact that sometimes a permission as
 * it is requested in the manifest is not known to SAAF.
 * 
 * At a given time SAAF has a set known Permissions. These are modeled using the
 * PermissionInterface. Over the course of a SAAF deployment the set of known
 * permissions may change for a number of reasons:
 * 
 * <ul>
 * <li>Some permissions where forgotten in the inital config</li>
 * <li>The android platform changes. Google adds new permissions</li>
 * <li>New frameworks using custom permissions appear</li>
 * <li>...</li>
 * </ul>
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public interface PermissionInterface extends Entity,
		Comparable<PermissionInterface> {

	public abstract String getName();

	public abstract void setName(String permission);

	public abstract void setDescription(String description);

	public abstract String getDescription();

	public abstract void setType(PermissionType type);

	public abstract PermissionType getType();

}