package de.rub.syssec.saaf.db.persistence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rub.syssec.saaf.analysis.steps.slicing.BTPattern;
import de.rub.syssec.saaf.application.MockApplication;
import de.rub.syssec.saaf.application.MockClass;
import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.dao.mysql.MockPackge;
import de.rub.syssec.saaf.db.persistence.sql.ClassEntityManager;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.application.ClassInterface;

public class ClassEntityManagerTest {

	private ClassEntityManager manager;
	private Connection connection;
	private MockApplication mockApplication;
	private MockPackge mockPackage;
	private DatabaseHelper helper;

	@BeforeClass
	public static void setupBeforeClass() throws Exception{
		PropertyConfigurator.configure("conf/log4j.properties");
	}
	
	@Before
	public void setUp() throws Exception {
		helper = new DatabaseHelper(Config.getInstance());
		helper.createDatabaseSchema();
		connection = helper.getConnection();
		manager = new ClassEntityManager(connection);

		new BTPattern("com/test/Tester", "test", "String", 0, "test");
		mockApplication = new MockApplication();
		mockPackage = new MockPackge("Testpackage", "fuzzyhash",
				mockApplication);
		mockPackage.setApplication(mockApplication);
	}

	@After
	public void tearDown() throws Exception {
		helper.dropTables();
		connection.close();
	}

	@Test
	public void testSave() throws Exception {
		ClassInterface mockClass = new MockClass("TestClass", mockPackage);
		assertTrue(manager.save(mockClass));
		assertTrue(mockClass.getId() > 0);
	}

	@Test
	public void testDelete() throws Exception {
		ClassInterface mockClass = new MockClass("TestClass", mockPackage);
		mockClass.setPackage(mockPackage);
		assertTrue(manager.save(mockClass));
		assertTrue(manager.delete(mockClass));
	}

	@Test
	public void testValidate() throws Exception {
		assertFalse(manager.validate(null));
	}

}
