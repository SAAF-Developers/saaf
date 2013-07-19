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

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;

/**
 * Provides default implementations for getters and setters common to all steps.
 * 
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public abstract class AbstractStep implements Step {

	protected String name = "Unnamed Step";
	protected String description = "I do nothing";
	protected Config config;
	protected boolean enabled;
	protected Logger logger = Logger.getLogger(getClass());

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	/**
	 * @return the config
	 */
	public Config getConfig() {
		return config;
	}

	/**
	 * @param config
	 *            the config to set
	 */
	public void setConfig(Config config) {
		this.config = config;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[name=" + name + ", description=" + description + ", enabled="
				+ enabled + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.analysis.steps.analysis.AnalysisStep#process(de.rub
	 * .syssec.saaf.model.analysis.AnalysisInterface)
	 */
	@Override
	public final boolean process(AnalysisInterface analysis)
			throws AnalysisException {
		boolean success = true;
		if (this.enabled) {
			if (doBefore(analysis)) {
				success = doProcessing(analysis);
				doAfter(analysis);
			}
			
		}
		return success;
	}

	/**
	 * This is where the main activity happens.
	 * 
	 * @return true if the processing of further steps should proceed
	 */
	protected abstract boolean doProcessing(AnalysisInterface analysis)
			throws AnalysisException;

	/**
	 * A hook that can be implemented by subclasses to do things before main
	 * processing.
	 * 
	 * @param analysis
	 * @return true if the doProcessing method should be processed
	 */
	protected boolean doBefore(AnalysisInterface analysis)
			throws AnalysisException {
		logger.debug("Start Analysis Step: " + this.name);
		return true;
	}

	/**
	 * A hook that can be implemented by subclasses to do things before main
	 * processing.
	 * 
	 * @param analysis
	 * @return if the processing of further steps should proceed
	 */
	protected boolean doAfter(AnalysisInterface analysis)
			throws AnalysisException {
		logger.debug("Stop Analysis Step: " + this.name);
		return true;
	}

}