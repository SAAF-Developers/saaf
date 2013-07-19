package de.rub.syssec.saaf.db.persistence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rub.syssec.saaf.analysis.MockAnalysis;
import de.rub.syssec.saaf.application.MockApplication;
import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.persistence.sql.AnalysisEntityManager;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;

public class AnalysisEntityManagerTest {

	private Connection connection;
	private AnalysisEntityManager manager;
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
		manager = new AnalysisEntityManager(connection);
	}

	@After
	public void tearDown() throws Exception {
		helper.dropTables();
		connection.close();
	}

	@Test
	public void testSave() throws Exception {
		ApplicationInterface app = new MockApplication();
		AnalysisInterface ana = new MockAnalysis();
		ana.setApp(app);

		assertTrue(manager.save(ana));
		assertTrue(ana.getId()>0);
	}

	@Test
	public void testDelete() throws Exception {
		ApplicationInterface app = new MockApplication();
		AnalysisInterface ana = new MockAnalysis();
		ana.setApp(app);
		manager.save(ana);
		assertTrue(manager.delete(ana));
	}

	@Test
	public void testValidate() throws Exception {
		assertFalse(manager.validate(null));
	}
	
	@Test 
	public void testcountAllByApp() throws Exception
	{
		ApplicationInterface app = new MockApplication();
		AnalysisInterface ana = new MockAnalysis();
		ana.setApp(app);
		manager.save(ana);
		assertTrue(manager.countAllByApp(app)==1);
	}

}
