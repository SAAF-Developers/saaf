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
package de.rub.syssec.saaf.db.dao.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import de.rub.syssec.saaf.application.Application;
import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.db.dao.exceptions.DuplicateEntityException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.interfaces.NuApplicationDAO;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.Digest;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
 */
public class MySQLApplicaitonDAO implements NuApplicationDAO {

	private static final String DB_TABLE_NAME = "apk_files";
	private static final String DB_COLUMN_ID = "id";
	private static final String DB_COLUMN_CODELINES = "codelines";
	private static final String DB_COLUMN_CLASSES = "classes";

	private static final String DB_COLUMN_MAN_MIN_SDK = "man_minSDK";
	private static final String DB_COLUMN_MAN_VERSION_CODE = "man_versionCode";
	private static final String DB_COLUMN_MAN_VERSION_NAME = "man_versionName";
	private static final String DB_COLUMN_MAN_ACTIVITIES = "man_activities";
	private static final String DB_COLUMN_MAN_RECEIVERS = "man_receivers";
	private static final String DB_COLUMN_MAN_SERVICES = "man_services";

	private static final String DB_COLUMN_HASH_MD5 = "hash_md5";
	private static final String DB_COLUMN_HASH_SHA1 = "hash_sha1";
	private static final String DB_COLUMN_HASH_SHA256 = "hash_sha256";
	private static final String DB_COLUMN_HASH_FUZZY = "hash_fuzzy";
	private static final String DB_COLUMN_FILE_NAME = "file_name";

	private static final String DB_COLUMN_MAN_PACKAGE = "man_package";
	private static final String DB_COLUMN_MAN_APP_LABEL = "man_appLabel";
	private static final String DB_COLUMN_MAN_APP_LABEL_RESOLVED = "man_appLabelResolved";
	private static final String DB_COLUMN_MAN_APP_DEBUGGABLE = "man_appDebuggable";

	private static final String DB_QUERY_INSERT = "INSERT INTO " + DB_TABLE_NAME
			+ " ("
			+ DB_COLUMN_CODELINES + ","
			+ DB_COLUMN_CLASSES + ","
			+ DB_COLUMN_HASH_MD5 + ","
			+ DB_COLUMN_HASH_SHA1 + ","
			+ DB_COLUMN_HASH_SHA256 + ","
			+ DB_COLUMN_HASH_FUZZY + ","
			+ DB_COLUMN_FILE_NAME + ","
			+ DB_COLUMN_MAN_MIN_SDK + ","
			+ DB_COLUMN_MAN_VERSION_CODE + ","
			+ DB_COLUMN_MAN_VERSION_NAME + ","
			+ DB_COLUMN_MAN_APP_LABEL + ","
			+ DB_COLUMN_MAN_APP_LABEL_RESOLVED + ","
			+ DB_COLUMN_MAN_PACKAGE + ","
			+ DB_COLUMN_MAN_APP_DEBUGGABLE + ","
			+ DB_COLUMN_MAN_ACTIVITIES + ","
			+ DB_COLUMN_MAN_RECEIVERS + ","
			+ DB_COLUMN_MAN_SERVICES + ""
			+ ")VALUES(?,?,?,?,?,?,? ,?,?,?,?,?,?,?,?,?,?)";

	private static final String DB_QUERY_READ = "SELECT * FROM " + DB_TABLE_NAME
			+ " WHERE " + DB_COLUMN_ID + "=?";

