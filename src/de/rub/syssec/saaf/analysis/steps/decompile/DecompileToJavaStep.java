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
package de.rub.syssec.saaf.analysis.steps.decompile;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import com.googlecode.dex2jar.v3.Main;

import de.rub.syssec.saaf.analysis.steps.AbstractStep;
import de.rub.syssec.saaf.analysis.steps.extract.ApkUnzipper;
import de.rub.syssec.saaf.misc.FileList;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;

/**
 * Decompiles dex into Java Code using dex2jar.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class DecompileToJavaStep extends AbstractStep {

	public DecompileToJavaStep(Config config, boolean enabled) {
		this.config = config;
		this.name = "Decompile to Java";
		this.description = "Decompiles dex into Java Code using dex2jar";
		this.enabled = enabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	de.rub.syssec.saafractProcessingStep#process(de.rub.syssec.saaf.model.application.
	 * ApplicationInterface)
	 */
	@Override
	public boolean doProcessing(AnalysisInterface analysis)
			throws AnalysisException {
		ApplicationInterface app = analysis.getApp();
		File apkFile = app.getApkFile();
		File bytecodedir = app.getBytecodeDirectory();
		File gen = null;
		logger.info("Decompilation to Java started ...");
		try {
			//create jar 			
			//TODO: Not sure where this went in the new version.
			//DexFileReader.ContinueOnException = true;

			gen = new File(bytecodedir.getAbsolutePath() + File.separator
					+ app.getApplicationName() + ".jar");
			logger.debug("Generating jar file for " + app.getApplicationName()
					+ " at " + gen);

			Main.doFile(apkFile, gen);

			//extract the jar to obtain .class files
			ApkUnzipper.extractArchive(gen,
					new File(bytecodedir.getAbsolutePath()));

			//decompile the .class files to .java
			Process p;

			Vector<File> files;
			// FileList gAF = new FileList();
			//
			// files = gAF.getFileVector();
			// gAF.getAllFilesRecursive(folder, ".class");

			// FIXME: use Application.getClassFiles?
			files = new FileList(bytecodedir, FileList.CLASS_FILES)
					.getAllFoundFiles(true);

			String jadPath = config.getConfigValue(ConfigKeys.EXECUTABLE_JAD);
			File toDecompile = null;
			for (int i = 0; i < files.size(); i++) {
				try {

					// System.out.print(i + "/" + files.size());
					// System.out.println(jadPath + " -sjava -ff -o " +
					// "-d " + files.get(i).getParent() + " " + files.get(i));
					toDecompile = files.get(i);
					logger.debug("Decompiling " + toDecompile);
					p = Runtime.getRuntime().exec(
							jadPath + " -sjava -ff -o " + "-d "
									+ files.get(i).getParent() + " "
									+ toDecompile);

					p.waitFor();
					toDecompile.delete();
					// System.out.println(System.getProperty("user.dir") +
					// File.separator + "jad -sjava -ff -o -lnc " +
					// files.get(i));

				} catch (IOException e) {
					throw e;
				} catch (Exception e) {
					logger.warn("Problem decompiling class files in folder "
							+ bytecodedir.getAbsolutePath(), e);
				}
			}
			logger.info("Decompilation to Java finished ...");
		} catch (Throwable e2) {
			throw new AnalysisException(e2);
		} finally {
			// remove the .jar file
			if (gen != null && gen.exists()) {
				gen.delete();
			}
		}

		return true;
	}

}
