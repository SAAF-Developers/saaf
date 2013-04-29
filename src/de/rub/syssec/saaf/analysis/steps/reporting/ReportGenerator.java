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

import de.rub.syssec.saaf.model.analysis.AnalysisInterface;

/**
 * Interface for ReportGenerator.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public interface ReportGenerator {
	
	/**
	 * Generates a report from an analysis and writes it to a file. 
	 * 
	 * The method generates the report using the default template and writes
	 * it to a file with a generated unique name.
	 * 
	 * @param analysis the analysis that should be rendered
	 * @return the file that the report was written to
	 * @throws ReportingException 
	 */
	public File generateReport(AnalysisInterface analysis) throws ReportingException;
	
	/**
	 * Generate a report from an analysis using a specific template.
	 *
	 * The method generates the report using the template and writes
	 * it to a file with a generated unique name.
	 *
	 * @param analysis the analysis that should be rendered
	 * @param template the template that should be used to render the analysis
	 * @return the file that the report was written to
	 * @throws ReportingException 
	 */
	public File generateReport(AnalysisInterface analysis ,String template) throws ReportingException;

	/**
	 * Generate a report from the analysis using a specific template and destination file.
	 * 
	 * @param analysis
	 * @param template
	 * @param destinationPath the file or folder where report should be created
	 * @return the file that the report was written to
	 * @throws ReportingException 
	 */
	public File generateReport(AnalysisInterface analysis ,String template, File destinationPath) throws ReportingException;
}
