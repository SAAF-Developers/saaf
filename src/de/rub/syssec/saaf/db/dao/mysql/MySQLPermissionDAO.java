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
import java.util.ArrayList;
import java.util.List;

import de.rub.syssec.saaf.application.manifest.permissions.Permission;
import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.db.dao.exceptions.DuplicateEntityException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.interfaces.NuPermissionDAO;
import de.rub.syssec.saaf.model.application.PermissionInterface;
import de.rub.syssec.saaf.model.application.PermissionType;

/**
 * Saves a Permission to MySQL database.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class MySQLPermissionDAO implements NuPermissionDAO {

	private static final String DB_COLUMN_ID = "id";
	private static final String DB_COLUMN_NAME = "name";
	private static final String DB_COLUMN_TYPE = "enum_type";
	private static final String DB_COLUMN_DESCRIPTION = "description";
	private static final String DB_QUERY_INSERT = "INSERT INTO permissions("+DB_COLUMN_NAME+","
																			+DB_COLUMN_TYPE+","
																			+DB_COLUMN_DESCRIPTION+")VALUES(?,?,?)";

	private static final String QUERY_READ = "SELECT * FROM permissions WHERE "+DB_COLUMN_ID+"=?";
	private static final String QUERY_UPDATE = "UPDATE permissions SET "+DB_COLUMN_NAME+"=?,"
																		+DB_COLUMN_TYPE+"=?,"
																		+DB_COLUMN_DESCRIPTION+"=? "
																		+"WHERE "+DB_COLUMN_ID+"=?";
	private static final String QUERY_DELETE = "DELETE FROM permissions WHERE "+DB_COLUMN_ID+"=?";
	private static final String DB_QUERY_DELETE_ALL = "DELETE FROM permissions";
	private static final String DB_QUERY_FIND_EXISITING = "SELECT * FROM permissions WHERE "+DB_COLUMN_NAME+"=? AND "
																						  +DB_COLUMN_TYPE+"=?";
	private static final String QUERY_READ_ALL = "SELECT * FROM permissions";
	
	private Connection connection;

	public MySQLPermissionDAO(Connection conn) {
		this.connection = conn;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.dao.interfaces.GenericDAO#create(java.lang.Object)
	 */
	@Override
	public int create(PermissionInterface entity)
			throws DuplicateEntityException, DAOException {
		int id;
		int index=0;
		try {
			PreparedStatement insert = connection.prepareStatement(
					DB_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS);
			//test parameters for null and set accordingly
			if(entity.getName()!=null)
			{
				insert.setString(++index, entity.getName());
			}else{
				insert.setNull(++index, Types.VARCHAR);
			}

			
			if(entity.getType()!=null)
			{
				insert.setString(++index, entity.getType().toString().toUpperCase());
			}else{
				insert.setNull(++index, Types.VARCHAR);
			}
			
			if(entity.getDescription()!=null)
			{
				insert.setString(++index, entity.getDescription());
			}else{
				insert.setNull(++index, Types.VARCHAR);
			}
	
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

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.dao.interfaces.GenericDAO#read(int)
	 */
	@Override
	public PermissionInterface read(int id) throws DAOException {
		PermissionInterface result = null;
		PermissionType type =null;
		PreparedStatement selectStmt;
		try {
			selectStmt = connection.prepareStatement(QUERY_READ);
			selectStmt.setInt(1, id);
			ResultSet rs = selectStmt.executeQuery();
			if (rs.next()) {
				type = PermissionType.valueOf(rs.getString(DB_COLUMN_TYPE));
				result = new Permission(rs.getString(DB_COLUMN_NAME), type, rs.getString(DB_COLUMN_DESCRIPTION));
			}
		} catch (SQLException e) {
			throw new DAOException(e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.dao.interfaces.GenericDAO#readAll()
	 */
	@Override
	public List<PermissionInterface> readAll() throws DAOException {
		ArrayList<PermissionInterface> permissions = new ArrayList<PermissionInterface>();
		PreparedStatement selectStmt;
		PermissionInterface permission;
		try {
			selectStmt = connection.prepareStatement(QUERY_READ_ALL);
			ResultSet rs = selectStmt.executeQuery();
			while (rs.next()) {
				permission = new Permission(rs.getString(DB_COLUMN_NAME),
						PermissionType.valueOf(rs.getString(DB_COLUMN_TYPE).toUpperCase()),
						rs.getString(DB_COLUMN_DESCRIPTION));
				permission.setId(rs.getInt(DB_COLUMN_ID));
				permissions.add(permission);
			}
		} catch (SQLException e) {
			throw new DAOException(e);
		}
		return permissions;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.dao.interfaces.GenericDAO#update(java.lang.Object)
	 */
	@Override
	public boolean update(PermissionInterface entity)
			throws NoSuchEntityException, DAOException {
		boolean success = false;
		int recordsUpdated;
		PreparedStatement updateStmt;

		try {
			updateStmt = connection.prepareStatement(QUERY_UPDATE);
			if(entity.getName()!=null)
			{
				updateStmt.setString(1, entity.getName());
			}else{
				updateStmt.setNull(1, Types.VARCHAR);
			}

			if(entity.getType()!=null)
			{
				updateStmt.setString(2, entity.getType().toString().toUpperCase());
			}else{
				updateStmt.setNull(2, Types.VARCHAR);
			}
			
			if(entity.getDescription()!=null)
			{
				updateStmt.setString(3, entity.getDescription());
			}else{
				updateStmt.setNull(3, Types.VARCHAR);
			}	
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

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.dao.interfaces.GenericDAO#delete(java.lang.Object)
	 */
	@Override
	public boolean delete(PermissionInterface entity)
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

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.dao.interfaces.GenericDAO#deleteAll()
	 */
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

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.db.dao.interfaces.GenericDAO#findId(java.lang.Object)
	 */
	@Override
	public int findId(PermissionInterface candidate) throws DAOException {
		int id=0;
		PreparedStatement selectStmt;
		try {
			selectStmt = connection.prepareStatement(DB_QUERY_FIND_EXISITING);
			if(candidate.getName()!=null)
			{
				selectStmt.setString(1, candidate.getName());
			}else{
				selectStmt.setNull(1, Types.VARCHAR);
			}
			if(candidate.getType()!=null)
			{
				selectStmt.setString(2, candidate.getType().toString().toUpperCase());
			}else{
				selectStmt.setNull(2, Types.VARCHAR);
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
