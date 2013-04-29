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
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.db.dao.DAOFactory;
import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.db.dao.exceptions.DuplicateEntityException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.interfaces.NuAnalysisDAO;
import de.rub.syssec.saaf.db.persistence.exceptions.InvalidEntityException;
import de.rub.syssec.saaf.db.persistence.exceptions.PersistenceException;
import de.rub.syssec.saaf.db.persistence.interfaces.AnalysisEntityManagerInterface;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface.Status;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.Digest;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 * @author Hanno Lemoine <hanno.lemoine@gdata.de> (see Interface)
 */
public class AnalysisEntityManager implements AnalysisEntityManagerInterface {

	private NuAnalysisDAO dao;
	private ApplicationEntityManager applicationManager;
	private Logger logger = Logger.getLogger(getClass());
	private Connection connection;

	/**
	 * @param connection
	 *            the database connection to use
	 */
	public AnalysisEntityManager(Connection connection) {
		super();
		// FIXME: The SQL-Dialect should not be hardcoded
		this.connection = connection;
		DAOFactory factory = DAOFactory.getDAOFactory(DAOFactory.MYSQL_DIALECT);
		this.dao = factory.getAnalysisDAO(connection);
		this.applicationManager = new ApplicationEntityManager(connection);
	}

	@Override
	public boolean save(AnalysisInterface entity)
			throws InvalidEntityException, PersistenceException {
		boolean success = false;
		if (validate(entity)) {
			if (entity.isChanged()) {
				try {
					saveParentObject(entity);
					// test whether we should create or update
					logger.debug("Trying to save entity: " + entity);
					if (entity.getId() > 0) {
						logger.debug("Entity has an ID (" + entity.getId()
								+ "). Updating existing record...");
						success = dao.update(entity);
						//mark it as unchanged
						entity.setChanged(!success);
					} else {
						try {
							// the entity has not been saved so far, let's try
							// to save it
							logger.debug("Entity does not have ID. Creating new record...");
							entity.setId(dao.create(entity));
							logger.debug("Record created with ID: "
									+ entity.getId());
							success = true;
							entity.setChanged(!success);
						} catch (DuplicateEntityException e) {
							// seems that an entity with these values already
							// exists in the database,
							// let's find it and update it.
							/*
							 * NOTE: Technically this should never happen since
							 * there is no unique constraint for analyses. If
							 * this branch of code is taken, check the code that
							 * creates the tables
							 */
							logger.debug("Entity was stored in the database during a previous run. Getting the ID ...");
							int id = dao.findId(entity);
							if (id > 0) {
								logger.debug("The id of the previously saved entity is: "
										+ id);
								// we found the id of the existing one
								entity.setId(id);
								// we update the existing one
								success = dao.update(entity);
								entity.setChanged(!success);
							} else {
								throw new PersistenceException(e);
							}
						}
					}
				} catch (DAOException e) {
					throw new PersistenceException(e);
				} catch (NoSuchEntityException e) {
					throw new PersistenceException(e);
				}
			}else{
				logger.debug("Entity is unchanged. Skipping.");
				success=true;
			}
		}
		return success;
	}

	private void saveParentObject(AnalysisInterface entity)
			throws InvalidEntityException, PersistenceException {
		logger.debug("Saving parent object of " + entity);
		applicationManager.save(entity.getApp());
	}

	@Override
	public boolean delete(AnalysisInterface entity)
			throws InvalidEntityException, PersistenceException {
		boolean success = false;
		if (validate(entity)) {
			try {
				logger.debug("Trying to delete entity: " + entity);
				success = dao.delete(entity);
			} catch (NoSuchEntityException e) {
				throw new PersistenceException(e);
			} catch (DAOException e) {
				throw new PersistenceException(e);
			}
		}
		return success;
	}

	@Override
	public boolean validate(AnalysisInterface entity)
			throws InvalidEntityException {
		return entity != null;
	}

	@Override
	public List<?> readAll(Class<?> entitClass) throws PersistenceException {
		return this.readAll();
	}

	@Override
	public List<AnalysisInterface> readAll() throws PersistenceException {
		try {
			return dao.readAll();
		} catch (DAOException e) {
			throw new PersistenceException(e);
		}
	}

	@Override
	public int deleteAllByApp(ApplicationInterface app)
			throws PersistenceException, NoSuchEntityException,
			InvalidEntityException {
		int deleted = 0;
		if (applicationManager.validate(app)) {
			try {
				deleted = this.dao.deleteAllByApplication(app);
			} catch (DAOException e) {
				throw new PersistenceException(e);
			}
		}
		return deleted;
	}

	@Override
	public int countAllByApp(ApplicationInterface app)
			throws PersistenceException, NoSuchEntityException,
			InvalidEntityException {
		int count = 0;
		if (applicationManager.validate(app)) {
			try {
				int id = applicationManager.findIdByHash(app.getMessageDigest(Digest.MD5));
				app.setId(id);
				count = this.dao.countAllByApplication(app);
			} catch (DAOException e) {
				throw new PersistenceException(e);
			}
		}
		return count;
	}

	@Override
	public boolean saveAll(List<AnalysisInterface> entities)
			throws PersistenceException, InvalidEntityException {
		for (AnalysisInterface analysis : entities) {
			this.save(analysis);
		}
		return true;
	}

	@Override
	public void shutdown() throws PersistenceException{
		try {
			this.applicationManager.shutdown();
			if(!this.connection.isClosed())
			{
				this.connection.close();
			}
		}
		catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}


	@Override
	public int countAnalysis() throws PersistenceException {
		return countAnalysis(false, null);
	}


	@Override
	public int countAnalysis(Status status) throws PersistenceException{
		return countAnalysis(true, status);
	}

	private int countAnalysis(boolean withStatus, Status status) throws PersistenceException {
		int count = 0;
		try {
			count = this.dao.countAnalysis(withStatus,status);
		} catch (DAOException e) {
			throw new PersistenceException(e);
		}
		return count;
	}
}
