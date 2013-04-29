package de.rub.syssec.saaf.application;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.rub.syssec.saaf.application.JavaPackage;

public class JavaPackageTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSetNameString() {
		JavaPackage testpackage = new JavaPackage();
		testpackage.setName("this.is.the.name");
		assertEquals("this.is.the.name", testpackage.getName(true));
	}
}
