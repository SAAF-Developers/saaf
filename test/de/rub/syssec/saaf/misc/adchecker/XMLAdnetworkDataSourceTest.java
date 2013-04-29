package de.rub.syssec.saaf.misc.adchecker;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.rub.syssec.saaf.misc.adchecker.XMLAdnetworkDataSource;

public class XMLAdnetworkDataSourceTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetAdNetworks() throws Exception {
		XMLAdnetworkDataSource ds = new XMLAdnetworkDataSource("conf/AdNetworks.xml","conf/schema/AdNetworks.xsd");
		assertEquals("Number of known ad-networks not correct",101,ds.getData().size());
	}

}
