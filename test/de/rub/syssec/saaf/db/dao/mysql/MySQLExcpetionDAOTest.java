/**
 * 
 */
package de.rub.syssec.saaf.db.dao.mysql;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rub.syssec.saaf.analysis.MockAnalysis;
import de.rub.syssec.saaf.application.MockApplication;
import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.db.dao.exceptions.DuplicateEntityException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.mysql.MySQLAnalysisDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLApplicaitonDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLExcpetionDAO;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.SAAFException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class MySQLExcpetionDAOTest {

	@BeforeClass
	public static void setupBeforeClass() throws Exception{
		PropertyConfigurator.configure("conf/log4j.properties");
	}

	private DatabaseHelper helper;
	private Connection connection;
	private Logger logger=Logger.getLogger(getClass());
	private MySQLExcpetionDAO dao;

	private AnalysisInterface mockanalysis;
	private MySQLAnalysisDAO analysisDAO;

	private ApplicationInterface mockapplication;
	private MySQLApplicaitonDAO applicationDAO;
	private MockAnalysis mockanalysis2;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		try {
			
			helper = new DatabaseHelper(Config.getInstance());
			helper.createDatabaseSchema();
			connection = helper.getConnection();
			//create the dao under test
			dao = new MySQLExcpetionDAO(connection);
			mockapplication = new MockApplication();
			applicationDAO = new MySQLApplicaitonDAO(connection);
			
			mockanalysis = new MockAnalysis();
			mockanalysis.setApp(mockapplication);
			//make a second one so we can test update later (attach exception to different analysis)
			mockanalysis2 = new MockAnalysis();
			mockanalysis2.setApp(mockapplication);

			analysisDAO = new MySQLAnalysisDAO(connection);
			mockapplication.setId(applicationDAO.create(mockapplication));
			
			mockanalysis.setId(analysisDAO.create(mockanalysis));
			mockanalysis2.setId(analysisDAO.create(mockanalysis2));


			
		} catch (Exception e) {
			logger.error("Unable to connect to DB!",e);
		}
	}

	@After
	public void tearDown() throws Exception {
		helper.dropTables();
		connection.close();
	}

	/**
	 * Test method for {@link de.rub.syssec.saaf.saaf.db.dao.mysql.MySQLExcpetionDAO#create(de.rub.syssec.saaf.saaf.model.SAAFException)}.
	 * @throws DuplicateEntityException 
	 * @throws DAOException 
	 */
	@Test
	public void testCreate() throws DAOException, DuplicateEntityException {
		SAAFException exception = new SAAFException("Error in analysis");
		exception.setAnalysis(mockanalysis);
		int id = dao.create(exception);
		assertTrue("The id returned by MySQLBTPatternDAO.create must be a positive integer",id>0);
	}

	/**
	 * Test method for {@link de.rub.syssec.saaf.saaf.db.dao.mysql.MySQLExcpetionDAO#read(int)}.
	 * @throws DAOException 
	 */
	@Test(expected=UnsupportedOperationException.class)
	public void testRead() throws DAOException {
		dao.read(1);
	}

	/**
	 * Test method for {@link de.rub.syssec.saaf.saaf.db.dao.mysql.MySQLExcpetionDAO#update(de.rub.syssec.saaf.saaf.model.SAAFException)}.
	 * @throws Exception 
	 * @throws DAOException 
	 */
	@Test
	public void testUpdate() throws DAOException, Exception {
		SAAFException exception = new SAAFException("Error in analysis");
		exception.setAnalysis(mockanalysis);
		exception.setId(dao.create(exception));
		assertTrue(mockanalysis.getId()!=mockanalysis2.getId());
		exception.setAnalysis(mockanalysis2);
		assertTrue(dao.update(exception));
	}

	/**
	 * Test method for {@link de.rub.syssec.saaf.saaf.db.dao.mysql.MySQLExcpetionDAO#delete(de.rub.syssec.saaf.saaf.model.SAAFException)}.
	 * @throws DuplicateEntityException 
	 * @throws DAOException 
	 * @throws NoSuchEntityException 
	 */
	@Test
	public void testDelete() throws DAOException, DuplicateEntityException, NoSuchEntityException {
		SAAFException exception = new SAAFException("Error in analysis");
		exception.setAnalysis(mockanalysis);
		exception.setId(dao.create(exception));
		assertTrue(dao.delete(exception));
	}

	/**
	 * Test method for {@link de.rub.syssec.saaf.saaf.db.dao.mysql.MySQLExcpetionDAO#readAll()}.
	 */
	@Test(expected=UnsupportedOperationException.class)
	public void testReadAll() {
		dao.readAll();
	}

	/**
	 * Test method for {@link de.rub.syssec.saaf.saaf.db.dao.mysql.MySQLExcpetionDAO#deleteAll()}.
	 * @throws DuplicateEntityException 
	 * @throws DAOException 
	 */
	@Test
	public void testDeleteAll() throws DAOException, DuplicateEntityException {
		SAAFException exception = new SAAFException("Error in analysis");
		exception.setAnalysis(mockanalysis);
		SAAFException exception2 = new SAAFException("Yet another error in analysis");
		exception2.setAnalysis(mockanalysis);
		exception.setId((dao.create(exception)));
		exception2.setId((dao.create(exception2)));
		assertTrue(dao.deleteAll()==2);

	}

	/**
	 * Test method for {@link de.rub.syssec.saaf.saaf.db.dao.mysql.MySQLExcpetionDAO#findId(de.rub.syssec.saaf.saaf.model.SAAFException)}.
	 * @throws Exception 
	 */
	@Test(expected=UnsupportedOperationException.class)
	public void testFindId() throws Exception {
		SAAFException exception = new SAAFException("Error in analysis");
		exception.setAnalysis(mockanalysis);
		dao.findId(exception);
	}

}
