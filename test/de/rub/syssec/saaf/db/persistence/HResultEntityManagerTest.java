package de.rub.syssec.saaf.db.persistence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.LinkedList;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rub.syssec.saaf.analysis.MockAnalysis;
import de.rub.syssec.saaf.analysis.steps.heuristic.HPattern;
import de.rub.syssec.saaf.analysis.steps.heuristic.HResult;
import de.rub.syssec.saaf.application.MockApplication;
import de.rub.syssec.saaf.application.MockClass;
import de.rub.syssec.saaf.application.MockCodeLine;
import de.rub.syssec.saaf.application.MockMethod;
import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.dao.mysql.MockPackge;
import de.rub.syssec.saaf.db.persistence.sql.HResultEntityManager;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.HResultInterface;
import de.rub.syssec.saaf.model.analysis.PatternType;
import de.rub.syssec.saaf.model.application.CodeLineInterface;

public class HResultEntityManagerTest {

	private Connection connection;
	private HResultEntityManager manager;
	private HPattern mockPattern;
	private MockApplication mockApplication;
	private MockPackge mockPackage;
	private MockClass mockClass;
	private MockCodeLine mockCodeline;
	private LinkedList<CodeLineInterface> lines;
	private MockMethod mockMethod;
	private MockAnalysis mockAnalysis;
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
		manager = new HResultEntityManager(connection);

		mockPattern = new HPattern(
				"android/telephony/TelephonyManager->getSubscriberId",
				PatternType.INVOKE, -55, "IMSI");
		mockApplication = new MockApplication();
		mockPackage = new MockPackge("Testpackage", "fuzzyhash",
				mockApplication);
		mockPackage.setApplication(mockApplication);
		mockClass = new MockClass("TestClass", mockPackage);
		byte[] bytes = {};
		mockCodeline = new MockCodeLine(bytes, 0, mockClass);

		lines = new LinkedList<CodeLineInterface>();
		lines.add(mockCodeline);
		mockMethod = new MockMethod("test", mockClass, lines);
		mockCodeline.setMethod(mockMethod);

		mockAnalysis = new MockAnalysis();
		mockAnalysis.setApp(mockApplication);

	}

	@After
	public void tearDown() throws Exception {
		helper.dropTables();
		connection.close();
	}

	@Test
	public void testSave() throws Exception {
		HResultInterface testResult = new HResult(mockAnalysis, mockPattern,
				mockCodeline);
		assertTrue(manager.save(testResult));
		assertTrue(testResult.getId() > 0);
	}

	@Test
	public void testDelete() throws Exception {
		HResultInterface testResult = new HResult(mockAnalysis, mockPattern,
				mockCodeline);
		assertTrue(manager.save(testResult));
		assertTrue(manager.delete(testResult));
	}

	@Test
	public void testValidate() throws Exception {
		assertFalse(manager.validate(null));
	}

}
