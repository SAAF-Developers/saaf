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

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.Digest;

/**
 * Creates necessary folders for further analysis.
 * 
 * Depending on the parameter Temp the directories will either be setup in the
 * working directory or a temporary directory
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class SetupFileSystemStep extends AbstractStep {

	private boolean temp;
	private static final Logger LOGGER = Logger.getLogger(SetupFileSystemStep.class);

	public SetupFileSystemStep(Config config, boolean temporary, boolean enabled) {
		this.config = config;
		this.name = "Create Folders";
		this.description = "Creates the folder structure necessary for further analysis.";
		this.enabled = enabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see steps.AbstractProcessingStep#process(de.
	 * rub.syssec.model.application.ApplicationInterface)
	 */
	@Override
	public boolean doProcessing(AnalysisInterface analysis) throws AnalysisException {
		ApplicationInterface app=analysis.getApp();
		File apk =app.getApkFile();
		File parentfolder = new File(config.getConfigValue(ConfigKeys.DIRECTORY_HOME));
		if (temp) {
			parentfolder = new File("");
		}

		// where to store the apk file for later use
		File apkDirectory = new File(parentfolder.getAbsolutePath()
				+ File.separator + config.getConfigValue(ConfigKeys.DIRECTORY_APPS));
		apkDirectory.mkdir();
		app.setApkDirectory(apkDirectory);

		// Set up the analysis directory. All further activity on the apk will
		// be in this directory or subdirs
		LOGGER.debug("Setting up analysis directory for: " + apk.getName());
		File analysesDirecotry = new File(parentfolder.getAbsolutePath()
				+ File.separator + config.getConfigValue(ConfigKeys.DIRECTORY_BYTECODE));
		
		LOGGER.debug("The analysis will be stored at: "+analysesDirecotry.getAbsolutePath());
		
		app.setBytecodeDirectory(analysesDirecotry);
		File appDirectory = new File(analysesDirecotry.getAbsolutePath()
				+ File.separator + app.getApplicationName() + "_" + app.getMessageDigest(Digest.SHA1));
		app.setApplicationDirectory(appDirectory);
		LOGGER.debug("The appdirectory will be at: "+appDirectory.getAbsolutePath());
		
		// Bytecode Directory for this app
		File decompiledContentDir = new File(appDirectory.getAbsolutePath()
				+ File.separator + config.getConfigValue(ConfigKeys.DIRECTORY_DECOMPILED_CONTENT));
		decompiledContentDir.mkdirs();
		app.setDecompiledContentDir(decompiledContentDir);
		LOGGER.debug("The decoded content  will be at: "+decompiledContentDir.getAbsolutePath());

		File apkContentDir = new File(appDirectory.getAbsolutePath()
				+ File.separator + config.getConfigValue(ConfigKeys.DIRECTORY_APK_CONTENT));
		
		app.setApkContentDir(apkContentDir);
		apkContentDir.mkdirs();
		LOGGER.debug("The apk content  will be at: "+apkContentDir.getAbsolutePath());


		// set smali directory of the Application (containing smali, java and
		// class files)
		File bytecodeDirectory = new File(
				decompiledContentDir.getAbsolutePath() + File.separator
						+ File.separator + "smali");
		LOGGER.debug("The smali files will be at: "+bytecodeDirectory.getAbsolutePath());
		bytecodeDirectory.mkdirs();
		app.setBytecodeDirectory(bytecodeDirectory);
		
		File manifestFile = new File(decompiledContentDir.getAbsolutePath()
				+ File.separator + "AndroidManifest.xml");
		app.setManifestFile(manifestFile);
		LOGGER.debug("The mainfest  will be at: "+manifestFile.getAbsolutePath());
		return true;
	}
}
