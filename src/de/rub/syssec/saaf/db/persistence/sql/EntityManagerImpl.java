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
package de.rub.syssec.saaf.db.persistence.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.Entity;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.analysis.BTPatternInterface;
import de.rub.syssec.saaf.model.analysis.BTResultInterface;
import de.rub.syssec.saaf.model.analysis.HPatternInterface;
import de.rub.syssec.saaf.model.analysis.HResultInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.MethodInterface;
import de.rub.syssec.saaf.model.application.PackageInterface;
import de.rub.syssec.saaf.model.application.manifest.PermissionRequestInterface;

/**
 * Default Implementation of GenericEntityManager that works as a facade.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class EntityManagerImpl implements EntityManagerFacade {

	private ApplicationEntityManagerInterface appManager;
	private PackageEntityManagerInterface packageManger;
	private ClassEntityManagerInterface classManager;
	private MethodEntityManagerInterface methodManager;
	private AnalysisEntityManagerInterface analysisManager;
	private BTResultEntityManagerInterface btResultManager;
	private HResultEntityManagerInterface hResultManager;
	private BTPatternEntityManagerInterface btPatternManager;
	private HPatternEntityManagerInterface hPatternManager;
	private PermissionRequestEntityManagerInterface permRequestManager;
	private SAAFExceptionEntityManagerInterface exceptionManager;
	
	private Connection connection;
	private PermissionEntityManagerInterface permissionManager;


	/**
	 * 
	 * @param config
	 * @throws PersistenceException 
	 */
	public EntityManagerImpl(Config config) throws PersistenceException{
		super();

		String driver = config.getConfigValue(ConfigKeys.DATABASE_DRIVER);
		String connectString = config.getConfigValue(ConfigKeys.DATABASE_CONNECTION_STRING);
		String username = config.getConfigValue(ConfigKeys.DATABASE_USER);
		String password = config.getConfigValue(ConfigKeys.DATABASE_PASSWORD);
		this.connection = null;

		try {
			Class.forName(driver);
			// create a database connection
			connection = DriverManager.getConnection(connectString, username, password);
			this.appManager = new ApplicationEntityManager(connection);
			this.packageManger = new PackageEntityManager(connection);
			this.classManager = new ClassEntityManager(connection);
			this.methodManager = new MethodEntityManager(connection);
			this.analysisManager = new AnalysisEntityManager(connection);
			this.btResultManager = new BTResultEntityManager(connection);
			this.hResultManager = new HResultEntityManager(connection);
			this.btPatternManager = new BTPatternEntityManager(connection);
			this.hPatternManager = new HPatternEntityManager(connection);
			this.permissionManager = new PermissionEntityManager(connection);
			this.permRequestManager = new PermissionRequestEntityManager(connection);
			this.exceptionManager = new ExceptionEntityManager(connection);
		
		} catch (ClassNotFoundException e) {
			throw new PersistenceException("Error setting up EntityManagers", e);
		} catch (SQLException e) {
			throw new PersistenceException("Error setting up EntityManagers", e);
		}



	}

	@Override
	public boolean save(Entity entity) throws InvalidEntityException,
			PersistenceException {
		if (entity instanceof ApplicationInterface)
			return this.appManager.save((ApplicationInterface) entity);

		if (entity instanceof BTPatternInterface)
			return this.btPatternManager.save((BTPatternInterface) entity);

		if (entity instanceof HPatternInterface)
			return this.hPatternManager.save((HPatternInterface) entity);

		if (entity instanceof ClassInterface)
			return this.classManager.save((ClassInterface) entity);

		if (entity instanceof MethodInterface)
			return this.methodManager.save((MethodInterface) entity);

		if (entity instanceof PackageInterface)
			return this.packageManger.save((PackageInterface) entity);

		if (entity instanceof AnalysisInterface)
		{	
			AnalysisInterface analysis = (AnalysisInterface) entity;
			boolean success = this.analysisManager.save(analysis);
			if(success)
			{
				List<BTResultInterface> btresults = analysis.getBTResults();
				if (btresults != null)
				{	
					btResultManager.saveAll(btresults);
				}
				List<HResultInterface> hresults = analysis.getHResults();
				if (hresults != null)
				{
					hResultManager.saveAll(hresults);
				}
				if(analysis.getApp()!=null && analysis.getApp().getManifest()!=null)
				{
					Collection<PermissionRequestInterface> permrequests = analysis.getApp().getManifest().getRequestedPermissions();
					for(PermissionRequestInterface request : permrequests)
					{
						if(request.getAnalysis()==null)
						{
							request.setAnalysis(analysis);
						}
						permRequestManager.save(request);
					}
				}
			
				exceptionManager.saveAll(((AnalysisInterface) entity).getNonCriticalExceptions());
				exceptionManager.saveAll(((AnalysisInterface) entity).getCriticalExceptions());
			}
			return success;
		}

		if (entity instanceof HResultInterface)
			return this.hResultManager.save((HResultInterface) entity);

		if (entity instanceof BTResultInterface)
			return this.btResultManager.save((BTResultInterface) entity);

		return false;
	}

	@Override
	public boolean delete(Entity entity) throws InvalidEntityException,
			PersistenceException {
		if (entity instanceof ApplicationInterface)
			return this.appManager.delete((ApplicationInterface) entity);

		if (entity instanceof BTPatternInterface)
			return this.btPatternManager.delete((BTPatternInterface) entity);

		if (entity instanceof HPatternInterface)
			return this.hPatternManager.delete((HPatternInterface) entity);

		if (entity instanceof ClassInterface)
			return this.classManager.delete((ClassInterface) entity);

		if (entity instanceof MethodInterface)
			return this.methodManager.delete((MethodInterface) entity);

		if (entity instanceof PackageInterface)
			return this.packageManger.delete((PackageInterface) entity);

		if (entity instanceof AnalysisInterface)
			return this.analysisManager.delete((AnalysisInterface) entity);

		if (entity instanceof HResultInterface)
			return this.hResultManager.delete((HResultInterface) entity);

		if (entity instanceof BTResultInterface)
			return this.btResultManager.delete((BTResultInterface) entity);

		return false;
	}

	@Override
	public boolean validate(Entity entity) throws InvalidEntityException {
		if (entity instanceof ApplicationInterface)
			return this.appManager.validate((ApplicationInterface) entity);

		if (entity instanceof BTPatternInterface)
			return this.btPatternManager.validate((BTPatternInterface) entity);

		if (entity instanceof HPatternInterface)
			return this.hPatternManager.validate((HPatternInterface) entity);

		if (entity instanceof ClassInterface)
			return this.classManager.validate((ClassInterface) entity);

		if (entity instanceof MethodInterface)
			return this.methodManager.validate((MethodInterface) entity);

		if (entity instanceof PackageInterface)
			return this.packageManger.validate((PackageInterface) entity);

		if (entity instanceof AnalysisInterface)
			return this.analysisManager.validate((AnalysisInterface) entity);

		if (entity instanceof HResultInterface)
			return this.hResultManager.validate((HResultInterface) entity);

		if (entity instanceof BTResultInterface)
			return this.btResultManager.validate((BTResultInterface) entity);

		return false;
	}

	@Override
	public List<?> readAll(Class<?> entitClass) throws PersistenceException {
		if (entitClass.equals(AnalysisInterface.class))
			return analysisManager.readAll();
		if (entitClass.equals(PackageInterface.class))
			return packageManger.readAll();
		if (entitClass.equals(ClassInterface.class))
			return classManager.readAll();
		if (entitClass.equals(MethodInterface.class))
			return methodManager.readAll();
		if (entitClass.equals(BTResultInterface.class))
			return btResultManager.readAll();
		if (entitClass.equals(HResultInterface.class))
			return hResultManager.readAll();
		if (entitClass.equals(BTPatternInterface.class))
			return btPatternManager.readAll();
		if (entitClass.equals(HPatternInterface.class))
			return hPatternManager.readAll();

		return new ArrayList<Object>();
	}

	@Override
	public List<Entity> readAll() {
		throw new UnsupportedOperationException(
				"Cannot implement this generic Method here. Use parameterized Method instead.");
	}

	@Override
	public boolean saveAll(List<Entity> entities) throws PersistenceException,
			InvalidEntityException {
		for (Entity entity : entities) {
			this.save(entity);
		}
		return true;
	}

	@Override
	public void shutdown() throws PersistenceException {
		appManager.shutdown();
		packageManger.shutdown();
		classManager.shutdown();
		methodManager.shutdown();
		analysisManager.shutdown();
		btResultManager.shutdown();
		hResultManager.shutdown();
		btPatternManager.shutdown();
		hPatternManager.shutdown();
		permRequestManager.shutdown();
		try {
			if(!this.connection.isClosed())
			{
				this.connection.close();
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * @return the appManager
	 */
	@Override
	public ApplicationEntityManagerInterface getAppManager() {
		return appManager;
	}

	/**
	 * @param appManager the appManager to set
	 */
	@Override
	public void setAppManager(ApplicationEntityManagerInterface appManager) {
		this.appManager = appManager;
	}

	/**
	 * @return the packageManger
	 */
	@Override
	public PackageEntityManagerInterface getPackageManger() {
		return packageManger;
	}

	/**
	 * @param packageManger the packageManger to set
	 */
	@Override
	public void setPackageManger(PackageEntityManagerInterface packageManger) {
		this.packageManger = packageManger;
	}

	/**
	 * @return the classManager
	 */
	@Override
	public ClassEntityManagerInterface getClassManager() {
		return classManager;
	}

	/**
	 * @param classManager the classManager to set
	 */
	@Override
	public void setClassManager(ClassEntityManagerInterface classManager) {
		this.classManager = classManager;
	}

	/**
	 * @return the methodManager
	 */
	@Override
	public MethodEntityManagerInterface getMethodManager() {
		return methodManager;
	}

	/**
	 * @param methodManager the methodManager to set
	 */
	@Override
	public void setMethodManager(MethodEntityManagerInterface methodManager) {
		this.methodManager = methodManager;
	}

	/**
	 * @return the analysisManager
	 */
	@Override
	public AnalysisEntityManagerInterface getAnalysisManager() {
		return analysisManager;
	}

	/**
	 * @param analysisManager the analysisManager to set
	 */
	@Override
	public void setAnalysisManager(AnalysisEntityManagerInterface analysisManager) {
		this.analysisManager = analysisManager;
	}

	/**
	 * @return the btResultManager
	 */
	@Override
	public BTResultEntityManagerInterface getBtResultManager() {
		return btResultManager;
	}

	/**
	 * @param btResultManager the btResultManager to set
	 */
	@Override
	public void setBtResultManager(BTResultEntityManagerInterface btResultManager) {
		this.btResultManager = btResultManager;
	}

	/**
	 * @return the hResultManager
	 */
	@Override
	public HResultEntityManagerInterface gethResultManager() {
		return hResultManager;
	}

	/**
	 * @param hResultManager the hResultManager to set
	 */
	@Override
	public void sethResultManager(HResultEntityManagerInterface hResultManager) {
		this.hResultManager = hResultManager;
	}

	/**
	 * @return the btPatternManager
	 */
	@Override
	public BTPatternEntityManagerInterface getBtPatternManager() {
		return btPatternManager;
	}

	/**
	 * @param btPatternManager the btPatternManager to set
	 */
	@Override
	public void setBtPatternManager(BTPatternEntityManagerInterface btPatternManager) {
		this.btPatternManager = btPatternManager;
	}

	/**
	 * @return the hPatternManager
	 */
	@Override
	public HPatternEntityManagerInterface gethPatternManager() {
		return hPatternManager;
	}

	/**
	 * @param hPatternManager the hPatternManager to set
	 */
	@Override
	public void sethPatternManager(HPatternEntityManagerInterface hPatternManager) {
		this.hPatternManager = hPatternManager;
	}

	/**
	 * @return the permRequestManager
	 */
	@Override
	public PermissionRequestEntityManagerInterface getPermRequestManager() {
		return permRequestManager;
	}

	/**
	 * @param permRequestManager the permRequestManager to set
	 */
	@Override
	public void setPermRequestManager(
			PermissionRequestEntityManagerInterface permRequestManager) {
		this.permRequestManager = permRequestManager;
	}

	/**
	 * @return the exceptionManager
	 */
	@Override
	public SAAFExceptionEntityManagerInterface getExceptionManager() {
		return exceptionManager;
	}

	/**
	 * @param exceptionManager the exceptionManager to set
	 */
	@Override
	public void setExceptionManager(SAAFExceptionEntityManagerInterface exceptionManager) {
		this.exceptionManager = exceptionManager;
	}

	@Override
	public PermissionEntityManagerInterface getPermissionManager() {
		return this.permissionManager;
	}

	@Override
	public void setPermissionEntityManager(PermissionEntityManagerInterface permissionManager) {
		this.permissionManager=permissionManager;
		
	}

}
