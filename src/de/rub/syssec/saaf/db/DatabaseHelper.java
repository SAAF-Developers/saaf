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
package de.rub.syssec.saaf.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.db.dao.exceptions.DuplicateEntityException;
import de.rub.syssec.saaf.db.datasources.DataSourceException;
import de.rub.syssec.saaf.db.datasources.Datasource;
import de.rub.syssec.saaf.db.persistence.exceptions.InvalidEntityException;
import de.rub.syssec.saaf.db.persistence.exceptions.PersistenceException;
import de.rub.syssec.saaf.db.persistence.interfaces.EntityManagerFacade;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.analysis.BTPatternInterface;
import de.rub.syssec.saaf.model.analysis.HPatternInterface;
import de.rub.syssec.saaf.model.application.PermissionInterface;

/**
 * this class provides methods to create and destroy tables in the DB
 * FIXME: error msgs
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
 */
public class DatabaseHelper {

	private Datasource<BTPatternInterface> btPatternSrc;
	private Datasource<HPatternInterface>  hPatternSrc ;
	private Datasource<PermissionInterface> permissionSrc;

	private Logger logger = Logger.getLogger(DatabaseHelper.class);

	private Connection connection = null;

	public DatabaseHelper(Config conf) throws PersistenceException {
		this.hPatternSrc = conf.getHTPatternSource();
		this.btPatternSrc = conf.getBTPatternSource();
		this.permissionSrc = conf.getPermissionSource();
		String driver=conf.getConfigValue(ConfigKeys.DATABASE_DRIVER);
		String connectString=conf.getConfigValue(ConfigKeys.DATABASE_CONNECTION_STRING);
		String username=conf.getConfigValue(ConfigKeys.DATABASE_USER);
		String password=conf.getConfigValue(ConfigKeys.DATABASE_PASSWORD);
		try {
			Class.forName(driver);
			// create a database connection
			 connection = DriverManager.getConnection(connectString, username,
					password);
		} catch (ClassNotFoundException e) {
			throw new PersistenceException(e);
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}		
	}

	// ############ create sets #########################
	/**
	 * creates all necessary tables for a regular start
	 * 
	 * @throws SQLException
	 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
	 */
	public void createDatabaseSchema() throws SQLException {

		// Database.getInstance().setAutoCommit(false);
		connection.setAutoCommit(false);
		try {
			createApkTable();
			createAnalysesTable();
			createPackagesTable();
			createClassesTable();
			createMethodsTable();
			createHeuristicPatternTable();
			createBackTrackPatternTable();
			createHeuristicResultsTable();
			createBackTrackResultsTable();
			createPermissionTables();
			createAdsTable();
			createErrorTable();

			// CreateViews
			createBTcountView();
			createApkView();
			createAnalyseView();
			createBTView();
			createHView();
			createFailedView();

			// CreateIndices
			createResultIndices();
		} catch (SQLException e) {
			connection.rollback();
			connection.setAutoCommit(true);			
			throw e;
		}

		connection.commit();
	}

	/**
	 * Fill some of the tables with necessary sample data.
	 * 
	 * @throws SQLException
	 * @throws DAOException
	 * @throws DuplicateEntityException
	 * @throws InvalidEntityException 
	 * @throws PersistenceException 
	 * @throws DataSourceException 
	 */
	public void populateTables() throws PersistenceException, InvalidEntityException, DataSourceException {

		logger.debug("Populating backtracking pattern table from datasource: "+btPatternSrc);
		EntityManagerFacade facade = Config.getInstance().getEntityManager();
		List<BTPatternInterface> btpatterns = new ArrayList<BTPatternInterface>(this.btPatternSrc.getData());
		if(btpatterns.isEmpty()){
			logger.warn("Datasource returned 0 backtracking-patterns.");
		}else{
			facade.getBtPatternManager().saveAll(btpatterns);
		}	
		logger.debug("finished populating backtracking pattern table");

		logger.debug("Populating heuristic pattern table from datasource: "+hPatternSrc);
		List<HPatternInterface> hpatterns = new ArrayList<HPatternInterface>(this.hPatternSrc.getData());
		if(hpatterns.isEmpty()){
			logger.warn("Datasource returned 0 heuristic-patterns.");
		}else{
			facade.gethPatternManager().saveAll(hpatterns);
		}
		logger.debug("finished populating heuristic pattern table");
		
		logger.debug("Populating heuristic permissions table from datasource: "+permissionSrc);
		List<PermissionInterface> permissions = new ArrayList<PermissionInterface>(this.permissionSrc.getData());
		if(permissions.isEmpty()){
			logger.warn("Datasource returned 0 permissions.");
		}else{
			facade.getPermissionManager().saveAll(permissions);
		}
		logger.debug("Finished populating permissions table");

		try {
			connection.commit();
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}		
	}

