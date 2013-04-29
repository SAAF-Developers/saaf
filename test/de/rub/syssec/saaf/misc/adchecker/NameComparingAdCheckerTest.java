package de.rub.syssec.saaf.misc.adchecker;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.rub.syssec.saaf.db.datasources.DataSourceException;
import de.rub.syssec.saaf.misc.adchecker.NameComparingAdChecker;
import de.rub.syssec.saaf.misc.adchecker.XMLAdnetworkDataSource;

public class NameComparingAdCheckerTest {

	private NameComparingAdChecker adChecker;

	@Before
	public void setUp() throws Exception {
		NameComparingAdChecker.reset();
		this.adChecker = NameComparingAdChecker.getInstance(new XMLAdnetworkDataSource("conf/AdNetworks.xml","conf/schema/AdNetworks.xsd"));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testContainsAnAd() throws DataSourceException {
		File testpath = new File("/admob");
		assertTrue("AdNetwork recognition does not recognize /admob/",adChecker.containsAnAd(testpath));
	}

}
