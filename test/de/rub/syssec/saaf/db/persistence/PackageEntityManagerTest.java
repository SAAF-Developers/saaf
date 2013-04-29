package de.rub.syssec.saaf.db.persistence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rub.syssec.saaf.application.MockApplication;
import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.dao.mysql.MockPackge;
import de.rub.syssec.saaf.db.persistence.sql.PackageEntityManager;
import de.rub.syssec.saaf.misc.config.Config;

public class PackageEntityManagerTest {

	private Connection connection;
	private PackageEntityManager manager;
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
		manager = new PackageEntityManager(connection);
		mockApplication = new MockApplication();
	}

	@After
	public void tearDown() throws Exception {
		helper.dropTables();
		connection.close();
	}

	@Test
	public void testSave() throws Exception {
		mockPackage = new MockPackge("Testpackage", "fuzzyhash",
				mockApplication);
		mockPackage.setApplication(mockApplication);
		assertTrue(manager.save(mockPackage));
		assertTrue(mockPackage.getId() > 0);
	}

	@Test
	public void testDelete() throws Exception {
		mockPackage = new MockPackge("Testpackage", "fuzzyhash",
				mockApplication);
		mockPackage.setApplication(mockApplication);
		assertTrue(manager.save(mockPackage));
		assertTrue(manager.delete(mockPackage));
	}

	@Test
	public void testValidate() throws Exception {
		assertFalse(manager.validate(null));
	}

}
