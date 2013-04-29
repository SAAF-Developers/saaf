package de.rub.syssec.saaf.db.dao.mysql;

import static org.junit.Assert.*;

import java.sql.Connection;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
import de.rub.syssec.saaf.db.dao.mysql.MySQLAnalysisDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLApplicaitonDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLBTPatternDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLBTResultDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLClassDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLDAOFactory;
import de.rub.syssec.saaf.db.dao.mysql.MySQLExcpetionDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLHPatternDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLHResultDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLMethodDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLPackageDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLPermissionDAO;

public class MySQLDAOFactoryTest {

	private MySQLDAOFactory factory;
	private Connection conn;

	@BeforeClass
	public static void setupBeforeClass() throws Exception{
		PropertyConfigurator.configure("conf/log4j.properties");
	}
	
	@Before
	public void setUp() throws Exception {
		this.factory = new MySQLDAOFactory();
		this.conn = null;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetApplicationDAO() {
		NuApplicationDAO dao = factory.getApplicationDAO(conn);
		assertNotNull("MySQLDAOFactory must return a NuApplicationDAO",dao);
		assertTrue("MySQLDAOFactory must return DAOs specific for MySQL", dao instanceof MySQLApplicaitonDAO);
	}

	@Test
	public void testGetAnalysisDAO() {
		NuAnalysisDAO dao = factory.getAnalysisDAO(conn);
		assertNotNull("MySQLDAOFactory must return a NuAnalysisDAO",dao);
		assertTrue("MySQLDAOFactory must return DAOs specific for MySQL", dao instanceof MySQLAnalysisDAO);

	}

	@Test
	public void testGetBTPatternDAO() {
		NuBTPatternDAO dao = factory.getBTPatternDAO(conn);
		assertNotNull("MySQLDAOFactory must return a NuBTPatternDAO",dao);
		assertTrue("MySQLDAOFactory must return DAOs specific for MySQL", dao instanceof MySQLBTPatternDAO);
	}

	@Test
	public void testGetBTResultDAO() {
		NuBTResultDAO dao = factory.getBTResultDAO(conn);
		assertNotNull("MySQLDAOFactory must return a NuBTResultDAO",dao);
		assertTrue("MySQLDAOFactory must return DAOs specific for MySQL", dao instanceof MySQLBTResultDAO);
	}

	@Test
	public void testGetClassDAO() {
		NuClassDAO dao = factory.getClassDAO(conn);
		assertNotNull("MySQLDAOFactory must return a NuClassDAO",dao);
		assertTrue("MySQLDAOFactory must return DAOs specific for MySQL", dao instanceof MySQLClassDAO);
	}

	@Test
	public void testGetExceptionDAO() {
		NuExceptionDAO dao = factory.getExceptionDAO(conn);
		assertNotNull("MySQLDAOFactory must return a NuExceptionDAO",dao);
		assertTrue("MySQLDAOFactory must return DAOs specific for MySQL", dao instanceof MySQLExcpetionDAO);
	}

	@Test
	public void testGetHPatternDAO() {
		NuHPatternDAO dao = factory.getHPatternDAO(conn);
		assertNotNull("MySQLDAOFactory must return a NuHPatternDAO",dao);
		assertTrue("MySQLDAOFactory must return DAOs specific for MySQL", dao instanceof MySQLHPatternDAO);
	}

	@Test
	public void testGetHResultDAO() {
		NuHResultDAO dao = factory.getHResultDAO(conn);
		assertNotNull("MySQLDAOFactory must return a NuHResultDAO",dao);
		assertTrue("MySQLDAOFactory must return DAOs specific for MySQL", dao instanceof MySQLHResultDAO);
	}

	@Test
	public void testGetMethodDAO() {
		NuMethodDAO dao = factory.getMethodDAO(conn);
		assertNotNull("MySQLDAOFactory must return a NuMethodDAO",dao);
		assertTrue("MySQLDAOFactory must return DAOs specific for MySQL", dao instanceof MySQLMethodDAO);
	}

	@Test
	public void testGetPackageDAO() {
		NuPackageDAO dao = factory.getPackageDAO(conn);
		assertNotNull("MySQLDAOFactory must return a NuPackageDAO",dao);
		assertTrue("MySQLDAOFactory must return DAOs specific for MySQL", dao instanceof MySQLPackageDAO);
	}
	
	@Test
	public void testGetPermissionDAO() {
		NuPermissionDAO dao = factory.getPermissionDAO(conn);
		assertNotNull("MySQLDAOFactory must return a NuPermissionDAO",dao);
		assertTrue("MySQLDAOFactory must return DAOs specific for MySQL", dao instanceof MySQLPermissionDAO);
	}

}
