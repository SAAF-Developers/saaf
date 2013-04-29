package de.rub.syssec.saaf.db.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rub.syssec.saaf.application.manifest.permissions.Permission;
import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.persistence.sql.PermissionEntityManager;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.application.PermissionInterface;

public class PermissionEntityManagerTest {

	@BeforeClass
	public static void setupBeforeClass() throws Exception{
		PropertyConfigurator.configure("conf/log4j.properties");
	}

	private DatabaseHelper helper;
	private Connection connection;
	private PermissionEntityManager manager;

	@Before
	public void setUp() throws Exception {
		helper = new DatabaseHelper(Config.getInstance());
		helper.createDatabaseSchema();
		connection = helper.getConnection();
		manager = new PermissionEntityManager(connection);
	}

	@After
	public void tearDown() throws Exception {
		helper.dropTables();
		connection.close();
	}

	@Test
	public void testSave() throws Exception {
		PermissionInterface perm= new Permission("android.permission.SEND_SMS");
		assertTrue(manager.save(perm));
		assertTrue(perm.getId() > 0);
	}

	@Test
	public void testDelete() throws Exception {
		PermissionInterface perm= new Permission("android.permission.SEND_SMS");
		assertTrue(manager.save(perm));
		assertTrue(manager.delete(perm));
	}

	@Test
	public void testValidate() throws Exception {
		PermissionInterface perm= new Permission("android.permission.SEND_SMS");
		assertTrue(manager.validate(perm));
	}

	@Test
	public void testReadAll() throws Exception {
		PermissionInterface perm= new Permission("android.permission.SEND_SMS");
		PermissionInterface perm2= new Permission("android.permission.BLUETOOTH");
		assertTrue(manager.save(perm));
		assertTrue(manager.save(perm2));
		List<PermissionInterface> read = manager.readAll();	
		
		assertNotNull(read);
		assertFalse(read.isEmpty());
		assertEquals(2,read.size());
	}

	@Test
	public void testSaveAll() throws Exception {
		PermissionInterface perm= new Permission("android.permission.SEND_SMS");
		PermissionInterface perm2= new Permission("android.permission.BLUETOOTH");
		List<PermissionInterface> all = new ArrayList<PermissionInterface>();
		all.add(perm);
		all.add(perm2);
		assertTrue(manager.saveAll(all));
		List<PermissionInterface> read = manager.readAll();	
		
		assertNotNull(read);
		assertFalse(read.isEmpty());
		assertEquals(2,read.size());	}

}
