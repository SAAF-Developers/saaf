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
package de.rub.syssec.saaf.analysis.steps.cleanup;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import de.rub.syssec.saaf.analysis.steps.AbstractStep;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;

/**
 * Mark the analyzed APK file for deletion.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class DeleteFilesStep extends AbstractStep {
	
	private static final Logger LOGGER = Logger.getLogger(DeleteFilesStep.class);

	public DeleteFilesStep(Config cfg, boolean enabled)
	{
		this.config = cfg;
		this.name = "Cleanup analysis files";
		this.description = "Delete the coped APK file, smali+java files and unsaved CFGs unless -k/--keep is requested or user is using the GUI.";
		this.enabled = enabled;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.analysis.steps.analysis.AbstractAnalysisStep#process(de.rub.syssec.saaf.model.analysis.AnalysisInterface)
	 */
	@Override
	public boolean doProcessing(AnalysisInterface analysis) throws AnalysisException {
		if (!config.getBooleanConfigValue(ConfigKeys.ANALYSIS_IS_HEADLESS, true)) {
			LOGGER.debug("Keeping all generated files b/c GUI mode is enabled.");
			return true;
		} 
		else if (config.getBooleanConfigValue(ConfigKeys.ANALYSIS_KEEP_FILES)) { // keep?
			LOGGER.debug("Keeping all generated files in app/ and bytecode/ directory");
			return true;
		} 
		else {// else delete them
			LOGGER.debug("Deleting files from app/ and bytecode/ directory");
			return deleteAppData(analysis.getApp());
		}
	}
	
	/**
	 * Delete all related app data.
	 * @param app
	 */
	private static boolean deleteAppData(ApplicationInterface app) {
		 // Directory may be null if, eg, the file magic does not match and hence nothing was unpacked
		if (app != null && app.getApplicationDirectory() != null) {
			if (app.getApplicationDirectory().exists()) {
				try {
					FileUtils.deleteDirectory(app.getApplicationDirectory());
				} catch (IOException e) {
					LOGGER.error("Could not delete directory: "+e.getMessage());
					return false;
				}
			}

			File appCopy = new File(Config.getInstance().getConfigValue(ConfigKeys.DIRECTORY_APPS, "apps")
					+ File.separator + app.getApplicationName() + "."
					+ app.getFileExtension());
			if (appCopy.exists()) {
				appCopy.delete();
			}
		}
		return true;
	}
}
