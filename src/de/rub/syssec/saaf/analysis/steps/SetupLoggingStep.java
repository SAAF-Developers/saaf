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

import java.io.File;

import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;

/**
 * Sets up a specific logfile for the application (apart from global log).
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class SetupLoggingStep extends AbstractStep {

	public SetupLoggingStep(Config config, boolean enabled) {
		this.config = config;
		this.name = "Setup Separate Logfile";
		this.description = "Sets up an extra logfile for the analysis of the application (apart from global logfile).";
		this.enabled = enabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * steps.AbstractProcessingStep#process(de.
	 * rub.syssec.model.application.ApplicationInterface)
	 */
	@Override
	public boolean doProcessing(AnalysisInterface analysis) throws AnalysisException {
		// setup an appender so that the output for the following session is
		// written to a logfile

		File appDir = analysis.getApp().getApplicationDirectory();
		String appName = analysis.getApp().getApplicationName();
		if (appDir != null && appName != null) {
			config.setupAnalysisLogfile(appDir.getAbsolutePath()
					+ File.separator + appName + "-"
					+ System.currentTimeMillis() + ".log");
			return true;
		}
		return false;
	}

}
