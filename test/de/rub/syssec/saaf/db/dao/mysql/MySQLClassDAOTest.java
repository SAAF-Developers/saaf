package de.rub.syssec.saaf.db.dao.mysql;

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
import de.rub.syssec.saaf.application.MockClass;
import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.db.dao.exceptions.DuplicateEntityException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.interfaces.GenericDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLApplicaitonDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLClassDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLPackageDAO;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.PackageInterface;

public class MySQLClassDAOTest {

	private Connection connection;
	private MySQLClassDAO dao;
	private GenericDAO<PackageInterface> packageDAO;
	private Logger logger=Logger.getLogger(MySQLClassDAOTest.class);
	private MockApplication app;
	private JavaPackage dummyPackage;
	private MySQLApplicaitonDAO appDAO;
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
			dao = new MySQLClassDAO(connection);
			//dao expects that the package a class belongs to has already been persisted
			//to do that we need a MySQLPackageDAO here
			packageDAO = new MySQLPackageDAO(connection);
			//packageDAO in turn expects that the application a package belongs to has already been presided
			//so we need to do that first
			appDAO=new MySQLApplicaitonDAO(connection);
			//first create the application
			app = new MockApplication();

			app.setId(appDAO.create(app));
			assertTrue("Application not created correctly", app.getId()>0);
			//create a package an link the two together
			dummyPackage = new JavaPackage();
			dummyPackage.setApplication(app);
			String name = "Ltest/android";
			dummyPackage.setName(name);
			dummyPackage.setId(packageDAO.create(dummyPackage));
			assertTrue("Package not created correctly", dummyPackage.getId()>0);

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
		//create a class
		ClassInterface newClass = new MockClass("TestClass", dummyPackage);
		int id = dao.create(newClass);
		newClass.setId(id);
		assertTrue("The id returned by create must be a positive integer",id>0);
	}
	
	@Test(expected=DuplicateEntityException.class)
	public void testCreateSameTwice() throws Exception {
		//create a class
		ClassInterface newClass = new MockClass("TestClass", dummyPackage);
		dao.create(newClass);
		dao.create(newClass);
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testRead() throws DAOException {
		dao.read(1);
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testReadNonExisting() throws Exception {
		dao.read(1);
	}

	@Test
	public void testUpdate() throws Exception {
		//create a class
		ClassInterface newClass = new MockClass("TestClass", dummyPackage);
		int id = dao.create(newClass);
		newClass.setId(id);
		assertTrue("Update must return true on successful completion",dao.update(newClass));
	}

	@Test(expected=NoSuchEntityException.class)
	public void testUpdateNonExisting() throws Exception {
		//create a class
		ClassInterface newClass = new MockClass("TestClass", dummyPackage);
		//Do not save it!
		//Try to update it
		dao.update(newClass);
	}
	
	
	@Test
	public void testDelete() throws Exception {
		//create a class
		ClassInterface newClass = new MockClass("TestClass", dummyPackage);
		int id = dao.create(newClass);
		newClass.setId(id);
		assertTrue("Successful deletion must return true",dao.delete(newClass));
		//assertNull("After delete reading a deleted entity must return null",dao.read(id));
	}
	
	@Test
	public void testDeleteAll() throws Exception
	{
		//create a class
		ClassInterface newClass = new MockClass("TestClass", dummyPackage);
		dao.create(newClass);
		ClassInterface newClass2 = new MockClass("TestClass2", dummyPackage);
		dao.create(newClass2);
	}
	
	@Test(expected=NoSuchEntityException.class)
	public void testDeleteNonExisiting() throws Exception {
		//create a class
		ClassInterface newClass = new MockClass("TestClass", dummyPackage);
		//do not save it!
		//try to delete it
		dao.delete(newClass);
	}


}
