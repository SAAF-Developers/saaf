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

import de.rub.syssec.saaf.application.JavaPackage;
import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.db.dao.exceptions.DuplicateEntityException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.interfaces.NuPackageDAO;
import de.rub.syssec.saaf.model.application.PackageInterface;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class MySQLPackageDAO implements NuPackageDAO {

	private static final String DB_COLUMN_APK = "id_apk";
	private static final String DB_COLUMN_FUZZY = "hash_fuzzy";
	private static final String DB_COLUMN_NAME = "name";
	private static final String DB_COLUMN_ID = "id";
	
	private static final String DB_QUERY_INSERT = "INSERT INTO packages("
													+DB_COLUMN_APK+","
													+DB_COLUMN_FUZZY+","
													+DB_COLUMN_NAME+
													")VALUES(?,?,?)";

	private static final String DB_QUERY_READ = "SELECT * FROM packages WHERE "+DB_COLUMN_ID+"=?";
	private static final String DB_QUERY_UPDATE = "UPDATE packages SET " +
												DB_COLUMN_APK+"=?, "+
												DB_COLUMN_FUZZY+"=?, "+
												DB_COLUMN_NAME+"=? WHERE "+
												DB_COLUMN_ID+"=?";
	private static final String DB_QUERY_DELETE = "DELETE FROM packages WHERE "+
													DB_COLUMN_ID+"=?";
	private static final String DB_QUERY_DELETE_ALL = "DELETE FROM packages";
	private static final String DB_QUERY_FIND_EXISITING = "SELECT "+DB_COLUMN_ID+" from packages WHERE "+DB_COLUMN_APK+"=? AND "+DB_COLUMN_NAME+"=?";
	
	private Connection connection;
	
	private boolean useDots = true;

	/**
	 * Creates a MySQLPackageDAO that uses the supplied Connection.
	 * 
	 * @param connection
	 */
	public MySQLPackageDAO(Connection connection) {
		super();
		this.connection = connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#create(java.lang.Object)
	 */
	@Override
	public int create(PackageInterface entity) throws DAOException, DuplicateEntityException {
		int id;
		int index=0;
		try {
			PreparedStatement insert = connection.prepareStatement(
					DB_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS);
			//test parameters for null and set accordingly
			if(entity.getApplication()!=null)
			{
				insert.setInt(++index, entity.getApplication().getId());
			}else{
				insert.setNull(++index, Types.INTEGER);
			}

			
			if(entity.getFuzzyHash()!=null)
			{
				insert.setString(++index, entity.getFuzzyHash());
			}else{
				insert.setNull(++index, Types.VARCHAR);
			}
			
			if(entity.getName(useDots)!=null)
			{
				insert.setString(++index, entity.getName(useDots));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#read(int)
	 */
	@Override
	public PackageInterface read(int id) throws DAOException {
		PackageInterface javaPackage = null;
		PreparedStatement selectStmt;
		try {
			selectStmt = connection.prepareStatement(DB_QUERY_READ);
			selectStmt.setInt(1, id);
			ResultSet rs = selectStmt.executeQuery();
			if (rs.next()) {
				javaPackage = new JavaPackage();
				javaPackage.setId(rs.getInt(DB_COLUMN_ID));
				javaPackage.setName(rs.getString(DB_COLUMN_NAME));
			}
		} catch (SQLException e) {
			throw new DAOException(e);
		}
		return javaPackage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#update(java.lang.Object)
	 */
	@Override
	public boolean update(PackageInterface entity) throws DAOException, NoSuchEntityException {
		boolean success = false;
		int recordsUpdated;
		int index=0;
		PreparedStatement updateStmt;

		try {
			updateStmt = connection.prepareStatement(DB_QUERY_UPDATE);
			//test parameters for null and set accordingly
			if(entity.getApplication()!=null)
			{
				updateStmt.setInt(++index, entity.getApplication().getId());
			}else{
				updateStmt.setNull(++index, Types.INTEGER);
			}

			
			if(entity.getName(useDots)!=null)
			{
				updateStmt.setString(++index, entity.getFuzzyHash());
			}else{
				updateStmt.setNull(++index, Types.VARCHAR);
			}
			
			if(entity.getName(useDots)!=null)
			{
				updateStmt.setString(++index, entity.getName(useDots));
			}else{
				updateStmt.setNull(++index, Types.VARCHAR);
			}
			
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#delete(java.lang.Object)
	 */
	@Override
	public boolean delete(PackageInterface entity) throws DAOException, NoSuchEntityException {
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
	public List<PackageInterface> readAll() {
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
	public int findId(PackageInterface candidate) throws DAOException {
		int id=0;
		PreparedStatement selectStmt;
		try {
			selectStmt = connection.prepareStatement(DB_QUERY_FIND_EXISITING);
			selectStmt.setInt(1, candidate.getApplication().getId());
			selectStmt.setString(2, candidate.getName(useDots));
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
