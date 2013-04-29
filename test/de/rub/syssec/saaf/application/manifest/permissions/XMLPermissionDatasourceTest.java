/**
 * 
 */
package de.rub.syssec.saaf.application.manifest.permissions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.rub.syssec.saaf.db.datasources.Datasource;
import de.rub.syssec.saaf.db.datasources.XMLPermissionDatasource;
import de.rub.syssec.saaf.model.application.PermissionInterface;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class XMLPermissionDatasourceTest {

	Datasource<PermissionInterface> ds;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		ds = new XMLPermissionDatasource("conf/permissions.xml","conf/schema/permissions.xsd");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNotEmpty() throws Exception {
		
		assertFalse("XMLPermissionDatasource did not return any permissions",ds.getData().isEmpty());
	}
	
	@Test
	public void testExactNumber() throws Exception {
		assertEquals("Number of known permissions XMLPermissionDatasource did not match",130,ds.getData().size());
	}
	

}
