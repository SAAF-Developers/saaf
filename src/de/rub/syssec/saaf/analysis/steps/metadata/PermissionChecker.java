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
package de.rub.syssec.saaf.analysis.steps.metadata;

import java.util.Collection;

import de.rub.syssec.saaf.db.datasources.DataSourceException;
import de.rub.syssec.saaf.model.application.PermissionInterface;
import de.rub.syssec.saaf.model.application.manifest.PermissionRequestInterface;

/**
 * Provides knowledge about Permissions.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public interface PermissionChecker {
	
	/**
	 * Check whether a PermissionRequest is valid.
	 * 
	 * @param p
	 * @throws DataSourceException 
	 */
	public void check(PermissionRequestInterface p) throws DataSourceException;
		
	public Collection<PermissionInterface> getKnownPermissions() throws DataSourceException;

}
