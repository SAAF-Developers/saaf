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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jf.util.ConsoleUtil;
import org.jf.util.SmaliHelpFormatter;

import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.datasources.DataSourceException;
import de.rub.syssec.saaf.db.persistence.exceptions.InvalidEntityException;
import de.rub.syssec.saaf.db.persistence.exceptions.PersistenceException;
import de.rub.syssec.saaf.gui.MainWindow;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;

public class Main {

	private static final Logger LOGGER = Logger.getLogger(Main.class);

	private static final String VERSION_PROPERTIES = "de/rub/syssec/saaf/version.properties";
	public static String title;

	private static Options basicOptions = new Options();
	private static Options headlessOptions = new Options();
	private static Options guiOptions = new Options();
	private static Options reportDbLogOptions = new Options();
	private static Options options = new Options();

	private static Properties props;

	private static File apkPath;

	public static void main(String[] args) throws Exception {
		// make sure log4j configuration is read before doing anything else
		updateLog4jConfiguration(false, false);
		try {
			//Setup the commandline
			buildOptions();
			Config conf = Config.getInstance();
			//Parse the arguments and adjust configuration accordingly
			processCommandline(args);
			//Check if the adjusted configuration contains any errors/inconsistencies
			conf.validate();
			//prepare database and filesystem
			prepare(conf);

			//should we just watch a folder for incoming apks?
			if(conf.getBooleanConfigValue(ConfigKeys.DAEMON_ENABLED))
			{
				String watched = conf.getConfigValue(ConfigKeys.DAEMON_DIRECTORY);
				long interval = conf.getIntConfigValue(ConfigKeys.DAEMON_POLLING_INTERVAL,5000);
				FolderWatcher watcher = new FolderWatcher(watched,interval);
				watcher.startWatching();
			}
			// Create GUI?
			else if (conf.getBooleanConfigValue(
					ConfigKeys.ANALYSIS_IS_HEADLESS)) {
				// no GUI
				Headless.startAnalysis(apkPath);
			} else {
				// yes, I want a GUI
				// Schedule a job for the event-dispatching thread:
				// creating and showing this Application's GUI.
				// TODO: use apk_path in gui mode!
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						MainWindow m = new MainWindow();
						m.createAndShowGUI();
					}
				});
			}
		} catch (Exception e) {
			LOGGER.error("An error occured", e);
			System.exit(0);
		}

	}

	/**
	 * Reads the config file and prepares database and filessystem accordingly.
	 * 
	 * @param conf
	 * @throws PersistenceException
	 * @throws SQLException
	 * @throws IOException
	 * @throws InvalidEntityException
	 * @throws DataSourceException
	 */
	private static void prepare(Config conf) throws PersistenceException,
			SQLException, IOException, InvalidEntityException,
			DataSourceException {
		if (conf.getBooleanConfigValue(ConfigKeys.ANALYSIS_DROP_DB_AND_FILES)) {
			if (!conf.getBooleanConfigValue(ConfigKeys.DATABASE_DISABLED)) {
				LOGGER.info("Dropping database tables...");
				DatabaseHelper dbh = new DatabaseHelper(conf);
				dbh.dropTables();
				dbh.getConnection().close();
			}
			LOGGER.info("Deleting directories...");

			FileUtils.deleteDirectory(new File(conf
					.getConfigValue(ConfigKeys.DIRECTORY_APPS)));
			File f = new File(
					conf.getConfigValue(ConfigKeys.DIRECTORY_APPS));// necessary?
			f.mkdirs();// necessary?
			FileUtils.deleteDirectory(new File(conf
					.getConfigValue(ConfigKeys.DIRECTORY_APPS)));
			f = new File(conf.getConfigValue(ConfigKeys.DIRECTORY_BYTECODE));// necessary?
			f.mkdirs();// necessary?
		}

		if (!conf.getBooleanConfigValue(ConfigKeys.DATABASE_DISABLED)) {
			LOGGER.info("Checking DB and creating tables if necessary...");
			DatabaseHelper dbh = new DatabaseHelper(conf);
			dbh.createDatabaseSchema(); // New DB Layout
			dbh.populateTables();
			dbh.getConnection().close();
			LOGGER.info("DB check successful.");
		}

		if (!conf.getBooleanConfigValue(ConfigKeys.ANALYSIS_KEEP_FILES)) {
			// Add a shutdown hook which ensures cleanup if the VM exists.
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					LOGGER.debug("Running ShutdownHook: Deleting directories as -k/--keep was not requested.");
					File dir = new File(Config.getInstance()
							.getConfigValue(ConfigKeys.DIRECTORY_APPS,
									"apps"));
					FileUtils.deleteQuietly(dir);
					dir.mkdir();
					dir = new File(Config.getInstance().getConfigValue(
							ConfigKeys.DIRECTORY_BYTECODE, "bytecode"));
					FileUtils.deleteQuietly(dir);
					dir.mkdir();
					LOGGER.debug("ShutdownHook finished.");
				}
			});
		}
	}

	/**
	 * Parses the commandline and sets config parameters accordingly.
	 * 
	 * @param args
	 * @return
	 * @throws ParseException
	 */
	private static void processCommandline(String[] args)
			throws ParseException {
		CommandLineParser parser = new PosixParser();
		CommandLine cmdLine = null;
		apkPath = null;

		try {
			cmdLine = parser.parse(options, args);
		} catch (UnrecognizedOptionException e) {
			System.out.println("Found an unrecognized option( "
					+ e.getMessage()
					+ " ), the following possibilities are supported: ");
			usage();
			exit();
		}

		String[] remainingArgs = cmdLine.getArgs();

		switch (remainingArgs.length) {
		case 0:
			apkPath = null;
			break;
		case 1:
			apkPath = new File(remainingArgs[0]);
			break;
		default:
			LOGGER.error("You can't insert more than one Path!");
			for (int i = 0; i < remainingArgs.length; i++) {
				System.err.print("Unknown arguments: " + remainingArgs[i]
						+ "\n");
			}
			usage(true);
			exit();
		}

		if (cmdLine.hasOption(props.getProperty("options.version.short"))) {
			version();
			exit();
		}
		if (cmdLine.hasOption(props.getProperty("options.help.short"))) {
			usage();
			exit();
		}
		if (cmdLine.hasOption(props.getProperty("options.nobt.short"))
				&& cmdLine.hasOption(props
						.getProperty("options.noheuristic.short"))) {
			LOGGER.error("You diabled quick checks as well as program slicing, this is currently not supported.");
			// usage();
			exit();
		}
		
		parseOptions(cmdLine);
	}

	private static void exit() {
		System.exit(0);
	}

	/**
	 * Import properties from outsourced file The values for the
	 * properties/options are outsourced in a ASCII-File, so we have to import
	 * them first.
	 * 
	 * @author Tilman Bender
	 * @return commandline properties
	 */
	private static Properties parseVersionProperties() {
		InputStream in = Main.class.getClassLoader().getResourceAsStream(
				VERSION_PROPERTIES);
		Properties props = null;
		if (in != null) {
			props = new Properties();
			try {
				props.load(in);
			} catch (IOException e) {
				System.out
						.println("Could not load commandline properties. Exiting");
				exit();
			}
		} else {
			System.out
					.println("Could not load commandline properties. Exiting");
			exit();
		}
		return props;
	}

	public static Properties getProperties() {
		return props;
	}

	/**
	 * Creates the options for the appache.common.cli parser
	 * 
	 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
	 * @param props
	 */
	private static void buildOptions() {
		props = parseVersionProperties();
		if (props != null) {
			title = props.getProperty("software.name");
			// VERSION = props.getProperty("software.version");
		} else {
			System.out
					.println("Could not load commandline properties. Exiting.");
			exit();
		}
		
		basicOptions.addOption(props.getProperty("options.version.short"),
				props.getProperty("options.version.long"), false,
				props.getProperty("options.version.descr"));
		basicOptions.addOption(props.getProperty("options.help.short"),
				props.getProperty("options.help.long"), false,
				props.getProperty("options.help.descr"));
		basicOptions.addOption(props.getProperty("options.drop.short"),
				props.getProperty("options.drop.long"), false,
				props.getProperty("options.drop.descr"));
		basicOptions.addOption(props.getProperty("options.color.short"),
				props.getProperty("options.color.long"), false,
				props.getProperty("options.color.descr"));
		basicOptions.addOption(props.getProperty("options.colorinverse.short"),
				props.getProperty("options.colorinverse.long"), false,
				props.getProperty("options.colorinverse.descr"));
		basicOptions.addOption(props.getProperty("options.genjava.short"),
				props.getProperty("options.genjava.long"), false,
				props.getProperty("options.genjava.descr"));
		basicOptions.addOption(props.getProperty("options.fuzzy.short"),
				props.getProperty("options.fuzzy.long"), false,
				props.getProperty("options.fuzzy.descr"));
		// basicOptions.addOption("NgJava", "no-gJava", false,
		// "do not generate Java source code.");

		headlessOptions.addOption(props.getProperty("options.headless.short"),
				props.getProperty("options.headless.long"), false,
				props.getProperty("options.headless.descr"));
		headlessOptions.addOption(props.getProperty("options.skip.short"),
				props.getProperty("options.skip.long"), false,
				props.getProperty("options.skip.descr"));
		headlessOptions.addOption(props.getProperty("options.keep.short"),
				props.getProperty("options.keep.long"), false,
				props.getProperty("options.keep.descr"));
		headlessOptions.addOption(
				props.getProperty("options.noheuristic.short"),
				props.getProperty("options.noheuristic.long"), false,
				props.getProperty("options.noheuristic.descr"));
		headlessOptions.addOption(props.getProperty("options.nobt.short"),
				props.getProperty("options.nobt.long"), false,
				props.getProperty("options.nobt.descr"));
		headlessOptions.addOption(props.getProperty("options.cfg.short"),
				props.getProperty("options.cfg.long"), false,
				props.getProperty("options.cfg.descr"));
		headlessOptions.addOption(
				props.getProperty("options.hl.recursive.short"),
				props.getProperty("options.hl.recursive.long"), false,
				props.getProperty("options.hl.recursive.descr"));
		headlessOptions.addOption(
				props.getProperty("options.hl.filelist.short"),
				props.getProperty("options.hl.filelist.long"), false,
				props.getProperty("options.hl.filelist.descr"));
		headlessOptions.addOption(
				props.getProperty("options.hl.singlethreaded.short"),
				props.getProperty("options.hl.singlethreaded.long"), false,
				props.getProperty("options.hl.singlethreaded.descr"));

		// option for running as daemon that watches a folder
		headlessOptions.addOption(props.getProperty("options.daemon.short"),
				props.getProperty("options.daemon.long"), true,
				props.getProperty("options.daemon.descr"));
		// TODO does not work yet
		// headlessOptions.addOption("sc", false, "do Similarity Check");
		// headlessOptions.addOption("Nsc", "no-sc", false,
		// "do not do Similarity Check");

		// TODO: Check if ignore-errors is still used
		// headlessOptions.addOption("ie", "ignore-errors", false,
		// "do not stop, if an analysis of an apk has crashed");
		// TODO: @Hanno: Check if this still works!
		// headlessOptions.addOption("doa", "del-old-analyses", false,
		// "delete all existing analyses in the database, before making a new one.");

		reportDbLogOptions.addOption(props.getProperty("options.nodb.short"),
				props.getProperty("options.nodb.long"), false,
				props.getProperty("options.nodb.descr"));
		reportDbLogOptions.addOption(props.getProperty("options.report.short"),
				props.getProperty("options.report.long"), true,
				props.getProperty("options.report.descr"));
		reportDbLogOptions.addOption(
				props.getProperty("options.rtemplate.short"),
				props.getProperty("options.rtemplate.long"), true,
				props.getProperty("options.rtemplate.descr"));
		reportDbLogOptions.addOption(props.getProperty("options.log.short"),
				props.getProperty("options.log.long"), true,
				props.getProperty("options.log.descr"));
		reportDbLogOptions.getOption(props.getProperty("options.log.short"))
				.setArgName("file");

		guiOptions.addOption(props.getProperty("options.gui.short"),
				props.getProperty("options.gui.long"), false,
				props.getProperty("options.gui.descr"));

		// include all sub-options in "options"
		for (Object option : basicOptions.getOptions()) {
			options.addOption((Option) option);
		}
		for (Object option : headlessOptions.getOptions()) {
			options.addOption((Option) option);
		}
		for (Object option : guiOptions.getOptions()) {
			options.addOption((Option) option);
		}
		for (Object option : reportDbLogOptions.getOptions()) {
			options.addOption((Option) option);
		}
	}

	/**
	 * Prints the usage/help message.
	 * 
	 * @author Hanno Lemoine <Hanno.Lemoine@gdata.de> Thanks to Ben Gruver
	 *         (JesusFreke)
	 */
	private static void usage(boolean printHeadlessGuiOptions) {
		SmaliHelpFormatter formatter = new SmaliHelpFormatter();
		int consoleWidth = ConsoleUtil.getConsoleWidth();
		formatter.setWidth(consoleWidth);

		PrintWriter writer = new PrintWriter(System.out);

		writer.write("SAAF  Copyright (C) 2013  syssec.rub.de\n");
		writer.write("This program comes with ABSOLUTELY NO WARRANTY.\n");
		writer.write("This is free software, and you are welcome to redistribute it\n");
		writer.write("under certain conditions.");

		writer.write("\n\n#########################################\n");
		writer.write("# SAAF: A static analyzer for APK files #\n");
		writer.write("#########################################\n");
		writer.write("\nUsage: java -jar saaf.jar [options] [file/directory]");
		writer.write("\nIf no options are set, SAAF will start in GUI mode.\n");

		writer.write("\nBasic Options:\n");
		formatter.printOptions(writer, consoleWidth, basicOptions, 1, 3);

		if (printHeadlessGuiOptions) {
			writer.write("\nHeadless Options:\n");
			formatter.printOptions(writer, consoleWidth, headlessOptions, 1, 3);

			writer.write("\nGUI Options:\n");
			formatter.printOptions(writer, consoleWidth, guiOptions, 1, 3);

			writer.write("\nReport, DB and Log Options:\n");
			formatter.printOptions(writer, consoleWidth, reportDbLogOptions, 1,
					3);
		}
		writer.flush();
		// Do not close writer, otherwise System.out would be dead.
	}

	private static void usage() {
		usage(true);
	}

	/**
	 * Prints the version message.
	 */
	private static void version() {
		System.out.println(props.getProperty("software.name") + " "
				+ props.getProperty("software.version")
				+ props.getProperty("software.descr"));
		exit();
	}

	private static void parseOptions(CommandLine cmdLine) {
		Config conf = Config.getInstance();

		if (cmdLine.hasOption(props.getProperty("options.headless.short"))) {
			conf.setBooleanConfigValue(ConfigKeys.ANALYSIS_IS_HEADLESS, true);
		}

		if (cmdLine.hasOption(props.getProperty("options.color.long"))) {
			conf.setBooleanConfigValue(ConfigKeys.LOGGING_USE_COLOR, true);
			conf.setBooleanConfigValue(ConfigKeys.LOGGING_USE_INVERSE_COLOR,
					false);
		}

		if (cmdLine.hasOption(props.getProperty("options.colorinverse.short"))) {
			conf.setBooleanConfigValue(ConfigKeys.LOGGING_USE_COLOR, true);
			conf.setBooleanConfigValue(ConfigKeys.LOGGING_USE_INVERSE_COLOR,
					true);
		}

		updateLog4jConfiguration(
				conf.getBooleanConfigValue(ConfigKeys.LOGGING_USE_COLOR),
				conf.getBooleanConfigValue(ConfigKeys.LOGGING_USE_INVERSE_COLOR));

		if (cmdLine.hasOption(props.getProperty("options.genjava.short"))) {
			conf.setBooleanConfigValue(ConfigKeys.ANALYSIS_GENERATE_JAVA, true);
		}

		if (cmdLine.hasOption(props.getProperty("options.drop.short"))) {
			conf.setBooleanConfigValue(ConfigKeys.ANALYSIS_DROP_DB_AND_FILES,
					true);
		}

		if (cmdLine.hasOption(props.getProperty("options.skip.short"))) {
			conf.setBooleanConfigValue(ConfigKeys.ANALYSIS_SKIP_KNOWN_APP, true);
		}

		if (cmdLine.hasOption(props.getProperty("options.hl.recursive.short"))) {
			conf.setBooleanConfigValue(ConfigKeys.RECURSIVE_DIR_ANALYSIS, true);
		}

		if (cmdLine.hasOption(props.getProperty("options.hl.filelist.short"))) {
			conf.setBooleanConfigValue(ConfigKeys.USE_FILE_LIST, true);
		}

		if (cmdLine.hasOption(props
				.getProperty("options.hl.singlethreaded.short"))) {
			conf.setBooleanConfigValue(ConfigKeys.MULTITHREADING_ENABLED, false);

		}

		if (cmdLine.hasOption(props.getProperty("options.nodb.short"))) {
			conf.setBooleanConfigValue(ConfigKeys.DATABASE_DISABLED, true);
		}
		// feature #35: run without database
		if (conf.getBooleanConfigValue(ConfigKeys.DATABASE_DISABLED)) {
			// disable database backend
			conf.setBooleanConfigValue(ConfigKeys.DATABASE_DISABLED, true);
			// if we are headless (i.e. no other way to see the results
			if (cmdLine.hasOption(props.getProperty("options.headless.short"))) {
				// automatically turn on reporting
				conf.setBooleanConfigValue(ConfigKeys.ANALYSIS_GENERATE_REPORT,
						true);
			}
		}

		if (cmdLine.hasOption(props.getProperty("options.gui.short"))
				&& cmdLine.hasOption(props
						.getProperty("options.headless.short"))) {
			LOGGER.error("You have to decide if GUI or HEADLESS mode. Both is "
					+ "not possible!");
			exit();
		}
		
		if (cmdLine.hasOption(props.getProperty("options.daemon.short"))) {
			conf.setBooleanConfigValue(ConfigKeys.ANALYSIS_IS_HEADLESS, true);
			conf.setBooleanConfigValue(ConfigKeys.DAEMON_ENABLED, true);
			String watched = cmdLine.getOptionValue(props
					.getProperty("options.daemon.short"));
			conf.setConfigValue(ConfigKeys.DAEMON_DIRECTORY, watched);

		}

		if (cmdLine.hasOption(props.getProperty("options.headless.short"))) {
			conf.setBooleanConfigValue(ConfigKeys.ANALYSIS_IS_HEADLESS, true);
		}

		if (cmdLine.getOptions().length <= 0
				|| cmdLine.hasOption(props.getProperty("options.gui.short"))) {
			conf.setBooleanConfigValue(ConfigKeys.ANALYSIS_IS_HEADLESS, false);
		}

		// What to analyze?
		if (cmdLine.hasOption(props.getProperty("options.noheuristic.short"))) {
			conf.setBooleanConfigValue(ConfigKeys.ANALYSIS_DO_HEURISTIC, false);
		}
		if (cmdLine.hasOption(props.getProperty("options.nobt.short"))) {
			conf.setBooleanConfigValue(ConfigKeys.ANALYSIS_DO_BACKTRACK, false);
		}
		// if (cmdLine.hasOption("no-sc"))
		// Config.DO_SIMILARITY = false;
		// if (cmdLine.hasOption("sc"))
		// Config.DO_SIMILARITY = true;
		// How to analyze?
		// if (cmdLine.hasOption("ignore-errors"))
		// Config.QUIT_ON_ERROR = false;
		// if (cmdLine.hasOption("skip-known-apps"))
		// Config.SKIP_KNOWN_APP = true;
		// if (cmdLine.hasOption("del-old-analyses"))
		// Config.HOLD_ONLY_ONE_ANA_PER_APP = true;
		if (cmdLine.hasOption(props.getProperty("options.report.short"))) {
			conf.setBooleanConfigValue(ConfigKeys.ANALYSIS_GENERATE_REPORT,
					true);
			String reportPath = cmdLine.getOptionValue(props
					.getProperty("options.report.short"));
			conf.setConfigValue(ConfigKeys.DIRECTORY_REPORTS, reportPath);
		}
		if (cmdLine.hasOption(props.getProperty("options.rtemplate.short"))) {
			String templateName = cmdLine.getOptionValue(props
					.getProperty("options.rtemplate.short"));
			conf.setConfigValue(ConfigKeys.REPORTING_TEMPLATE_GROUP_DEFAULT,
					templateName);
		}

		if (cmdLine.hasOption(props.getProperty("options.log.short"))) {
			conf.setBooleanConfigValue(ConfigKeys.LOGGING_CREATE_SEPERATE, true);
			String logpath = cmdLine.getOptionValue(props
					.getProperty("options.log.short"));
			conf.setConfigValue(ConfigKeys.LOGGING_FILE_PATH, logpath);

		}

		if (cmdLine.hasOption(props.getProperty("options.cfg.short"))) {
			conf.setBooleanConfigValue(ConfigKeys.ANALYSIS_GENERATE_CFG, true);
		}
		if (cmdLine.hasOption(props.getProperty("options.fuzzy.short"))) {
			conf.setBooleanConfigValue(ConfigKeys.ANALYSIS_GENERATE_FUZZYHASH,
					true);
		}
		if (cmdLine.hasOption(props.getProperty("options.keep.short"))) {
			conf.setBooleanConfigValue(ConfigKeys.ANALYSIS_KEEP_FILES, true);
		}


	}

	/**
	 * Load or update log4j properties from 'conf/log4j.properties' file. If
	 * useColor is set, the Appender Layout is overwritten with the
	 * ANSIColorLayout.
	 * 
	 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
	 * @param useColor
	 *            Boolean to activate ANSIColorLayout.
	 * @param inverseColor
	 *            set if you have a white background
	 */
	private static void updateLog4jConfiguration(Boolean useColor,
			Boolean inverseColor) {
		Properties log4j_props = new Properties();
		try {
			log4j_props.load(new FileInputStream("conf/log4j.properties"));
		} catch (IOException e) {
			System.err
					.println("Error: Cannot laod log4j configuration file. Exiting.");
			exit();
		}
		if (useColor) {
			log4j_props.setProperty("log4j.appender.A1.layout",
					"de.rub.syssec.saaf.misc.log4j.ANSIColorLayout");
		}
		if (useColor && inverseColor) {
			// final String cWhite = "\u001B[1;37m";
			final String cBlack = "\u001B[0;30m";
			final String cGray = "\u001B[1;30m";
			final String cRed = "\u001B[0;31m";
			final String cYel = "\u001B[1;33m";
			final String cCya = "\u001B[0;36m";
			log4j_props.setProperty("log4j.appender.A1.layout.all", cBlack);
			log4j_props.setProperty("log4j.appender.A1.layout.fatal", cRed);
			log4j_props.setProperty("log4j.appender.A1.layout.error", cRed);
			log4j_props.setProperty("log4j.appender.A1.layout.warn", cYel);
			log4j_props.setProperty("log4j.appender.A1.layout.info", cGray);
			log4j_props.setProperty("log4j.appender.A1.layout.debug", cCya);
			log4j_props
					.setProperty("log4j.appender.A1.layout.stacktrace", cRed);
			log4j_props.setProperty("log4j.appender.A1.layout.defaultcolor",
					cBlack);
		}
		LogManager.resetConfiguration();
		PropertyConfigurator.configure(log4j_props);
	}
}
