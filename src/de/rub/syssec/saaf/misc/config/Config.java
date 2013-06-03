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
package de.rub.syssec.saaf.misc.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.varia.FallbackErrorHandler;

import de.rub.syssec.saaf.db.datasources.Datasource;
import de.rub.syssec.saaf.db.datasources.XMLBTPatternSource;
import de.rub.syssec.saaf.db.datasources.XMLHPatternSource;
import de.rub.syssec.saaf.db.datasources.XMLPermissionDatasource;
import de.rub.syssec.saaf.db.persistence.exceptions.PersistenceException;
import de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade;
import de.rub.syssec.saaf.db.persistence.nodb.NoDBEntityManager;
import de.rub.syssec.saaf.db.persistence.sql.EntityManagerImpl;
import de.rub.syssec.saaf.misc.adchecker.AdChecker;
import de.rub.syssec.saaf.misc.adchecker.AdNetwork;
import de.rub.syssec.saaf.misc.adchecker.NameComparingAdChecker;
import de.rub.syssec.saaf.misc.adchecker.XMLAdnetworkDataSource;
import de.rub.syssec.saaf.model.analysis.BTPatternInterface;
import de.rub.syssec.saaf.model.analysis.HPatternInterface;
import de.rub.syssec.saaf.model.application.PermissionInterface;

/**
 * The goto-place to obtain information from the config file
 * 
 * @author Johannes Hoffmann <johannes.hoffmann@rub.de>
 * @author Tilman Bender <tilman.bender@rub.de>
 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
 * 
 */
public class Config implements ConfigInterface {

	private static final String APP_DIRECTORY = "path_apps";
	private static final String BYTECODE_DIRECTORY = "path_bytecode";
	// key value store taht is read from the config file
	// and modified by cmdline parameters
	private Properties settings;
	private static Logger LOGGER;

	private static Config instance;

	public synchronized static Config getInstance() {
		if (instance == null) {
			instance = new Config();
		}
		return instance;
	}

	private Config() {
		try {
			LOGGER = Logger.getLogger(Config.class);
			readConfigFile();
			validate();
		} catch (Exception e) {
			System.out
					.println("There was a problem when parsing the config file 'saaf.conf'.\n"
							+ "Please make sure you have a valid saaf.conf in the current directory\n"
							+ "Alternatively set the environment variable SAAF_HOME to tell SAAF where to look for its files.\n"
							+ "A sample configuration file can be found in folder doc-manually");
			System.exit(-1);
		}

	}

	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void readConfigFile() throws FileNotFoundException, IOException {
		// check if the SAAF_HOME environment variable
		settings = new Properties();
		String saafHome = System.getenv("SAAF_HOME");
		if (saafHome != null) {
			setConfigValue(ConfigKeys.DIRECTORY_HOME, saafHome);
		} else {
			// default to local directory if the variable is not set
			setConfigValue(ConfigKeys.DIRECTORY_HOME,
					System.getProperty("user.dir"));
		}
		LOGGER.info("SAAF will be looking for configuration in:"
				+ getConfigValue(ConfigKeys.DIRECTORY_HOME) + File.separator
				+ "conf");

		File config = new File(getConfigValue(ConfigKeys.DIRECTORY_HOME)
				+ File.separator + "conf" + File.separator + "saaf.conf");
		FileInputStream propInputStream = null;

		if (config.exists()) {
			propInputStream = new FileInputStream(config);
		} else {
			propInputStream = new FileInputStream(
					getConfigValue(ConfigKeys.DIRECTORY_HOME) + File.separator
							+ "conf" + File.separator + "saaf.conf");
		}

		settings.load(propInputStream);
		propInputStream.close();
	}

