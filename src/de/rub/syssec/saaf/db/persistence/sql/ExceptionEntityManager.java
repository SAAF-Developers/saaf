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
import de.rub.syssec.saaf.db.dao.interfaces.GenericDAO;
import de.rub.syssec.saaf.db.persistence.exceptions.InvalidEntityException;
import de.rub.syssec.saaf.db.persistence.exceptions.PersistenceException;
import de.rub.syssec.saaf.db.persistence.interfaces.GenericEntityManager;
import de.rub.syssec.saaf.db.persistence.interfaces.SAAFExceptionEntityManagerInterface;
import de.rub.syssec.saaf.model.SAAFException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class ExceptionEntityManager implements SAAFExceptionEntityManagerInterface {

	private Logger logger=Logger.getLogger(getClass());
	private GenericDAO<SAAFException> dao;
	private GenericEntityManager<AnalysisInterface> analysisEntityManager;
	private Connection connection;

	public ExceptionEntityManager(Connection connection2) {
		this.connection = connection2;
		this.dao = DAOFactory.getDAOFactory(DAOFactory.MYSQL_DIALECT).getExceptionDAO(connection);
		this.analysisEntityManager=new AnalysisEntityManager(connection2);
	}

	@Override
	public boolean save(SAAFException entity) throws InvalidEntityException,
			PersistenceException {
		boolean success = false;
		if (validate(entity)) {
			if (entity.isChanged()) {
				try {
					saveParentObject(entity);
					logger.debug("Trying to save entity: " + entity);

					if (entity.getId() > 0) {
						logger.debug("Entity has an ID (" + entity.getId()
								+ "). Updating existing record...");
						success = dao.update(entity);
						entity.setChanged(!success);
					} else {
						try {
							logger.debug("Entity does not have ID. Creating new record...");
							entity.setId(dao.create(entity));
							logger.debug("Record created with ID: "
									+ entity.getId());
							success = true;
							entity.setChanged(!success);
						} catch (DuplicateEntityException e) {
							// seems that an entity with these values already
							// exists
							// in the database,
							// let's find it and update it.
							int id = dao.findId(entity);
							logger.debug("Entity was stored in the database during a previous run. Getting the ID ...");
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
			} else {
				logger.debug("Entity is unchanged. Skipping.");
				success=true;			}
		}
		return success;
	}

	private void saveParentObject(SAAFException entity) throws InvalidEntityException, PersistenceException {
		this.analysisEntityManager.save(entity.getAnalysis());
		
	}

	@Override
	public boolean delete(SAAFException entity) throws InvalidEntityException,
			PersistenceException {
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
	public boolean validate(SAAFException entity) throws InvalidEntityException {
		return entity != null;
	}

	@Override
	public List<?> readAll(Class<?> entitClass) throws PersistenceException {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public List<SAAFException> readAll() throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveAll(List<SAAFException> entities)
			throws PersistenceException, InvalidEntityException {
		for (SAAFException result : entities) {
			this.save(result);
		}
		return true;
	}

	@Override
	public void shutdown() throws PersistenceException {
		analysisEntityManager.shutdown();
		try {
			if(!this.connection.isClosed())
			{
				this.connection.close();
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

}