	// ############ New Layout #########################

	/**
	 * creates the table for error messages during analysis
	 * 
	 * @throws SQLException
	 */
	public void createErrorTable() throws SQLException {
		// Statement statement = Database.getInstance().getNewStatement();
		Statement statement = connection.createStatement();
		statement
				.executeUpdate("CREATE TABLE IF NOT EXISTS error_messages "
						+ "(id INTEGER PRIMARY KEY AUTO_INCREMENT,"
						+ "id_analyses INTEGER NOT NULL,"
						+ "error_message text,"
						+ "FOREIGN KEY(id_analyses) REFERENCES analyses(id) ON UPDATE CASCADE ON DELETE CASCADE"
						+ ")ENGINE=INNODB;");
	}

	/**
	 * creates the table for the application
	 * 
	 * @throws SQLException
	 */
	public void createApkTable() throws SQLException {
		Statement statement = connection.createStatement();
		statement.executeUpdate("CREATE TABLE IF NOT EXISTS apk_files "
				+ "(id INTEGER PRIMARY KEY AUTO_INCREMENT,"
				+ "codelines INTEGER,"
				+ "classes INTEGER,"
				+ "man_minSDK INTEGER,"
				+ "man_versionCode INTEGER,"
				+ "man_versionName VARCHAR(255),"
				+ "man_activities INTEGER,"
				+ "man_receivers INTEGER,"
				+ "man_services INTEGER,"
				+ "cert_hash CHAR(40),"
				+ "cert_date_start DATETIME,"
				+ "cert_date_stop DATETIME,"
				+ "hash_md5 CHAR(32) UNIQUE,"
				+ "hash_sha1 CHAR(40) UNIQUE,"
				+ "hash_sha256 CHAR(64) UNIQUE,"
				+ "hash_fuzzy VARCHAR(127),"
				+ "file_name VARCHAR(255) NOT NULL,"
				+ "man_package VARCHAR(255),"
				+ "man_appLabel VARCHAR(255),"
				+ "man_appLabelResolved VARCHAR(255),"
				+ "man_appDebuggable BOOLEAN,"
				+ "cert_author VARCHAR(255)"
				+ ") ENGINE = INNODB;");
	}

	public void createAnalysesTable() throws SQLException {
		// Statement statement = Database.getInstance().getNewStatement();
		Statement statement = connection.createStatement();
		statement
				.executeUpdate("CREATE TABLE IF NOT EXISTS analyses "
						+ "(id INTEGER PRIMARY KEY AUTO_INCREMENT,"
						+ "id_apk INTEGER NOT NULL,"
						+ "analysis_status ENUM('NOT_STARTED','RUNNING','FINISHED','FAILED','FINISHED_WITH_EXCEPTION','SKIPPED') NOT NULL,"
						// + "enum_analysis_status INTEGER DEFAULT 0,"
						// FIXME: Change format DATETIME to INTEGER, and check
						// mySQL
						+ "analysis_created DATETIME,"
						+ "analysis_start DATETIME,"
						+ "analysis_stop DATETIME,"
						+ "heuristic_result INTEGER,"
						+ "FOREIGN KEY(id_apk) REFERENCES apk_files(id) ON UPDATE CASCADE ON DELETE CASCADE"
						+ ") ENGINE = INNODB;");
	}

