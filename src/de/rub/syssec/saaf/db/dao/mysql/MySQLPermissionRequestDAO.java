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
import java.util.List;

import org.objectweb.asm.Type;

import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.db.dao.exceptions.DuplicateEntityException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.interfaces.NuPermissionRequestDAO;
import de.rub.syssec.saaf.model.application.manifest.PermissionRequestInterface;

/**
 * Deals with persisting a PermissionRequest.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class MySQLPermissionRequestDAO implements NuPermissionRequestDAO {

	private static final String DB_COLUMN_ID = "id";
	private static final String DB_COLUMN_ANALYSIS = "analysis_id";
	private static final String DB_COLUMN_PERMISSION = "permission_id";
	private static final String DB_COLUMN_VALID = "valid";

	private static final String DB_QUERY_INSERT = "INSERT INTO permission_requests("+DB_COLUMN_ANALYSIS+","
																					+DB_COLUMN_PERMISSION+","
																					+DB_COLUMN_VALID+")VALUES(?,?,?)";
//	private static final String QUERY_READ = "SELECT * FROM permission_requests WHERE "+DB_COLUMN_ID+"=?";
	private static final String QUERY_DELETE = "DELETE FROM permission_requests WHERE "+DB_COLUMN_ID+"=?";
	private static final String QUERY_UPDATE = "UPDATE permission_requests SET "+DB_COLUMN_ANALYSIS+"=?,"
																				+DB_COLUMN_PERMISSION+"=?,"
																				+DB_COLUMN_VALID+"=?"
																				+" WHERE "+DB_COLUMN_ID+"=?";
	private static final String DB_QUERY_FIND_EXISITING = "SELECT * FROM permission_requests WHERE "+DB_COLUMN_ANALYSIS+"=? AND "
																									+DB_COLUMN_PERMISSION+"=? AND "
																									+DB_COLUMN_VALID+"=?";
	private static final String DB_QUERY_DELETE_ALL = "DELETE FROM permission_requests";
	private Connection connection;

	public MySQLPermissionRequestDAO(Connection connection) {
		this.connection = connection;
	}

	@Override
	public int create(PermissionRequestInterface entity)
			throws DuplicateEntityException, DAOException {
		int id;
		try {
			PreparedStatement insert = connection.prepareStatement(
					DB_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS);
			//test parameters for null and set accordingly
			if(entity.getAnalysis()!=null)
			{
				insert.setInt(1, entity.getAnalysis().getId());
			}else
			{
				insert.setNull(1, Type.INT);
			}
			
			if(entity.getRequestedPermission()!=null)
			{
				insert.setInt(2, entity.getRequestedPermission().getId());
			}else{
				insert.setNull(2, Type.INT);
			}
			insert.setBoolean(3, entity.isValid());	
			insert.executeUpdate();
			ResultSet rs = insert.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getInt(1);
			} else {
				throw new DAOException(
						"Generated keys could not be retrieved!");
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

	@Override
	public PermissionRequestInterface read(int id) throws DAOException {
			throw new UnsupportedOperationException("Reading permission requests is not supported yet.");
	}

	@Override
	public List<PermissionRequestInterface> readAll() throws DAOException {
		throw new UnsupportedOperationException("Reading permission requests is not supported yet.");
	}

	@Override
	public boolean update(PermissionRequestInterface entity)
			throws NoSuchEntityException, DAOException {
		boolean success = false;
		int recordsUpdated;
		PreparedStatement updateStmt;

		try {
			updateStmt = connection.prepareStatement(QUERY_UPDATE);
			if(entity.getAnalysis()!=null)
			{
				updateStmt.setInt(1, entity.getAnalysis().getId());
			}else
			{
				updateStmt.setNull(1, Type.INT);
			}
			
			if(entity.getRequestedPermission()!=null)
			{
				updateStmt.setInt(2, entity.getRequestedPermission().getId());
			}else{
				updateStmt.setNull(2, Type.INT);
			}
			updateStmt.setBoolean(3, entity.isValid());				
			
			updateStmt.setInt(4, entity.getId());
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

	@Override
	public boolean delete(PermissionRequestInterface entity)
			throws NoSuchEntityException, DAOException {
		boolean success = false;
		int recordsAffected;
		try {
			PreparedStatement deleteStmt = connection.prepareStatement(QUERY_DELETE);
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
	public int findId(PermissionRequestInterface candidate) throws DAOException {
		int id=0;
		PreparedStatement selectStmt;
		try {
			selectStmt = connection.prepareStatement(DB_QUERY_FIND_EXISITING);
			if(candidate.getAnalysis()!=null)
			{
				selectStmt.setInt(1, candidate.getAnalysis().getId());
			}else
			{
				selectStmt.setNull(1, Type.INT);
			}
			
			if(candidate.getRequestedPermission()!=null)
			{
				selectStmt.setInt(2, candidate.getRequestedPermission().getId());
			}else{
				selectStmt.setNull(2, Type.INT);
			}
			selectStmt.setBoolean(3, candidate.isValid());		
			
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
