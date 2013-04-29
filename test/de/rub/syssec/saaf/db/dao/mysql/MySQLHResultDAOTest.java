package de.rub.syssec.saaf.db.dao.mysql;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.LinkedList;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rub.syssec.saaf.analysis.steps.heuristic.HPattern;
import de.rub.syssec.saaf.analysis.steps.heuristic.HResult;
import de.rub.syssec.saaf.application.MockApplication;
import de.rub.syssec.saaf.application.MockClass;
import de.rub.syssec.saaf.application.MockCodeLine;
import de.rub.syssec.saaf.application.MockMethod;
import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.db.dao.exceptions.DuplicateEntityException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.interfaces.GenericDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLAnalysisDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLApplicaitonDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLBTResultDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLClassDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLHPatternDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLHResultDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLMethodDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLPackageDAO;
import de.rub.syssec.saaf.logicTier.MockAnalysis;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.HPatternInterface;
import de.rub.syssec.saaf.model.analysis.HResultInterface;
import de.rub.syssec.saaf.model.analysis.PatternType;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.PackageInterface;

public class MySQLHResultDAOTest {
	
	
	
	@BeforeClass
	public static void setupBeforeClass() throws Exception{
		PropertyConfigurator.configure("conf/log4j.properties");
	}

	private Connection connection;
	private MySQLBTResultDAO dao;
	private HPatternInterface mockPattern;
	private MockApplication mockApplication;
	private MockPackge mockPackage;
	private MockClass mockClass;
	private MockCodeLine mockCodeline;
	private LinkedList<CodeLineInterface> lines;
	private MockMethod mockMethod;
	private MockAnalysis mockAnalysis;
	private MySQLApplicaitonDAO applicationDAO;
	private GenericDAO<PackageInterface> packageDAO;
	private MySQLClassDAO classDAO;
	private MySQLMethodDAO methodDAO;
	private MySQLAnalysisDAO analysisDAO;
	private MySQLHPatternDAO hpatternDAO;
	private MySQLHResultDAO hresultDAO;
	private DatabaseHelper helper;
	private MockCodeLine mockCodeline2;
		
	@Before
	public void setUp() throws Exception {
		helper = new DatabaseHelper(Config.getInstance());
		helper.createDatabaseSchema();
		connection = helper.getConnection();
		DatabaseHelper helper = new DatabaseHelper(Config.getInstance());
		helper.createDatabaseSchema();
		//create the dao under test
		dao = new MySQLBTResultDAO(connection);

		mockPattern = new HPattern("android/telephony/TelephonyManager->getSubscriberId", PatternType.INVOKE, -55, "IMSI");
		mockApplication = new MockApplication();
		mockPackage = new MockPackge("Testpackage","fuzzyhash",mockApplication);
		mockPackage.setApplication(mockApplication);
		mockClass = new MockClass("TestClass", mockPackage);
		byte[] bytes = {0x01};
		mockCodeline = new MockCodeLine(bytes, 0, mockClass);
		mockCodeline2 = new MockCodeLine(bytes, 2, mockClass);
		
		lines = new LinkedList<CodeLineInterface>();
		lines.add(mockCodeline);
		mockMethod = new MockMethod("test", mockClass, lines);
		mockCodeline.setMethod(mockMethod);
		
		mockAnalysis = new MockAnalysis();
		mockAnalysis.setApp(mockApplication);
		
		
		//persist pattern first (it is independent from the other objects)
		hpatternDAO = new MySQLHPatternDAO(connection);
		mockPattern.setId(hpatternDAO.create(mockPattern));

		//perisist the application it is the root
		applicationDAO = new MySQLApplicaitonDAO(connection);
		mockApplication.setId(applicationDAO.create(mockApplication));

		//persist the package (required by class)
		packageDAO = new MySQLPackageDAO(connection);
		mockPackage.setId(packageDAO.create(mockPackage));
		

		//persist the class (required by method)
		classDAO = new MySQLClassDAO(connection);
		mockClass.setId(classDAO.create(mockClass));
		

		//persist the method(required by ?)
		methodDAO = new MySQLMethodDAO(connection);
		mockMethod.setId(methodDAO.create(mockMethod));
		
		analysisDAO = new MySQLAnalysisDAO(connection);
		mockAnalysis.setId(analysisDAO.create(mockAnalysis));
		
		hresultDAO = new MySQLHResultDAO(connection);
	}

	@After
	public void tearDown() throws Exception {
		helper.dropTables();
		connection.close();
	}

	@Test
	public void testCreate() throws Exception {
		HResultInterface testResult = new HResult(mockAnalysis, mockPattern, mockCodeline);
		int id = hresultDAO.create(testResult);
		assertTrue("The id returned by create must be a positive integer",id>0);
	}
	
	@Test
	public void testCreateSameTwice() throws Exception, DuplicateEntityException {
		HResultInterface testResult = new HResult(mockAnalysis, mockPattern, mockCodeline);
		hresultDAO.create(testResult);
		hresultDAO.create(testResult);
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
		HResultInterface testResult = new HResult(mockAnalysis, mockPattern, mockCodeline);
		testResult.setId(hresultDAO.create(testResult));
		testResult.setCodeline(mockCodeline2);
		assertTrue("Update must return true on successful completion",hresultDAO.update(testResult));
	}

	@Test(expected=NoSuchEntityException.class)
	public void testUpdateNonExisting() throws Exception {
		HResultInterface testResult = new HResult(mockAnalysis, mockPattern, mockCodeline);
		//do not create it, so it does not exist
		testResult.setCodeline(mockCodeline2);
		hresultDAO.update(testResult);
	}
	
	@Test
	public void testDelete() throws Exception {
		HResultInterface testResult = new HResult(mockAnalysis, mockPattern, mockCodeline);
		testResult.setId(hresultDAO.create(testResult));
		assertTrue("Delete must return true on successful completion",hresultDAO.delete(testResult));
	}
	
	@Test
	public void testDeleteAll() throws Exception
	{
		hresultDAO.create(new HResult(mockAnalysis, mockPattern, mockCodeline));
		hresultDAO.create(new HResult(mockAnalysis, mockPattern, mockCodeline));
		assertTrue(hresultDAO.deleteAll()==2);
	}
	
	@Test(expected=NoSuchEntityException.class)
	public void testDeleteNonExisting() throws Exception {
		HResultInterface testResult = new HResult(mockAnalysis, mockPattern, mockCodeline);
		//do not create it, so it does not exist
		hresultDAO.delete(testResult);
	}

}
