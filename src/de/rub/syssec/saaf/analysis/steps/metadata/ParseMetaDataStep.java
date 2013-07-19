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

import java.io.File;

import de.rub.syssec.saaf.analysis.steps.AbstractStep;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.manifest.ManifestInterface;

/**
 * Extracts information from the Manifest.xml.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class ParseMetaDataStep extends AbstractStep {

	
	public ParseMetaDataStep(Config config, boolean enabled)
	{
		this.config = config;
		this.name = "Metadata Parsing";
		this.description = "Reads the applications Manifest and makes information available to other steps.";
		this.enabled=enabled;
	}
	/* (non-Javadoc)
	 * @see steps.AbstractProcessingStep#process(de.rub.syssec.saaf.model.application.ApplicationInterface)
	 */
	@Override
	public boolean doProcessing(AnalysisInterface analysis) throws AnalysisException {
		ApplicationInterface app = analysis.getApp();
		File manifestFile = app.getManifestFile();
		boolean success=false;
		if(manifestFile!=null)
		{
			ManifestInterface manifest;
			try {
				manifest = new DOMManifestParser(new SimplePermissionChecker(config.getPermissionSource())).parse(manifestFile);
				//nullcheck just to make sure nothing bad happend without an exception
				if(manifest!=null)
				{
					app.setManifest(manifest);
					success=true;
				}
			} catch (ManifestParserException e) {
				analysis.addNonCriticalException(e);
				logger.warn("An Exception occured while parsing the Mainfest",e);
				//let us not halt the whole process just because the manifest is not available
				success=true;
			}

		}
		return success;
	}
	
	
}
