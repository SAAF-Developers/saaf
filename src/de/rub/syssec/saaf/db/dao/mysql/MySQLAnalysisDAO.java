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
import java.text.SimpleDateFormat;
import java.util.List;

import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.interfaces.NuAnalysisDAO;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface.Status;
import de.rub.syssec.saaf.model.application.ApplicationInterface;


/**
 * @author Tilman Bender <tilman.bender@rub.de>
 * @author Hanno Lemoine <hanno.lemoine@gdata.de> (see Interface)
 */
public class MySQLAnalysisDAO implements NuAnalysisDAO {

	SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final String DB_COLUMN_ID = "id";
	private static final String DB_COLUMN_APK = "id_apk";
	private static final String DB_COLUMN_STATUS = "analysis_status";
	private static final String DB_COLUMN_CREATED = "analysis_created";
	private static final String DB_COLUMN_STARTED = "analysis_start";
	private static final String DB_COLUMN_STOPPED = "analysis_stop";
	private static final String DB_COLUMN_RESULT = "heuristic_result";
	
	private static final String DB_QUERY_INSERT = "INSERT INTO analyses("+
													DB_COLUMN_APK+","+
													DB_COLUMN_STATUS+","+
													DB_COLUMN_CREATED+","+
													DB_COLUMN_STARTED+","+
													DB_COLUMN_STOPPED+","+
													DB_COLUMN_RESULT+")VALUES(?,?,?,?,?,?)";
	
	private static final String DB_QUERY_UPDATE = "UPDATE analyses SET "+
													DB_COLUMN_APK+"=?,"+
													DB_COLUMN_STATUS+"=?,"+
													DB_COLUMN_CREATED+"=?,"+
													DB_COLUMN_STARTED+"=?,"+
													DB_COLUMN_STOPPED+"=?,"+
													DB_COLUMN_RESULT+"=? WHERE "+
													DB_COLUMN_ID+"=?";
	
	private static final String DB_QUERY_DELETE = "DELETE FROM analyses WHERE "+DB_COLUMN_ID+"=?";
	private static final String DB_QUERY_DELETE_ALL_FOR_APP = "DELETE FROM analyses WHERE "+DB_COLUMN_APK+"=?";

	private static final String DB_QUERY_DELETE_ALL = "DELETE FROM analyses";
	private static final String DB_QUERY_COUNT = "SELECT COUNT(id) FROM analyses";
	private static final String DB_QUERY_COUNT_BY_STATUS = DB_QUERY_COUNT + " WHERE "+DB_COLUMN_STATUS+"=?";
	private static final String DB_QUERY_COUNT_BY_APP = DB_QUERY_COUNT + " WHERE "+DB_COLUMN_APK+"=?";

