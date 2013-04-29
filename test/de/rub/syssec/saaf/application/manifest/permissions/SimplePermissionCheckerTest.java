package de.rub.syssec.saaf.application.manifest.permissions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.rub.syssec.saaf.analysis.steps.metadata.SimplePermissionChecker;
import de.rub.syssec.saaf.application.manifest.permissions.Permission;
import de.rub.syssec.saaf.application.manifest.permissions.PermissionRequest;
import de.rub.syssec.saaf.db.datasources.DataSourceException;
import de.rub.syssec.saaf.db.datasources.Datasource;
import de.rub.syssec.saaf.db.datasources.XMLPermissionDatasource;
import de.rub.syssec.saaf.model.application.PermissionInterface;

public class SimplePermissionCheckerTest {

	Datasource<PermissionInterface> ds;
	@Before
	public void setUp() throws Exception {
		ds = new XMLPermissionDatasource("conf/permissions.xml","conf/schema/permissions.xml");	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testIsKnownPermission() throws DataSourceException {
		SimplePermissionChecker checker = new SimplePermissionChecker(ds);
		PermissionRequest request = new PermissionRequest(new Permission("android.permission.SEND_SMS"));
		checker.check(request);
		assertTrue("SimplePermissionChecker does not know SEND_SMS",request.isValid());
	}
		
	@Test
	public void testGetKnownPermissions() throws DataSourceException {
		SimplePermissionChecker checker = new SimplePermissionChecker(ds);
		assertFalse("SimplePermissionChecker did not return any permissions",checker.getKnownPermissions().isEmpty());
	}
}