	public void createPackagesTable() throws SQLException {
		// Statement statement = Database.getInstance().getNewStatement();
		Statement statement = connection.createStatement();
		statement
				.executeUpdate("CREATE TABLE IF NOT EXISTS packages "
						+ "(id INTEGER PRIMARY KEY AUTO_INCREMENT,"
						+ "id_apk INTEGER NOT NULL,"
						+ "hash_fuzzy VARCHAR(127),"
						+ "name VARCHAR(255) NOT NULL, "
						+ "FOREIGN KEY(id_apk) REFERENCES apk_files(id) ON UPDATE CASCADE ON DELETE CASCADE,"
						+ "UNIQUE KEY id_apk_name (id_apk,name)"
						+ ") ENGINE = INNODB;");
	}

	public void createClassesTable() throws SQLException {
		// Statement statement = Database.getInstance().getNewStatement();
		Statement statement = connection.createStatement();
		statement
				.executeUpdate("CREATE TABLE IF NOT EXISTS classes "
						+ "(id INTEGER PRIMARY KEY AUTO_INCREMENT,"
						+ "id_packages INTEGER NOT NULL,"
						+ "codelines INTEGER,"
						+ "hash_fuzzy VARCHAR(127),"
						+ "name VARCHAR(127),"
						+ "source VARCHAR(127),"
						+ "extends VARCHAR(127),"
						+ "implements text,"
						+ "entropy DOUBLE,"
						+ "FOREIGN KEY(id_packages) REFERENCES packages(id) ON UPDATE CASCADE ON DELETE CASCADE,"
						+ "UNIQUE KEY packge_sha1_fuzzy_name (id_packages,hash_fuzzy,name)"
						+ ") ENGINE = INNODB;");
//		statement
//				.executeUpdate("ALTER TABLE classes ADD CONSTRAINT packge_sha1_fuzzy_name UNIQUE (id_packages,hash_fuzzy,name)");
	}

	public void createMethodsTable() throws SQLException {
		// Statement statement = Database.getInstance().getNewStatement();
		Statement statement = connection.createStatement();
		statement
				.executeUpdate("CREATE TABLE IF NOT EXISTS methods "
						+ "(id INTEGER PRIMARY KEY AUTO_INCREMENT,"
						+ "id_classes INTEGER NOT NULL,"
						+ "in_line INTEGER,"
						+ "codelines INTEGER,"
						+ "arithmetic_fraction DOUBLE," // This is the part for
														// the crypto find, by
														// Felix Gröbert, makes
														// only sense for a
														// BB(TODO).
						+ "hash_fuzzy VARCHAR(127),"
						+ "name VARCHAR(255),"
						+ "parameters VARCHAR(255),"
						+ "return_value VARCHAR(255),"
						+ "path_to_cfg text,"
						+ "entropy DOUBLE,"
						+ "FOREIGN KEY(id_classes) REFERENCES classes(id) ON UPDATE CASCADE ON DELETE CASCADE,"
						+ "UNIQUE KEY class_name_params (id_classes,name,parameters,return_value)"
						+ ") ENGINE = INNODB;");
//		statement
//				.executeUpdate("ALTER TABLE methods ADD CONSTRAINT class_sha1_fuzzy_name UNIQUE (id_classes,name)");
	}

	public void createHeuristicPatternTable() throws SQLException {
		// Statement statement = Database.getInstance().getNewStatement();

		Statement statement = connection.createStatement();
		statement
				.executeUpdate("CREATE TABLE IF NOT EXISTS heuristic_pattern "
						+ "(id INTEGER PRIMARY KEY AUTO_INCREMENT,"
						+ "enum_searchin ENUM('MANIFEST', 'INVOKE', 'SMALI', 'METHOD_MOD', 'SUPERCLASS', 'PATCHED_CODE') NOT NULL,"
						+ "heuristic_value INTEGER NOT NULL,"
						+ "pattern VARCHAR(255) NOT NULL,"
						+ "description VARCHAR(255),"
						+ "active BOOL NOT NULL,"
						+ "UNIQUE KEY pattern_searchin_hval_desc (pattern,enum_searchin,heuristic_value,description) "
						+ ") ENGINE = INNODB; ");
//		statement
//				.executeUpdate("ALTER TABLE heuristic_pattern ADD CONSTRAINT pattern_searchin_hval_desc UNIQUE (pattern,enum_searchin,heuristic_value,description)");
	}

