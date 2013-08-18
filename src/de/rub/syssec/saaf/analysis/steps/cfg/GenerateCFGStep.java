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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import de.rub.syssec.saaf.analysis.steps.AbstractStep;
import de.rub.syssec.saaf.gui.actions.ExportAction;
import de.rub.syssec.saaf.misc.CFGGraph;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.Digest;
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
		
		File outDir = new File(Config.getInstance().getValue(ConfigKeys.DIRECTORY_CFGS)+File.separator+app.getApplicationName()+"_"+app.getMessageDigest(Digest.MD5));

		BufferedWriter outputFile= null;	
		try {
			if(!outDir.exists()){
				outDir.mkdirs();
			}
			outputFile = new BufferedWriter(new FileWriter(outDir+File.separator+"names.txt"));

		} catch (IOException e3) {
			e3.printStackTrace();
		}
			
		logger.info("Generating all control flow graphs for "+totalClasses+" classes...");
		for (ClassInterface file : classes) {// ignore
			// the
			// files
			// in
			// advertisement
			// packages
			for (MethodInterface method : file.getMethods()) {
				//TODO: wrap all this in its own method
				CFGGraph c = new CFGGraph(method);
				ExportAction ex = new ExportAction(c.getGraph(), outDir.toString());
				String parameters = "("+method.getParameterString().replaceAll("/", "_")+")";//TODO: maybe do this in method.getParameterString, or at least the "(" and ")"
				String methodName = method.getName().replace("<", "_").replace(">", "_");//This is due to windows not supporting < and > in file names
				StringBuilder realFileName = new StringBuilder();
				realFileName.append(method.getSmaliClass().getClassName());
				realFileName.append("_");
				realFileName.append(methodName);
				realFileName.append(parameters);
				realFileName.append(method.getReturnValueString());
				realFileName.append(".png");
										
				String newFileName = ex.export(method.getSmaliClass().getClassName(), "_",methodName,parameters,method.getReturnValueString(),".png",method.getSmaliClass().getPackageName(false));
				
				if(!realFileName.toString().equals(newFileName)){
					try {
						outputFile.write("Generated Filename:");
						outputFile.newLine();
						outputFile.write(newFileName);
						outputFile.newLine();
						outputFile.write("Real Filename");
						outputFile.newLine();
						outputFile.write(realFileName.toString());
						outputFile.newLine();
						outputFile.newLine();
						
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
				}

		}
			processedClasses++;
			if(processedClasses%scalefactor==0)
			{
		
				logger.info(String.format("Processed %d/%d classes", processedClasses,totalClasses));
			}
		}
		try {
			outputFile.close();
		} catch (IOException e1) {
			e1.printStackTrace();
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
