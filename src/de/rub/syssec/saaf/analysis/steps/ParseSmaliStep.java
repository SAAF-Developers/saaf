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
package de.rub.syssec.saaf.analysis.steps;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import de.rub.syssec.saaf.application.SmaliClass;
import de.rub.syssec.saaf.db.datasources.DataSourceException;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.DetectionLogicError;
import de.rub.syssec.saaf.model.application.SmaliClassError;

/**
 * Parses .smali files into ClassInteface objects.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class ParseSmaliStep extends AbstractStep {

	/**
	 * Denotes the maximum size of all parsed SMALI files in bytes.
	 */
	private static final int MAXIMUM_FILES_SIZE = 1024 * 1024 * 1000; // 1000 mb

	
	public ParseSmaliStep(Config config, boolean enabled) {
		this.config = config;
		this.name = "Parse Smali";
		this.description = "Parses the .smali files created by <whom?> into an object-model.";
		this.enabled = enabled;
		
	}


	/* (non-Javadoc)
	 * @see steps.AbstractProcessingStep#process(de.rub.syssec.saaf.model.application.ApplicationInterface)
	 */
	@Override
	public boolean doProcessing(AnalysisInterface analysis) throws AnalysisException {
		// PARSE THE SMALI FILES
		/*
		 * Parse all Smali files TODO: ad packages are currently not checked,
		 * use the config for this! All smali files are parsed this way, this
		 * includes ad packages although they might never be used!
		 */
		int size = 0;
		boolean inAdFramework=false;
		ApplicationInterface app = analysis.getApp();
		int smaliClassLabel=app.getSmaliClassLabel();
		
		HashMap<String,ClassInterface> smaliClassMap=new HashMap<String, ClassInterface>();
		
		for (File f : app.getAllRawSmaliFiles(true)) {

			inAdFramework = config.getAdChecker().containsAnAd(f);
			if (!inAdFramework) {
				SmaliClass sf;
				try {
					sf = new SmaliClass(f, app, smaliClassLabel++);
					sf.setInAdFramework(inAdFramework);
					
					smaliClassMap.put(f.getAbsolutePath(), sf);
					size += sf.getSize();
				} catch (IOException e) {
					throw new AnalysisException(e);
				} catch (DetectionLogicError e) {
					throw new AnalysisException(e);
				} catch (SmaliClassError e) {
					throw new AnalysisException(e);
				}


			}
			if (size > MAXIMUM_FILES_SIZE)
				throw new AnalysisException(
						"Parsed SMALI files exceed maximum size of "
								+ MAXIMUM_FILES_SIZE + " bytes!");
		}
		app.setAllSmaliClasss(smaliClassMap);
		app.setSmaliClassLabel(smaliClassLabel);
		return true;
	}
}
