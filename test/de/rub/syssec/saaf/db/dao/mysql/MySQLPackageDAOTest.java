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

import de.rub.syssec.saaf.application.JavaPackage;
import de.rub.syssec.saaf.application.MockApplication;
import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.dao.exceptions.DuplicateEntityException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.interfaces.GenericDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLApplicaitonDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLPackageDAO;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.PackageInterface;

public class MySQLPackageDAOTest {
	
	private Connection connection;
	private GenericDAO<PackageInterface> dao;
	private Logger logger=Logger.getLogger(MySQLClassDAOTest.class);
	private ApplicationInterface app;
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
			helper.createDatabaseSchema();
			//create the dao under test
			dao = new MySQLPackageDAO(connection);
			
			app = new MockApplication();
			MySQLApplicaitonDAO appDAO=new MySQLApplicaitonDAO(connection);
			app.setId(appDAO.create(app));
			
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
		PackageInterface dummyPackage = new JavaPackage("Ltest/android",app,null);
		int id = dao.create(dummyPackage);
		assertTrue("The id returned by MySQLPackageDAO.create must be a positive integer",id>0);
	}
	
	@Test(expected=DuplicateEntityException.class)
	public void testcreateSameTwice() throws Exception
	{
		PackageInterface dummyPackage = new JavaPackage("Ltest/android",app,null);
		dao.create(dummyPackage);
		dao.create(dummyPackage);
	}

	@Test
	public void testRead() throws Exception {
		PackageInterface dummyPackage = new JavaPackage("Ltest/android",app,null);
		int id = dao.create(dummyPackage);
		
		PackageInterface read = dao.read(id);
		assertNotNull("read for an exisiting package must not return null",read);
		assertEquals("Names are not equal","Ltest/android",read.getName(true));
	}
	
	@Test
	public void testReadNonExisting() throws Exception{
		assertNull(dao.read(1));
	}
		
	@Test
	public void testUpdate() throws Exception{
		PackageInterface dummyPackage = new JavaPackage("Ltest/android",app,null);
		int id = dao.create(dummyPackage);
		dummyPackage.setId(id);
		dummyPackage.setName("Ltest/android/omfg");
		assertTrue(dao.update(dummyPackage));
	}
	
	@Test(expected=NoSuchEntityException.class)
	public void testUpdateNonExisting()throws Exception {
		PackageInterface dummyPackage = new JavaPackage();
		String name = "Ltest/android";
		dummyPackage.setName(name);
		//do not create it so it cannot successfully be updated
		dummyPackage.setId(1);
		dao.update(dummyPackage);
	}

	@Test
	public void testDelete() throws Exception{
		PackageInterface dummyPackage = new JavaPackage("Ltest/android",app,null);
		dummyPackage.setId(dao.create(dummyPackage));
		assertTrue("Successful deletion must return true",dao.delete(dummyPackage));
		assertNull("After HTBatternDAO.delete reading a deleted Pattern should return null",dao.read(dummyPackage.getId()));
	}
	
	@Test(expected=NoSuchEntityException.class)
	public void testDeleteNonExisting() throws Exception{
		PackageInterface dummyPackage = new JavaPackage();
		dummyPackage.setApplication(app);
		String name = "Ltest/android";
		dummyPackage.setName(name);
		dao.delete(dummyPackage);
	}

	@Test
	public void testDeleteAll() throws Exception
	{
		PackageInterface dummyPackage = new JavaPackage("Ltest/android",app,null);
		dao.create(dummyPackage);
		PackageInterface dummyPackage2 = new JavaPackage("Ltest/android/omfg",app,null);
		dao.create(dummyPackage2);
		assertTrue(dao.deleteAll()==2);
	}

	@Test
	public void testFindId() throws Exception
	{
		PackageInterface dummyPackage = new JavaPackage("Ltest/android",app,null);
		int id = dao.create(dummyPackage);
		assertEquals("FindId did not find the correct id",id,dao.findId(dummyPackage));
	}

}
