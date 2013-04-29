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
import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.persistence.sql.BTPatternEntityManager;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.BTPatternInterface;

public class BTPatternEntityManagerTest {

	private Connection connection;
	private BTPatternEntityManager manager;
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
		manager = new BTPatternEntityManager(connection);
	}

	@After
	public void tearDown() throws Exception {
		helper.dropTables();
		connection.close();
	}

	@Test
	public void testSave() throws Exception {
		BTPatternInterface testPattern = new BTPattern("java/lang/String",
				"append", "Ljava/lang/String;", 1, "test pattern");
		assertTrue(manager.save(testPattern));
		assertTrue(testPattern.getId() > 0);
	}

	@Test
	public void testDelete() throws Exception {
		BTPatternInterface testPattern = new BTPattern("java/lang/String",
				"append", "Ljava/lang/String;", 1, "test pattern");
		manager.save(testPattern);
		assertTrue(manager.save(testPattern));
	}

	@Test
	public void testValidate() throws Exception {
		assertFalse(manager.validate(null));
	}

}
