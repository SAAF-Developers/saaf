package de.rub.syssec.saaf.db.dao.mysql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import de.rub.syssec.saaf.application.manifest.permissions.Permission;
import de.rub.syssec.saaf.application.manifest.permissions.PermissionRequest;
import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.mysql.MySQLAnalysisDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLApplicaitonDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLPermissionDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLPermissionRequestDAO;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.application.PermissionInterface;
import de.rub.syssec.saaf.model.application.manifest.PermissionRequestInterface;

public class MySQLPermissionRequestDAOTest {

	@BeforeClass
	public static void setupBeforeClass() throws Exception{
		PropertyConfigurator.configure("conf/log4j.properties");
	}

	private DatabaseHelper helper;
	private Connection connection;
	private MySQLPermissionRequestDAO dao;
	private Logger logger=Logger.getLogger(getClass());
	private MockApplication mockApplication;
	private MockAnalysis mockAnalysis;
	private Permission mockPermission;
	private MySQLApplicaitonDAO applicationDAO;
	private MySQLAnalysisDAO analysisDAO;
	private MySQLPermissionDAO permissionDAO;
	
	@Before
	public void setUp() throws Exception {
		try {
			
			helper = new DatabaseHelper(Config.getInstance());
			helper.createDatabaseSchema();
			connection = helper.getConnection();
			helper.createDatabaseSchema();
			
			mockApplication = new MockApplication();
			// perisist the application it is the root
			applicationDAO = new MySQLApplicaitonDAO(connection);
			mockApplication.setId(applicationDAO.create(mockApplication));
			
			mockAnalysis = new MockAnalysis();
			mockAnalysis.setApp(mockApplication);
			analysisDAO = new MySQLAnalysisDAO(connection);
			mockAnalysis.setId(analysisDAO.create(mockAnalysis));
			
			mockPermission  = new Permission("android.permission.SEND_SMS");
			permissionDAO =  new MySQLPermissionDAO(connection);
			mockPermission.setId(permissionDAO.create(mockPermission));
			
			//create the dao under test
			dao = new MySQLPermissionRequestDAO(connection);
			
		} catch (Exception e) {
			logger.error("Unable to connect to DB!",e);
		}
	}

	@After
	public void tearDown() throws Exception {
		helper.dropTables();
		connection.close();
	}

	@Test
	public void testCreate() throws Exception {
		PermissionRequestInterface request = new PermissionRequest(mockPermission);
		request.setAnalysis(mockAnalysis);
		int id = dao.create(request);
		assertTrue("The id returned by create must be a positive integer",id>0);
	}

	@Test
	public void testcreateSameTwice() throws Exception
	{
		PermissionRequestInterface request = new PermissionRequest(mockPermission);
		request.setAnalysis(mockAnalysis);

		dao.create(request);
		dao.create(request);
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testRead() throws Exception {
		PermissionRequestInterface request = new PermissionRequest(mockPermission);
		request.setAnalysis(mockAnalysis);
		int id = dao.create(request);
		
		PermissionRequestInterface read = dao.read(id);
		assertNotNull("read for an existing entity must not return null",read);
		assertEquals("Names are not equal",request,read);
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testReadNonExisting() throws Exception{
		assertNull(dao.read(1));
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testReadAll() throws Exception {
		dao.readAll();
	}

	@Test
	public void testUpdate() throws Exception {
		PermissionRequestInterface request = new PermissionRequest(mockPermission);
		request.setAnalysis(mockAnalysis);
		int id = dao.create(request);
		request.setId(id);
		request.setValid(true);
		assertTrue(dao.update(request));
	}
	
	@Test(expected=NoSuchEntityException.class)
	public void testUpdateNonExisting()throws Exception {
		PermissionRequestInterface request = new PermissionRequest(new Permission("android.permission.SEND_SMS"));
		//do not create it so it cannot successfully be updated
		request.setValid(true);
		request.setId(1);
		dao.update(request);
	}

	@Test
	public void testDelete() throws Exception {
		PermissionRequestInterface request = new PermissionRequest(mockPermission);
		request.setAnalysis(mockAnalysis);
		request.setId(dao.create(request));
		assertTrue("Successful deletion must return true",dao.delete(request));
		assertEquals("After delete, searching for id of a deleted entity should return 0",0,dao.findId(request));	}

	@Test(expected=NoSuchEntityException.class)
	public void testDeleteNonExisting() throws Exception{
		PermissionRequestInterface request = new PermissionRequest(new Permission("android.permission.SEND_SMS"));
		dao.delete(request);
	}
	
	@Test
	public void testDeleteAll() throws Exception {
		PermissionRequestInterface request1 = new PermissionRequest(mockPermission);
		PermissionInterface permission2=new Permission("android.permission.BRICK");
		permission2.setId(permissionDAO.create(permission2));
		request1.setAnalysis(mockAnalysis);
		dao.create(request1);
		PermissionRequestInterface request2 = new PermissionRequest(permission2);
		request2.setAnalysis(mockAnalysis);
		dao.create(request2);
		assertTrue(dao.deleteAll()==2);
	}

	@Test
	public void testFindId() throws Exception {
		PermissionRequestInterface request = new PermissionRequest(mockPermission);
		request.setAnalysis(mockAnalysis);
		int id = dao.create(request);
		assertEquals("FindId did not find the correct id",id,dao.findId(request));	}

}
