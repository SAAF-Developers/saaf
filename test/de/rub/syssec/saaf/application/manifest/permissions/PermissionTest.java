package de.rub.syssec.saaf.application.manifest.permissions;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rub.syssec.saaf.application.manifest.permissions.Permission;
import de.rub.syssec.saaf.model.application.PermissionType;

public class PermissionTest {

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
		Permission p1 = new Permission("android.permission.SEND_SMS", PermissionType.PLATFORM, "Permission to send SMS");
		Permission p2 = new Permission("android.permission.SEND_SMS", PermissionType.PLATFORM, "Permission to send SMS");
		assertEquals(p1, p2);
		assertEquals(p1.hashCode(), p2.hashCode());
	}

	@Test
	public void testPermissionString() {
		String name = "android.permission.SEND_SMS";
		Permission p = new Permission(name);
		assertEquals("The name is set via the constructor", name, p.getName());
		assertEquals(
				"The default Type of a new Permission is UNKNOWN if it was not specified in the constructor",
				PermissionType.UNKNOWN, p.getType());
		assertTrue(
				"The default description of a new Permission is empty if it was not specified in the constructor",
				p.getDescription().isEmpty());
		assertTrue("A newly created permission is always changed",
				p.isChanged());
	}

	@Test
	public void testPermissionStringPermissionType() {
		String name = "android.permission.SEND_SMS";
		PermissionType type = PermissionType.CUSTOM;
		Permission p = new Permission(name, type);
		assertEquals("The name is set via the constructor", name, p.getName());
		assertEquals(
				"The default Type of a new Permission is UNKNOWN if it was not specified in the constructor",
				PermissionType.CUSTOM, p.getType());
		assertTrue(
				"The default description of a new Permission is empty if it was not specified in the constructor",
				p.getDescription().isEmpty());
		assertTrue("A newly created permission is always changed",
				p.isChanged());
	}

	@Test
	public void testPermissionStringPermissionTypeString() {
		String name = "android.permission.SEND_SMS";
		PermissionType type = PermissionType.CUSTOM;
		String desc = "Permission to send SMS messaages";
		Permission p = new Permission(name, type, desc);

		assertEquals("The name is set via the constructor", name, p.getName());
		assertEquals(
				"The default Type of a new Permission is UNKNOWN if it was not specified in the constructor",
				PermissionType.CUSTOM, p.getType());
		assertEquals(
				"The default description of a new Permission is empty if it was not specified in the constructor",
				desc, p.getDescription());
		assertTrue("A newly created permission is always changed",
				p.isChanged());
	}

	@Test
	public void testGetName() {
		String name = "android.permission.SEND_SMS";
		Permission p = new Permission(name);
		assertEquals(name, p.getName());
	}

	@Test
	public void testSetName() {
		String name = "android.permission.SEND_SMS";
		Permission p = new Permission(name);
		p.setName("android.permission.BLUETOOTH");
		assertEquals("android.permission.BLUETOOTH", p.getName());
	}

	@Test
	public void testEqualsObject() {
		Permission p1 = new Permission("android.permission.SEND_SMS");
		Permission p2 = new Permission("android.permission.BLUETOOTH");
		assertNotSame("Permissions are the same only if they have the same name, type and description", p1, p2);
		p2.setName("android.permission.SEND_SMS");
		assertEquals("Permissions are the same only if they have the same name, type and description", p1, p2);
		p2.setId(1);
		assertEquals("The ID has no influence on the equality of permissions", p1, p2);

		
	}

	@Test
	public void testGetType() {
		String name = "android.permission.SEND_SMS";
		PermissionType type = PermissionType.CUSTOM;
		Permission p = new Permission(name, type);
		assertEquals(type, p.getType());
	}

	@Test
	public void testSetType() {
		String name = "android.permission.SEND_SMS";
		PermissionType type = PermissionType.CUSTOM;
		Permission p = new Permission(name, type);
		p.setType(PermissionType.FRAMEWORK);
		assertEquals(PermissionType.FRAMEWORK, p.getType());
	}

	@Test
	public void testGetDescription() {
		String name = "android.permission.SEND_SMS";
		PermissionType type = PermissionType.CUSTOM;
		String desc = "Permission to send SMS messaages";
		Permission p = new Permission(name, type, desc);
		assertEquals(desc, p.getDescription());

	}

	@Test
	public void testSetDescription() {
		String name = "android.permission.SEND_SMS";
		PermissionType type = PermissionType.CUSTOM;
		String desc = "Permission to send SMS messaages";
		Permission p = new Permission(name, type, desc);
		p.setDescription("android.permission.BLUETOOTH");
		assertEquals("android.permission.BLUETOOTH", p.getDescription());
	}

	@Test
	public void testIsChanged() {
		String name = "android.permission.SEND_SMS";
		PermissionType type = PermissionType.CUSTOM;
		String desc = "Permission to send SMS messaages";
		Permission p = new Permission(name, type, desc);
		assertTrue(p.isChanged());
	}

	@Test
	public void testSetChanged() {
		String name = "android.permission.SEND_SMS";
		PermissionType type = PermissionType.CUSTOM;
		String desc = "Permission to send SMS messaages";
		Permission p = new Permission(name, type, desc);
		assertTrue(p.isChanged());
		p.setChanged(false);
		assertFalse(p.isChanged());
	}

	@Test
	public void testGetId() {
		String name = "android.permission.SEND_SMS";
		PermissionType type = PermissionType.CUSTOM;
		String desc = "Permission to send SMS messaages";
		Permission p = new Permission(name, type, desc);
		assertEquals(0, p.getId());
	}

	@Test
	public void testSetId() {
		String name = "android.permission.SEND_SMS";
		PermissionType type = PermissionType.CUSTOM;
		String desc = "Permission to send SMS messaages";
		Permission p = new Permission(name, type, desc);
		assertEquals(0, p.getId());	
		p.setId(1);
		assertEquals(1, p.getId());	

	}

}
