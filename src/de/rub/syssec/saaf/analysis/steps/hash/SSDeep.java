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
package de.rub.syssec.saaf.analysis.steps.hash;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;

public class SSDeep implements RollingHashGenerator {

	private static final String TMP = "temp";
	private static final Logger LOGGER = Logger.getLogger(SSDeep.class);
	private static final String SSDEEP_PATH = Config.getInstance().getConfigValue(ConfigKeys.EXECUTABLE_SSDEEP);

	public SSDeep() {
		/* nothing */
	}
	
	public String generateHash(File f) throws IOException {
		return calculateFuzzyHash(f);
	}
	
	public void generateHash(ApplicationInterface apk) throws IOException{
		generateHash(apk, false);
	}
	
	/**
	 * Generate hashes for all smali files.
	 */
	@Override
	public void generateHash(ApplicationInterface apk, boolean includeFilesFromAdPackages) throws IOException {
		File tmpDir = new File(apk.getApplicationDirectory()+File.separator+TMP);
		for (ClassInterface f : apk.getAllSmaliClasss(includeFilesFromAdPackages)) {
			// write smali file
			File target = new File(tmpDir.getAbsolutePath()+File.separator+f.getFullClassName(false)+".smali");
			FileUtils.copyFile(f.getFile(), target);
			f.setSsdeepHash(calculateFuzzyHash(target));
			target.delete();

			/*
			 * generate ssdeep for methods, this is not used, because usually methods are too short to give reliable ssdeep results
			 * 
			for(MethodInterface m: f.getMethods()){
				//take the name of the class and append the methodname+ parameters
				File methodTarget= new File (target.getAbsolutePath().substring(0, target.getAbsolutePath().length()-6));
				methodTarget = new File(methodTarget.getAbsolutePath()+"_"+m.getName()+"__"+m.getParameterString()+".smali");
				//write the method data to a file
				makeFileforMethod(m, methodTarget);
				m.setHash(calcHash(methodTarget));
//				System.out.println(m.getName()+" "+m.getParameterString()+" "+m.getHash());
			}
			*/
		}
		try {
			FileUtils.deleteDirectory(tmpDir);
		}
		catch (IOException e) {
			LOGGER.error("Could not delete directory. e="+e.getMessage());
		}
		
	}
	

	protected static String calculateFuzzyHash(File f) throws IOException{
		String hash = null;
		
		if (SSDEEP_PATH != null){
			ProcessBuilder pb = new ProcessBuilder(SSDEEP_PATH, f.getAbsolutePath());
			Process proc;
			Scanner in = null;
			try {
				proc = pb.start();
				// Start reading from the program
				in = new Scanner(proc.getInputStream());
		        while (in.hasNextLine()) {
		        	hash = in.nextLine();
		        }
				if (hash != null) return hash.substring(0, hash.lastIndexOf(","));
			}
			finally {
				try { if (in != null) in.close(); } catch (Exception ignored) { }
			}
		} else {
			LOGGER.warn("exec_ssdeep could not be found in saaf.conf");
		}
		return "";
	}
}
