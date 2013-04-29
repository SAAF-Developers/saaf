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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.stringtemplate.v4.AttributeRenderer;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.misc.STMessage;

import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;

/**
 * A ReportGenerator using StrinTemplate.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class STReportGenerator implements ReportGenerator, STErrorListener {

	private String reportNamePattern="Report-<analysis.app.applicationName>-<time>.xml";
	
	private String template="report";
	
	private File templateFolder;
	
	private String templateGroup;

	private static final Logger LOGGER=Logger.getLogger(STReportGenerator.class);
	
	
	
    public static class XMlEscapeStringRenderer implements AttributeRenderer {
        public String toString(Object o, String s, Locale locale) {
            return (String) (s == null ? o : StringEscapeUtils.escapeXml((String) o));
        }
    }
	/**
	 * @param templateFolder
	 * @param templateGroup
	 */
	public STReportGenerator(File templateFolder,
			String templateGroup) throws ReportingException {
		super();
		this.templateFolder = templateFolder;
		ensureTemplateFolder(this.templateFolder);
		this.templateGroup = templateGroup;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * reporting.ReportGenerator#generateReport(de.rub.syssec.saaf.logicTier.HResult)
	 */
	@Override
	public File generateReport(AnalysisInterface analysis) throws ReportingException {
		return this.generateReport(analysis, this.templateGroup,analysis.getApp().getApplicationDirectory());				
	}

	@Override
	public File generateReport(AnalysisInterface analysis, String templateGroup)
			throws ReportingException {
		return this.generateReport(analysis, templateGroup, analysis.getApp().getApplicationDirectory());
	}

	@Override
	public File generateReport(AnalysisInterface analysis, String templateGroup,
			File reportFolder) throws ReportingException {
		File generatedPath = generateUniqueReportFile(reportFolder, analysis);
		ensureDestinationPath(generatedPath);
		if(!new File(templateFolder + File.separator + templateGroup).exists())
		{
			LOGGER.warn("Template "+templateGroup+" was not found in "+templateFolder+"! Defaulting to xml.stg");
			templateGroup=this.templateGroup;
		}
		try {
		
		STGroup group = new STGroupFile(templateFolder + File.separator
				+ templateGroup, '$', '$');
		//make sure we are notified if something goes wrong with the rendering
		group.setListener(this);
		//add a special renderer for the XML-Template to make sure Method names like "<init>" are 
		//escaped correctly and don't screw up our XML
		group.registerRenderer(String.class, new XMlEscapeStringRenderer());
		//add an adapter to handle some special fields in Application
		group.registerModelAdaptor(ApplicationInterface.class, new ApplicationModelAdaptor());
		ST st = group.getInstanceOf(this.template);
		st.add("analysis", analysis);
		String result = st.render();


		writeToFile(generatedPath, result);
		} catch (Exception e) {
			throw new ReportingException("Problem writing to reports file", e);
		}
		// System.out.println(result);
		return generatedPath;
	}

	/**
	 * @return the reportNamePattern
	 */
	public String getReportNamePattern() {
		return reportNamePattern;
	}

	/**
	 * @return the template
	 */
	public String getTemplate() {
		return template;
	}

	/**
	 * @return the templateFolder
	 */
	public File getTemplateFolder() {
		return templateFolder;
	}

	/**
	 * @return the templateGroup
	 */
	public String getTemplateGroup() {
		return templateGroup;
	}

	/**
	 * @param reportNamePattern the reportNamePattern to set
	 */
	public void setReportNamePattern(String reportNamePattern) {
		this.reportNamePattern = reportNamePattern;
	}

	/**
	 * @param template the template to set
	 */
	public void setTemplate(String template) {
		this.template = template;
	}

	/**templateFolder
	 * @param templateFolder the templateFolder to set
	 */
	public void setTemplateFolder(File templateFolder) {
		this.templateFolder = templateFolder;
	}

	/**
	 * @param templateGroup the templateGroup to set
	 */
	public void setTemplateGroup(String templateGroup) {
		this.templateGroup = templateGroup;
	}

	private void ensureDestinationPath(File path) throws ReportingException {
		File parent = path.getAbsoluteFile().getParentFile();
		if(!parent.exists())
		{
			parent.mkdirs();
		}
	}

	private void ensureTemplateFolder(File folder) throws ReportingException {
		if (!folder.isDirectory() || !folder.exists()) {
			throw new ReportingException(
					"Could not find directory for reporting templates at: "
							+ folder.getAbsolutePath());
		}
	}

	private File generateUniqueReportFile(File parentDirectory, AnalysisInterface ana) {
		ST template = new ST(this.reportNamePattern);
		template.add("analysis", ana);
		template.add("time", System.currentTimeMillis());
		String actualname = template.render();
		return new File(parentDirectory.getAbsolutePath() + File.separator + actualname);
	}

	private void writeToFile(File reportFile, String result) throws IOException {
		reportFile.createNewFile();
		FileOutputStream os = new FileOutputStream(reportFile);
		OutputStreamWriter writer = new OutputStreamWriter(os,Charset.forName("utf-8"));
		writer.write(result);
		writer.flush();
		writer.close();
	}

	@Override
	public void IOError(STMessage arg0) {
		LOGGER.warn(arg0.toString());
	}

	@Override
	public void compileTimeError(STMessage arg0) {
		LOGGER.warn(arg0.toString());
	}

	@Override
	public void internalError(STMessage arg0) {
		LOGGER.warn(arg0.toString());
	}

	@Override
	public void runTimeError(STMessage arg0) {
		LOGGER.warn(arg0.toString());		
	}}
