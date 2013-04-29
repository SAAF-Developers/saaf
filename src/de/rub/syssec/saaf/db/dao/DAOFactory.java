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
package de.rub.syssec.saaf.db.dao;

import java.sql.Connection;

import de.rub.syssec.saaf.db.dao.interfaces.NuAnalysisDAO;
import de.rub.syssec.saaf.db.dao.interfaces.NuApplicationDAO;
import de.rub.syssec.saaf.db.dao.interfaces.NuBTPatternDAO;
import de.rub.syssec.saaf.db.dao.interfaces.NuBTResultDAO;
import de.rub.syssec.saaf.db.dao.interfaces.NuClassDAO;
import de.rub.syssec.saaf.db.dao.interfaces.NuExceptionDAO;
import de.rub.syssec.saaf.db.dao.interfaces.NuHPatternDAO;
import de.rub.syssec.saaf.db.dao.interfaces.NuHResultDAO;
import de.rub.syssec.saaf.db.dao.interfaces.NuMethodDAO;
import de.rub.syssec.saaf.db.dao.interfaces.NuPackageDAO;
import de.rub.syssec.saaf.db.dao.interfaces.NuPermissionDAO;
import de.rub.syssec.saaf.db.dao.interfaces.NuPermissionRequestDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLDAOFactory;


/**
 * Superclass that provides the groundwork for specific DAO facotries
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public abstract class DAOFactory {
	
	public static final int MYSQL_DIALECT = 1;
	
	public abstract NuApplicationDAO getApplicationDAO(Connection conn);
	public abstract NuAnalysisDAO getAnalysisDAO(Connection conn);
	public abstract NuBTPatternDAO getBTPatternDAO(Connection conn);
	public abstract NuBTResultDAO	getBTResultDAO(Connection conn);
	public abstract NuClassDAO getClassDAO(Connection conn);
	public abstract NuExceptionDAO getExceptionDAO(Connection conn);
	public abstract NuHPatternDAO getHPatternDAO(Connection conn);
	public abstract NuHResultDAO getHResultDAO(Connection conn);
	public abstract NuMethodDAO getMethodDAO(Connection conn);
	public abstract NuPackageDAO getPackageDAO(Connection conn);
	public abstract NuPermissionDAO getPermissionDAO(Connection conn);
	public abstract NuPermissionRequestDAO getPermissionRequestDAO(Connection connection);
	
	public static DAOFactory getDAOFactory(int whichfactory)
	{
		switch(whichfactory){
		case MYSQL_DIALECT: return new MySQLDAOFactory();
			default: return new MySQLDAOFactory();
		}
	}
	

}
