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
package de.rub.syssec.saaf.analysis.steps.reporting;

import java.io.File;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.analysis.steps.AbstractStep;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;

/**
 * Creates a report in a configurable format.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class GenerateReportStep extends AbstractStep {

	private ReportGenerator reporting;
	private static final Logger LOGGER = Logger.getLogger(GenerateReportStep.class);
	public GenerateReportStep(Config cfg, boolean enabled) {
		this.config = cfg;
		this.name = "Generate analysis report";
		this.description = "Creates a report in a configurable format.";
		ReportGenerator gen=null;
		String templates = config.getConfigValue(ConfigKeys.DIRECTORY_REPORT_TEMPLATES);
		String templateGroup = config.getConfigValue(ConfigKeys.REPORTING_TEMPLATE_GROUP_DEFAULT);
		try {
			gen= new STReportGenerator(new File(templates),templateGroup);
		} catch (ReportingException e) {
			LOGGER.error("Problem instantiating report generator",e);
		}
		this.reporting = gen;
		this.enabled = enabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * steps.analysis.AbstractAnalysisStep#process(de.rub.syssec.saaf.model.analysis
	 * .AnalysisInterface)
	 */
	@Override
	public boolean doProcessing(AnalysisInterface analysis)
			throws AnalysisException {
		File reportFile;
		try {
			LOGGER.info("Generating analysis-report for "
					+ analysis.getApp().getApplicationName());
			// check if we were told to write to a user-provided reports
			// directory
			if (config.getConfigValue(ConfigKeys.DIRECTORY_REPORTS) != null) {
				// Write the report there

				reportFile = reporting.generateReport(analysis,
						config.getConfigValue(ConfigKeys.REPORTING_TEMPLATE_GROUP_DEFAULT), new File(config.getConfigValue(ConfigKeys.DIRECTORY_REPORTS)));
			} else {
				// we were not told where to write the report
				// check whether the working directory will be preserved
				// otherwise it would make no sense to write the report there
				if (config.getBooleanConfigValue(ConfigKeys.ANALYSIS_KEEP_FILES)) {
					// the working dir is preserved, we can write the report
					// there
					reportFile = reporting.generateReport(analysis,
							config.getConfigValue(ConfigKeys.REPORTING_TEMPLATE_GROUP_DEFAULT));
				} else {
					// the working dir is not preserved and we were not told
					// where to
					// write the report. We will use the value from the config
					File reportDir = new File(config.getConfigValue(ConfigKeys.DIRECTORY_REPORTS));
					LOGGER.info("No directory for reports was given and the working directories of the analysis will be deleted. Reports will be written to "
							+ reportDir.getAbsolutePath());

					reportFile = reporting.generateReport(analysis,
							config.getConfigValue(ConfigKeys.REPORTING_TEMPLATE_GROUP_DEFAULT), reportDir);

				}
			}
			analysis.setReportFile(reportFile);
			LOGGER.info("Report written to: " + reportFile.getAbsolutePath());
		} catch (ReportingException e) {
			throw new AnalysisException(e);
		}

		return true;
	}

}
