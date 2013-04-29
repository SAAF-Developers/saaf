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
package de.rub.syssec.saaf.misc.adchecker;

import java.io.File;
import java.util.Set;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.db.datasources.DataSourceException;
import de.rub.syssec.saaf.db.datasources.Datasource;

/**
 * Decides whether a File belongs to an Ad-Network based on its path.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class NameComparingAdChecker implements AdChecker {

	
	private static final Logger LOGGER = Logger.getLogger(NameComparingAdChecker.class);
	private static NameComparingAdChecker instance;
	private Datasource<AdNetwork> datasource;
	private Set<AdNetwork> adNetworks;

	private NameComparingAdChecker(Datasource<AdNetwork> datasource) {
		this.datasource = datasource;
	}

	public static NameComparingAdChecker getInstance(Datasource<AdNetwork> datasource){
		if (instance == null) {
			instance = new NameComparingAdChecker(datasource);
		}
		return instance;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.misc.adchecker.AdChecker#containsAnAd(java.io.File)
	 */
	@Override
	public boolean containsAnAd(File folder){
		try {
			initializeAdNetworks();
			String absolutePath = folder.getAbsolutePath();
			for (AdNetwork network : adNetworks) {
				if (absolutePath.contains(network.getPath())) {
					return true;
				} 
			}
		} catch (DataSourceException e) {
			LOGGER.warn("Could not retrieve information about advertising networks");
		}
		return false;
	}

	private void initializeAdNetworks() throws DataSourceException{
		if(this.adNetworks==null)
		{
			this.adNetworks = this.datasource.getData();
		}		
	}


	
	/**
	 * Resets the singleton (sets the internal instance to null).
	 * This method is primarily used for testing so the singleton can be reset before each test.
	 */
	public static void reset()
	{
		instance=null;
	}

	
}
