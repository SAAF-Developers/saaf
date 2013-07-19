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

import de.rub.syssec.saaf.analysis.MockAnalysis;
import de.rub.syssec.saaf.analysis.steps.slicing.BTPattern;
import de.rub.syssec.saaf.analysis.steps.slicing.BTResult;
import de.rub.syssec.saaf.application.MockApplication;
import de.rub.syssec.saaf.application.MockClass;
import de.rub.syssec.saaf.application.MockCodeLine;
import de.rub.syssec.saaf.application.MockMethod;
import de.rub.syssec.saaf.application.heuristic.MockConstant;
import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.interfaces.GenericDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLAnalysisDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLApplicaitonDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLBTPatternDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLBTResultDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLClassDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLMethodDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLPackageDAO;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.analysis.BTPatternInterface;
import de.rub.syssec.saaf.model.analysis.BTResultInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.ConstantInterface;
import de.rub.syssec.saaf.model.application.MethodInterface;
import de.rub.syssec.saaf.model.application.PackageInterface;

public class MySQLBTResultDAOTest {

	private Connection connection;
	MySQLBTResultDAO dao;
	private Logger logger = Logger.getLogger(MySQLBTResultDAOTest.class);
	private CodeLineInterface mockCodeline;
	private ConstantInterface mockConstant;

	private AnalysisInterface mockAnalysis;
	private MySQLAnalysisDAO analysisDAO;

	private BTPatternInterface mockPattern;
	private MySQLBTPatternDAO btpatternDAO;

	private ClassInterface mockClass;
	private MySQLClassDAO classDAO;

	private ApplicationInterface mockApplication;
	private MySQLApplicaitonDAO applicationDAO;

	private PackageInterface mockPackage;
	private GenericDAO<PackageInterface> packageDAO;

	private MethodInterface mockMethod;
	private MySQLMethodDAO methodDAO;
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
			// create the dao under test
			dao = new MySQLBTResultDAO(connection);

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

			// perisist the application it is the root
			applicationDAO = new MySQLApplicaitonDAO(connection);
			mockApplication.setId(applicationDAO.create(mockApplication));

			// persist the package (required by class)
			packageDAO = new MySQLPackageDAO(connection);
			mockPackage.setId(packageDAO.create(mockPackage));

			// persist the class (required by method)
			classDAO = new MySQLClassDAO(connection);
			mockClass.setId(classDAO.create(mockClass));

			// persist the method(required by ?)
			methodDAO = new MySQLMethodDAO(connection);
			mockMethod.setId(methodDAO.create(mockMethod));

			analysisDAO = new MySQLAnalysisDAO(connection);
			mockAnalysis.setId(analysisDAO.create(mockAnalysis));

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
		BTResultInterface testResult = new BTResult(mockAnalysis, mockPattern,
				mockConstant, 0);
		int id = dao.create(testResult);
		assertTrue("The id returned by create must be a positive integer",
				id > 0);
	}

	@Test
	public void testcreateSameTwice() throws Exception {
		BTResultInterface testResult = new BTResult(mockAnalysis, mockPattern,
				mockConstant, 0);
		dao.create(testResult);
		dao.create(testResult);
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
		BTResultInterface testResult = new BTResult(mockAnalysis, mockPattern,
				mockConstant, 0);
		testResult.setId(dao.create(testResult));
		testResult.setArgument(1);
		assertTrue("Update must return true on successful completion",
				dao.update(testResult));
	}

	@Test(expected = NoSuchEntityException.class)
	public void testUpdateNonExisting() throws Exception {
		BTResultInterface testResult = new BTResult(mockAnalysis, mockPattern,
				mockConstant, 0);
		// do not save it
		dao.update(testResult);
	}

	@Test
	public void testDelete() throws Exception {
		BTResultInterface testResult = new BTResult(mockAnalysis, mockPattern,
				mockConstant, 0);
		testResult.setId(dao.create(testResult));
		dao.delete(testResult);
	}

	@Test
	public void testDeleteAll() throws Exception {
		BTResultInterface testResult = new BTResult(mockAnalysis, mockPattern,
				mockConstant, 0);
		testResult.setId(dao.create(testResult));
		BTResultInterface testResult2 = new BTResult(mockAnalysis, mockPattern,
				mockConstant, 1);
		testResult.setId(dao.create(testResult2));
		assertTrue(dao.deleteAll() == 2);
	}

	@Test(expected = NoSuchEntityException.class)
	public void testDeleteNonExisting() throws Exception {
		BTResultInterface testResult = new BTResult(mockAnalysis, mockPattern,
				mockConstant, 0);
		// do not save it
		dao.delete(testResult);
	}
}
