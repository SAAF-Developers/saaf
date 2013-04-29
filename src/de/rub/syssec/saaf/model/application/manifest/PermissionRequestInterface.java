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
package de.rub.syssec.saaf.model.application.manifest;

import de.rub.syssec.saaf.model.Entity;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.PermissionInterface;

/**
 * A permission as it is requested by the Manifest.
 * 
 * The fact that there are two intefaces PermissionInterface
 * and PermissionRequestInterface stems from the fact that
 * sometimes a permission as it is requested int he manifest
 * is not known to SAAF.
 * 
 * At a given time SAAF has a set known Permissions.
 * These are modeled using the PermissionInterface.
 * Over the course of a SAAF deployment the set of
 * known permissions may change for a number of reasons:
 * 
 * <ul>
 * 	<li>Some permissions where forgotten in the inital config</li>
 *  <li>The android platform changes. Google adds new permissions</li>
 *  <li>New frameworks using custom permissions appear</li>
 *  <li>...</li>
 * </ul>
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public interface PermissionRequestInterface extends Entity {
	public PermissionInterface getRequestedPermission();
	
	public void setRequestedPermission(PermissionInterface permission);
	
	public boolean isValid();
	
	public void setValid(boolean valid);

	public AnalysisInterface getAnalysis();
	
	public void setAnalysis(AnalysisInterface analysis);

}
