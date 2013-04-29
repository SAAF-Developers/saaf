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

import de.rub.syssec.saaf.analysis.steps.hash.Hash;
import de.rub.syssec.saaf.application.MockApplication;
import de.rub.syssec.saaf.application.manifest.MockManifest;
import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.db.dao.exceptions.DuplicateEntityException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.mysql.MySQLApplicaitonDAO;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.Digest;

public class MySQLApplicaitonDAOTest {

	private Connection connection;
	MySQLApplicaitonDAO dao;
	private Logger logger = Logger.getLogger(MySQLBTPatternDAOTest.class);
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
			dao = new MySQLApplicaitonDAO(connection);
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
		ApplicationInterface mockApp = new MockApplication();
		mockApp.setManifest(new MockManifest());
		int id = dao.create(mockApp);
		assertTrue(
				"The create method must return an id > 0 on successful completion",
				id > 0);
	}
	
	@Test
	public void testCreateNullManifest() throws Exception {
		ApplicationInterface mockApp = new MockApplication();
		int id = dao.create(mockApp);
		assertTrue(
				"The create method must return an id > 0 on successful completion",
				id > 0);
	}

	@Test(expected = DAOException.class)
	public void testCreateWithNullClassName() throws Exception {
		ApplicationInterface application = new MockApplication();
		application.setApplicationName(null);
		dao.create(application);
	}

	@Test(expected = DuplicateEntityException.class)
	public void testcreateSameTwice() throws DAOException,
			DuplicateEntityException {
		ApplicationInterface application = new MockApplication();
		dao.create(application);
		dao.create(application);
	}

	@Test
	public void testRead() throws Exception {
		ApplicationInterface application = new MockApplication();
		int id = dao.create(application);
		ApplicationInterface read = dao.read(id);
		assertNotNull("Read  must not return null", read);
		assertEquals("Hasshes do no match", application.getMessageDigest(Digest.MD5),
				read.getMessageDigest(Digest.MD5));
	}

	@Test
	public void testReadNonExisting() throws Exception {
		assertNull(
				"Trying to read an non-existent BTPattern should return null",
				dao.read(1));
	}

	@Test
	public void testUpdate() throws Exception {
		ApplicationInterface application = new MockApplication();
		int id = dao.create(application);
		application.setId(id);
		application.setApplicationName("CuiSonFu.apk");
		boolean updated = dao.update(application);
		assertTrue("Update must return true on successful completion", updated);
		assertEquals("The field was not updated", "CuiSonFu.apk",
				application.getApplicationName());
	}

	@Test(expected = NoSuchEntityException.class)
	public void testUpdateNonExisting() throws Exception {
		ApplicationInterface application = new MockApplication();
		application.setId(1);
		application.setApplicationName("CuiSonFu.apk");
		dao.update(application);
	}

	@Test
	public void testDelete() throws Exception {
		ApplicationInterface application = new MockApplication();
		int id = dao.create(application);
		application.setId(id);
		boolean deleted = dao.delete(application);
		assertTrue("delete must return true on successful deletion", deleted);
	}

	@Test(expected = NoSuchEntityException.class)
	public void testDeleteNonExisting() throws Exception {
		ApplicationInterface application = new MockApplication();
		application.setId(1);
		dao.delete(application);
	}

	@Test
	public void testDeleteAll() throws Exception {
		ApplicationInterface application = new MockApplication();
		ApplicationInterface application2 = new MockApplication();
		application2.setApplicationName("DroidKillar.apk");
		application2.setMessageDigest(Digest.MD5, "fe6d659b773d4c7955f9be0f0699472c");
		application2.setMessageDigest(Digest.SHA1, "b0db779914627eb16d7fa12b46d8f073e44db6a3");
		application2.setMessageDigest(Digest.SHA256,"2ec2d248a19dcc8aa9d59b7234a1b921b5812a6ee89c9ebfb268f1da0745d336");
		dao.create(application);
		dao.create(application2);
		assertTrue(dao.deleteAll() == 2);
	}

	@Test
	public void testFindIdByHash() throws Exception {
		ApplicationInterface app = new MockApplication();
		int id = dao.create(app);
		assertEquals(id, dao.findByMD5Hash(app.getMessageDigest(Digest.MD5)));
	}

	@Test
	public void testFindNonExistingByHash() throws Exception {
		ApplicationInterface app = new MockApplication();
		// do not save it!
		assertEquals(0, dao.findByMD5Hash(app.getMessageDigest(Hash.DEFAULT_DIGEST)));
	}

	@Test
	public void testFindNameById() throws Exception {
		ApplicationInterface app = new MockApplication();
		int id = dao.create(app);
		assertEquals(app.getApplicationName(), dao.findNameById(id));

	}
}
