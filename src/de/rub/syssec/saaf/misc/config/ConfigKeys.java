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

/**
 * Specifies keys used to query the Config Class.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public enum ConfigKeys {

	/**
	 * The directory where SAAF will look for subdirectories like DIRECTORY_APPS
	 * etc.
	 */
	DIRECTORY_HOME("directory.home"),
	/**
	 * Retrieve the name of the directory where we store the actual apps (apks).
	 * Default is "apps".
	 * 
	 */
	DIRECTORY_APPS("subdir.apps", "apps"),
	/**
	 * Retrieve the name of the folder where we create directories for analyzed
	 * apps. Default is "bytecode"
	 */
	DIRECTORY_BYTECODE("subdir.analyzed", "bytecode"),
	/**
	 * The name of the folder where the unpacked content of the APK is stored.
	 * Default is "apk_content".
	 */
	DIRECTORY_APK_CONTENT("subdir.apk.content", "apk_content"),
	/**
	 * The name of the folder where the decompiled content (smali,java) is
	 * stored. Default is "bytecode". This is NOT the same as DIRECTORY_BYTECODE
	 * but a subdir of an application specific folder within DIRECTORY_BYTECODE
	 * e.g:
	 * 
	 * <ul>
	 * <li>DIRECTORY_BYTECODE</li>
	 * <li>
	 * <ul>
	 * <li>apkname_hash</li>
	 * <li>
	 * <ul>
	 * <li>DIRECTORY_APK_CONTENT</li>
	 * <li>DIRECTORY_DECOMPILED_CONTENT</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * </li>
	 * </ul>
	 */
	DIRECTORY_DECOMPILED_CONTENT("subdir.decompiled", "bytecode"),
	/**
	 * Retrieve the name of the folder where reports are written if no other
	 * path is specified. Default is "reports"
	 */
	DIRECTORY_REPORTS("reporting.output.folder", "reports"),
	/**
	 * Retrieve the name of the folder where we look for templates. Default is
	 * "templates".
	 */
	DIRECTORY_REPORT_TEMPLATES("reporting.templates.folder", "templates"),
	/**
	 * Retrieve the name of the folder where we write the cfg files. Default is
	 * "cfgs".
	 */
	DIRECTORY_CFGS("subdir.cfgs", "cfgs"),

	/**
	 * Retrieve whether we should do heuristic analysis. Default is true.
	 */
	ANALYSIS_DO_HEURISTIC("analysis.heuristic.enable", true),
	/**
	 * Retrieve whether we should do backtracking analysis. Default is true.
	 */
	ANALYSIS_DO_BACKTRACK("analysis.backtrack.enable", true),
	/**
	 * Retrieve whether we should do similarity checks for analyzed applications
	 * Default is false.
	 */
	ANALSIS_DO_SIMILARITY("analysis.similarity.enable", false),
	/**
	 * Should we drop the database and clean the directories before starting to
	 * analyze Default is false;
	 */
	ANALYSIS_DROP_DB_AND_FILES("analysis.drop", false),
	/**
	 * Retrieve whether we should remove the unpacked/analyzed content on
	 * completion. Default is true.
	 */
	ANALYSIS_KEEP_FILES("analysis.keep.files", false),
	/**
	 * Retrieve whether we should try to decompile into java code. Default is
	 * false.
	 */
	ANALYSIS_GENERATE_JAVA("analysis.java.enable", false),
	/**
	 * Retrieve whether we should generate file with control-flow-graphs.
	 * Default is false.
	 */
	ANALYSIS_GENERATE_CFG("analysis.flowgraphs.enable", false),
	/**
	 * Retrieve whether we should generate a report for the analysis. The
	 * default is false.
	 */
	ANALYSIS_GENERATE_REPORT("analysis.reporting.enable", false),
	/**
	 * Retrieve whether we should generate fuzzy hashes for analyzed files.
	 * Default is false. TODO: Which files? apks? smali? all?
	 */
	ANALYSIS_GENERATE_FUZZYHASH("analysis.fuzzyhashing.enable", false),

	/**
	 * Retrieve whether we should consider the manifest-based patterns when
	 * doing heuristic analysis. Default is true.
	 */
	HEURISTIC_PATTERN_MANIFEST("heuristic.patterns.manifest.enable", true),
	/**
	 * Retrieve whether we should consider the invocation-based patterns when
	 * doing heuristic analysis
	 */
	HEURISTIC_PATTERN_INVOKE("heuristic.patterns.invoke.enable", true),
	/**
	 * Retrieve whether we should consider the smali-based patterns when doing
	 * heuristic analysis
	 */
	HEURISTIC_PATTERN_SMALI("heuristic.patterns.smali.enable", true),
	/**
	 * Retrieve whether we should consider the invocation-based patterns when
	 * doing heuristic analysis
	 */
	HEURISTIC_PATTERN_METHOD_MOD("heuristic.patterns.method.enable", true),
	/**
	 * Retrieve whether we should consider the superclass-based patterns when
	 * doing heuristic analysis
	 */
	HEURISTIC_PATTERN_SUPERCLASS("heuristic.patterns.superclass.enable", true),
	/**
	 * Retrieve whether we should consider the patterns to detect patched code
	 * when doing heuristic analysis
	 */
	HEURISTIC_SEARCH_PATCHED_CODE("heuristic.patterns.patched.enable", true),

	/**
	 * Retrieve whether we should consider the cryptoblock-based patterns when
	 * doing heuristic analysis
	 */
	HEURISTIC_PATTERN_CRYPTOBLOCK("heuristic.patterns.crypto.enable", true),
	/**
	 * Retrieve whether the analysis should run without gui Default is false.
	 */
	ANALYSIS_IS_HEADLESS("analysis.headless", false),
	/**
	 * Retrieve whether we should quit analyzing as soon as an error occurs.
	 * Default is false.
	 */
	ANALYSIS_QUIT_ON_ERROR("analysis.errorhandling.quit", false),
	/**
	 * Retrieve whether we should skip applications that have been analyzed
	 * before. Default is false.
	 */
	ANALYSIS_SKIP_KNOWN_APP("analysis.skip", false),
	/**
	 * Retrieve whether we should actually save the analysis result
	 * TODO: Is ANALYSIS_SAVE_PERSISTENT still in use?
	 */
	ANALYSIS_SAVE_PERSISTENT("analysis.save"),
	/**
	 * Retrieve whether we should keep only one analysis result per application.
	 * The default is false.
	 */
	ANALYSIS_KEEP_ONLY_ONE("analysis.exactlyone", false),
	/**
	 * Retrieve whether analysis steps should include packages/compilation units
	 * contained in ad-frameworks. Default is true.
	 */
	ANALYSIS_INCLUDE_AD_FRAMEWORKS("analysis.include.adpackages",true),

	/**
	 * Retrieve the name of the report template to use. The default is "xml.stg"
	 */
	REPORT_TEMPLATE("reporting.templates.template.default", "report"),
	/**
	 * Retrieve the template group for reporting (only needed with
	 * STReportGenerator)
	 */
	REPORTING_TEMPLATE_GROUP_DEFAULT("reporting.templates.group.default",
			"xml.stg"),
	/**
	 * Retrieve the naming scheme for a report
	 */
	REPORT_NAME_PATTERN("reporting.output.naming.pattern"),
	/**
	 * Generate control-flow graphs for methods that belong to advertising
	 * frameworks. Default is true.
	 */
	CFGS_INCLUDE_AD_FRAMEWORKS("cfg.include.ads",true),

	/**
	 * Retrieve whether we should log to a separate file for the analyzed
	 * binary. Default is false.
	 */
	LOGGING_CREATE_SEPERATE("logging.separate", false),
	/**
	 * Retrieve where to write the specific logs
	 */
	LOGGING_FILE_PATH("logging.logfolder","logs"),
	/**
	 * Retrieve whether to use coloring in log output. Default is false.
	 */
	LOGGING_USE_COLOR("logging.color", false),
	/**
	 * Should we use an inverted color scheme (for white terminal backgrounds).
	 * Default is false
	 */
	LOGGING_USE_INVERSE_COLOR("logging.color.inverted",false),
	/**
	 * Are we descending into sub-directories. Default is false.
	 */
	RECURSIVE_DIR_ANALYSIS("analysis.recursive", false),
	/**
	 * Indicates we were given a file containing the paths of apks to analyze.
	 * Check commandline options. Default is false.
	 */
	USE_FILE_LIST("analysis.use.list",false),
	/**
	 * A prefix that is appended to all files in a file-list.
	 */
	FILE_LIST_PREFIX("path_fileList_prefix"),
	
	/**
	 * Should the analysis be multithreaded. Default is true.
	 */
	MULTITHREADING_ENABLED("multithreading.enable", true),
	/**
	 * How many threads should be used
	 */
	MULTITHREADING_THREADS("multithreading.threads"),
	/**
	 * The data-source that provides the permissions. Default is "conf/permissions.xml"
	 */
	DATASOURCE_PERMISSIONS("datasource.permissions","conf/permissions.xml"),
	/**
	 * The file that provides the schema to validate the slicing patterns
	 * Default: "conf/schema/permissions.xsd"
	 */
	DATASOURCE_SCHEMA_PERMISSIONS("datasource.schema.permissions","conf/schema/permissions.xsd"),
	/**
	 * The data-source that provides the patterns for program slicing
	 * Default: "conf/backtracking-patterns.xml"
	 */
	DATASOURCE_SLICING_PATTERNS("datasource.patterns.backtracking","conf/backtracking-patterns.xml"),
	/**
	 * The file that provides the schema to validate the slicing patterns.
	 * Default: "conf/schema/backtracking-patterns.xsd"
	 */
	DATASOURCE_SCHEMA_SLICING("datasource.schema.backtracking","conf/schema/backtracking-patterns.xsd"),
	/**
	 * The data-source that provides the patterns for quick checks
	 * Default: "conf/heuristic-patterns.xml"
	 */
	DATASOURCE_HEURISTIC_PATTERNS("datasource.patterns.heuristic","conf/heuristic-patterns.xml"),
	/**
	 * The file that provides the schema to validate the heuristic patterns
	 * Default: "conf/schema/heuristic-patterns.xsd"
	 */
	DATASOURCE_SCHEMA_HEURISTIC_PATTERNS("datasource.schema.heuristic","conf/schema/heuristic-patterns.xsd"),
	/**
	 * The file that provides the ad-network definitions
	 * Default:"conf/AdNetworks.xml"
	 */
	DATASOURCE_AD_NETWORKS("datasource.adnetworks","conf/AdNetworks.xml"),
	/**
	 * The file that provides the schema to validate the ad-network definitions
	 * Default: "conf/schema/AdNetworks.xsd"
	 */
	DATASOURCE_SCHEMA_AD_NETWORKS("datasource.schema.adnetworks","conf/schema/AdNetworks.xsd"),
	/**
	 * The driver to use when connecting to the database
	 * Default: "com.mysql.jdbc.Driver"
	 */
	DATABASE_DRIVER("db.driver","com.mysql.jdbc.Driver"),
	/**
	 * Database should not be used at all. Default is false.
	 * Default: false
	 */
	DATABASE_DISABLED("db.disable", false),
	/**
	 * The connection-string to connect to the database
	 * Default: "jdbc:mysql://localhost/saaf"
	 */
	DATABASE_CONNECTION_STRING("db.connection.string","jdbc:mysql://localhost/saaf"),
	/**
	 * The username to use when connecting to the database
	 * Default: "saafuser"
	 */
	DATABASE_USER("db.user","saafuser"),
	/**
	 * The password to use when connecting to the database
	 * Default: "saafpass"
	 */
	DATABASE_PASSWORD("db.password","saafpass"), 
	/**
	 * The string used to launch the image viewer e.g. "eog %f"
	 * Default: ""
	 */
	VIEWER_IMAGES("viewer.images"),
	
	/**
	 * The string used to launch the report viewer e.g. "firefox %f"
	 */
	VIEWER_REPORTS("viewer.reports"),
	/**
	 * The path of the dot program that is used to generate control-flow graphs
	 */
	EXECUTABLE_DOT("external.dot","/usr/bin/dot"),
	/**
	 * The path of the jad program that is used to decompile the application into .java source code.
	 */
	EXECUTABLE_JAD("external.jad",""),
	/**
	 * The path of the ssdeep program taht is used to generate fuzzy hashes
	 */
	EXECUTABLE_SSDEEP("external.ssdeep","/usr/bin/ssdeep"), 
	/**
	 * What to do with framework files from an apktool.
	 * See ApkDecoderInterface.Treatment. Default is "DONT_TOUCH"
	 */
	APKTOOL_TREATMENT("apktool.frameworks","DONT_TOUCH"),
	/**
	 * Signals that SAAF is running as a daemon.
	 */
	DAEMON_ENABLED("daemon.enabled",false),
	/**
	 * The value in milliseconds that SAAF will wait when polling for new APKs
	 */
	DAEMON_POLLING_INTERVAL("daemon.polling.interval"),
	/**
	 * The directory that SAAF will watch for new apks
	 */
	DAEMON_DIRECTORY("daemon.polling.directory","incoming");
	
	private String name;
	public String defaultString;
	public boolean defaultBoolean;

	private ConfigKeys(String name) {
		this.name = name;
	}

	private ConfigKeys(String name, String defaultValue) {
		this.name = name;
		this.defaultString = defaultValue;
	}

	private ConfigKeys(String name, boolean defaultValue) {
		this.name = name;
		this.defaultBoolean = defaultValue;
	}

	@Override
	public String toString() {
		return name;
	}

	public static ConfigKeys fromString(String s)
			throws IllegalArgumentException {
		for (ConfigKeys candidate : ConfigKeys.values()) {
			if (s.equals(candidate.name)) {
				return candidate;
			}
		}
		// none of the ConfigKeys matched so the string is incorrect
		if (true) {
			throw new IllegalArgumentException("The string " + s
					+ " is not a valid configuration key");
		}
		return null;
	}
}
