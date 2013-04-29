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

import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.persistence.exceptions.InvalidEntityException;
import de.rub.syssec.saaf.db.persistence.exceptions.PersistenceException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface.Status;
import de.rub.syssec.saaf.model.application.ApplicationInterface;

/**
 * Provides some special methods only needed from an EntityManager for Analysis.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public interface AnalysisEntityManagerInterface extends
		GenericEntityManager<AnalysisInterface> {

	public abstract int countAllByApp(ApplicationInterface app) throws PersistenceException,
			NoSuchEntityException, InvalidEntityException;

	public abstract int deleteAllByApp(ApplicationInterface app) throws PersistenceException,
			NoSuchEntityException, InvalidEntityException;

	/**
	 * @return Count of all Analysis in DB
	 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
	 * @throws PersistenceException
	 */
	public abstract int countAnalysis() throws PersistenceException;

	/**
	 * @return Count of all Analysis with the given status in DB
	 * @param status
	 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
	 * @throws PersistenceException
	 */
	public abstract int countAnalysis(Status status) throws PersistenceException;
}
