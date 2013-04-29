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
		this.enabled=enabled;
		this.name="Generate CFGs";
		this.description="Generates Control Flow Graphs (GFG) as PNG files.";
		this.config=config;
}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.analysis.steps.analysis.AbstractAnalysisStep#process(de.rub.syssec.saaf
	 * .model.analysis.AnalysisInterface)
	 */
	@Override
	public boolean doProcessing(AnalysisInterface analysis) throws AnalysisException {
		ApplicationInterface app = analysis.getApp();
		for (ClassInterface file : app.getAllSmaliClasss(config.getBooleanConfigValue(ConfigKeys.CFGS_INCLUDE_AD_FRAMEWORKS))) {// ignore
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
						Config.getInstance().getConfigValue(ConfigKeys.DIRECTORY_CFGS),
						app.getApplicationName() + "_" + app.getMessageDigest(Hash.DEFAULT_DIGEST));
			}
		}
		return true;
	}

}
