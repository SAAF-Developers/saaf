package de.rub.syssec.saaf.db.persistence;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rub.syssec.saaf.application.MockApplication;
import de.rub.syssec.saaf.application.manifest.permissions.Permission;
import de.rub.syssec.saaf.application.manifest.permissions.PermissionRequest;
import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.persistence.sql.PermissionRequestEntityManager;
import de.rub.syssec.saaf.logicTier.MockAnalysis;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.application.PermissionInterface;
import de.rub.syssec.saaf.model.application.manifest.PermissionRequestInterface;

public class PermissionRequestEntityManagerTest {

	@BeforeClass
	public static void setupBeforeClass() throws Exception{
		PropertyConfigurator.configure("conf/log4j.properties");
	}

	private DatabaseHelper helper;
	private Connection connection;
	private PermissionRequestEntityManager manager;
	private MockApplication mockApplication;
	private MockAnalysis mockAnalysis;

	@Before
	public void setUp() throws Exception {
		helper = new DatabaseHelper(Config.getInstance());
		helper.createDatabaseSchema();
		connection = helper.getConnection();
		
		mockApplication = new MockApplication();
		// perisist the application it is the root
		mockAnalysis = new MockAnalysis();
		mockAnalysis.setApp(mockApplication);
		
		manager = new PermissionRequestEntityManager(connection);
	}

	@After
	public void tearDown() throws Exception {
		helper.dropTables();
		connection.close();
	}

	@Test
	public void testSave() throws Exception {
		PermissionInterface perm = new Permission("android.permission.SEND_SMS");
		PermissionRequestInterface request = new PermissionRequest(perm);
		request.setAnalysis(mockAnalysis);
		assertTrue(manager.save(request));
		assertTrue(request.getId() > 0);
		assertTrue(perm.getId() > 0);
		assertTrue(mockAnalysis.getId()>0);
		assertTrue(mockApplication.getId()>0);
	}

	@Test
	public void testDelete() throws Exception {
		PermissionInterface perm = new Permission("android.permission.SEND_SMS");
		PermissionRequestInterface request = new PermissionRequest(perm);
		request.setAnalysis(mockAnalysis);
		assertTrue(manager.save(request));
		assertTrue(manager.delete(request));
	}

	@Test
	public void testValidate() throws Exception {
		PermissionInterface perm= new Permission("android.permission.SEND_SMS");
		PermissionRequestInterface request = new PermissionRequest(perm);
		request.setAnalysis(mockAnalysis);
		assertTrue(manager.validate(request));
	}

	@Test
	public void testSaveAll() throws Exception {
		PermissionInterface perm= new Permission("android.permission.SEND_SMS");
		PermissionRequestInterface request = new PermissionRequest(perm);
		request.setAnalysis(mockAnalysis);
		PermissionRequestInterface request2 = new PermissionRequest(perm);
		request2.setAnalysis(mockAnalysis);

		List<PermissionRequestInterface> all = new ArrayList<PermissionRequestInterface>();
		all.add(request);
		all.add(request2);
		assertTrue(manager.saveAll(all));	
	}

}
