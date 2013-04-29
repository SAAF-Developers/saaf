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
import java.util.HashMap;

import de.rub.syssec.saaf.db.datasources.DataSourceException;
import de.rub.syssec.saaf.db.datasources.Datasource;
import de.rub.syssec.saaf.model.application.PermissionInterface;
import de.rub.syssec.saaf.model.application.PermissionType;
import de.rub.syssec.saaf.model.application.manifest.PermissionRequestInterface;

/**
 * Provides information about permissions from a datasource.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class SimplePermissionChecker implements PermissionChecker {

	Datasource<PermissionInterface> dataSource;
	HashMap<String, PermissionInterface> knownPermissions;

	/**
	 * @param dataSource
	 */
	public SimplePermissionChecker(Datasource<PermissionInterface> dataSource) {
		super();
		this.dataSource = dataSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.application.manifest.permissions.PermissionChecker#isKnown
	 * (de.rub.syssec.saaf.application.manifest.permissions.Permission)
	 */
	@Override
	public void check(PermissionRequestInterface request) throws DataSourceException {
		initPermissions();
		if(categorize(request.getRequestedPermission())==PermissionType.UNKNOWN)
		{
			request.setValid(false);
		}else
		{
			request.setValid(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.application.manifest.permissions.PermissionChecker#
	 * getKnownPermissions()
	 */
	@Override
	public Collection<PermissionInterface> getKnownPermissions() throws DataSourceException {
		initPermissions();
		return this.knownPermissions.values();
	}

	/**
	 * @throws DataSourceException 
	 * 
	 */
	private void initPermissions() throws DataSourceException {
		if (this.knownPermissions == null) {
			this.knownPermissions = new HashMap<String, PermissionInterface>();
			for (PermissionInterface p : this.dataSource.getData()) {
				this.knownPermissions.put(p.getName(), p);
			}
		}
	}
	
	private PermissionType categorize(PermissionInterface p)
	{
		PermissionType type = PermissionType.UNKNOWN;
		if (this.knownPermissions.containsKey(p.getName())) {
			type= knownPermissions.get(p.getName()).getType();
			p.setType(type);
		}
		return type;
	}

}
