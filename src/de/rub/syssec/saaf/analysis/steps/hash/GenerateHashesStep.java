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

import de.rub.syssec.saaf.analysis.steps.AbstractStep;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.Digest;

/**
 * Creates cryptographic hashes for the application archive.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class GenerateHashesStep extends AbstractStep {
	
	public GenerateHashesStep(Config config, boolean enabled) {
		this.config = config;
		this.name = "Hash APK";
		this.description = "Creates cryptographic hashes for the application archive.";
		this.enabled = enabled;
	}

	@Override
	public boolean doProcessing(AnalysisInterface analysis)	throws AnalysisException {
		ApplicationInterface app = analysis.getApp();
		File apk = app.getApkFile();
		if (apk == null) throw new AnalysisException("APK file must not be null!");
		try {
			app.setMessageDigest(Digest.MD5, Hash.calculateHash(Digest.MD5, apk));
			app.setMessageDigest(Digest.SHA1, Hash.calculateHash(Digest.SHA1, apk));
			app.setMessageDigest(Digest.SHA256, Hash.calculateHash(Digest.SHA256, apk));
		}
		catch (Exception e) {
			throw new AnalysisException("Could not calculate message digests.", e);
		}	
		return true;
	}
}
