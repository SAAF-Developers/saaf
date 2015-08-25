package de.rub.syssec.saaf;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.analysis.AnalysisException;

/**
 * Own implementation of a ThreadPoolExcetuor which prints out some statistics.
 *
 * You have to call the SAAFThreadPoolExecutor.shutdown() after all threads
 * are submitted to the pool, because the terminate function is called
 * so you can react after that.
 *
 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
 * @author Johannes Hoffmann <johannes.hoffmann@rub.de>
 */
class SAAFThreadPoolExecutor extends ThreadPoolExecutor {
	private static final Logger LOGGER = Logger.getLogger(SAAFThreadPoolExecutor.class);
	
	private boolean gotUncaughtException = false; // Uncaught RuntimeExceptions
	private int criticalExceptionCount = 0; // Caught critical exceptions
	private int uncriticalExceptionCount = 0; // Caught uncritical exceptions from, eg, Program Slicing
	private int analysisCount = 0;
	private final int apkCount;
	private boolean aborted = false;
	private int skipped = 0;

	/**
	 * A ThreadPoolExecutor which will automatically start all analyzes for all given files.
	 * @param apks the files to analyze
	 * @param corePoolSize
	 * @param maximumPoolSize
	 * @param keepAliveTime
	 * @param unit
	 * @param workQueue
	 */
	public SAAFThreadPoolExecutor(Collection<File> apks, int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new ArrayBlockingQueue<Runnable>(apks.size()));
		
		apkCount = apks.size();
		for (File apk : apks) { // submit jobs
			try {
				AnalysisTask aft = new AnalysisTask(apk);
				this.submit(aft, aft); // ignore future
			}
			catch (AnalysisException e) {
				LOGGER.error("Could not generate Analysis object, skipping file: "+apk, e);
				skipped++;
			}
		}
	}
	
	@Override
	protected synchronized void beforeExecute(Thread t, Runnable r) {
		analysisCount++;
		LOGGER.info("Beginning analysis "+analysisCount+" of "+apkCount);
		super.beforeExecute(t, r);
	}
	
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		boolean errorOccured = false;
		
		if (t != null) { // never actually happened
			LOGGER.error("afterExecute() found a throwable.", t);
			gotUncaughtException = true;
		}
		else if (r instanceof FutureTask<?>) {
			try {
				@SuppressWarnings("unchecked")
				AnalysisTask at = ((FutureTask<AnalysisTask>) r).get();
			   	if (at.hasNonCriticalExceptions()) {
			   		uncriticalExceptionCount ++;
			   	}
			   	if (at.hasCriticalException()) {
			   		errorOccured = true;
			   		criticalExceptionCount++;
					LOGGER.error("Analysis of `" + at.getAnalysis().getApp().getApplicationName()
							+ " failed!\n\n", at.getCriticalException());
			   	}
			} catch (CancellationException e) {
				LOGGER.warn("Analysis skipped!");
				skipped++;
			}
			catch (InterruptedException e) {
				LOGGER.warn("Analysis interrupted!");
				skipped++;
			}
			catch (ExecutionException e) {
				/*
				 * This is thrown by FutureTask.get() if the task threw an exception.
				 */
				gotUncaughtException = true;
				LOGGER.error("Analysis failed with exception!", e);
			}
		}
		
		if (errorOccured && Config.getInstance().getBooleanConfigValue(ConfigKeys.ANALYSIS_QUIT_ON_ERROR)) {
			LOGGER.error("An error occured and QUIT_ON_ERROR is set. Exiting!");
			skipped += shutdownNow().size();
			aborted = true;
		}
		else if (gotUncaughtException) { // We must exit on unforeseen happenings :) Disk full? OOM?
			LOGGER.error("Something unexpected occurred, see above. Will now perform an unclean exit (DB connections are not closed etc, but ShutdownHooks are run)!");
			System.exit(2);
		}
	}
	
	/**
	 * Print some nice statistics.
	 */
	public void printStatistic() {
			String cWhite  = "\033[m";
			String cGreen  = "\033[1;32m";
			String cYellow = "\033[1;33m";
			String cBlue   = "\033[1;34m";
			String cRed    = "\033[1;31m";
			Config conf = Config.getInstance();

			if (!conf.getBooleanConfigValue(ConfigKeys.LOGGING_USE_COLOR, false)) { // colors disabled
				cWhite = cGreen = cYellow = cBlue = cRed = "";
			}

			StringBuilder sb = new StringBuilder();
			if (aborted) {
				sb.append(cRed);
				sb.append("\n!!! ANALYSIS PREMATURELY ABORTED DUE TO ERROR AND CONFIG.QUIT_ON_ERROR SET TO TRUE !!!\n");
			}
			sb.append(cRed);
			sb.append("\nAnalysis statistics.\n====================");
			sb.append(cBlue);	
			sb.append("\n#APKs: ");
			sb.append(apkCount);
			sb.append(cGreen);
			sb.append("\n#Analyses: ");
			sb.append(analysisCount);
			sb.append(cYellow);
			if (skipped > 0) {
				sb.append("\nSkipped APK analyses due to error: ");
				sb.append(skipped);
			}
			sb.append("\n#Analyses w/ uncritical exceptions: ");
			sb.append(uncriticalExceptionCount);
			sb.append(cRed);
			sb.append("\n#Critical Exceptions: ");
			sb.append(criticalExceptionCount);
			sb.append(cWhite);

			if (conf.getBooleanConfigValue(ConfigKeys.ANALYSIS_SKIP_KNOWN_APP)) {
				sb.append(cYellow);
				sb.append("\nAlready known APKs were skipped.");
			}
			if (conf.getBooleanConfigValue(ConfigKeys.DATABASE_DISABLED)) {
				sb.append(cYellow);
				sb.append("\nNothing was persisted to the DB.");
			}
			sb.append("\n");
			LOGGER.info(sb.toString());
	}

	/**
	 * checks if there is any successful analysis
	 * @return true if each APK has a critical exception or aborted is set.
	 */
	public boolean hasNoSuccess() {
		return (apkCount == criticalExceptionCount || aborted);
	}

}
