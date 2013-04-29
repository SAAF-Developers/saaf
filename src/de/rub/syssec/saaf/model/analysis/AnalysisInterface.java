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
package de.rub.syssec.saaf.model.analysis;

import java.io.File;
import java.util.Date;
import java.util.List;

import de.rub.syssec.saaf.model.Entity;
import de.rub.syssec.saaf.model.SAAFException;
import de.rub.syssec.saaf.model.application.ApplicationInterface;

public interface AnalysisInterface extends Entity {

	public static enum Status {
		NOT_STARTED, RUNNING, FINISHED, FAILED, FINISHED_WITH_EXCEPTION, SKIPPED
	}

	/**
	 * @param heuristicValue
	 *            the heuristicValue to set
	 */
	public void setHeuristicValue(int heuristicValue);

	/**
	 * @return the status
	 */
	public Status getStatus();

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(Status status);

	/**
	 * @return the app
	 */
	public ApplicationInterface getApp();

	/**
	 * 
	 * @param app
	 */
	public void setApp(ApplicationInterface app);

	/**
	 * @return the heuristicValue
	 */
	public int getHeuristicValue();

	public Date getStartTime();

	public Date getStopTime();

	public  Date getCreationTime();

	/**
	 * @return the bt_results
	 */
	public List<BTResultInterface> getBTResults();

	public void setBTResults(List<BTResultInterface> btResults);

	/**
	 * @return the h_results
	 */
	public List<HResultInterface> getHResults();

	public void setHResults(List<HResultInterface> heuristicResults);

	public void setNonCriticalExceptions(
			List<SAAFException> backTrackExceptions);

	public List<SAAFException> getNonCriticalExceptions();

	public List<SAAFException> getCriticalExceptions();

	void setCriticalExceptions(List<SAAFException> criticalExceptions);

	public abstract void setReportFile(File report);

	public abstract File getReportFile();

	public abstract void doPreprocessing() throws AnalysisException;

	public abstract void doAnalysis() throws AnalysisException;

	public abstract void doCleanUp() throws AnalysisException;

	public abstract void doGenerateReport() throws AnalysisException;
}