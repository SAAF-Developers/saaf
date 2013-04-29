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
package de.rub.syssec.saaf;

import java.io.File;

import de.rub.syssec.saaf.analysis.Analysis;
import de.rub.syssec.saaf.application.Application;
import de.rub.syssec.saaf.db.persistence.exceptions.PersistenceException;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;

/**
 * Class to handle threats for analyzing multiple APKs.
 *
 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
 * @author Johannes Hoffmann <johannes.hoffmann@rub.de>
 */
public class AnalysisThread implements Runnable {
	private Analysis analysis = null;
	private boolean hasNonCriticalExceptions = false;
	private boolean hasCriticalExceptions = false;
	private Exception criticalException;
	private final Application app;

	/**
	 * New Thread to analyze one APK-file.
	 *
	 * @param apk A file pointing to an APK to be analyzed.
	 * @throws AnalysisException 
	 */
	public AnalysisThread(File apk) throws AnalysisException {
		app = new Application(apk, false);
		analysis = new Analysis(app);
	}

	@Override
	public void run() {
		Exception e = null;
		
		try { // Do not catch All exceptions otherwise things can get nasty! (Disk full etc)
			analysis.run();
			if (analysis.getNonCriticalExceptions().size() > 0) {
				hasNonCriticalExceptions = true;
			}
			if (analysis.getCriticalExceptions().size() > 0) {
				hasCriticalExceptions = true;
			}
		} catch (AnalysisException e1) {
			e = e1;
		}
		catch (PersistenceException e2) {
			e = e2;
		}
		finally {
			if (e != null) {
				hasCriticalExceptions = true;
				criticalException = e;
			}
		}
	}
	
	/**
	 * Returns whether at least one non-critical exception occurred during the analysis step. 
	 * Only call this method after the thread has finished, otherwise the result
	 * will always be false.
	 * @return
	 */
	public boolean hasNonCriticalExceptions() {
		return hasNonCriticalExceptions;
	}
	
	/**
	 * Returns whether a critical exception occurred during the analysis step. 
	 * Only call this method after the thread has finished, otherwise the result
	 * will always be false.
	 * @return
	 */
	public boolean hasCriticalException() {
		return hasCriticalExceptions;
	}
	
	/**
	 * The critical exception. This can only be an uncaught exception which was
	 * not handled by the Analysis logic itself.
	 * @return null if none occurred
	 */
	public Exception getCriticalException() {
		return criticalException;
	}
	
	/**
	 * The corresponding analysis object
	 * @return
	 */
	public AnalysisInterface getAnalysis() {
		return analysis;
	}
}
