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
package de.rub.syssec.saaf.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.analysis.steps.CheckSimilartiyStep;
import de.rub.syssec.saaf.analysis.steps.ParseSmaliStep;
import de.rub.syssec.saaf.analysis.steps.ProgressHandler;
import de.rub.syssec.saaf.analysis.steps.ProgressListener;
import de.rub.syssec.saaf.analysis.steps.SetupFileSystemStep;
import de.rub.syssec.saaf.analysis.steps.SetupLoggingStep;
import de.rub.syssec.saaf.analysis.steps.SkipKnownAppStep;
import de.rub.syssec.saaf.analysis.steps.Step;
import de.rub.syssec.saaf.analysis.steps.ThrowRuntimeExceptions;
import de.rub.syssec.saaf.analysis.steps.TrashOldAnalysisStep;
import de.rub.syssec.saaf.analysis.steps.cfg.GenerateCFGStep;
import de.rub.syssec.saaf.analysis.steps.cleanup.DeleteFilesStep;
import de.rub.syssec.saaf.analysis.steps.decompile.DecompileToJavaStep;
import de.rub.syssec.saaf.analysis.steps.extract.ExtractApkStep;
import de.rub.syssec.saaf.analysis.steps.extract.FileCheckStep;
import de.rub.syssec.saaf.analysis.steps.hash.GenerateFuzzyStep;
import de.rub.syssec.saaf.analysis.steps.hash.GenerateHashesStep;
import de.rub.syssec.saaf.analysis.steps.hash.Hash;
import de.rub.syssec.saaf.analysis.steps.heuristic.HeuristicSearchStep;
import de.rub.syssec.saaf.analysis.steps.metadata.CategorizePermissionsStep;
import de.rub.syssec.saaf.analysis.steps.metadata.ParseMetaDataStep;
import de.rub.syssec.saaf.analysis.steps.obfuscation.LengthBasedDetectObfuscationStep;
import de.rub.syssec.saaf.analysis.steps.obfuscation.EntropyBasedDetectObfuscationStep;
import de.rub.syssec.saaf.analysis.steps.reporting.GenerateReportStep;
import de.rub.syssec.saaf.analysis.steps.slicing.SlicingStep;
import de.rub.syssec.saaf.db.persistence.exceptions.InvalidEntityException;
import de.rub.syssec.saaf.db.persistence.exceptions.PersistenceException;
import de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.SAAFException;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.analysis.BTResultInterface;
import de.rub.syssec.saaf.model.analysis.HResultInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;

/**
 * This class takes care of all involved steps which are related to any analysis
 * operation of an APK.
 * 
 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
 * @author Johannes Hoffmann <johannes.hoffmann@rub.de>
 * 
 */
public class Analysis implements AnalysisInterface {
	private ApplicationInterface app;
	private List<BTResultInterface> slicingResults; 
	private List<HResultInterface> heuristicResults;
	private int heuristicValue;
	private Date creationTime = new Date(0);
	private Date startTime = creationTime;
	private Date stopTime = startTime;
	private Status status;
	private int analysisIdInDb = -1; // ID from the table in db
	private List<SAAFException> nonCriticalExceptions = new ArrayList<SAAFException>();
	private List<SAAFException> criticalExceptions = new ArrayList<SAAFException>();
	// the next 3 should be final and unmodifiable, but static init would be
	// very ugly
	private static final List<Step> PROCESSING_STEPS = new LinkedList<Step>();
	private static final List<Step> ANALYSIS_STEPS = new LinkedList<Step>();
	private static final List<Step> CLEANUP_STEPS = new LinkedList<Step>();
	private boolean changed;
	private File reportFile = null;
	private ProgressHandler progressHandler;

	private static final Logger LOGGER = Logger.getLogger(Analysis.class);

	private static final boolean INIT_OK;

	/**
	 * Static initializer. Sets up all the steps, these are always the same.
	 */
	static {
		boolean b = true;
		try {
			PROCESSING_STEPS.addAll(buildProcessingSteps());
			ANALYSIS_STEPS.addAll(buildAnalysisSteps());
			CLEANUP_STEPS.addAll(buildCleanupSteps());
		} catch (PersistenceException e) {
			LOGGER.error("Could not build steps required for analysis!", e);
			b = false;
		}
		INIT_OK = b;
	}

