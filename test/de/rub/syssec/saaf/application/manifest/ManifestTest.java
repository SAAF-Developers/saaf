package de.rub.syssec.saaf.application.manifest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.rub.syssec.saaf.application.manifest.Manifest;
import de.rub.syssec.saaf.application.manifest.components.Activity;
import de.rub.syssec.saaf.application.manifest.components.Receiver;
import de.rub.syssec.saaf.application.manifest.components.Service;
import de.rub.syssec.saaf.application.manifest.permissions.Permission;
import de.rub.syssec.saaf.application.manifest.permissions.PermissionRequest;
import de.rub.syssec.saaf.model.application.manifest.ManifestInterface;

public class ManifestTest {

	private ManifestInterface manifest;

	@Before
	public void setUp() throws Exception {
		manifest = new Manifest(new File("AndroidManifest.xml"));
	}

	@After
	public void tearDown() throws Exception {
		manifest = null;
	}

	@Test
	public void testAddActivity() {
		Activity p1= new Activity("ABC");
		manifest.addActivity(p1);
		Activity p2= new Activity("ABC");
		manifest.addActivity(p2);
		assertEquals("Manifest may not contain duplicate activities",1,manifest.getNumberOfActivities());	
	}

	@Test
	public void testGetNumberOfActivities() {
		assertTrue("An empty Manifest may not have activities",manifest.hasNoActivities());
	}

	@Test
	public void testHasNoActivities() {
		assertTrue("An empty Manifest may not have activities",manifest.hasNoActivities());
	}

	@Test
	public void testAddPermission() {
		Permission p1= new Permission("ABC");
		manifest.addPermissionRequest(new PermissionRequest(p1));
		Permission p2= new Permission("ABC");
		manifest.addPermissionRequest(new PermissionRequest(p2));
		assertEquals("Manifest may not contain duplicate permissions",1,manifest.getNumberOfPermissions());	
	}

	@Test
	public void testGetNumberOfPermissions() {
		assertEquals("An empty Manifest may not have permissions",0,manifest.getNumberOfPermissions());
	}

	@Test
	public void testAddService() {
		Service s1= new Service("ABC");
		manifest.addService(s1);
		Service s2= new Service("ABC");
		manifest.addService(s2);
		assertEquals("Manifest may not contain duplicate services",1,manifest.getNumberOfServices());	
	}

	@Test
	public void testGetNumberOfServices() {
		assertEquals("An empty Manifest may not have permissions",0,manifest.getNumberOfServices());
	}

	@Test
	public void testAddReceiver() {
		Receiver s1= new Receiver("ABC");
		manifest.addReceiver(s1);
		Receiver s2= new Receiver("ABC");
		manifest.addReceiver(s2);
		assertEquals("Manifest may not contain duplicate receivers",1,manifest.getNumberOfReceivers());	
	}

	@Test
	public void testGetNumberOfReceivers() {
		assertEquals("An empty Manifest may not have receivers",0,manifest.getNumberOfServices());
	}

	@Test
	public void testGetPath() {
		assertNotNull("A manifest must have a path",manifest.getPath());
	}

	@Test
	public void testSetPath() {
		//how do ya test this?
	}

}
