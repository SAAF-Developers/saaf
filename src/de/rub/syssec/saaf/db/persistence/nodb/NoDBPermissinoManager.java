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
import java.util.Set;
import java.util.TreeSet;

import de.rub.syssec.saaf.db.datasources.DataSourceException;
import de.rub.syssec.saaf.db.persistence.exceptions.InvalidEntityException;
import de.rub.syssec.saaf.db.persistence.exceptions.PersistenceException;
import de.rub.syssec.saaf.db.persistence.interfaces.PermissionEntityManagerInterface;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.application.PermissionInterface;

/**
 * Just returns the Permissions from the XML-File.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class NoDBPermissinoManager implements PermissionEntityManagerInterface {

	public NoDBPermissinoManager() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.db.persistence.interfaces.GenericEntityManager#save(de.
	 * rub.syssec.model.Entity)
	 */
	@Override
	public boolean save(PermissionInterface entity)
			throws InvalidEntityException, PersistenceException {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.db.persistence.interfaces.GenericEntityManager#delete(de
	 * .rub.syssec.model.Entity)
	 */
	@Override
	public boolean delete(PermissionInterface entity)
			throws InvalidEntityException, PersistenceException {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.db.persistence.interfaces.GenericEntityManager#validate
	 * (de.rub.syssec.saaf.model.Entity)
	 */
	@Override
	public boolean validate(PermissionInterface entity)
			throws InvalidEntityException {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.db.persistence.interfaces.GenericEntityManager#readAll(
	 * java.lang.Class)
	 */
	@Override
	public List<?> readAll(Class<?> entitClass) throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.db.persistence.interfaces.GenericEntityManager#readAll()
	 */
	@Override
	public List<PermissionInterface> readAll() throws PersistenceException {
		Set<PermissionInterface> permissions=new TreeSet<PermissionInterface>();
		try {
			permissions = Config.getInstance().getPermissionSource().getData();
		} catch (DataSourceException e) {
			throw new PersistenceException(e);
		}
		return new ArrayList<PermissionInterface>(permissions);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.db.persistence.interfaces.GenericEntityManager#saveAll(
	 * java.util.List)
	 */
	@Override
	public boolean saveAll(List<PermissionInterface> entities)
			throws PersistenceException, InvalidEntityException {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.db.persistence.interfaces.GenericEntityManager#shutdown()
	 */
	@Override
	public void shutdown() throws PersistenceException {
		// TODO Auto-generated method stub

	}

}