	public void createBackTrackPatternTable() throws SQLException {
		// Statement statement = Database.getInstance().getNewStatement();
		Statement statement = connection.createStatement();
		statement.executeUpdate("CREATE TABLE IF NOT EXISTS backtrack_pattern "
				+ "(id INTEGER PRIMARY KEY AUTO_INCREMENT,"
				+ "qualified_class VARCHAR(255) NOT NULL,"
				+ "method_name VARCHAR(255) NOT NULL,"
				+ "parameter_types VARCHAR(255) NOT NULL,"
				+ "param_of_interest INTEGER UNSIGNED NOT NULL,"
				+ "description VARCHAR(255),"
				+ "active BOOL NOT NULL"
				+ ") ENGINE = INNODB;");
		statement
				.executeUpdate("ALTER TABLE backtrack_pattern ADD UNIQUE INDEX(qualified_class,method_name,parameter_types,param_of_interest,description)");
	}

	public void createHeuristicResultsTable() throws SQLException {
		// Statement statement = Database.getInstance().getNewStatement();
		Statement statement = connection.createStatement();
		statement
				.executeUpdate("CREATE TABLE IF NOT EXISTS heuristic_results "
						+ "(id INTEGER PRIMARY KEY AUTO_INCREMENT,"
						+ "id_analyses INTEGER NOT NULL,"
						+ "id_heuristic_pattern INTEGER NOT NULL,"
						+ "id_class INTEGER,"
						+ "id_method INTEGER,"
						+ "in_line INTEGER,"
						+ "line text,"
						+ "in_ad_framework BOOL,"
						+ "FOREIGN KEY(id_analyses) REFERENCES analyses(id) ON UPDATE CASCADE ON DELETE CASCADE,"
						+ "FOREIGN KEY(id_heuristic_pattern) REFERENCES heuristic_pattern(id) ON UPDATE CASCADE ON DELETE CASCADE,"
						+ "FOREIGN KEY(id_class) REFERENCES classes(id) ON UPDATE CASCADE ON DELETE CASCADE,"
						+ "FOREIGN KEY(id_method) REFERENCES methods(id) ON UPDATE CASCADE ON DELETE CASCADE"
						+ ") ENGINE = INNODB;;");
	}

	public void createBackTrackResultsTable() throws SQLException {
		// Statement statement = Database.getInstance().getNewStatement();
		Statement statement = connection.createStatement();
		statement
				.executeUpdate("CREATE TABLE IF NOT EXISTS backtrack_results "
						+ "(id INTEGER PRIMARY KEY AUTO_INCREMENT,"
						+ "id_analyses INTEGER NOT NULL,"
						+ "id_backtrack_pattern INTEGER NOT NULL,"
						+ "id_class INTEGER,"
						+ "id_method INTEGER,"
						+ "in_line INTEGER,"
						+ "variable_descr VARCHAR(127),"
						+ "enum_variable_type ENUM('FIELD_CONSTANT','LOCAL_VARIABLE','MATH_OPCODE_CONSTANT','ARRAY','LOCAL_ANONYMOUS_CONSTANT','EXTERNAL_METHOD','INTERNAL_BYTECODE_OP','UNCALLED_METHOD') NOT NULL," 
						+ "enum_type ENUM('boolean','byte','short','char','int','long','float','double','String','Math-Operator','Unknown','Other-Class','Array') NOT NULL,"
						+ "argument INTEGER,"
						+ "array_dimension INTEGER,"
						+ "fuzzy_level INTEGER,"
						+ "identifier VARCHAR(255),"
						+ "value text,"
						+ "search_Id INTEGER,"
						+ "in_ad_framework BOOL,"
						+ "FOREIGN KEY(id_analyses) REFERENCES analyses(id) ON UPDATE CASCADE ON DELETE CASCADE,"
						+ "FOREIGN KEY(id_backtrack_pattern) REFERENCES backtrack_pattern(id) ON UPDATE CASCADE ON DELETE CASCADE,"
						+ "FOREIGN KEY(id_class) REFERENCES classes(id) ON UPDATE CASCADE ON DELETE CASCADE,"
						+ "FOREIGN KEY(id_method) REFERENCES methods(id) ON UPDATE CASCADE ON DELETE CASCADE"
						+ ") ENGINE = INNODB;");

	}