	/**
	 * Perform several checks on the values specified in the configuration.
	 * 
	 * Checks include:
	 * <ul>
	 * <li>existence of executables
	 * <li>permissions to execute executables
	 * <li>check if directories exist and are readable
	 * </ul>
	 */
	public void validate() {
		boolean foundErrors = false;
		HashSet<Object> keysInConfigFile = new HashSet<Object>(
				settings.keySet());
		LOGGER.info("Validating configuration");
		for (Object keyInConfigFile : keysInConfigFile) {
			String entry = (String) keyInConfigFile;
			try {
				if (entry.startsWith("path_")) {
					File f = new File(settings.getProperty(entry));
					if (!f.exists()
							&& (entry.equalsIgnoreCase(APP_DIRECTORY) || entry
									.equalsIgnoreCase(BYTECODE_DIRECTORY)))
						f.mkdirs();
					if (!f.exists() || !f.isDirectory() || !f.canRead()) {
						LOGGER.error(entry
								+ "="
								+ settings.getProperty(entry)
								+ ": Directory does not exist or is not readable!");
						foundErrors = true;
					} else {
						// System.out.println("CONF: "+entry+"="+prop.getProperty(entry));
					}
				}
			} catch (IllegalArgumentException e) {
				LOGGER.warn("Problem validating config: " + e.getMessage());
			}

		}

		foundErrors = hasMissingExecutable();

		if (!settings.containsKey(ConfigKeys.DIRECTORY_APPS.toString())) {
			LOGGER.error("No 'apps' directory configured!");
			foundErrors = true;
		}
		if (!settings.containsKey(ConfigKeys.DIRECTORY_BYTECODE.toString())) {
			LOGGER.error("No 'bytecode' directory configured!");
			foundErrors = true;
		}

		if (!foundErrors) {
			LOGGER.info("Config looks sane, proceeding.");
		} else {
			LOGGER.error("Found errors in the configuration, aborting.");
			System.exit(-5);
		}
	}

	/**
	 * Checks if the required external programss (for graphing, decompiling
	 * etc.) are required and available.
	 * 
	 * @return
	 */
	private boolean hasMissingExecutable() {
		boolean foundErrors = false;
		// check if the external executables are required and available
		if (getBooleanConfigValue(ConfigKeys.ANALYSIS_GENERATE_CFG)) {
			foundErrors |= !isValidExecutable(ConfigKeys.EXECUTABLE_DOT);
		}
		if (getBooleanConfigValue(ConfigKeys.ANALYSIS_GENERATE_JAVA)) {
			foundErrors |= !isValidExecutable(ConfigKeys.EXECUTABLE_JAD);
		}
		if (getBooleanConfigValue(ConfigKeys.ANALYSIS_GENERATE_FUZZYHASH)) {
			foundErrors |= !isValidExecutable(ConfigKeys.EXECUTABLE_SSDEEP);
		}
		return foundErrors;
	}

	public boolean isValidExecutable(ConfigKeys key) {
		boolean isValid = true;
		String keyName = key.toString();
		if (settings.containsKey(keyName)) {
			File f = new File(this.settings.getProperty(keyName));
			if (!f.exists()) {
				LOGGER.warn(key + "=" + settings.getProperty(keyName)
						+ ": File does not exist!");
				isValid = false;
			} else if (!f.canExecute()) {
				LOGGER.error(key + "=" + settings.getProperty(keyName)
						+ ": File is not executable!");
				isValid = false;
			}
		}else{
			LOGGER.error(key + " could not be found in the configuration");
			isValid=false;
		}
		return isValid;
	}

	public AdChecker getAdChecker() {
		String adnetworks = this
				.getConfigValue(ConfigKeys.DATASOURCE_AD_NETWORKS);
		String schema = this
				.getConfigValue(ConfigKeys.DATASOURCE_SCHEMA_AD_NETWORKS);
		Datasource<AdNetwork> ds = new XMLAdnetworkDataSource(adnetworks,
				schema);
		return NameComparingAdChecker.getInstance(ds);
	}

