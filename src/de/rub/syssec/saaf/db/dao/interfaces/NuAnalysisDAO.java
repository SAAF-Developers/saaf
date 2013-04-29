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
package de.rub.syssec.saaf.db.dao.interfaces;

import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface.Status;
import de.rub.syssec.saaf.model.application.ApplicationInterface;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public interface NuAnalysisDAO extends GenericDAO<AnalysisInterface> {


	/**
	 * Deletes all analyses for the given application.
	 * 
	 * @param application
	 */
	public abstract int deleteAllByApplication(ApplicationInterface application) throws NoSuchEntityException, DAOException;
	
	public abstract int countAllByApplication(ApplicationInterface application) throws NoSuchEntityException, DAOException;

	/**
	 * Count Analysis in DB (by status)
	 * @param withStatus  if false ignore given status and count all
	 * @param status      count all analysis with this status
	 * @return count of analysis
	 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
	 */
	public abstract int countAnalysis(boolean withStatus, Status status) throws DAOException;
}
