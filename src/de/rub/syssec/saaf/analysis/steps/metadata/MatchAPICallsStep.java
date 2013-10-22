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
package de.rub.syssec.saaf.analysis.steps.metadata;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.analysis.steps.AbstractStep;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;

/**
 * Read in APICalls.txt during startup and match apicalls onto apk.
 *  
 */
public class MatchAPICallsStep extends AbstractStep {

	
	private static final Logger logger=Logger.getLogger(MatchAPICallsStep.class);

	/**
	 * @param enabled TODO
	 */
	public MatchAPICallsStep(Config cfg, boolean enabled) {
		super();
		this.config = cfg;
		this.name="Match Permissions";
		this.description="Matches the APIcalls onto a set of currently known Permissions.";
		this.enabled=enabled;
	}

	@Override
	protected boolean doProcessing(AnalysisInterface analysis)
			throws AnalysisException {
		logger.info("Searching apicalls of application "+analysis.getApp());
		analysis.getApp().matchCalls();
		return true;
	}

}
