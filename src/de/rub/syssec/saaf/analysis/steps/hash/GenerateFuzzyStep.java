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
package de.rub.syssec.saaf.analysis.steps.hash;

import java.io.IOException;

import de.rub.syssec.saaf.analysis.steps.AbstractStep;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;

/**
 * Generates a fuzzy hash of the application archive.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class GenerateFuzzyStep extends AbstractStep {
	
	private SSDeep hasher;

	public GenerateFuzzyStep(Config config, boolean enabled) {
		this.config = config;
		this.name = "Generate Fuzzy Hash";
		this.description = "Computes a fuzzy hash of the application archive.";
		this.hasher = new SSDeep();
		this.enabled=enabled;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.analysis.steps.processing.AbstractProcessingStep#process(de.rub.syssec.saaf.model.application.ApplicationInterface)
	 */
	@Override
	public boolean doProcessing(AnalysisInterface analysis) throws AnalysisException {
		ApplicationInterface app = analysis.getApp();
		if (app != null) {
			try {
				hasher.generateHash(app, false);
			} catch (IOException e) {
				logger.error("An error occured during Fuzzy Hash generation: "+e.getMessage());
			}
		} else {
			throw new AnalysisException("There was no Application in this Analysis!");
		}
		return true;
	}
	
	
}
