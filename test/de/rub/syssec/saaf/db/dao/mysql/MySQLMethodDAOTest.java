package de.rub.syssec.saaf.db.dao.mysql;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rub.syssec.saaf.application.MockApplication;
import de.rub.syssec.saaf.application.MockClass;
import de.rub.syssec.saaf.application.MockCodeLine;
import de.rub.syssec.saaf.application.MockMethod;
import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.db.dao.exceptions.DuplicateEntityException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.interfaces.GenericDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLApplicaitonDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLClassDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLMethodDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLPackageDAO;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.MethodInterface;
import de.rub.syssec.saaf.model.application.PackageInterface;

public class MySQLMethodDAOTest {

	private Connection connection;
	private Logger logger = Logger.getLogger(MySQLMethodDAOTest.class);

	private ClassInterface mockClass;
	private MySQLClassDAO classDAO;

	private ApplicationInterface mockApplication;
	private MySQLApplicaitonDAO applicationDAO;

	private PackageInterface mockPackage;
	private GenericDAO<PackageInterface> packageDAO;

	private MySQLMethodDAO dao;
	private LinkedList<CodeLineInterface> lines;
	private DatabaseHelper helper;

	@BeforeClass
	public static void setupBeforeClass() throws Exception {
		PropertyConfigurator.configure("conf/log4j.properties");
	}

	@Before
	public void setUp() throws Exception {
		try {

			helper = new DatabaseHelper(Config.getInstance());
			helper.createDatabaseSchema();
			connection = helper.getConnection();
			mockApplication = new MockApplication();
			mockPackage = new MockPackge("Testpackage", "fuzzyhash",
					mockApplication);
			mockPackage.setApplication(mockApplication);
			mockClass = new MockClass("TestClass", mockPackage);

			// since a Method belongs to a class which in turn belongs a package
			// which belongs to an application
			// we need some more DAOs
			applicationDAO = new MySQLApplicaitonDAO(connection);
			mockApplication.setId(applicationDAO.create(mockApplication));

			packageDAO = new MySQLPackageDAO(connection);
			mockPackage.setId(packageDAO.create(mockPackage));

			classDAO = new MySQLClassDAO(connection);
			mockClass.setId(classDAO.create(mockClass));
			// create the dao under test
			dao = new MySQLMethodDAO(connection);
			lines = new LinkedList<CodeLineInterface>();
			byte[] bytes = {};
			lines.add(new MockCodeLine(bytes, 0, mockClass));

		} catch (Exception e) {
			logger.error("Unable to connect to DB!", e);
		}
	}

	@After
	public void tearDown() throws Exception {
		helper.dropTables();
		connection.close();
	}

	@Test
	public void testCreate() throws Exception {
		MethodInterface testMethod = new MockMethod("test", mockClass, lines);
		int id = dao.create(testMethod);
		assertTrue("The id returned by create must be a positive integer",
				id > 0);
	}

	@Test(expected = DuplicateEntityException.class)
	public void testCreateSameTwice() throws Exception,
			DuplicateEntityException {
		MockMethod testMethod = new MockMethod("test", mockClass, lines);
		testMethod.setParameterString("I");
		testMethod.setReturnValueString("I");
		dao.create(testMethod);
		dao.create(testMethod);
	}
	
	@Test
	public void testCreateOverloadedMethod() throws Exception
	{
		//create a method with name foo and param int
		MethodInterface testMethod = new MockMethod("foo", mockClass, lines);
		testMethod.setParameterString("I");
		MethodInterface testMethod2 = new MockMethod("foo", mockClass, lines);
		testMethod2.setParameterString("Ljava/lang/String;");
		assertTrue(dao.create(testMethod)>0);
		assertTrue(dao.create(testMethod2)>0);
		//create a method with name foo and param string
		//the second create should not reveal an error
	}
	
	//test for the rare case that a method was overloading using the return type
	//while this is not possible in java source code it is possible in the vm.
	@Test
	public void testCreateOverloadedReturnMethod() throws Exception
	{
		//create a method with name foo and param int
		MockMethod testMethod = new MockMethod("foo", mockClass, lines);
		testMethod.setParameterString("I");
		testMethod.setReturnValueString("I");
		MockMethod testMethod2 = new MockMethod("foo", mockClass, lines);
		testMethod2.setParameterString("I");
		testMethod2.setReturnValueString("Ljava/lang/String;");
		assertTrue(dao.create(testMethod)>0);
		assertTrue(dao.create(testMethod2)>0);
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
		MethodInterface testMethod = new MockMethod("test", mockClass, lines);
		testMethod.setId(dao.create(testMethod));
		testMethod.setName("test");
		assertTrue("Update must return true on successful completion",
				dao.update(testMethod));
	}

	@Test(expected = NoSuchEntityException.class)
	public void testUpdateNonExisting() throws Exception {
		MethodInterface testMethod = new MockMethod("test", mockClass, lines);
		// do not save it
		dao.update(testMethod);
	}

	@Test
	public void testDelete() throws Exception {
		MethodInterface testMethod = new MockMethod("test", mockClass, lines);
		testMethod.setId(dao.create(testMethod));
		assertTrue("Delete must return true on successful completion",
				dao.delete(testMethod));
	}

	@Test(expected = NoSuchEntityException.class)
	public void testDeleteNonExisting() throws Exception {
		MethodInterface testMethod = new MockMethod("test", mockClass, lines);
		// do not save it
		dao.delete(testMethod);
	}

	@Test
	public void testDeleteAll() throws Exception {
		dao.create(new MockMethod("test", mockClass, lines));
		dao.create(new MockMethod("test2", mockClass, lines));
		assertTrue(dao.deleteAll() == 2);
	}
}