	/**
	 * Each analysis of each APK is handled by this class.
	 * 
	 * @param app
	 *            the application to be analyzed
	 * @throws AnalysisException
	 *             thrown if this class could not initialize
	 */
	public Analysis(ApplicationInterface app) throws AnalysisException {
		if (!INIT_OK)
			throw new AnalysisException(
					"Analysis initialization failed, see log!");
		this.progressHandler = new ProgressHandler();
		creationTime = Calendar.getInstance().getTime();
		this.app = app;
		status = Status.NOT_STARTED;
		changed = true;
	}

	/**
	 * Run all configured analysis steps.
	 * 
	 * @throws PersistenceException
	 * @throws AnalysisException
	 */
	public void run() throws PersistenceException, AnalysisException {
		LOGGER.debug("Preparing analysis of application " + app);
		LOGGER.debug("Setting up database connection");
		EntityManagerFacade manager = Config.getInstance().getEntityManager();
		LOGGER.debug("Configure the preprocessing and analysis steps");
		logEnabledStepsToConfig();
		startTime = Calendar.getInstance().getTime();
		status = updateStatus();
		LOGGER.info("Analysis for application " + app.getApplicationName()
				+ " started\n\n");

		try {
			// do what needs to be done so we can start analyzing
			doPreprocessing();
			//check if preprocessing didn't result in skipping the apk
			if (status == Status.SKIPPED) {
				LOGGER.info("Further analysis steps for "
						+ app.getApplicationName() + " are skipped.");
			} else { // run the different analyzes on the extracted apks'
				doAnalysis();
			}

			stopTime = Calendar.getInstance().getTime();
			status = updateStatus();

		} catch (AnalysisException e) { // do not catch all Exceptions here
			handleCaughtException(e);
		} catch (NullPointerException e) {
			handleCaughtException(e);
		} catch (NoSuchElementException e) {
			handleCaughtException(e);
		} catch (ArrayIndexOutOfBoundsException e) {
			handleCaughtException(e);
		}
		finally {
			if (status != Status.SKIPPED) {
				if (Config.getInstance().getBooleanConfigValue(ConfigKeys.ANALYSIS_GENERATE_REPORT)) {
					doGenerateReport();
				}
				try {
					// store the results
					int btrSize = 0;
					int hrSize = 0;
					if (getBTResults() != null)
						btrSize = getBTResults().size();
					if (getHResults() != null)
						hrSize = getHResults().size();
					LOGGER.info("Storing results. Backtracking: " + btrSize
							+ " Heuristic: " + hrSize);
					// save Analysis with new status to DB
					manager.save(this);
					LOGGER.info("Results stored.");
				} catch (InvalidEntityException e) {
					LOGGER.error("Problem storing the exceptions for analysis "
							+ this.app.getApplicationName());
				}
				// cleanup
				doCleanUp();
			}
			// close connections
			manager.shutdown();
		}
		LOGGER.info("Analysis for application " + app.getApplicationName()
				+ " completed\n\n");
	}

	/**
	 * @throws AnalysisException
	 */
	@Override
	public void doGenerateReport() throws AnalysisException {
		Step reporting = new GenerateReportStep(
				Config.getInstance(), true);
		reporting.process(this);
	}

	/**
	 * @throws AnalysisException
	 */
	@Override
	public void doCleanUp() throws AnalysisException {
		for (Step step : CLEANUP_STEPS) {
			if (!step.process(this))
				break;
		}
	}

	/**
	 * @throws AnalysisException
	 */
	@Override
	public void doAnalysis() throws AnalysisException {
		int done = 0;
		this.progressHandler.notifyMax(PROCESSING_STEPS.size());
		for (Step step : ANALYSIS_STEPS) {
			this.progressHandler.notifyProgress(step.getName());
			if (!step.process(this))
			{	
				return;
			}
			this.progressHandler.notifyProgress(++done);
		}
		this.progressHandler.notifyFinsihed();
	}

	/**
	 * @throws AnalysisException
	 */
	@Override
	public void doPreprocessing() throws AnalysisException {
		int done = 0;
		this.progressHandler.notifyMax(PROCESSING_STEPS.size());
		for (Step step : PROCESSING_STEPS) {
			this.progressHandler.notifyProgress(step.getName());
			if (!step.process(this)) {
				status = Status.SKIPPED;
				return;
			}
			this.progressHandler.notifyProgress(++done);
		}
	}

