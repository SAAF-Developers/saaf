package de.rub.syssec.saaf.application.manifest.permissions;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rub.syssec.saaf.application.manifest.permissions.Permission;
import de.rub.syssec.saaf.application.manifest.permissions.PermissionRequest;

public class PermissionRequestTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testHashCode() {
		Permission permA = new Permission("android.permission.SEND_SMS");
		Permission permB = new Permission("android.permission.SEND_SMS");
		PermissionRequest reqA = new PermissionRequest(permA);
		PermissionRequest reqB = new PermissionRequest(permB);
		assertEquals(reqA, reqB);
		assertEquals(reqA.hashCode(), reqB.hashCode());
	}

	@Test
	public void testPermissionRequest() {
		Permission permA = new Permission("android.permission.SEND_SMS");
		PermissionRequest reqA = new PermissionRequest(permA);
		assertTrue(
				"A new PermissionRequest that has not been persisted is always changed",
				reqA.isChanged());
		assertFalse(
				"A new PermissionRequest that has not been checked is alyaws invalid",
				reqA.isValid());
	}

	@Test
	public void testGetRequestedPermission() {
		Permission permA = new Permission("android.permission.SEND_SMS");
		PermissionRequest reqA = new PermissionRequest(permA);
		assertEquals(permA, reqA.getRequestedPermission());
	}

	@Test
	public void testSetRequestedPermission() {
		Permission permA = new Permission("android.permission.SEND_SMS");
		Permission permB = new Permission("android.permission.BLUETOOTH");
		PermissionRequest reqA = new PermissionRequest(permA);
		reqA.setRequestedPermission(permB);
		assertEquals(permB, reqA.getRequestedPermission());
	}

	@Test
	public void testIsValid() {
		Permission permA = new Permission("android.permission.SEND_SMS");
		PermissionRequest reqA = new PermissionRequest(permA);
		assertFalse(reqA.isValid());
	}

	@Test
	public void testSetValid() {
		Permission permA = new Permission("android.permission.SEND_SMS");
		PermissionRequest reqA = new PermissionRequest(permA);
		assertFalse(reqA.isValid());
		reqA.setValid(true);
		assertTrue(reqA.isValid());
	}

	@Test
	public void testGetId() {
		Permission permA = new Permission("android.permission.SEND_SMS");
		PermissionRequest reqA = new PermissionRequest(permA);
		assertEquals(0, reqA.getId());
	}

	@Test
	public void testSetId() {
		Permission permA = new Permission("android.permission.SEND_SMS");
		PermissionRequest reqA = new PermissionRequest(permA);
		assertEquals(0, reqA.getId());
		reqA.setId(1);
		assertEquals(1, reqA.getId());
	}

	@Test
	public void testSetChanged() {
		Permission permA = new Permission("android.permission.SEND_SMS");
		PermissionRequest reqA = new PermissionRequest(permA);
		assertTrue(reqA.isChanged());
		reqA.setChanged(false);
		assertFalse(reqA.isChanged());
	}

	@Test
	public void testIsChanged() {
		Permission permA = new Permission("android.permission.SEND_SMS");
		PermissionRequest reqA = new PermissionRequest(permA);
		assertTrue(reqA.isChanged());
	}

	@Test
	public void testEqualsObject() {
		Permission permA = new Permission("android.permission.SEND_SMS");
		Permission permB = new Permission("android.permission.BLUETOOTH");
		PermissionRequest reqA = new PermissionRequest(permA);
		PermissionRequest reqB = new PermissionRequest(permB);
		assertEquals(reqA, reqA);
		assertNotSame(reqA, reqB);
		assertFalse(reqA.equals(reqB));
	}

}
