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
package de.rub.syssec.saaf.analysis.steps;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.persistence.exceptions.InvalidEntityException;
import de.rub.syssec.saaf.db.persistence.exceptions.PersistenceException;
import de.rub.syssec.saaf.db.persistence.interfaces.AnalysisEntityManagerInterface;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;

/**
 * Remove all previous analyses for a given APK.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class TrashOldAnalysisStep extends AbstractStep {

	private AnalysisEntityManagerInterface manager;
	private static final Logger LOGGER = Logger.getLogger(TrashOldAnalysisStep.class);

	public TrashOldAnalysisStep(Config cfg, AnalysisEntityManagerInterface manager,  boolean enabled)
	{
		this.config = cfg;
		this.name = "Delete Old Analyses";
		this.description = "Removes all old analyses for the given application";
		this.enabled = enabled;
		this.manager=manager;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.analysis.steps.analysis.AbstractAnalysisStep#process(de.rub.syssec.saaf.model.analysis.AnalysisInterface)
	 */
	@Override
	public boolean doProcessing(AnalysisInterface analysis) throws AnalysisException {
		ApplicationInterface app = analysis.getApp();
		if(app!=null)
		{
			try {
				LOGGER.info("Removing old analyses for Application "+analysis.getApp().getApplicationName()+" from database.");
				int deleted = manager.deleteAllByApp(app);
				LOGGER.info("Removed "+deleted+" old analyses.");
				return true;
			} catch (PersistenceException e) {
				throw new AnalysisException(e);
			} catch (NoSuchEntityException e) {
				throw new AnalysisException(e);
			} catch (InvalidEntityException e) {
				throw new AnalysisException(e);
			}
		}
		return false;
	}
	
	
	

}
