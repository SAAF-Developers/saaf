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
package de.rub.syssec.saaf.db.datasources;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.application.manifest.permissions.Permission;
import de.rub.syssec.saaf.model.application.PermissionInterface;

/**
 * Reads permissions from a properties file.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * @deprecated Properties format is not sufficient for complex data. Use XML.
 */
public class PropertiesPermissionDataSource implements Datasource<PermissionInterface> {

	String properties;
	private Logger logger;
	
	/**
	 * @param properties
	 */
	public PropertiesPermissionDataSource(String properties) {
		super();
		this.properties = properties;
		this.logger = Logger.getLogger(PropertiesPermissionDataSource.class);
	}
	
	@Override
	public Set<PermissionInterface> getData() {
		Set<PermissionInterface> perms = new TreeSet<PermissionInterface>();
		logger.debug("Initializing list of known permissions.");
		Properties permissionNames = new Properties();
		InputStream is = null;
		try {
			is = new FileInputStream(properties);
			permissionNames.load(is);
		
			for (Object name : permissionNames.values()) {
				logger.debug("Added known permission: " + name);
				perms.add(new Permission((String) name));
			}
			logger.debug("Done initializing permission. Using a set of "
				+ perms.size() + " known permissions");
		} catch (IOException e) {
			logger.error("Cannot read list of known permissions.", e);
		}
		finally{
			try { if (is != null) is.close(); } catch (IOException ignored) { }
		}
		return perms;
	}
}
