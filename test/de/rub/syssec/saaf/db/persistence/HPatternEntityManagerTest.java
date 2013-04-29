package de.rub.syssec.saaf.db.persistence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rub.syssec.saaf.analysis.steps.heuristic.HPattern;
import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.persistence.sql.HPatternEntityManager;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.HPatternInterface;
import de.rub.syssec.saaf.model.analysis.PatternType;

public class HPatternEntityManagerTest {

	private Connection connection;
	private HPatternEntityManager manager;
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
		manager = new HPatternEntityManager(connection);

	}

	@After
	public void tearDown() throws Exception {
		helper.dropTables();
		connection.close();
	}

	@Test
	public void testSave() throws Exception {
		HPatternInterface newPattern = new HPattern(
				"android/telephony/TelephonyManager->getSubscriberId",
				PatternType.INVOKE, -55, "test pattern");
		assertTrue(manager.save(newPattern));
		assertTrue(newPattern.getId() > 0);
	}

	@Test
	public void testDelete() throws Exception {
		HPatternInterface newPattern = new HPattern(
				"android/telephony/TelephonyManager->getSubscriberId",
				PatternType.INVOKE, -55, "test pattern");
		manager.save(newPattern);
		assertTrue(manager.delete(newPattern));
	}

	@Test
	public void testValidate() throws Exception {
		assertFalse(manager.validate(null));
	}

}
