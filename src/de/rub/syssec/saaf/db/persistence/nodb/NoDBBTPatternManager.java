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
import de.rub.syssec.saaf.db.persistence.interfaces.BTPatternEntityManagerInterface;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.BTPatternInterface;

/**
 * Just returns the Patterns from the XML-File.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class NoDBBTPatternManager implements BTPatternEntityManagerInterface {

	/**
	 * 
	 */
	public NoDBBTPatternManager() {
		super();
	}

	@Override
	public boolean save(BTPatternInterface entity)
			throws InvalidEntityException, PersistenceException {
		return true;
	}

	@Override
	public boolean delete(BTPatternInterface entity)
			throws InvalidEntityException, PersistenceException {
		return true;
	}

	@Override
	public boolean validate(BTPatternInterface entity)
			throws InvalidEntityException {
		return true;
	}

	@Override
	public List<?> readAll(Class<?> entitClass) throws PersistenceException {
		return new ArrayList<Object>();
	}

	@Override
	public List<BTPatternInterface> readAll() throws PersistenceException {
		Set<BTPatternInterface> patterns=new TreeSet<BTPatternInterface>();
		try {
			patterns = Config.getInstance().getBTPatternSource().getData();
		} catch (DataSourceException e) {
			throw new PersistenceException(e);
		}
		return new ArrayList<BTPatternInterface>(patterns);
	}

	@Override
	public boolean saveAll(List<BTPatternInterface> entities)
			throws PersistenceException, InvalidEntityException {
		return true;
	}

	@Override
	public void shutdown() throws PersistenceException {
		// TODO Auto-generated method stub

	}

}
