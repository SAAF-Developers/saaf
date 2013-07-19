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
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.mysql.MySQLAnalysisDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLApplicaitonDAO;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;

public class MySQLAnalysisDAOTest {

	private Connection connection;
	private Logger logger = Logger.getLogger(MySQLAnalysisDAOTest.class);
	private MockApplication app;
	private MySQLAnalysisDAO analysisDAO;
	private DatabaseHelper helper;

	@BeforeClass
	public static void setupBeforeClass() throws Exception{
		PropertyConfigurator.configure("conf/log4j.properties");
	}
	
	@Before
	public void setUp() throws Exception {
		try {
			helper = new DatabaseHelper(Config.getInstance());
			helper.createDatabaseSchema();
			connection = helper.getConnection();
			//we need an  application object in the database so we need an ApplicationDAO
			MySQLApplicaitonDAO appDAO = new MySQLApplicaitonDAO(connection);
			app = new MockApplication();
			app.setId(appDAO.create(app));
			analysisDAO = new MySQLAnalysisDAO(connection);
			

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
		AnalysisInterface ana = new MockAnalysis();
		ana.setApp(app);
		int id = analysisDAO.create(ana);
		assertTrue("The create method must return an id > 0 on successful completion",id>0);		
	}
	
	@Test
	public void testcreateSameTwice() throws Exception {
		AnalysisInterface ana = new MockAnalysis();
		ana.setApp(app);
		analysisDAO.create(ana);
		analysisDAO.create(ana);
	}

	//currently we expect this to fail since reading is not implemented.
	@Test(expected=UnsupportedOperationException.class)
	public void testRead() throws DAOException {
		analysisDAO.read(1);
	}

	//currently we expect this to fail since reading is not implemented.
	@Test(expected=UnsupportedOperationException.class)
	public void testReadNonExisting() throws Exception {
		analysisDAO.read(1);
	}

	@Test
	public void testUpdate() throws Exception {
		MockAnalysis ana = new MockAnalysis();
		ana.setApp(app);
		ana.setId(analysisDAO.create(ana));
		ana.setHeuristicValue(-150);
		assertTrue("Update must return true on successful completion",analysisDAO.update(ana));
	}
	
	@Test(expected=NoSuchEntityException.class)
	public void testUpdateNonExisting() throws Exception {
		MockAnalysis ana = new MockAnalysis();
		ana.setApp(app);
		//do not create it!
		ana.setHeuristicValue(-150);
		//try to update it
		analysisDAO.update(ana);
	}
	

	@Test
	public void testDelete() throws Exception {
		MockAnalysis ana = new MockAnalysis();
		ana.setApp(app);
		ana.setId(analysisDAO.create(ana));
		assertTrue("Delete must return true on successful completion",analysisDAO.delete(ana));
	}
	
	@Test(expected=NoSuchEntityException.class)
	public void testDeleteNonExisiting() throws Exception {
		MockAnalysis ana = new MockAnalysis();
		ana.setApp(app);
		//do not create it!
		//try to delete it
		analysisDAO.delete(ana);
	}

	@Test
	public void testDeleteAllForApplication() throws Exception
	{
		MockAnalysis ana = new MockAnalysis();
		ana.setApp(app);
		ana.setId(analysisDAO.create(ana));
		assertTrue(analysisDAO.deleteAllByApplication(app)>0);
	}
	
	@Test
	public void testDeleteAll() throws Exception
	{
		MockAnalysis ana = new MockAnalysis();
		ana.setApp(app);
		ana.setId(analysisDAO.create(ana));
		MockAnalysis ana2 = new MockAnalysis();
		ana2.setApp(app);
		ana2.setId(analysisDAO.create(ana2));
		assertTrue(analysisDAO.deleteAll()==2);
	}
	
	@Test
	public void testCountAllByApplication() throws Exception
	{
		MockAnalysis ana = new MockAnalysis();
		ana.setApp(app);
		ana.setId(analysisDAO.create(ana));
		MockAnalysis ana2 = new MockAnalysis();
		ana2.setApp(app);
		ana2.setId(analysisDAO.create(ana2));
		assertTrue(analysisDAO.countAllByApplication(app)==2);
	}

}
