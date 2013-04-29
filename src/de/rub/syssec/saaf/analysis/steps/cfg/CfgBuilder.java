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

import org.apache.log4j.Logger;
import org.apache.commons.io.FileUtils;

///import de.rub.syssec.saaf.misc.FileUtils;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.MethodInterface;

/**
 * Class to build Control Flow Graphs.
 */
public class CfgBuilder {
	
	private static final int MAX_FILE_LENGTH = 255;
	private static final String DOT_EXTENSION = ".dot";
	private static final String CFG_EXTENSION = ".png";
	private static final Logger LOGGER = Logger.getLogger(CfgBuilder.class);
	
	private static String buildPath(String filename, String extension){
		if ((filename.length()  +extension.length()) >= MAX_FILE_LENGTH) {
			return filename.substring(0, 255-extension.length())+extension;
		}
		else{
			return filename+extension;
		}
	}

	/**
	 * Builds the dot graph for a given method and all corresponding BasicBlocks.
	 * @param method
	 * @return
	 */
	private static String generateDOTGraphForMethod(MethodInterface method) {
		StringBuilder dot = new StringBuilder();
		dot.append("digraph G { \n");
		dot.append("/* BasicBlocks */\n");
		dot.append(method.getInstructionsForDot());
		dot.append("}\n");
		return dot.toString();
	}

	/**
	 * Generate a DOT and a CFG for the given method
	 * @param smali
	 * @param 
	 */
	public static void generateDotAndCfg(ClassInterface smali, MethodInterface method, boolean deleteOnExit, String targetDir, String appName) {
		String dotPath = Config.getInstance().getValue(ConfigKeys.EXECUTABLE_DOT);

		String params=method.getParameterString();
		File dotFile=null;
		
		String path;
		String fileName;
		
		
		if(targetDir==null){
			path = smali.getFile().getParent();
			fileName = smali.getClassName()+ "_" +method.getName() +"("+ params+")"+method.getReturnValueString();
			dotFile = new File(path +File.separator+ buildPath(fileName,DOT_EXTENSION));
		} else {
			String relativePath = smali.getFullClassName(false);
			relativePath = relativePath.replace("/", File.separator);
			
			//FIXME: the following two lines surely can be done better (and in one line). The first line generates a path to the dotFile which might be longer thatn MAX_FILE_LENGTH, the second line than takes that path and shortens it to be <= MAX_FILE_LENGTH
			dotFile = new File(targetDir+File.separator+appName+File.separator+relativePath
					+ "_" +method.getName() +"("+ params+")"+method.getReturnValueString() +DOT_EXTENSION);
			dotFile = new File(dotFile.getParent() +File.separator+ buildPath(dotFile.getName().substring(0, dotFile.getName().length()-4),DOT_EXTENSION));
		}
		File cfgFile = new File(dotFile.getParent() +File.separator+ buildPath(dotFile.getName().substring(0, dotFile.getName().length()-4),CFG_EXTENSION));
		if(deleteOnExit){
			dotFile.deleteOnExit();
			cfgFile.deleteOnExit();
		}

		if ((!dotFile.exists() || !cfgFile.exists())) {

			try {

				FileUtils.writeStringToFile(dotFile, generateDOTGraphForMethod(method));

				Process p = Runtime.getRuntime().exec(
						dotPath + " -Tpng " + dotFile.getAbsolutePath()//" -Tsvg:cairo:cairo "//-Tpng  //-Tjpg  //png needs different dot input (svg needs > escaped to &gt, png does not need this
								+ " -o " + cfgFile.getAbsolutePath());
				p.waitFor();

			}
			catch (Exception e) {
				LOGGER.error("Could not create CFG/DOT.", e);
			}
			
		}
	}
	
	/**
	 * Generate a DOT and a PNG for the given method
	 * @param smali
	 * @param 
	 */
	public static File generateDotAndCfgGUI(ClassInterface smali, MethodInterface method, String targetDir) {
		String dotPath = Config.getInstance().getValue(ConfigKeys.EXECUTABLE_DOT);

		String params = method.getParameterString();
		File dotFile = null;
		File cfgFile = null;
		String path ;
		String fileName;
		
		if(targetDir == null){
			path = smali.getFile().getParent();
				fileName = smali.getClassName()+ "_" +method.getName() +"("+ params+")"+method.getReturnValueString();
				dotFile = new File(path +File.separator+ buildPath(fileName,DOT_EXTENSION));
		} else {
			String relativePath = smali.getFullClassName(false);
			relativePath = relativePath.replace("/", File.separator);
			dotFile = new File(targetDir+File.separator/*+appName+File.separator*/+relativePath
					+ "_"+method.getName() +"("+ params+")"+method.getReturnValueString() +DOT_EXTENSION);
			dotFile = new File(dotFile.getParent() +File.separator+ buildPath(dotFile.getName().substring(0, dotFile.getName().length()-4),DOT_EXTENSION));
		}
		//FIXME: the following two lines surely can be done better (and in one line). The first line generates a path to the dotFile which might be longer thatn MAX_FILE_LENGTH, the second line than takes that path and shortens it to be <= MAX_FILE_LENGTH
		cfgFile = new File(dotFile.getAbsolutePath().substring(0,
				dotFile.getAbsolutePath().length() - 4)
				, CFG_EXTENSION);
		cfgFile = new File(dotFile.getParent() +File.separator+ buildPath(dotFile.getName().substring(0, dotFile.getName().length()-4),CFG_EXTENSION));

		if ((!dotFile.exists() || !cfgFile.exists())/*&& !smali.isInAdFrameworkPackage()*/) {
			try {
				FileUtils.writeStringToFile(dotFile, generateDOTGraphForMethod(method));
				Process p = Runtime.getRuntime().exec(
						dotPath + " -Tpng " + dotFile.getAbsolutePath()//" -Tsvg:cairo:cairo "//-Tpng  //-Tjpg  //png needs different dot input (svg needs > escaped to &gt, png does not need this
								+ " -o " + cfgFile.getAbsolutePath());
				p.waitFor();
			}
			catch (Exception e) {
				LOGGER.error("Could not create CFG/DOT.", e);
			}
		}
		return cfgFile;
	}
}