	private static final String DB_QUERY_UDPATE = "UPDATE " + DB_TABLE_NAME
			+ " SET "
			+ DB_COLUMN_CODELINES + "=?," 
			+ DB_COLUMN_CLASSES + "=?,"
			+ DB_COLUMN_HASH_MD5 + "=?," 
			+ DB_COLUMN_HASH_SHA1 + "=?,"
			+ DB_COLUMN_HASH_SHA256 + "=?," 
			+ DB_COLUMN_HASH_FUZZY + "=?,"
			+ DB_COLUMN_FILE_NAME + "=?,"
			+ DB_COLUMN_MAN_MIN_SDK + "=?,"
			+ DB_COLUMN_MAN_VERSION_CODE + "=?,"
			+ DB_COLUMN_MAN_VERSION_NAME + "=?,"
			+ DB_COLUMN_MAN_APP_LABEL + "=?,"
			+ DB_COLUMN_MAN_APP_LABEL_RESOLVED + "=?,"
			+ DB_COLUMN_MAN_PACKAGE + "=?,"
			+ DB_COLUMN_MAN_APP_DEBUGGABLE + "=?,"
			+ DB_COLUMN_MAN_ACTIVITIES + "=?,"
			+ DB_COLUMN_MAN_RECEIVERS + "=?,"
			+ DB_COLUMN_MAN_SERVICES + "=?"
			+ " WHERE " + DB_COLUMN_ID + "=?";

	private static final String DB_QUERY_DELETE = "DELETE FROM " + DB_TABLE_NAME
			+ " WHERE " + DB_COLUMN_ID + "=?";

	private static final String DB_QUERY_DELETE_ALL = "DELETE FROM "
			+ DB_TABLE_NAME;

	private static final String DB_QUER_READ_BY_HASH = "SELECT * FROM "
			+ DB_TABLE_NAME + " WHERE "
			+ DB_COLUMN_HASH_MD5 + "=?";

	private static final String DB_QUERY_FIND_EXISITING = "SELECT * FROM "
			+ DB_TABLE_NAME + " WHERE "
			+ DB_COLUMN_HASH_MD5+"=? AND "
			+ DB_COLUMN_HASH_SHA1+"=? AND "
			+ DB_COLUMN_HASH_SHA256+"=?";

	private Connection connection;