	/**
	 * Create a mapping for an apk file to all the used permissions. Uses a
	 * helper table.
	 * 
	 * @throws SQLException
	 */
	public void createPermissionTables() throws SQLException {
		// Statement statement = Database.getInstance().getNewStatement();
		Statement statement = connection.createStatement();
		statement.executeUpdate("CREATE TABLE IF NOT EXISTS permissions "
				+ "(id INTEGER PRIMARY KEY AUTO_INCREMENT,"
				+ "name VARCHAR(255) NOT NULL," 
				+ "enum_type ENUM('PLATFORM','FRAMEWORK','CUSTOM','UNKNOWN') NOT NULL,"
				+ "description VARCHAR(255),"
				+ "UNIQUE KEY name_type(name,enum_type) "
				+ ") ENGINE = INNODB;");
		statement.close();

		statement = connection.createStatement();
		statement
				.executeUpdate("CREATE TABLE IF NOT EXISTS permission_requests "
						+"(id INTEGER PRIMARY KEY AUTO_INCREMENT,"
						+ "analysis_id INTEGER NOT NULL,"
						+ "permission_id INTEGER NOT NULL,"
						+ "valid BOOL NOT NULL,"
						+ "FOREIGN KEY(analysis_id) REFERENCES analyses(id) ON UPDATE CASCADE ON DELETE CASCADE,"
						+ "FOREIGN KEY(permission_id) REFERENCES permissions(id) ON UPDATE CASCADE ON DELETE CASCADE"
						+ ") ENGINE = INNODB;");
		statement.close();
	}

//############ Views for the New Layout #########################
	public void createBTcountView() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute(
		"CREATE OR REPLACE VIEW v_bt_count_per_ana AS " +
		"SELECT " +
		"	ana.id_apk," +
		"	bt.id_analyses," +
		"	COUNT(bt.id) AS bt_count," +
		"	SUM(IF(bt.fuzzy_level>0,1,0)) AS bt_fuzzy_count " +
		"FROM backtrack_results bt " +
		"LEFT JOIN analyses ana ON ana.id = bt.id_analyses " +
		"GROUP BY id_analyses;");

	}

	public void createAnalyseView() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE OR REPLACE VIEW v_ana AS SELECT " +
				"analyses.id, " +
				"analyses.analysis_status AS status, " +
				"analyses.id_apk, " +
				"apk.file_name AS name, " +
				"analyses.heuristic_result, " +
				//FIXME: Überprüfe, ob der Count noch falsche Werte anzeigt?
				//"COUNT(hRe.id) AS count_HResults, "+
				"(SELECT COUNT(id) FROM heuristic_results WHERE id_analyses=analyses.id) AS count_HResults, "+
				"bt.bt_count AS count_BTResults, "+
				"bt.bt_fuzzy_count AS count_BTRes_fuzzy," +
				"(SELECT COUNT(id) FROM permission_requests WHERE analysis_id=analyses.id) AS count_permissions, "+
				"analyses.analysis_start, " +
				"apk.hash_md5 AS md5 "+
				"FROM analyses "+
