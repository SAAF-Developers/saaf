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

import de.rub.syssec.saaf.db.dao.DAOFactory;
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

/**
 * Generates DAO Objects to interact with a MySQL database.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class MySQLDAOFactory extends DAOFactory {

	@Override
	public NuApplicationDAO getApplicationDAO(Connection conn) {
		return new MySQLApplicaitonDAO(conn);
	}

	@Override
	public NuAnalysisDAO getAnalysisDAO(Connection conn) {
		return new MySQLAnalysisDAO(conn);
	}

	@Override
	public NuBTPatternDAO getBTPatternDAO(Connection conn) {
		return new MySQLBTPatternDAO(conn);
	}

	@Override
	public NuBTResultDAO getBTResultDAO(Connection conn) {
		return new MySQLBTResultDAO(conn);
	}

	@Override
	public NuClassDAO getClassDAO(Connection conn) {
		return new MySQLClassDAO(conn);
	}

	@Override
	public NuExceptionDAO getExceptionDAO(Connection conn) {
		return new MySQLExcpetionDAO(conn);
	}

	@Override
	public NuHPatternDAO getHPatternDAO(Connection conn) {
		return new MySQLHPatternDAO(conn);
	}

	@Override
	public NuHResultDAO getHResultDAO(Connection conn) {
		return new MySQLHResultDAO(conn);
	}

	@Override
	public NuMethodDAO getMethodDAO(Connection conn) {
		return new MySQLMethodDAO(conn);
	}

	@Override
	public NuPackageDAO getPackageDAO(Connection conn) {
		return new MySQLPackageDAO(conn);
	}

	@Override
	public NuPermissionDAO getPermissionDAO(Connection conn) {
		return new MySQLPermissionDAO(conn);
	}

	@Override
	public NuPermissionRequestDAO getPermissionRequestDAO(Connection connection) {
		return new MySQLPermissionRequestDAO(connection);
	}

}