	/**
	 * Sets up an additional log appender that writes logging output to an
	 * apk-specific logfile (separate from the one configured in
	 * log4j.properties).
	 * 
	 * This was introduced in #36 to allow users to have a per-analysis logfile
	 * instead of logging everything to one global saaf.log
	 * 
	 * @param filename
	 *            relative or absolute path to logfile. Intermediate directories
	 *            will be created automatically.
	 */
	public void setupAnalysisLogfile(String filename) {
		PatternLayout layout = new PatternLayout(
				"%d{dd MMM yyyy HH:mm:ss,SSS} [%t] %-5p %C{1} %x - %m%n");
		if (this.getBooleanConfigValue(ConfigKeys.LOGGING_CREATE_SEPERATE)) {

			FileAppender appender;
			try {
				String path = (getConfigValue(ConfigKeys.LOGGING_FILE_PATH) != null) ? getConfigValue(ConfigKeys.LOGGING_FILE_PATH)
						: filename;
				appender = new FileAppender(layout, path);
				appender.setErrorHandler(new FallbackErrorHandler());
				appender.setName("APK Analysis Appender");
				org.apache.log4j.Logger.getRootLogger().addAppender(appender);
			} catch (IOException e) {
				org.apache.log4j.Logger.getLogger(Config.class).warn(
						"Failed to create a dedicated log " + filename, e);
			}
		}
	}

	public Datasource<HPatternInterface> getHTPatternSource() {
		String patterns = this
				.getConfigValue(ConfigKeys.DATASOURCE_HEURISTIC_PATTERNS);
		String schema = this
				.getConfigValue(ConfigKeys.DATASOURCE_SCHEMA_HEURISTIC_PATTERNS);
		return new XMLHPatternSource(patterns, schema);
	}

	public Datasource<BTPatternInterface> getBTPatternSource() {
		String patterns = this
				.getConfigValue(ConfigKeys.DATASOURCE_SLICING_PATTERNS);
		String schema = this
				.getConfigValue(ConfigKeys.DATASOURCE_SCHEMA_SLICING);
		return new XMLBTPatternSource(patterns, schema);
	}

	@Override
	public String getValue(ConfigKeys key) {
		return getConfigValue(key, key.defaultString);
	}

	@Override
	public void setConfigValue(ConfigKeys key, String value) {
		settings.setProperty(key.toString(), String.valueOf(value));
	}

	@Override
	public String getConfigValue(ConfigKeys key, String defaultValue) {
		return settings.getProperty(key.toString(), defaultValue);
	}

	@Override
	public int getIntConfigValue(ConfigKeys key, int defaultValue) {
		String r = settings.getProperty(key.toString());
		int ret = defaultValue;
		if (r != null) {
			ret = Integer.parseInt(r);
		}
		return ret;
	}

	@Override
	public void setIntConfigValue(ConfigKeys key, int value) {
		settings.setProperty(key.toString(), String.valueOf(value));
	}

	@Override
	public void setBooleanConfigValue(ConfigKeys key, boolean value) {
		this.setBooleanConfigValue(key.toString(), value);
	}

	public Datasource<PermissionInterface> getPermissionSource() {
		String patterns = this
				.getConfigValue(ConfigKeys.DATASOURCE_PERMISSIONS);
		String schema = this
				.getConfigValue(ConfigKeys.DATASOURCE_SCHEMA_PERMISSIONS);
		return new XMLPermissionDatasource(patterns, schema);
	}

	public EntityManagerFacade getEntityManager() throws PersistenceException {
		EntityManagerFacade facade = new NoDBEntityManager();
		if (!this.getBooleanConfigValue(ConfigKeys.DATABASE_DISABLED)) {
			facade = new EntityManagerImpl(this);
		}
		return facade;
	}

	public void setBooleanConfigValue(String key, boolean value) {
		settings.setProperty(key, String.valueOf(value));
	}

	public boolean getBooleanConfigValue(ConfigKeys key, boolean defaultValue) {
		String r = settings.getProperty(key.toString());
		boolean ret = defaultValue;
		if (r != null) {
			ret = Boolean.parseBoolean(r);
		}
		return ret;
	}

	public boolean getBooleanConfigValue(ConfigKeys key) {
		return this.getBooleanConfigValue(key, key.defaultBoolean);
	}

	public String getConfigValue(ConfigKeys key) {
		return this.getConfigValue(key, key.defaultString);
	}

	public File getFileConfigValue(ConfigKeys key) {
		return new File(settings.getProperty(key.toString(), key.defaultString));
	}
}