	private void handleCaughtException(Exception e) {
		LOGGER.error("Analysis for " + app.getApplicationName() + " failed!", e);
		status = Status.FAILED;
		this.addCriticalException(e);
	}

	/**
	 * Log the configured and therefore enabled steps.
	 */
	private void logEnabledStepsToConfig() {
		LOGGER.debug("Processing steps before analysis:");
		for (Step step : PROCESSING_STEPS) {
			LOGGER.debug(step);
		}
		for (Step step : ANALYSIS_STEPS) {
			LOGGER.debug(step);
		}
		for (Step step : CLEANUP_STEPS) {
			LOGGER.debug(step);
		}
	}

	/**
	 * Build analysis steps. These are the second steps run, after cleanup and
	 * before processing, eg, Program Slicing.
	 * 
	 * @return the steps
	 * @throws PersistenceException
	 */
	private static List<Step> buildAnalysisSteps() throws PersistenceException {
		Config config = Config.getInstance();
		EntityManagerFacade manager = config.getEntityManager();
		List<Step> analysisSteps = new LinkedList<Step>();
		analysisSteps.add(new TrashOldAnalysisStep(config, manager
				.getAnalysisManager(), config.getBooleanConfigValue(ConfigKeys.ANALYSIS_KEEP_ONLY_ONE)));
		analysisSteps.add(new EntropyBasedDetectObfuscationStep(config, true));
		analysisSteps.add(new CategorizePermissionsStep(config, true));
		analysisSteps.add(new HeuristicSearchStep(config, manager
				.gethPatternManager().readAll(), config.getBooleanConfigValue(ConfigKeys.ANALYSIS_DO_HEURISTIC)));
		analysisSteps.add(new ThrowRuntimeExceptions(config, false));
		analysisSteps.add(new SlicingStep(config, manager.getBtPatternManager()
				.readAll(), config.getBooleanConfigValue(ConfigKeys.ANALYSIS_DO_BACKTRACK)));
		// similarity checking is not implemented (just a dummy class)
		analysisSteps.add(new CheckSimilartiyStep(config, false));
		analysisSteps.add(new GenerateCFGStep(config, config.getBooleanConfigValue(ConfigKeys.ANALYSIS_GENERATE_CFG)));
		return analysisSteps;
	}

	/**
	 * Build processing steps. These are the first steps run and set everything
	 * up, eg, APK unpacking.
	 * 
	 * @param manager2
	 * @return the steps
	 * @throws PersistenceException
	 */
	private static List<Step> buildProcessingSteps()
			throws PersistenceException {
		Config config = Config.getInstance();
		EntityManagerFacade manager = config.getEntityManager();
		List<Step> processingSteps = new LinkedList<Step>();
		processingSteps.add(new FileCheckStep(config, true));
		processingSteps.add(new GenerateHashesStep(config, true));
		processingSteps.add(new SkipKnownAppStep(config, manager
				.getAnalysisManager(),config.getBooleanConfigValue(ConfigKeys.ANALYSIS_SKIP_KNOWN_APP)));
		processingSteps.add(new SetupFileSystemStep(config, false, true));
		processingSteps.add(new SetupLoggingStep(config, true));
		processingSteps.add(new ThrowRuntimeExceptions(config, false));
		processingSteps.add(new ExtractApkStep(config, true));
		processingSteps.add(new ParseMetaDataStep(config, true));
		processingSteps.add(new ParseSmaliStep(config, true));
		processingSteps.add(new LengthBasedDetectObfuscationStep(config, true));
		processingSteps.add(new EntropyBasedDetectObfuscationStep(config, true));

		processingSteps.add(new DecompileToJavaStep(config,
				config.getBooleanConfigValue(ConfigKeys.ANALYSIS_GENERATE_JAVA)));
		processingSteps.add(new GenerateFuzzyStep(config,
				config.getBooleanConfigValue(ConfigKeys.ANALYSIS_GENERATE_FUZZYHASH)));
		return processingSteps;
	}

	/**
	 * Build cleanup steps. These are run after all other steps.
	 * 
	 * @return the steps
	 */
	private static List<Step> buildCleanupSteps() {
		List<Step> cleanupSteps = new LinkedList<Step>();
		cleanupSteps.add(new DeleteFilesStep(Config.getInstance(),!Config.getInstance().getBooleanConfigValue(ConfigKeys.ANALYSIS_KEEP_FILES))); // delete
																			// old
																			// files
																			// if
																			// requested
		return cleanupSteps;
	}

