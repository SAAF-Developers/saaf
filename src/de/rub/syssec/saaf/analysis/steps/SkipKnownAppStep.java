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
import de.rub.syssec.saaf.model.analysis.AnalysisInterface.Status;
import de.rub.syssec.saaf.model.application.ApplicationInterface;

/**
 * Checks whether the App has been previously analyzed and cancels analysis
 * accordingly.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class SkipKnownAppStep extends AbstractStep {

	private AnalysisEntityManagerInterface manager;
	private static final Logger LOGGER = Logger.getLogger(SkipKnownAppStep.class);

	public SkipKnownAppStep(Config config, AnalysisEntityManagerInterface manager,
			boolean enabled) {
		this.config = config;
		this.name = "Skip Known";
		this.description = "Checks whether the App has been previously analyzed and cancels analysis accordingly";
		this.manager = manager;
		this.enabled = enabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.analysis.steps.analysis.AbstractAnalysisStep#doProcessing(de.rub
	 * .syssec.model.analysis.AnalysisInterface)
	 */
	@Override
	protected boolean doProcessing(AnalysisInterface analysis)
			throws AnalysisException {
		boolean continueAnalysis = true;
		ApplicationInterface app = analysis.getApp();
		try {
			if (manager.countAllByApp(app) > 0) {
				LOGGER.info("App `" + app.getApplicationName()
						+ "` was previously analyzed. Skipping.");
				analysis.setStatus(Status.SKIPPED);
				continueAnalysis = false;
			} else {
				LOGGER.info("App `" + app.getApplicationName()
						+ "` has not been analyzed yet. Continuing.");
				continueAnalysis = true;
			}
		} catch (PersistenceException e) {
			throw new AnalysisException(e);
		} catch (NoSuchEntityException e) {
			throw new AnalysisException(e);
		} catch (InvalidEntityException e) {
			throw new AnalysisException(e);
		}
		return continueAnalysis;
	}

}
