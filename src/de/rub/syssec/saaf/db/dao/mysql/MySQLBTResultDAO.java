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
import de.rub.syssec.saaf.db.dao.interfaces.NuBTResultDAO;
import de.rub.syssec.saaf.model.analysis.BTResultInterface;

/**
 * DAO that Handles persisting a BTResult.
 * 
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class MySQLBTResultDAO implements NuBTResultDAO {
	
	private static final String DB_QUERY_INSERT="INSERT INTO backtrack_results"+ 
			"(id_analyses," +
			" id_backtrack_pattern," +
			" id_class," +
			" id_method," +
			" in_line," +
			" enum_type," +
			" argument," +
			" identifier," +
			" value," +
			" variable_descr," +
			" array_dimension," +
			" fuzzy_level," +
			" search_Id," +
			" in_ad_framework," +
			" enum_variable_type) " +
			"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
//	private static final String DB_QUERY_SELECT="SELECT * FROM backtrack_resulst WHERE id=?";
	private static final String DB_QUERY_UPDATE="UPDATE backtrack_results SET " +
			"id_analyses=?," +
			"id_backtrack_pattern=?,"+
			"id_class=?," +
			"id_method=?," +
			" in_line=?,"+ 
			" enum_type=?," +
			" argument=?," +
			" identifier=?," +
			" value=?," +
			" variable_descr=?," +
			" array_dimension=?," +
			" fuzzy_level=?," +
			" search_Id=?," +
			" in_ad_framework=?, " +
			" enum_variable_type=? " +
			"WHERE id=? ";
	private static final String DB_QUERY_DELETE="DELETE FROM backtrack_results WHERE id=?";

	private static final String DB_QUERY_DELETE_ALL = "DELETE FROM backtrack_results";

	private Connection connection;

	/**
	 * Creates a MySQLBTResultDAO that uses the supplied Connection.
	 * 
	 * @param connection
	 */
	public MySQLBTResultDAO(Connection connection) {
		super();
		this.connection = connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#create(java.lang.Object)
	 */
	@Override
	public int create(BTResultInterface entity) throws DAOException, DuplicateEntityException {
		//first check if the leaf nodes of the object graph have been persisted yet.	
		PreparedStatement statement;
		int index=0;
		int id;
		try {
			statement = connection.prepareStatement(DB_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS);
			if(entity.getAnalysis()!=null){
				statement.setInt(++index, entity.getAnalysis().getId()); //id_analyses,
			}else{
				statement.setNull(++index, Types.INTEGER);
			}
			
			if(entity.getPattern()!=null){
				statement.setInt(++index, entity.getPattern().getId()); //id_backtrack_pattern
			}else{
				statement.setNull(++index, Types.INTEGER); //id_backtrack_pattern
			}
			
			if(entity.getFile()!=null){
				statement.setInt(++index, entity.getFile().getId()); //id_class
			}else{
				statement.setNull(++index, Types.INTEGER); //id_class
			}
			
			if(entity.getCodeline().getMethod()!=null){
				statement.setInt(++index, entity.getCodeline().getMethod().getId()); // id_method
			}else{
				statement.setNull(++index, Types.INTEGER); //id_method
			}

			if(entity.getCodeline()!=null && entity.getCodeline().getMethod()!=null){
				statement.setInt(++index, entity.getCodeline().getLineNr());//codeline
			}else{
				statement.setNull(++index, Types.INTEGER); //codeline
			}
				
			if(entity.getConstant()!=null && entity.getConstant().getType()!=null)
			{
				statement.setString(++index, entity.getConstant().getType().toString());	//enum_constanttype
			}else{
				statement.setNull(++index, Types.VARCHAR); //enum_constanttype
			}

			statement.setInt(++index, entity.getArgument()); //argument
			
			if(entity.getConstant()!=null){
				statement.setString(++index, entity.getConstant().getIdentifier()); //identifier
			}else{
				statement.setNull(++index, Types.VARCHAR); //identifier

			}
			if(entity.getConstant()!=null && entity.getConstant().getValue()!=null ){
				statement.setString(++index, entity.getConstant().getValue()); //value
			}else{
				statement.setNull(++index, Types.VARCHAR); //value
			}
			
			if(entity.getConstant()!=null && entity.getConstant().getTypeDescription()!=null){
				statement.setString(++index, entity.getConstant().getTypeDescription()); //type description
			}else{
				statement.setNull(++index, Types.VARCHAR); //type description
			}

			if(entity.getConstant()!=null){
				statement.setInt(++index, entity.getConstant().getArrayDimension()); //array dimension
				statement.setInt(++index, entity.getConstant().getFuzzyLevel()); // fuzzy level
				statement.setInt(++index, entity.getConstant().getSearchId()); // search id
				statement.setBoolean(++index, entity.getConstant().isInAdFrameworkPackage()); //in adframework
				statement.setString(++index, entity.getConstant().getVariableType().toString());	//enum_variabletype
			}
			else{
				statement.setNull(++index, Types.INTEGER); //array dimension
				statement.setNull(++index, Types.INTEGER); // fuzzy level
				statement.setNull(++index, Types.INTEGER); // search id
				statement.setNull(++index, Types.BOOLEAN); //in adframework
				statement.setNull(++index, Types.VARCHAR); //enum_variabletype
			}

			statement.executeUpdate();
			ResultSet rs = statement.getGeneratedKeys();
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
		//otherwise continue persisting the primitive types and 1-1 associations
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#read(int)
	 */
	@Override
	public BTResultInterface read(int id) throws DAOException {
		throw new UnsupportedOperationException("Reading BTResults is currently not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.db.dao.GenericDAO#update(java.lang.Object)
	 */
	@Override
	public boolean update(BTResultInterface entity) throws DAOException, NoSuchEntityException {
		boolean success = false;
		int recordsUpdated;
		int index=0;
		PreparedStatement updateStmt;

		try {
			updateStmt = connection.prepareStatement(DB_QUERY_UPDATE);
			if(entity.getAnalysis()!=null){
				updateStmt.setInt(++index, entity.getAnalysis().getId()); //id_analyses,
			}else{
				updateStmt.setNull(++index, Types.INTEGER);
			}
			
			if(entity.getPattern()!=null){
				updateStmt.setInt(++index, entity.getPattern().getId()); //id_backtrack_pattern
			}else{
				updateStmt.setNull(++index, Types.INTEGER); //id_backtrack_pattern
			}
			
			if(entity.getFile()!=null){
				updateStmt.setInt(++index, entity.getFile().getId()); //id_class
			}else{
				updateStmt.setNull(++index, Types.INTEGER); //id_class
			}
			
			if(entity.getCodeline().getMethod()!=null){
				updateStmt.setInt(++index, entity.getCodeline().getMethod().getId()); // id_method
			}else{
				updateStmt.setNull(++index, Types.INTEGER); //id_method
			}

			if(entity.getCodeline().getMethod()!=null){
				updateStmt.setInt(++index, entity.getCodeline().getLineNr());//codeline
			}else{
				updateStmt.setNull(++index, Types.INTEGER); //codeline
			}
				
			if(entity.getConstant()!=null && entity.getConstant().getType()!=null)
			{
				updateStmt.setString(++index, entity.getConstant().getType().toString());	//enum_constanttype
			}else{
				updateStmt.setNull(++index, Types.VARCHAR); //enum_constanttype
			}

			updateStmt.setInt(++index, entity.getArgument()); //argument
			
			if(entity.getConstant()!=null){
				updateStmt.setString(++index, entity.getConstant().getIdentifier()); //identifier
			}else{
				updateStmt.setNull(++index, Types.VARCHAR); //identifier

			}
			if(entity.getConstant()!=null && entity.getConstant().getValue()!=null ){
				updateStmt.setString(++index, entity.getConstant().getValue()); //value
			}else{
				updateStmt.setNull(++index, Types.VARCHAR); //value
			}
			
			if(entity.getConstant()!=null && entity.getConstant().getTypeDescription()!=null){
				updateStmt.setString(++index, entity.getConstant().getTypeDescription()); //type description
			}else{
				updateStmt.setNull(++index, Types.VARCHAR); //type description
			}

			if(entity.getConstant()!=null){
				updateStmt.setInt(++index, entity.getConstant().getArrayDimension()); //array dimension
				updateStmt.setInt(++index, entity.getConstant().getFuzzyLevel()); // fuzzy level
				updateStmt.setInt(++index, entity.getConstant().getSearchId()); // search id
				updateStmt.setBoolean(++index, entity.getConstant().isInAdFrameworkPackage()); //in adframework
				updateStmt.setString(++index, entity.getConstant().getVariableType().toString());	//enum_variabletype

			}
			else{
				updateStmt.setNull(++index, Types.INTEGER); //array dimension
				updateStmt.setNull(++index, Types.INTEGER); // fuzzy level
				updateStmt.setNull(++index, Types.INTEGER); // search id
				updateStmt.setNull(++index, Types.BOOLEAN); //in adframework
				updateStmt.setNull(++index, Types.VARCHAR); //enum_variabletype
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
				throw new DAOException("Update of one BTPattern affected multiple records. This should not happen!");
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
	public boolean delete(BTResultInterface entity) throws DAOException, NoSuchEntityException {
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
	public List<BTResultInterface> readAll() {
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
	public int findId(BTResultInterface candidate) throws DAOException {
		throw new UnsupportedOperationException("Not implemented");
	}

}