//				"LEFT JOIN enum_analyses_status AS status1 "+
//				"ON analyses.enum_analysis_status=status1.enum "+
				"LEFT JOIN apk_files AS apk "+
				"ON analyses.id_apk = apk.id "+
				"LEFT JOIN v_bt_count_per_ana AS bt "+
				"ON analyses.id = bt.id_analyses "+
				//"LEFT JOIN heuristic_results AS hRe "+
				//"ON analyses.id = hRe.id_analyses "+
				//"LEFT JOIN backtrack_results AS btRe "+
				//"ON analyses.id = btRe.id_analyses "+
				"GROUP BY analyses.id;");
	}

	public void createApkView() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute("CREATE OR REPLACE VIEW v_apk AS " +
		"SELECT " +
				"apk.id AS id, " +
				"apk.file_name AS name, " +
		"	COUNT(ana.id) AS ANA, " +
		"	AVG(ana.heuristic_result) AS avg_heuristic, " +
		"	AVG(bt.bt_count) AS avg_BTresults, " +
		"	AVG(bt.bt_fuzzy_count) AS avg_fuzzy_BTresults " +

		"FROM apk_files AS apk " +
		"RIGHT JOIN analyses AS ana " +
		"ON apk.id = ana.id_apk " +
		"LEFT JOIN v_bt_count_per_ana AS bt " +
		"ON ana.id = bt.id_analyses " +
		"GROUP BY apk.id" +
		";");
	}

	public void createBTView() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute(
				"CREATE OR REPLACE VIEW v_bt AS " +
				"SELECT " +
				"apk.file_name AS name, " +
				"apk.id AS apk_id, " +
				"ana.id AS ana_id, " +
				"bt.id AS bT_id, " +
				"bt.id_class AS cID, " +
				"bt.id_method AS mID, " +
				"BTp.id AS PatternID, " +
				"BTp.method_name AS Pattern_method, " +
				"bt.enum_variable_type AS Variable_Type," +
				"bt.variable_descr AS Type_Descr, " +
				"bt.enum_type AS Type, " +
				"bt.value AS bt_value, " +
				"bt.fuzzy_level AS fuzzy " +
				"FROM backtrack_results AS bt " +
				"INNER JOIN analyses AS ana ON ana.id = bt.id_analyses " +
				"INNER JOIN apk_files AS apk ON apk.id = ana.id_apk " +
//				"INNER JOIN enum_constant_type AS eST ON eST.enum = bt.enum_dataType " +
				"INNER JOIN backtrack_pattern AS BTp ON BTp.id = bt.id_backtrack_pattern;");
	}

	public void createHView() throws SQLException {
		Statement statement = connection.createStatement();
		statement.execute(
				"CREATE OR REPLACE VIEW v_h AS " +
				"SELECT " +
				"apk.id AS apk_id, " +
				"ana.id AS ana_id, " +
				"apk.file_name AS name, " +
				"h.id AS h_id, " +
				"Hp.id AS PatternID, " +
				"Hp.pattern AS Pattern_name, " +
				"Hp.description AS PatternDesc, " +
				"Hp.heuristic_value AS heuristic_value, " +
				"h.id_class AS cID, " +
				"h.id_method AS mID, " +
				"h.in_line AS h_lineNr, " +
				"h.line AS smali_line " +
				"FROM heuristic_results AS h " +
				"INNER JOIN analyses AS ana ON ana.id = h.id_analyses " +
				"INNER JOIN apk_files AS apk ON apk.id = ana.id_apk " +
				"INNER JOIN heuristic_pattern AS Hp ON Hp.id = h.id_heuristic_pattern;");
	}

	public void createFailedView() throws SQLException{
		Statement statement = connection.createStatement();
		statement.execute(
				"CREATE OR REPLACE view v_failures AS "+
				"SELECT"+
					" file_name,"+
					" error_message "+
				"FROM error_messages "+
				"JOIN analyses ON error_messages.id_analyses=analyses.id "+
				"JOIN apk_files ON analyses.id_apk=apk_files.id"
				);
		
	}
	
	// ############ Index for faster run of SAAF #####################
	public void createResultIndices() throws SQLException {
		// Statement statement = Database.getInstance().getNewStatement();
		Statement statement = connection.createStatement();
		try {
			statement
					.addBatch("CREATE INDEX  bt_result_index ON backtrack_results (id_analyses);");
			statement
					.addBatch("CREATE INDEX  h_result_index ON heuristic_results (id_analyses);");// IF
																									// NOT
																									// EXISTS
			statement.executeBatch();
		} catch (SQLException e) {
			if (e.getErrorCode() != 1061)// System.out.println("ERROR: "+e.getErrorCode()+e.getMessage());
				throw (e);
		}

	}

	// ############ Old Layout #########################

	public void createAppTable() throws SQLException {

		// Statement statement = Database.getInstance().getNewStatement();

		Statement statement = connection.createStatement();

		// statement.executeUpdate("drop table if exists person");
		statement.executeUpdate("create table if not exists applications "
				+ "(id INTEGER PRIMARY KEY AUTO_INCREMENT," + "name text,"
				+ "codelines INTEGER," + "classes INTEGER,"
				// + "batch varchar(255)," + "hash varchar(255)" + ")");
				+ "hash varchar(255)" + ") ENGINE = INNODB;");

	}

	public void createHeuristicTable() throws SQLException {

		// Statement statement = Database.getInstance().getNewStatement();

		Statement statement = connection.createStatement();

		statement
				.executeUpdate("create table if not exists heuristic "
						+ "(id INTEGER PRIMARY KEY AUTO_INCREMENT,"
						+ "app INTEGER,"
						+ "linenr INTEGER,"
						+ "line varchar(255),"
						+ "path varchar(255),"
						+ "file varchar(255),"
						+ "heuristicpattern INTEGER,"
						+ "FOREIGN KEY(heuristicpattern) REFERENCES heuristicsearchpattern(id) ON UPDATE CASCADE ON DELETE CASCADE,"
						+ "FOREIGN KEY(app) REFERENCES applications(id) ON UPDATE CASCADE ON DELETE CASCADE"
						+ ") ENGINE = INNODB;");

	}

	public void createTempAppTable() throws SQLException {

		// Statement statement = Database.getInstance().getNewStatement();

		Statement statement = connection.createStatement();

		// statement.executeUpdate("drop table if exists person");
		statement.executeUpdate("create table if not exists tempapplications "
				+ "(id INTEGER PRIMARY KEY AUTO_INCREMENT," + "name text,"
				+ "codelines INTEGER," + "classes INTEGER,"
				// + "batch varchar(255)," + "hash varchar(255)" + ")");
				+ "hash varchar(255)" + ") ENGINE = INNODB;");

	}

	// TODO: migrate to sql
	public void deleteTempTables() throws SQLException {

		// Statement statement = Database.getInstance().getNewStatement();

		Statement statement = connection.createStatement();

		statement.executeUpdate("drop table if exists tempapplications");
		statement
				.executeUpdate("drop table if exists tempgroupedheuristicvalues");
		statement.executeUpdate("drop table if exists tempheuristic");
	}

	public void createAdsTable() throws SQLException {

		// Statement statement = Database.getInstance().getNewStatement();

		Statement statement = connection.createStatement();

		// statement.executeUpdate("drop table if exists person");
		statement.executeUpdate("create table if not exists ads "
				+ "(id INTEGER PRIMARY KEY AUTO_INCREMENT,"
				+ "adpath varchar(255)" + ")  ENGINE = INNODB;");

	}

	public void dropTables() throws SQLException {
		ResultSet rs = null;
		Statement statement = connection.createStatement();

		rs = statement.executeQuery("show tables;");
		statement.addBatch("SET FOREIGN_KEY_CHECKS=0;");
		while (rs.next()) {
			String name=rs.getString(1);
			if(name.startsWith("v_"))
			{
				statement.addBatch("drop view " +name+ ";");
			}else{
				statement.addBatch("drop table "+name+";");
			}
			
		}
		statement.addBatch("SET FOREIGN_KEY_CHECKS=1;");
		statement.executeBatch();

	}

	/**
	 * @return the connection
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * @param connection the connection to set
	 */
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

}
