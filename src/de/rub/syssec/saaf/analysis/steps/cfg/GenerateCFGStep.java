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
package de.rub.syssec.saaf.analysis.steps.cfg;

import java.io.File;
import java.util.List;

import de.rub.syssec.saaf.analysis.steps.AbstractStep;
import de.rub.syssec.saaf.analysis.steps.hash.Hash;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.MethodInterface;

/**
 * Generates CFG Graphics for Methods.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class GenerateCFGStep extends AbstractStep {

	public GenerateCFGStep(Config config, boolean enabled) {
		this.enabled = enabled;
		this.name = "Generate CFGs";
		this.description = "Generates Control Flow Graphs (GFG) as PNG files.";
		this.config = config;
	}

	@Override
	protected boolean doBefore(AnalysisInterface analysis)
			throws AnalysisException {
		// check if we actually have the executable to perform dot file
		// generation
		boolean fine = true;
		File dotExecutable = new File(
				config.getConfigValue(ConfigKeys.EXECUTABLE_DOT));
		if (!dotExecutable.exists()) {
			throw new AnalysisException("Could not find the dot program to generate flow graphs. Please check your settings for "
					+ ConfigKeys.EXECUTABLE_DOT+" in the configuration file");
		} else {
			if (!dotExecutable.canExecute()) {
				throw new AnalysisException("The permissions ofor the dot program to generate flow graphs do not allow execution. Please check permissions of "
						+ dotExecutable.getAbsolutePath());
			}
		}

		return fine;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.analysis.steps.analysis.AbstractAnalysisStep#process
	 * (de.rub.syssec.saaf .model.analysis.AnalysisInterface)
	 */
	@Override
	public boolean doProcessing(AnalysisInterface analysis)
			throws AnalysisException {
		ApplicationInterface app = analysis.getApp();
		List<ClassInterface> classes = app.getAllSmaliClasss(config
				.getBooleanConfigValue(ConfigKeys.CFGS_INCLUDE_AD_FRAMEWORKS));
		int totalClasses = classes.size();
		int processedClasses=0;
		int scalefactor = calculateScaleFactor(totalClasses);
			
		logger.info("Generating all control flow graphs for "+totalClasses+" classes...");
		for (ClassInterface file : classes) {// ignore
			// the
			// files
			// in
			// advertisement
			// packages
			for (MethodInterface method : file.getMethods()) {
				CfgBuilder.generateDotAndCfg(
						file,
						method,
						false,
						Config.getInstance().getConfigValue(
								ConfigKeys.DIRECTORY_CFGS),
						app.getApplicationName() + "_"
								+ app.getMessageDigest(Hash.DEFAULT_DIGEST));
			}
			processedClasses++;
			if(processedClasses%scalefactor==0)
			{
		
				logger.info(String.format("Processed %d/%d classes", processedClasses,totalClasses));
			}
		}
		logger.info("Finished generating flow graphs");
		return true;
	}

	private int calculateScaleFactor(int totalClasses) {
		int scalefactor = 10;
		if(totalClasses>=200)
		{
			scalefactor=50;
		}
		if(totalClasses>=500)
		{
			scalefactor=100;
		}
		if(totalClasses>=1000)
		{
			scalefactor=200;
		}
		return scalefactor;
	}

}
