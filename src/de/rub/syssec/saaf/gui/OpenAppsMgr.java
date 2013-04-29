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
package de.rub.syssec.saaf.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.analysis.steps.hash.Hash;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;

public class OpenAppsMgr {
	
	private static final Logger logger = Logger.getLogger(OpenAppsMgr.class);

	private HashMap<String, OpenAnalysis> openedAnalysisMap = new HashMap<String, OpenAnalysis>();
	private HashMap<String,AnalysisInterface> analyses= new HashMap<String, AnalysisInterface>();
	
	protected OpenAppsMgr() {
		/* nothing */
	}
	
	
	/**
	 * 
	 * @param app
	 * @return true if app is unknown, false if the app is already known
	 * @throws Exception 
	 */
	public boolean addNewAnalysis(AnalysisInterface app) throws Exception {
		if (openedAnalysisMap.containsKey(app.getApp().getMessageDigest(Hash.DEFAULT_DIGEST))) { // already opened
			logger.info("An application with the same hash is already opened.\n" +
					"Hash: "+app.getApp().getMessageDigest(Hash.DEFAULT_DIGEST)+"\n" +
					"Name: "+openedAnalysisMap.get(app.getApp().getMessageDigest(Hash.DEFAULT_DIGEST)).getApplication().getApplicationName());
			return false;
		}
		else { // new
			openedAnalysisMap.put(app.getApp().getMessageDigest(Hash.DEFAULT_DIGEST), new OpenAnalysis(app, this));
			return true;
		}
	}

	
	/**
	 * Close the application and all associated windows.
	 * @param app
	 * @return
	 */
	public void closeAnalysis(AnalysisInterface app) {
		OpenAnalysis ao = openedAnalysisMap.get(app.getApp().getMessageDigest(Hash.DEFAULT_DIGEST));
		if (ao != null) {
			ao.close();
			openedAnalysisMap.remove(app.getApp().getMessageDigest(Hash.DEFAULT_DIGEST));
		}
	}
	
	
	/**
	 * Open a new frame for this analysis.
	 * @param app
	 * @param type
	 * @throws Exception 
	 */
	public void openFrame(AnalysisInterface app, de.rub.syssec.saaf.gui.OpenAnalysis.AppFrame type) throws Exception {
		OpenAnalysis oa = openedAnalysisMap.get(app.getApp().getMessageDigest(Hash.DEFAULT_DIGEST));
		if (oa == null) {
			throw new Exception("Unknown application requested!");
		}
		else {
			oa.showOrOpenNewFrame(type);
		}
	}
	
	
	/**
	 * Get all opened applications.
	 * @return
	 */
	public Vector<OpenAnalysis> getAllOpenedAnalysis() {
		return new Vector<OpenAnalysis>(openedAnalysisMap.values()); // this is not immutable
	}

	
	public List<AnalysisInterface> getAllAnalyses() {
		return new ArrayList<AnalysisInterface>(this.analyses.values());
	}
	
	
	public void addAnalysis(AnalysisInterface analysis)
	{
		this.analyses.put(analysis.getApp().getApplicationName(),analysis);
	}


	public int getOpenedAnalysisCnt() {
		return openedAnalysisMap.size();
	}
}
