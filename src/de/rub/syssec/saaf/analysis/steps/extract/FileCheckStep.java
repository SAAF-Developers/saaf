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
package de.rub.syssec.saaf.analysis.steps.extract;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import de.rub.syssec.saaf.analysis.steps.AbstractStep;
import de.rub.syssec.saaf.application.Application;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;

/**
 * Performs some sanity check on an APK file.
 *
 */
public class FileCheckStep extends AbstractStep {
	
	
	public FileCheckStep(Config config, boolean enabled) {
		this.config = config;
		this.name = "Check APK";
		this.description = "Performs some sanity checks on a given file before analysis.";
		this.enabled = enabled;
	}

	/* (non-Javadoc)
	 * @see steps.AbstractProcessingStep#process(de.rub.syssec.saaf.model.application.ApplicationInterface)
	 */
	@Override
	public boolean doProcessing(AnalysisInterface analysis) throws AnalysisException {
		boolean valid = true;		
		logger.info("Checking APK...");
		
		ApplicationInterface app = analysis.getApp(); 
		valid = Application.isAPKFile(app.getApkFile());
		logger.info("Success.");
		return valid;
	}

	/**
	 * @param app
	 */
	private boolean isAPKFile(File apk) {
		boolean valid = true;	
		FileInputStream fis = null;
		try {
			if (apk.length() <= 2) {
				logger.info("File too small. Aborting.");
				valid=false;
			}
			if (!apk.canRead()) {
				logger.info("File not readable. Aborting.");
				valid=false;
			}
			fis = new FileInputStream(apk);
			byte[] fileHead = new byte[8];
			int read = fis.read(fileHead);
			if (read <= 2) {
				logger.info("Could not read file: "+apk.getName()+". Aborting.");
				valid=false;
			}
			if (
				fileHead[0] != 'P' ||
				fileHead[1] != 'K'
			) {
				logger.info("Magic bytes do not match! Aborting.");
				valid=false;
			}
		} catch (IOException e) {
			logger.info("Could not check file, aborting. Message: "+e.getMessage());
			valid=false;
		}
		finally {
			if (fis != null) {
				try { fis.close(); } catch (Exception e) { /* ignore */ }
			}
		}
		return valid;
	}
}
