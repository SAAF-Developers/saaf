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

import de.rub.syssec.saaf.application.MockApplication;
import de.rub.syssec.saaf.application.manifest.permissions.Permission;
import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.dao.exceptions.DuplicateEntityException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.interfaces.GenericDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLApplicaitonDAO;
import de.rub.syssec.saaf.db.dao.mysql.MySQLPermissionDAO;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.PermissionInterface;

public class MySQLPermissionDAOTest {
	
	private Connection connection;
	private GenericDAO<PermissionInterface> dao;
	private Logger logger=Logger.getLogger(MySQLClassDAOTest.class);
	private ApplicationInterface app;
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
			helper.createDatabaseSchema();
			//create the dao under test
			dao = new MySQLPermissionDAO(connection);
			
			app = new MockApplication();
			MySQLApplicaitonDAO appDAO=new MySQLApplicaitonDAO(connection);
			app.setId(appDAO.create(app));
			
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
		PermissionInterface permission = new Permission("android.permission.SEND_SMS");
		int id = dao.create(permission);
		assertTrue("The id returned by MySQLPackageDAO.create must be a positive integer",id>0);
	}
	
	@Test(expected=DuplicateEntityException.class)
	public void testcreateSameTwice() throws Exception
	{
		PermissionInterface permission = new Permission("android.permission.SEND_SMS");
		dao.create(permission);
		dao.create(permission);
	}

	@Test
	public void testRead() throws Exception {
		PermissionInterface permission = new Permission("android.permission.SEND_SMS");
		int id = dao.create(permission);
		
		PermissionInterface read = dao.read(id);
		assertNotNull("read for an exisiting package must not return null",read);
		assertEquals("Names are not equal","android.permission.SEND_SMS",read.getName());
	}
	
	@Test
	public void testReadNonExisting() throws Exception{
		assertNull(dao.read(1));
	}
		
	@Test
	public void testUpdate() throws Exception{
		PermissionInterface permission = new Permission("android.permission.SEND_SMS");
		int id = dao.create(permission);
		permission.setId(id);
		permission.setName("android.permission.BRICK");
		assertTrue(dao.update(permission));
	}
	
	@Test(expected=NoSuchEntityException.class)
	public void testUpdateNonExisting()throws Exception {
		PermissionInterface permission = new Permission("android.permission.SEND_SMS");
		String name = "android.permission.BRICK";
		permission.setName(name);
		//do not create it so it cannot successfully be updated
		permission.setId(1);
		dao.update(permission);
	}

	@Test
	public void testDelete() throws Exception{
		PermissionInterface permission = new Permission("android.permission.SEND_SMS");
		permission.setId(dao.create(permission));
		assertTrue("Successful deletion must return true",dao.delete(permission));
		assertNull("After HTBatternDAO.delete reading a deleted Pattern should return null",dao.read(permission.getId()));
	}
	
	@Test(expected=NoSuchEntityException.class)
	public void testDeleteNonExisting() throws Exception{
		PermissionInterface permission = new Permission("android.permission.SEND_SMS");
		dao.delete(permission);
	}

	@Test
	public void testDeleteAll() throws Exception
	{
		PermissionInterface permission = new Permission("android.permission.SEND_SMS");
		dao.create(permission);
		PermissionInterface permission2 = new Permission("android.permission.BRICK");
		dao.create(permission2);
		assertTrue(dao.deleteAll()==2);
	}

	@Test
	public void testFindId() throws Exception
	{
		PermissionInterface permission = new Permission("android.permission.SEND_SMS");
		int id = dao.create(permission);
		assertEquals("FindId did not find the correct id",id,dao.findId(permission));
	}

}