	/**
	 * Creates a MySQLApplicaitonDAO that uses the supplied Connection.
	 * 
	 * @param connection
	 */
	public MySQLApplicaitonDAO(Connection connection) {
		super();
		this.connection = connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#create(java.lang.Object)
	 */
	@Override
	public int create(ApplicationInterface entity) throws DAOException, DuplicateEntityException {
		int id = 0;
		//parameter index in the prepared statement 
		//using this idiom its easier to add new params
		int index=0;
		try {
			PreparedStatement insert = connection.prepareStatement(
					DB_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS);
			insert.setInt(++index,entity.getNumberOfCodelines(true)); // codelines
			insert.setInt(++index, entity.getAllSmaliClasss(true).size()); // classes
			insert.setString(++index, entity.getMessageDigest(Digest.MD5));
			insert.setString(++index, entity.getMessageDigest(Digest.SHA1));
			insert.setString(++index, entity.getMessageDigest(Digest.SHA256));
			insert.setString(++index, entity.getMessageDigest(Digest.FuzzyHash));
			insert.setString(++index, entity.getApplicationName());// file_name
			if(entity.getManifest()!=null)
			{
				insert.setInt(++index, entity.getManifest().getMinSdkVersion());
				insert.setInt(++index, entity.getManifest().getVersionCode());
				insert.setString(++index, entity.getManifest().getVersionName());
				insert.setString(++index, entity.getManifest().getAppLabel());
				insert.setString(++index, entity.getManifest().getAppLabelResolved());
				insert.setString(++index, entity.getManifest().getPackageName());
				insert.setBoolean(++index, entity.getManifest().isAppDebuggable());
				insert.setInt(++index, entity.getManifest().getActivities().size());
				insert.setInt(++index, entity.getManifest().getReceivers().size());
				insert.setInt(++index, entity.getManifest().getServices().size());
			}else
			{
				insert.setNull(++index, Types.INTEGER);//minsdkversion
				insert.setNull(++index, Types.INTEGER);//versioncode
				insert.setNull(++index, Types.VARCHAR);//VersionName
				insert.setNull(++index, Types.VARCHAR);//AppLabel
				insert.setNull(++index, Types.VARCHAR);//AppLabelResolved
				insert.setNull(++index, Types.VARCHAR);//PackageName	
				insert.setNull(++index, Types.BOOLEAN);//AppDebuggable
				insert.setNull(++index, Types.INTEGER);//Activties
				insert.setNull(++index, Types.INTEGER);//Receivers
				insert.setNull(++index, Types.INTEGER);//Services
			}
			insert.executeUpdate();
			ResultSet rs = insert.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getInt(1);
			} else {
				throw new DAOException("Generated keys could not be retrieved!");
			}
		} catch (SQLException e) {
			//use the SQL Error Code to throw specific exception for duplicate entries
			if(e.getSQLState().equalsIgnoreCase(SQL_ERROR_DUPLICATE) && e.getMessage().toLowerCase().contains("duplicate"))
			{
				throw new DuplicateEntityException("An entity with the same key attributes already exists",e);
			}else
			{
				throw new DAOException(e);
			}
		}
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#read(int)
	 */
	@Override
	public ApplicationInterface read(int id) throws DAOException {
		PreparedStatement select;
		ApplicationInterface app = null;
		try {
			select = connection.prepareStatement(DB_QUERY_READ);
			select.setInt(1, id);
			ResultSet rs = select.executeQuery();
			if (rs.next()) {
				app = new Application();
				app.setApplicationName(rs.getString(DB_COLUMN_FILE_NAME));
				app.setMessageDigest(Digest.MD5, rs.getString(DB_COLUMN_HASH_MD5));
				app.setMessageDigest(Digest.SHA1, rs.getString(DB_COLUMN_HASH_SHA1));
				app.setMessageDigest(Digest.SHA256, rs.getString(DB_COLUMN_HASH_SHA256));
			}
		} catch (SQLException e) {
			throw new DAOException(e);
		}
		return app;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#update(java.lang.Object)
	 */
	@Override
	public boolean update(ApplicationInterface entity) throws DAOException, NoSuchEntityException {
		boolean success = false;
		int recordsUpdated;
		int index=0;
		PreparedStatement updateStmt;

		try {
			updateStmt = connection.prepareStatement(DB_QUERY_UDPATE);
			updateStmt.setInt(++index,entity.getNumberOfCodelines(true)); // codelines
			updateStmt.setInt(++index, entity.getAllSmaliClasss(true).size()); // classes
			updateStmt.setString(++index, entity.getMessageDigest(Digest.MD5));
			updateStmt.setString(++index, entity.getMessageDigest(Digest.SHA1));
			updateStmt.setString(++index, entity.getMessageDigest(Digest.SHA256));
			updateStmt.setString(++index, entity.getMessageDigest(Digest.FuzzyHash));
			updateStmt.setString(++index, entity.getApplicationName());// file_name
			if(entity.getManifest()!=null)
			{
				updateStmt.setInt(++index, entity.getManifest().getMinSdkVersion());
				updateStmt.setInt(++index, entity.getManifest().getVersionCode());
				updateStmt.setString(++index, entity.getManifest().getVersionName());
				updateStmt.setString(++index, entity.getManifest().getAppLabel());
				updateStmt.setString(++index, entity.getManifest().getAppLabelResolved());
				updateStmt.setString(++index, entity.getManifest().getPackageName());
				updateStmt.setBoolean(++index, entity.getManifest().isAppDebuggable());
				updateStmt.setInt(++index, entity.getManifest().getActivities().size());
				updateStmt.setInt(++index, entity.getManifest().getReceivers().size());
				updateStmt.setInt(++index, entity.getManifest().getServices().size());
			}else
			{
				updateStmt.setNull(++index, Types.INTEGER);//minsdkversion
				updateStmt.setNull(++index, Types.INTEGER);//versioncode
				updateStmt.setNull(++index, Types.VARCHAR);//VersionName
				updateStmt.setNull(++index, Types.VARCHAR);//AppLabel
				updateStmt.setNull(++index, Types.VARCHAR);//AppLabelResolved
				updateStmt.setNull(++index, Types.VARCHAR);//PackageName	
				updateStmt.setNull(++index, Types.BOOLEAN);//AppDebuggable
				updateStmt.setNull(++index, Types.INTEGER);//Activties
				updateStmt.setNull(++index, Types.INTEGER);//Receivers
				updateStmt.setNull(++index, Types.INTEGER);//Services
			}
			updateStmt.setInt(++index, entity.getId());// id
			recordsUpdated = updateStmt.executeUpdate();
			// this should affect at most one record
			if (recordsUpdated == 0) {
				throw new NoSuchEntityException();
			} else if (recordsUpdated == 1) {
				success = true;
			} else {
				// the update affected multiple records this should not happen!
				throw new DAOException(
						"Update of one entity affected multiple records. This should not happen!");
			}
		} catch (SQLException e) {
			throw new DAOException(e);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#delete(java.lang.Object)
	 */
	@Override
	public boolean delete(ApplicationInterface entity) throws DAOException, NoSuchEntityException {
		boolean success = false;
		int recordsAffected;
		try {
			PreparedStatement deleteStmt = connection
					.prepareStatement(DB_QUERY_DELETE);
			deleteStmt.setInt(1, entity.getId());
			recordsAffected = deleteStmt.executeUpdate();
			// this should affect at most one record
			if(recordsAffected==0){
				throw new NoSuchEntityException();
			}else if (recordsAffected == 1) {
				success = true;
			} else if(recordsAffected >1) {
				throw new DAOException("Delete of one entity affected multiple records. This should not happen!");
			}

		} catch (SQLException e) {
			throw new DAOException(e);
		}
		return success;
	}

	@Override
	public List<ApplicationInterface> readAll() throws DAOException {
		throw new UnsupportedOperationException();
	}

	
	@Override
	public int deleteAll() throws DAOException {
		int recordsAffected;
		try {
			PreparedStatement deleteStmt = connection.prepareStatement(DB_QUERY_DELETE_ALL);
			recordsAffected = deleteStmt.executeUpdate();
		} catch (SQLException e) {
			throw new DAOException(e);
		}
		return recordsAffected;
	}

	@Override
	public int findByMD5Hash(String hash) throws DAOException {
		PreparedStatement select;
		int id = 0;
		try {
			select = connection.prepareStatement(DB_QUER_READ_BY_HASH);
			select.setString(1, hash);
			ResultSet rs = select.executeQuery();
			if (rs.next()) {
				id = rs.getInt(DB_COLUMN_ID);
			}
		} catch (SQLException e) {
			throw new DAOException(e);
		}
		return id;
	}

	
	@Override
	public String findNameById(int id) throws DAOException {
		PreparedStatement select;
		String name = null;
		try {
			select = connection.prepareStatement(DB_QUERY_READ);
			select.setInt(1, id);
			ResultSet rs = select.executeQuery();
			if (rs.next()) {
				name = rs.getString(DB_COLUMN_FILE_NAME);
			}
		} catch (SQLException e) {
			throw new DAOException(e);
		}
		return name;
	}

	@Override
	public int findId(ApplicationInterface candidate) throws DAOException {
		int id = 0;
		PreparedStatement selectStmt;
		try {
			selectStmt = connection.prepareStatement(DB_QUERY_FIND_EXISITING);
			if (candidate.getMessageDigest(Digest.MD5) != null) {
				selectStmt.setString(1, candidate.getMessageDigest(Digest.MD5));
			} else {
				selectStmt.setNull(1, Types.VARCHAR);
			}
			if (candidate.getMessageDigest(Digest.SHA1) != null) {
				selectStmt.setString(2, candidate.getMessageDigest(Digest.SHA1));
			} else {
				selectStmt.setNull(2, Types.VARCHAR);
			}
			if (candidate.getMessageDigest(Digest.SHA256) != null) {
				selectStmt.setString(3, candidate.getMessageDigest(Digest.SHA256));
			} else {
				selectStmt.setNull(3, Types.VARCHAR);
			}
			ResultSet rs = selectStmt.executeQuery();
			if (rs.next()) {
				id = rs.getInt(DB_COLUMN_ID);
			}
		} catch (SQLException e) {
			throw new DAOException(e);
		}
		return id;
	}

}
