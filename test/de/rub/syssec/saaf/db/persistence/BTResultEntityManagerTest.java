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

import de.rub.syssec.saaf.analysis.steps.slicing.BTPattern;
import de.rub.syssec.saaf.analysis.steps.slicing.BTResult;
import de.rub.syssec.saaf.application.MockApplication;
import de.rub.syssec.saaf.application.MockClass;
import de.rub.syssec.saaf.application.MockCodeLine;
import de.rub.syssec.saaf.application.MockMethod;
import de.rub.syssec.saaf.application.heuristic.MockConstant;
import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.dao.mysql.MockPackge;
import de.rub.syssec.saaf.db.dao.mysql.MySQLBTPatternDAO;
import de.rub.syssec.saaf.db.persistence.sql.BTResultEntityManager;
import de.rub.syssec.saaf.logicTier.MockAnalysis;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.BTResultInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.ConstantInterface;

public class BTResultEntityManagerTest {

	private Connection connection;
	private BTResultEntityManager manager;
	private BTPattern mockPattern;
	private MockApplication mockApplication;
	private MockPackge mockPackage;
	private MockClass mockClass;
	private MockCodeLine mockCodeline;
	private MockConstant mockConstant;
	private LinkedList<CodeLineInterface> lines;
	private MockMethod mockMethod;
	private MockAnalysis mockAnalysis;
	private MySQLBTPatternDAO btpatternDAO;
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
		manager = new BTResultEntityManager(connection);

		mockPattern = new BTPattern("com/test/Tester", "test", "String", 0,
				"test");
		mockApplication = new MockApplication();
		mockPackage = new MockPackge("Testpackage", "fuzzyhash",
				mockApplication);
		mockPackage.setApplication(mockApplication);
		mockClass = new MockClass("TestClass", mockPackage);
		byte[] bytes = {};
		mockCodeline = new MockCodeLine(bytes, 0, mockClass);
		mockConstant = new MockConstant("just testing", 0,
				ConstantInterface.Type.STRING, ConstantInterface.VariableType.INTERNAL_BYTECODE_OP, "something", "other thing",
				mockCodeline, 0, false, "some/thing", false, 0);

		lines = new LinkedList<CodeLineInterface>();
		lines.add(mockCodeline);
		mockMethod = new MockMethod("test", mockClass, lines);
		mockCodeline.setMethod(mockMethod);

		mockAnalysis = new MockAnalysis();
		mockAnalysis.setApp(mockApplication);

		// persist pattern first (it is independent from the other objects)
		btpatternDAO = new MySQLBTPatternDAO(connection);
		mockPattern.setId(btpatternDAO.create(mockPattern));
	}

	@After
	public void tearDown() throws Exception {
		helper.dropTables();
		connection.close();
	}

	@Test
	public void testSave() throws Exception {
		BTResultInterface testResult = new BTResult(mockAnalysis, mockPattern,
				mockConstant, 0);
		assertTrue(manager.save(testResult));
		assertTrue(testResult.getId() > 0);
	}

	@Test
	public void testDelete() throws Exception {
		BTResultInterface testResult = new BTResult(mockAnalysis, mockPattern,
				mockConstant, 0);
		assertTrue(manager.save(testResult));
		assertTrue(manager.delete(testResult));
	}

	@Test
	public void testValidate() throws Exception {
		assertFalse(manager.validate(null));
	}

}
