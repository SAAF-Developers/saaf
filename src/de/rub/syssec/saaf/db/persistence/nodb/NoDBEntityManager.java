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
package de.rub.syssec.saaf.db.persistence.nodb;

import java.util.ArrayList;
import java.util.List;

import de.rub.syssec.saaf.db.persistence.exceptions.InvalidEntityException;
import de.rub.syssec.saaf.db.persistence.exceptions.PersistenceException;
import de.rub.syssec.saaf.db.persistence.interfaces.AnalysisEntityManagerInterface;
import de.rub.syssec.saaf.db.persistence.interfaces.ApplicationEntityManagerInterface;
import de.rub.syssec.saaf.db.persistence.interfaces.BTPatternEntityManagerInterface;
import de.rub.syssec.saaf.db.persistence.interfaces.BTResultEntityManagerInterface;
import de.rub.syssec.saaf.db.persistence.interfaces.ClassEntityManagerInterface;
import de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade;
import de.rub.syssec.saaf.db.persistence.interfaces.HPatternEntityManagerInterface;
import de.rub.syssec.saaf.db.persistence.interfaces.HResultEntityManagerInterface;
import de.rub.syssec.saaf.db.persistence.interfaces.MethodEntityManagerInterface;
import de.rub.syssec.saaf.db.persistence.interfaces.PackageEntityManagerInterface;
import de.rub.syssec.saaf.db.persistence.interfaces.PermissionEntityManagerInterface;
import de.rub.syssec.saaf.db.persistence.interfaces.PermissionRequestEntityManagerInterface;
import de.rub.syssec.saaf.db.persistence.interfaces.SAAFExceptionEntityManagerInterface;
import de.rub.syssec.saaf.model.Entity;

