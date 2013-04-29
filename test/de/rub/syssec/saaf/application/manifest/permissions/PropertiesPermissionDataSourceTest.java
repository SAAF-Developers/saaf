package de.rub.syssec.saaf.application.manifest.permissions;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.rub.syssec.saaf.db.datasources.PropertiesPermissionDataSource;

public class PropertiesPermissionDataSourceTest {

	
	private PropertiesPermissionDataSource ds;
	

	@Before
	public void setUp() throws Exception {
		ds = new PropertiesPermissionDataSource("conf/permissions.properties");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNotNull() {
		
		assertNotNull("PropertiesPermissionDataSource return ed null instead of a list of permissions",ds.getData());
	}

}
