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

import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.db.dao.exceptions.DuplicateEntityException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.interfaces.NuMethodDAO;
import de.rub.syssec.saaf.model.application.MethodInterface;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class MySQLMethodDAO implements NuMethodDAO {
	
	private static final String DB_COLUMN_CLASS = "id_classes";
	private static final String DB_COLUMN_FIRSTLINE = "in_line";
	private static final String DB_COLUMN_CODELINES = "codelines";
	private static final String DB_COLUMN_ARITHMETIC = "arithmetic_fraction";
	private static final String DB_COLUMN_FUZZY = "hash_fuzzy";
	private static final String DB_COLUMN_NAME = "name";
	private static final String DB_COLUMN_CFG = "path_to_cfg";
	private static final String DB_COLUMN_ID = "id";
	private static final String DB_COLUMN_PARAMS = "parameters";
	private static final String DB_COLUMN_RETURN = "return_value";

	private Connection connection;
	private static final String DB_QUERY_CREATE = "INSERT INTO methods(" 
			+ DB_COLUMN_CLASS+"," 
			+ DB_COLUMN_FIRSTLINE + "," 
			+ DB_COLUMN_CODELINES + ","
			+ DB_COLUMN_ARITHMETIC + "," 
			+ DB_COLUMN_FUZZY + "," 
			+ DB_COLUMN_NAME + "," 
			+ DB_COLUMN_PARAMS+","
			+ DB_COLUMN_RETURN+","
			+ DB_COLUMN_CFG+ ")VALUES(?,?,?,?,?,?,?,?,?)";

	private static final String DB_QUERY_UPDATE = "UPDATE methods SET " 
			+ DB_COLUMN_CLASS+ "=?," 
			+ DB_COLUMN_FIRSTLINE + "=?," 
			+ DB_COLUMN_CODELINES + "=?,"
			+ DB_COLUMN_ARITHMETIC + "=?," 
			+ DB_COLUMN_FUZZY + "=?," 
			+ DB_COLUMN_NAME + "=?," 
			+ DB_COLUMN_PARAMS + "=?,"
			+ DB_COLUMN_RETURN+"=?,"
			+ DB_COLUMN_CFG	+ "=? WHERE " 
			+ DB_COLUMN_ID + "=?";

	private static final String DB_QUERY_DELETE = "DELETE FROM methods WHERE "
			+ DB_COLUMN_ID + "=?";
	private static final String DB_QUERY_DELETE_ALL = "DELETE FROM methods";
	private static final String DB_QUERY_FIND_EXISITING = "SELECT * FROM methods WHERE "+DB_COLUMN_CLASS+"=? AND "
																						+DB_COLUMN_NAME+"=? AND "
																						+DB_COLUMN_PARAMS+"=?";
																						

	/**
	 * Creates a MySQLMethodDAO that uses the supplied Connection.
	 * 
	 * @param connection
	 */
	public MySQLMethodDAO(Connection connection) {
		super();
		this.connection = connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#create(java.lang.Object)
	 */
	@Override
	public int create(MethodInterface entity) throws DAOException, DuplicateEntityException {
		int id;
		int index=0;
		try {
			PreparedStatement insert = connection.prepareStatement(
					DB_QUERY_CREATE, Statement.RETURN_GENERATED_KEYS);
			// test parameters for null and set accordingly
			if (entity.getSmaliClass() != null) {
				insert.setInt(++index, entity.getSmaliClass().getId()); // the class 
			} else {
				insert.setNull(++index, Types.INTEGER); // the class
			}

			if (entity.getCodeLines() != null
					&& entity.getCodeLines().getFirst() != null) {
				insert.setInt(++index, entity.getCodeLines().getFirst().getLineNr()); // first loc
			} else {
				insert.setNull(++index, Types.INTEGER); // the initial line of code
			}

			if (entity.getCodeLines()!=null) {
				insert.setInt(++index, entity.getCodeLines().size()); //the number of lines of code
			}else
			{
				insert.setNull(++index, Types.INTEGER); //the number of lines of code
			}
			
			insert.setDouble(++index, entity.arithOps()); //the fraction of arithemtic operations
			
			insert.setNull(++index, Types.VARCHAR); //FIXME there is no getter for the fuzzy hash
			
			if(entity.getName()!=null)
			{
				insert.setString(++index, entity.getName());
			}else
			{
				insert.setNull(++index, Types.VARCHAR);
			}
			
			if(entity.getParameterString()!=null)
			{
				insert.setString(++index, entity.getParameterString());
			}else
			{
				insert.setNull(++index, Types.VARCHAR);
			}
			
			if(entity.getReturnValueString()!=null)
			{
				insert.setString(++index, entity.getReturnValueString());
			}else
			{
				insert.setNull(++index, Types.VARCHAR);
			}
			
			insert.setNull(++index, Types.VARCHAR	); //FIXME there is no getter for the cfg path
			

			insert.executeUpdate();
			ResultSet rs = insert.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getInt(1);
			} else {
				throw new DAOException(
						"Autogenerated keys could not be retrieved!");
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
	public MethodInterface read(int id) throws DAOException {
		throw new UnsupportedOperationException("Reading Methods is currently not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#update(java.lang.Object)
	 */
	@Override
	public boolean update(MethodInterface entity) throws DAOException, NoSuchEntityException {
		boolean success = false;
		int recordsUpdated;
		int index=0;
		PreparedStatement updateStmt;

		try {
			updateStmt = connection.prepareStatement(DB_QUERY_UPDATE);
			// set the params
			if (entity.getSmaliClass() != null) {
				updateStmt.setInt(++index, entity.getSmaliClass().getId()); // the class it
																	// belongs
																	// to
			} else {
				updateStmt.setNull(++index, Types.INTEGER); // the class it belongs to
			}

			if (entity.getCodeLines() != null
					&& entity.getCodeLines().getFirst() != null) {
				updateStmt.setInt(++index, entity.getCodeLines().getFirst().getLineNr()); // the
																				// initial
																				// line
																				// of
																				// code
			} else {
				updateStmt.setNull(++index, Types.INTEGER); // the initial line of code
			}

			if (entity.getCodeLines()!=null) {
				updateStmt.setInt(++index, entity.getCodeLines().size()); //the number of lines of code
			}else
			{
				updateStmt.setNull(++index, Types.INTEGER); //the number of lines of code
			}
			
			updateStmt.setDouble(++index, entity.arithOps()); //the fraction of arithemtic operations
			
			updateStmt.setNull(++index, Types.VARCHAR); //there is no getter for the fuzzy hash
			
			if(entity.getName()!=null)
			{
				updateStmt.setString(++index, entity.getName());
			}else
			{
				updateStmt.setNull(++index, Types.VARCHAR);
			}
			if(entity.getParameterString()!=null)
			{
				updateStmt.setString(++index, entity.getParameterString());
			}else
			{
				updateStmt.setNull(++index, Types.VARCHAR);
			}
			
			
			if(entity.getReturnValueString()!=null)
			{
				updateStmt.setString(++index, entity.getReturnValueString());
			}else
			{
				updateStmt.setNull(++index, Types.VARCHAR);
			}
			updateStmt.setNull(++index, Types.VARCHAR	); //FIXME there is no getter for the cfg path
			updateStmt.setInt(++index, entity.getId());
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
	public boolean delete(MethodInterface entity) throws DAOException,
			NoSuchEntityException {
		boolean success = false;
		int recordsAffected;
		try {
			PreparedStatement deleteStmt = connection
					.prepareStatement(DB_QUERY_DELETE);
			deleteStmt.setInt(1, entity.getId());
			recordsAffected = deleteStmt.executeUpdate();
			// this should affect at most one record
			if (recordsAffected == 0) {
				throw new NoSuchEntityException();
			} else if (recordsAffected == 1) {
				success = true;
			} else if (recordsAffected > 1) {
				throw new DAOException(
						"Delete of one entity affected multiple records. This should not happen!");
			}

		} catch (SQLException e) {
			throw new DAOException(e);
		}
		return success;
	}

	@Override
	public List<MethodInterface> readAll() {
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
	public int findId(MethodInterface candidate) throws DAOException {
		int id=0;
		PreparedStatement selectStmt;
		try {
			selectStmt = connection.prepareStatement(DB_QUERY_FIND_EXISITING);
			selectStmt.setInt(1, candidate.getSmaliClass().getId());
			selectStmt.setString(2, candidate.getName());
			selectStmt.setString(3, candidate.getParameterString());
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
