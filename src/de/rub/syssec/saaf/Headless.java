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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;

/**
 * This class handles the non GUI mode and starts the analysis.
 * 
 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
 * @author Johannes Hoffmann <johannes.hoffmann@rub.de>
 */
public class Headless {
	private static final Logger LOGGER = Logger.getLogger(Headless.class);
	private static Config CONFIG = Config.getInstance();
	
	private Headless() { /* no instance */}

	/**
	 * Analyze one or multiple APKs.
	 * If you use MultiThreading(default) be aware, sometimes if an error occurs,
	 * no Exception and no StackTrace is shown.
	 *
	 * important command parameter:
	 * <default>              APKs in given directory are analyzed.
	 * --recursive, -r        APKs in subdirectories are also analyzed.
	 * --fileList, -fl        APKs found at given file location are analyzed.
	 * --single-threaded, -st to deactivate MultiThreading
	 *
	 * @param path      {fileLocation of an APK,
	 *                  dir with several APKs in it,
	 *                  file with several file paths to APKs}
	 *                  depending on cmd-parameter.
	 * @return 1 as exitCode if isAborted or any Error
	 *
	 */
	public static int startAnalysis(File path) {
		if (path == null) {
			LOGGER.error("Please specify a file or directory to analyze!");
			return 1;
		} else if (!path.exists()) {
			LOGGER.error("File or directory does not exist. Inserted path: "
					+ path);
			return 1;
		}

		LinkedList<File> apks = new LinkedList<File>();
		if        (path.isFile()      && !CONFIG.getBooleanConfigValue(ConfigKeys.USE_FILE_LIST)) {
			apks = gatherApksFromPath(path);
		} else if (path.isFile()      && CONFIG.getBooleanConfigValue(ConfigKeys.USE_FILE_LIST)) {
			apks = gatherApksFromFileList(path);
		} else if (path.isDirectory() && !CONFIG.getBooleanConfigValue(ConfigKeys.RECURSIVE_DIR_ANALYSIS)) {
			apks = gatherApksFromPath(path);
		} else if (path.isDirectory() && CONFIG.getBooleanConfigValue(ConfigKeys.RECURSIVE_DIR_ANALYSIS)) {
			apks = gatherApksFromPath(path);
		}

		if (apks.size() <= 0) {
			LOGGER.error("Found no APK to analyze!");
			return 1;
		}

		return performAnalysis(apks);
	}

	/**
	 * Gather APKs for option-cases file, directory and recursive directory.
	 *
	 * @param path      APK file or directory of multiple APKs
	 * @return          LinkedList of APK-Files
	 * 
	 */
	private static LinkedList<File> gatherApksFromPath(File path) {
		LinkedList<File> apks = new LinkedList<File>();
		if (path.isDirectory()) {
			Collection<File> fc = FileUtils.listFiles(path,
					null, CONFIG.getBooleanConfigValue(ConfigKeys.RECURSIVE_DIR_ANALYSIS));
			apks = new LinkedList<File>(fc);
			if (apks.size() <= 0) {
				LOGGER.info("No files found in directory. Forgot a -r?");
			}
			LOGGER.info("Read " + apks.size() + " files from Directory " + path);
		} else if (path.isFile()) {
			apks.add(path);
		} 
		return apks;
	}

	/**
	 * Gather APKs for option-case fileList.
	 * You can configure a optional fileList_prefix in saaf.conf.
	 *
	 * @param fileList  ASCI-file with multiple paths to APK-files
	 * @return          LinkedList of APK-Files
	 */
	private static LinkedList<File> gatherApksFromFileList(File fileList) {
		LinkedList<File> apks = new LinkedList<File>();
		String pathFileListPrefix = CONFIG.getConfigValue(ConfigKeys.FILE_LIST_PREFIX);
		if (pathFileListPrefix == null) {
			LOGGER.warn("No 'prefix' directory configured for fileList! If" +
					"you want to use it, add 'path_fileList_prefix' to" +
					"saaf.conf.");
			pathFileListPrefix = "";
		}
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(fileList));
			String line = null;
			while ((line = in.readLine()) != null) {
				/**
				 * HACK: Every File produced by Microsoft SQL ... Studio,
				 * starts with the non printable Bytes EF BB BF. This is
				 * only a BugFix to destroy these bytes.
				 * 
				 * FIXME: Is this sequence on EACH line or only at the first?
				 */
				byte [] a = new byte[]{
						(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
				line = line.replaceAll(new String(a), "");

				File pathFileList = new File(pathFileListPrefix + File.separator +
					line.replace("\\\\", "").replace("\\", File.separator));
				if (!pathFileList.isFile() || !pathFileList.canRead()) {
					LOGGER.error("Skipping non valid file: "+pathFileListPrefix);
				} else {
					apks.add(pathFileList);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception ignored) {
				}
			}
		}
		LOGGER.info("Read " + apks.size() + " files from FileList " + fileList);
		return apks;
	}

	/**
	 * Analyze one or multiple APKs. Depending on the configuration this may
	 * happen single- or multithreaded.
	 *
	 * @param apks the apks to analyze
	 * @return 1 as exitCode if isAborted = true
	 */
	private static int performAnalysis(LinkedList<File> apks) {
		// Initialize MultiThreading and queue
		int corePoolSize = Runtime.getRuntime().availableProcessors();
		if (corePoolSize > 1) corePoolSize--;
		int numThreads = CONFIG.getIntConfigValue(ConfigKeys.MULTITHREADING_THREADS, corePoolSize);
		if (!CONFIG.getBooleanConfigValue(ConfigKeys.MULTITHREADING_ENABLED)) { // multithreading is disabled
			numThreads = 1;
		}
		// Create executor and submit jobs
		SAAFThreadPoolExecutor executor = new SAAFThreadPoolExecutor(apks,
				numThreads, numThreads, 5, TimeUnit.SECONDS);
		executor.allowCoreThreadTimeOut(true);
	
		// Tell the executor to shutdown afterwards
		executor.shutdown();
		boolean b = true;
		try {
			b = executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS); // timeout should not occur
		} catch (InterruptedException e) {
			LOGGER.error("Got interrupted while waiting for analyses to finish, this should not happen.", e);
		}
		if (!b) {
			LOGGER.error("Got a timeout while waiting for analyses to finish, this should not happen.");
		}
		executor.printStatistic();
		return executor.hasNoSuccess() ? 1 : 0;
	}
}