	private Connection connection;
	
	
	/**
	 * Creates a MySQLAnalysisDAO that uses the supplied Connection.
	 * 
	 * @param connection
	 */
	public MySQLAnalysisDAO(Connection connection) {
		super();
		this.connection = connection;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#create(java.lang.Object)
	 */
	@Override
	public int create(AnalysisInterface entity) throws DAOException {
		int id = 0;
		int index=0;
		try {
			PreparedStatement insert = connection.prepareStatement(
					DB_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS);
			if(entity.getApp()!=null){
				insert.setInt(++index, entity.getApp().getId());//id_apk
			}else{
				insert.setNull(++index, Types.INTEGER);//id_apk
			}

			//we need to increment the status by one since MySQL starts its enum values at 1 (zero is reserved for errors)
			insert.setInt(++index, entity.getStatus().ordinal()+1);//analysis_status
			
			if(entity.getCreationTime()!=null){
				insert.setString(++index, dateFormat.format(entity.getCreationTime()));//analysis_created
			}else{
				insert.setNull(++index, Types.DATE);//analysis_created
			}
			
			if(entity.getStartTime()!=null){
				insert.setString(++index, dateFormat.format(entity.getStartTime()));//analysis_started
			}else{
				insert.setNull(++index, Types.DATE);//analysis_started
			}
			
			if(entity.getStopTime()!=null){
				insert.setString(++index, dateFormat.format(entity.getStopTime()));//analysis_stopped
			}else{
				insert.setNull(++index, Types.DATE);//analysis_stopped
			}
			
			insert.setInt(++index, entity.getHeuristicValue());//heuristic_result
			
			insert.executeUpdate();
			ResultSet rs = insert.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getInt(1);
			} else {
				throw new DAOException("Generated keys could not be retrieved!");
			}
		} catch (SQLException e) {
			throw new DAOException(e);
		}
		return id;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#read(int)
	 */
	@Override
	public AnalysisInterface read(int id) throws DAOException {
		if (true) throw new UnsupportedOperationException("Not yet implemented!");
		return null;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#update(java.lang.Object)
	 */
	@Override
	public boolean update(AnalysisInterface entity) throws DAOException, NoSuchEntityException {
		boolean success = false;
		int recordsUpdated;
		int index=0;
		PreparedStatement updateStmt;

		try {
			updateStmt = connection.prepareStatement(DB_QUERY_UPDATE);
			updateStmt.setInt(++index, entity.getApp().getId());//id_apk
			//we need to increment the status by one since MySQL starts its enum values at 1 (zero is reserved for errors)
			updateStmt.setInt(++index, entity.getStatus().ordinal()+1);//analysis_status
			
			if(entity.getCreationTime()!=null){
				updateStmt.setString(++index, dateFormat.format(entity.getCreationTime()));//analysis_created
			}else{
				updateStmt.setNull(++index, Types.DATE);//analysis_created
			}
			
			if(entity.getStartTime()!=null){
				updateStmt.setString(++index, dateFormat.format(entity.getStartTime()));//analysis_started
			}else{
				updateStmt.setNull(++index, Types.DATE);//analysis_started
			}
			
			if(entity.getStopTime()!=null){
				updateStmt.setString(++index, dateFormat.format(entity.getStopTime()));//analysis_stopped
			}else{
				updateStmt.setNull(++index, Types.DATE);//analysis_stopped
			}
			
			updateStmt.setInt(++index, entity.getHeuristicValue());//heuristic_result
			updateStmt.setInt(++index, entity.getId());
			recordsUpdated = updateStmt.executeUpdate();
			// this should affect at most one record
			if (recordsUpdated == 0){
				throw new NoSuchEntityException();
			}else if(recordsUpdated == 1) {
				success = true;
			} else {
				// the update affected multiple records this should not happen!
				throw new DAOException("Update of one entity affected multiple records. This should not happen!");
			}
		} catch (SQLException e) {
			throw new DAOException(e);
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#delete(java.lang.Object)
	 */
	@Override
	public boolean delete(AnalysisInterface entity) throws DAOException, NoSuchEntityException {
		boolean success = false;
		int recordsAffected;
		try {
			PreparedStatement deleteStmt = connection.prepareStatement(DB_QUERY_DELETE);
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
	public List<AnalysisInterface> readAll() throws DAOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int deleteAllByApplication(ApplicationInterface application)
			throws NoSuchEntityException, DAOException {
		int recordsAffected;
		if(application.getId()<1)
			return 0;
		try {
			PreparedStatement deleteStmt = connection.prepareStatement(DB_QUERY_DELETE_ALL_FOR_APP);
			deleteStmt.setInt(1, application.getId());
			recordsAffected = deleteStmt.executeUpdate();
			// this should affect at most one record
		} catch (SQLException e) {
			throw new DAOException(e);
		}
		return recordsAffected;		
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
	public int countAllByApplication(ApplicationInterface application)
			throws DAOException, NoSuchEntityException {
		int count=0;
		if(application.getId()<1)
			return count;
		try {
			PreparedStatement countStmt = connection.prepareStatement(DB_QUERY_COUNT_BY_APP);
			countStmt.setInt(1, application.getId());
			ResultSet rs = countStmt.executeQuery();
			if(rs.next()) {
				count=rs.getInt(1);
			}
		} catch (SQLException e) {
			throw new DAOException(e);
		}
		return count;
	}

	@Override
	public int findId(AnalysisInterface candidate) throws DAOException {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public int countAnalysis(boolean withStatus, Status status)
			throws DAOException {
		int count=0;
		try {
			PreparedStatement countStmt;
			if (withStatus) {
				countStmt = connection.prepareStatement(DB_QUERY_COUNT_BY_STATUS);
				countStmt.setInt(1, status.ordinal()+1);
			} else {
				countStmt = connection.prepareStatement(DB_QUERY_COUNT);
			}
			ResultSet rs = countStmt.executeQuery();
			if(rs.next()) {
				count=rs.getInt("count(id)");
			}
		} catch (SQLException e) {
			throw new DAOException(e);
		}
		return count;
	}
}