	/**
	 * Increment the status of the analysis, eg, from NOT_STARTED to RUNNING
	 * unless already in FAILED or EXCEPTION state.
	 * 
	 * @return the new status
	 */
	private Status updateStatus() {
		switch (status) {
		case NOT_STARTED:
			status = Status.RUNNING;
			break;
		case RUNNING:
			if (nonCriticalExceptions.isEmpty())
				status = Status.FINISHED;
			else
				status = Status.FINISHED_WITH_EXCEPTION;
			break;
		case FINISHED: // status locked
		case SKIPPED:
		case FAILED:
		case FINISHED_WITH_EXCEPTION:
			break;
		default:
			LOGGER.warn("Unknown Status, will default to FAILED.");
			status = Status.FAILED;
			break;
		}
		setChanged(true);
		return status;
	}

	@Override
	public void setHeuristicValue(int heuristicValue) {
		this.heuristicValue = heuristicValue;
		setChanged(true);
	}

	@Override
	public int getId() {
		return analysisIdInDb;
	}

	/**
	 * Set a new ID. Can only be used once.
	 * 
	 * @param analysisIdInDb
	 *            the id which is used in the DB
	 */
	@Override
	public void setId(int id) {
		if (analysisIdInDb == -1) {
			analysisIdInDb = id;
		}
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public void setStatus(Status status) {
		this.status = status;
		setChanged(true);
	}

	@Override
	public ApplicationInterface getApp() {
		return app;
	}

	@Override
	public int getHeuristicValue() {
		return heuristicValue;
	}

	@Override
	public Date getStartTime() {
		return startTime;
	}

	@Override
	public Date getStopTime() {
		return stopTime;
	}

	@Override
	public Date getCreationTime() {
		return creationTime;
	}

	@Override
	public List<BTResultInterface> getBTResults() {
		return slicingResults;
	}

	@Override
	public List<HResultInterface> getHResults() {
		return heuristicResults;
	}

	/**
	 * Get all exceptions that occurred during the analysis and were handled
	 * locally.
	 * 
	 * @return the nonCriticalExceptions
	 */
	@Override
	public List<SAAFException> getNonCriticalExceptions() {
		return nonCriticalExceptions;
	}

	/**
	 * Set the exceptions that occurred during the analysis and were handled
	 * locally.
	 * 
	 * @param nonCriticalExceptions
	 *            the nonCriticalExceptions to set
	 */
	@Override
	public void setNonCriticalExceptions(List<SAAFException> backTrackExceptions) {
		this.nonCriticalExceptions = backTrackExceptions;
		setChanged(true);
	}

	@Override
	public void setApp(ApplicationInterface app) {
		this.app = app;
		setChanged(true);
	}

	@Override
	public void setBTResults(List<BTResultInterface> btResults) {
		this.slicingResults = btResults;
		setChanged(true);
	}

	@Override
	public void setHResults(List<HResultInterface> heuristicResults) {
		this.heuristicResults = heuristicResults;
		setChanged(true);
	}

	@Override
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	@Override
	public boolean isChanged() {
		return changed;
	}

	@Override
	public List<SAAFException> getCriticalExceptions() {
		return criticalExceptions;
	}

	@Override
	public void setCriticalExceptions(List<SAAFException> criticalExceptions) {
		this.criticalExceptions = criticalExceptions;
	}

	@Override
	public String toString() {
		return app.getApplicationName() + "_"
				+ app.getMessageDigest(Hash.DEFAULT_DIGEST);
	}
	
	@Override
	public File getReportFile(){
		return reportFile;
	}
	
	@Override
	public void setReportFile(File report)
	{
		this.reportFile=report;
	}

	@Override
	public void addNonCriticalException(Exception e) {
		nonCriticalExceptions.add(new SAAFException(e.getMessage(), e, this));		

	}

	@Override
	public void addCriticalException(Exception e) {
		criticalExceptions.add(new SAAFException(e.getMessage(), e, this));		
	}

	@Override
	public void addProgressListener(ProgressListener listener) {
		this.progressHandler.addProgressListener(listener);
	}

}
