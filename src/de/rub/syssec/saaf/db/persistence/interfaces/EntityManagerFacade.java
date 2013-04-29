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

import de.rub.syssec.saaf.db.persistence.exceptions.PersistenceException;
import de.rub.syssec.saaf.model.Entity;

/**
 * A facade that servers as a single point of contact for the user classes while allowing access to the individual specialized entity managers.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public interface EntityManagerFacade extends GenericEntityManager<Entity> {

	public abstract void setExceptionManager(SAAFExceptionEntityManagerInterface exceptionManager);

	public abstract SAAFExceptionEntityManagerInterface getExceptionManager();

	public abstract void setPermRequestManager(PermissionRequestEntityManagerInterface permRequestManager);

	public abstract PermissionRequestEntityManagerInterface getPermRequestManager();

	public abstract void sethPatternManager(HPatternEntityManagerInterface hPatternManager);

	public abstract HPatternEntityManagerInterface gethPatternManager();

	public abstract void setBtPatternManager(BTPatternEntityManagerInterface btPatternManager);

	public abstract BTPatternEntityManagerInterface getBtPatternManager();

	public abstract void sethResultManager(HResultEntityManagerInterface hResultManager);

	public abstract HResultEntityManagerInterface gethResultManager();

	public abstract void setBtResultManager(BTResultEntityManagerInterface btResultManager);

	public abstract BTResultEntityManagerInterface getBtResultManager();

	public abstract void setAnalysisManager(AnalysisEntityManagerInterface analysisManager);

	public abstract AnalysisEntityManagerInterface getAnalysisManager();

	public abstract void setMethodManager(MethodEntityManagerInterface methodManager);

	public abstract MethodEntityManagerInterface getMethodManager();

	public abstract void setClassManager(ClassEntityManagerInterface classManager);

	public abstract ClassEntityManagerInterface getClassManager();

	public abstract void setPackageManger(PackageEntityManagerInterface packageManger);

	public abstract PackageEntityManagerInterface getPackageManger();

	public abstract void setAppManager(ApplicationEntityManagerInterface appManager);

	public abstract ApplicationEntityManagerInterface getAppManager();
	
	public abstract PermissionEntityManagerInterface getPermissionManager();
	
	public abstract void setPermissionEntityManager(PermissionEntityManagerInterface permissionManager);

	public abstract void shutdown() throws PersistenceException, PersistenceException;



}