/**
 * EnttityManager that does not use a storage backend.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class NoDBEntityManager implements EntityManagerFacade {

	private PermissionEntityManagerInterface permissionManager;
	private ApplicationEntityManagerInterface appManager;
	private PackageEntityManagerInterface packageManager;
	private ClassEntityManagerInterface classManager;
	private MethodEntityManagerInterface methodManager;
	private BTResultEntityManagerInterface btResultManager;
	private AnalysisEntityManagerInterface analysisManager;
	private HResultEntityManagerInterface hresultManager;
	private BTPatternEntityManagerInterface btPatternManager;
	private HPatternEntityManagerInterface hPatternManager;
	private PermissionRequestEntityManagerInterface permRequestManager;
	private SAAFExceptionEntityManagerInterface exceptionManager;

	
	
	/**
	 * 
	 */
	public NoDBEntityManager() {
		super();
		permissionManager=new NoDBPermissinoManager();
		appManager=new NoDBApplicationManager();
		packageManager=new NoDBPackageManager();
		classManager=new NoDBClassManager();
		methodManager=new NoDBMethodManager();
		btResultManager=new NoDBBTResultManager();
		analysisManager=new NoDBAnalysisManager();
		hresultManager=new NoDBHResultManager();
		btPatternManager=new NoDBBTPatternManager();
		hPatternManager=new NoDBHPatternManager();
		permRequestManager=new NoDBPermissionRequestManager();
		exceptionManager=new NoDBExceptionManager();

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.GenericEntityManager#save(de.rub.syssec.saaf.model.Entity)
	 */
	@Override
	public boolean save(Entity entity) throws InvalidEntityException,
			PersistenceException {
		return true;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.GenericEntityManager#delete(de.rub.syssec.saaf.model.Entity)
	 */
	@Override
	public boolean delete(Entity entity) throws InvalidEntityException,
			PersistenceException {
		return true;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.GenericEntityManager#validate(de.rub.syssec.saaf.model.Entity)
	 */
	@Override
	public boolean validate(Entity entity) throws InvalidEntityException {
		return true;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.GenericEntityManager#readAll(java.lang.Class)
	 */
	@Override
	public List<?> readAll(Class<?> entitClass) throws PersistenceException {
		return new ArrayList<Entity>();
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.GenericEntityManager#readAll()
	 */
	@Override
	public List<Entity> readAll() throws PersistenceException {
		return new ArrayList<Entity>();
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.GenericEntityManager#saveAll(java.util.List)
	 */
	@Override
	public boolean saveAll(List<Entity> entities) throws PersistenceException,
			InvalidEntityException {
		return true;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#setExceptionManager(de.rub.syssec.saaf.db.persistence.interfaces.SAAFExceptionEntityManagerInterface)
	 */
	@Override
	public void setExceptionManager(
			SAAFExceptionEntityManagerInterface exceptionManager) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#getExceptionManager()
	 */
	@Override
	public SAAFExceptionEntityManagerInterface getExceptionManager() {
		return this.exceptionManager;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#setPermRequestManager(de.rub.syssec.saaf.db.persistence.interfaces.PermissionRequestEntityManagerInterface)
	 */
	@Override
	public void setPermRequestManager(
			PermissionRequestEntityManagerInterface permRequestManager) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#getPermRequestManager()
	 */
	@Override
	public PermissionRequestEntityManagerInterface getPermRequestManager() {
		return this.permRequestManager;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#sethPatternManager(de.rub.syssec.saaf.db.persistence.interfaces.HPatternEntityManagerInterface)
	 */
	@Override
	public void sethPatternManager(
			HPatternEntityManagerInterface hPatternManager) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#gethPatternManager()
	 */
	@Override
	public HPatternEntityManagerInterface gethPatternManager() {
		return this.hPatternManager;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#setBtPatternManager(de.rub.syssec.saaf.db.persistence.interfaces.BTPatternEntityManagerInterface)
	 */
	@Override
	public void setBtPatternManager(
			BTPatternEntityManagerInterface btPatternManager) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#getBtPatternManager()
	 */
	@Override
	public BTPatternEntityManagerInterface getBtPatternManager() {
		return this.btPatternManager;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#sethResultManager(de.rub.syssec.saaf.db.persistence.interfaces.HResultEntityManagerInterface)
	 */
	@Override
	public void sethResultManager(HResultEntityManagerInterface hResultManager) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#gethResultManager()
	 */
	@Override
	public HResultEntityManagerInterface gethResultManager() {
		return this.hresultManager;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#setBtResultManager(de.rub.syssec.saaf.db.persistence.interfaces.BTResultEntityManagerInterface)
	 */
	@Override
	public void setBtResultManager(
			BTResultEntityManagerInterface btResultManager) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#getBtResultManager()
	 */
	@Override
	public BTResultEntityManagerInterface getBtResultManager() {
		return this.btResultManager;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#setAnalysisManager(de.rub.syssec.saaf.db.persistence.interfaces.AnalysisEntityManagerInterface)
	 */
	@Override
	public void setAnalysisManager(
			AnalysisEntityManagerInterface analysisManager) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#getAnalysisManager()
	 */
	@Override
	public AnalysisEntityManagerInterface getAnalysisManager() {
		return this.analysisManager;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#setMethodManager(de.rub.syssec.saaf.db.persistence.interfaces.MethodEntityManagerInterface)
	 */
	@Override
	public void setMethodManager(MethodEntityManagerInterface methodManager) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#getMethodManager()
	 */
	@Override
	public MethodEntityManagerInterface getMethodManager() {
		return this.methodManager;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#setClassManager(de.rub.syssec.saaf.db.persistence.interfaces.ClassEntityManagerInterface)
	 */
	@Override
	public void setClassManager(ClassEntityManagerInterface classManager) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#getClassManager()
	 */
	@Override
	public ClassEntityManagerInterface getClassManager() {
		return this.classManager;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#setPackageManger(de.rub.syssec.saaf.db.persistence.interfaces.PackageEntityManagerInterface)
	 */
	@Override
	public void setPackageManger(PackageEntityManagerInterface packageManger) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#getPackageManger()
	 */
	@Override
	public PackageEntityManagerInterface getPackageManger() {
		return this.packageManager;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#setAppManager(de.rub.syssec.saaf.db.persistence.interfaces.ApplicationEntityManagerInterface)
	 */
	@Override
	public void setAppManager(ApplicationEntityManagerInterface appManager) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#getAppManager()
	 */
	@Override
	public ApplicationEntityManagerInterface getAppManager() {
		return this.appManager;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#getPermissionManager()
	 */
	@Override
	public PermissionEntityManagerInterface getPermissionManager() {
		return this.permissionManager;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#setPermissionEntityManager(de.rub.syssec.saaf.db.persistence.interfaces.PermissionEntityManagerInterface)
	 */
	@Override
	public void setPermissionEntityManager(
			PermissionEntityManagerInterface permissionManager) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade#shutdown()
	 */
	@Override
	public void shutdown() throws PersistenceException, PersistenceException {
		// TODO Auto-generated method stub

	}

}
