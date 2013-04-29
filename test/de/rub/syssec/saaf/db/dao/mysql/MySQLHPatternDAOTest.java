package de.rub.syssec.saaf.db.dao.mysql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rub.syssec.saaf.analysis.steps.heuristic.HPattern;
import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.db.dao.exceptions.DuplicateEntityException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.mysql.MySQLHPatternDAO;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.HPatternInterface;
import de.rub.syssec.saaf.model.analysis.PatternType;

public class MySQLHPatternDAOTest {
	
	private Connection connection;
	MySQLHPatternDAO dao;
	private Logger logger=Logger.getLogger(MySQLHPatternDAOTest.class);
	private DatabaseHelper helper;
	
	@BeforeClass
	public static void setupBeforeClass() throws Exception{
		PropertyConfigurator.configure("conf/log4j.properties");
	}

	@Before
	public void setUp() throws Exception {
		try {
			
			helper = new DatabaseHelper(Config.getInstance());
			helper.createDatabaseSchema();
			connection = helper.getConnection();
			//create the dao under test
			dao = new MySQLHPatternDAO(connection);
		} catch (Exception e) {
			logger.error("Unable to connect to DB!",e);
		}
	}

	@After
	public void tearDown() throws Exception {
		helper.dropTables();
		connection.close();
	}

	@Test
	public void testCreate() throws Exception {
		HPatternInterface newPattern = new HPattern("android/telephony/TelephonyManager->getSubscriberId", PatternType.INVOKE, -55, "test pattern");
		int id = dao.create(newPattern);
		assertTrue("The id returned by MySQLHPatternDAO.create must be a positive integer",id>0);
	}
	
	@Test(expected=DuplicateEntityException.class)
	public void testcreateSameTwice() throws DAOException, DuplicateEntityException
	{
		HPatternInterface newPattern = new HPattern("android/telephony/TelephonyManager->getSubscriberId", PatternType.INVOKE, -55, "test pattern");
		dao.create(newPattern);
		dao.create(newPattern);
	}
	
	@Test(expected=DAOException.class)
	public void testcreateWithNullPattern() throws DAOException, DuplicateEntityException
	{
		HPatternInterface newPattern = new HPattern(null, PatternType.INVOKE, -55, "test pattern");;
		dao.create(newPattern);
	}
	
	@Test(expected=DAOException.class)
	public void testcreateWithNullPatternType() throws DAOException, DuplicateEntityException
	{
		HPatternInterface newPattern = new HPattern("android/telephony/TelephonyManager->getSubscriberId", null, -55, "test pattern");
		dao.create(newPattern);
		dao.create(newPattern);
	}

	@Test
	public void testRead() throws Exception {
		HPatternInterface newPattern = new HPattern("android/telephony/TelephonyManager->getSubscriberId", PatternType.INVOKE, -55, "test pattern");
		int id=dao.create(newPattern);
		HPatternInterface read = dao.read(id);
		assertNotNull("There was no object read from the database",read);
		assertEquals("The id was not set correctly",1,read.getId());
	}

	@Test
	public void testReadNonExisting() throws Exception
	{
		assertNull("Trying to read an non-existent HTPattern should return null",dao.read(1));
	}
	
	@Test
	public void testUpdate() throws Exception {
		HPattern newPattern = new HPattern("android/telephony/TelephonyManager->getSubscriberId", PatternType.INVOKE, -55, "test pattern");
		int id = dao.create(newPattern);
		newPattern.setId(id);
		String changedPattern = "android/yourmama";
		newPattern.setPattern(changedPattern);
		dao.update(newPattern);
		HPatternInterface read =dao.read(id);
		assertEquals("Descriiption has not been changed",changedPattern,read.getPattern());
	}

	@Test(expected=NoSuchEntityException.class)
	public void testUpdateNonExisting()throws Exception
	{
		HPattern newPattern = new HPattern("android/telephony/TelephonyManager->getSubscriberId", PatternType.INVOKE, -55, "test pattern");
		dao.update(newPattern);
	}
	
	@Test
	public void testDelete() throws Exception {
		HPattern newPattern = new HPattern("android/telephony/TelephonyManager->getSubscriberId", PatternType.INVOKE, -55, "test pattern");
		int id=dao.create(newPattern);
		newPattern.setId(id);
		assertTrue("Successful deletion must return true",dao.delete(newPattern));
		assertNull("After HTBatternDAO.delete reading a deleted Pattern should return null",dao.read(id));

	}

	@Test
	public void testDeleteAll() throws Exception
	{
		HPattern newPattern = new HPattern("android/telephony/TelephonyManager->getSubscriberId", PatternType.INVOKE, -55, "test pattern");
		dao.create(newPattern);
		HPattern newPattern2 = new HPattern("android/telephony/TelephonyManager->getDeviceId", PatternType.INVOKE, -75, "test pattern");
		dao.create(newPattern2);
		assertTrue(dao.deleteAll()==2);
	}
	
	@Test(expected=NoSuchEntityException.class)
	public void testDeleteNonExisiting()throws Exception
	{
		HPattern newPattern = new HPattern("android/telephony/TelephonyManager->getSubscriberId", PatternType.INVOKE, -55, "test pattern");
		dao.delete(newPattern);
	}
}
