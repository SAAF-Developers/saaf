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
import de.rub.syssec.saaf.db.persistence.interfaces.HPatternEntityManagerInterface;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.HPatternInterface;

/**
 * Just returns the Patterns from the XML-File.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class NoDBHPatternManager implements HPatternEntityManagerInterface {

	/**
	 * 
	 */
	public NoDBHPatternManager() {
		super();

	}

	@Override
	public boolean save(HPatternInterface entity)
			throws InvalidEntityException, PersistenceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean delete(HPatternInterface entity)
			throws InvalidEntityException, PersistenceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean validate(HPatternInterface entity)
			throws InvalidEntityException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<?> readAll(Class<?> entitClass) throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<HPatternInterface> readAll() throws PersistenceException {
		Set<HPatternInterface> patterns = new TreeSet<HPatternInterface>();
		try {
			patterns = Config.getInstance().getHTPatternSource().getData();
		} catch (DataSourceException e) {
			throw new PersistenceException(e);
		}
		return new ArrayList<HPatternInterface>(patterns);
	}

	@Override
	public boolean saveAll(List<HPatternInterface> entities)
			throws PersistenceException, InvalidEntityException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void shutdown() throws PersistenceException {
		// TODO Auto-generated method stub
	}

}
