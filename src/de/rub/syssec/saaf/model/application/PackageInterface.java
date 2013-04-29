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

import java.util.List;

import de.rub.syssec.saaf.model.Entity;


/**
 * Contains Information about a package:
 * 
 * <ul>
 * 	<li> the name</li>
 *  <li> the fuzzy-hash</li>
 *  <li> the id of the application it belongs to<li>
 * </ul>
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public interface PackageInterface extends Entity{

	/**
	 * This method returns the package name, split by either a file.separator or a dot
	 * @param useDots this parameter detemines if the packagename is separated by a "." or a File.Separator
	 * @return the package name. example com.sun.package  or com/sun/package
	 */
	public abstract String getName(boolean useDots);
	
	/**
	 * Sets the package name, deeper levels have to be separated by a "." . example: com.sun.package
	 * @param string The package name
	 */
	public abstract void setName(String string);
	
	/**
	 * Sets the package name
	 * @param name a list containing all the package name info
	 */
	public abstract void setName(List<String> name);

	public abstract String getFuzzyHash();
	
	public abstract void setFuzzyHash(String hash);

	public abstract ApplicationInterface getApplication();
	
	public abstract void setApplication(ApplicationInterface app);


}