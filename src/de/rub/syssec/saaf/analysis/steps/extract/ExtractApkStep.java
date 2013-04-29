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

import de.rub.syssec.saaf.analysis.steps.AbstractStep;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;

/**
 * Copies APK to separate location and extracts and decodes it.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class ExtractApkStep extends AbstractStep {
	
	public ExtractApkStep(Config config, boolean enabled) {
		this.config = config;
		this.name = "Extract APK";
		this.description = "Copies the APK to a separate location and unpacks it.";
		this.enabled = enabled;
	}

	/* (non-Javadoc)
	 * @see steps.AbstractProcessingStep#process(de.rub.syssec.saaf.model.application.ApplicationInterface)
	 */
	@Override
	public boolean doProcessing(AnalysisInterface analysis) throws AnalysisException {
		ApplicationInterface app = analysis.getApp();
		File apk = app.getApkFile();
		File apkdir = app.getApkDirectory();
		File apkContentDir = app.getApkContentDir();
		File decompiledContentDir = app.getDecompiledContentDir();
		try {
			logger.info("Coyping apk-file to "+apkdir.getAbsolutePath());
			ApkUnzipper.copyApk(apk, apkdir);
			logger.info("Extracting content to "+apkContentDir.getAbsolutePath());
			ApkUnzipper.extractApk(apk, apkContentDir);
			logger.info("Decoding extracted content to "+apkContentDir.getAbsolutePath());
			ApkDecoderInterface.decode(apk, decompiledContentDir);
		} catch (Exception e1) {
			throw new AnalysisException(e1);
		}
		return true;
	}
}
