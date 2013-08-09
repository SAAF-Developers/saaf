package de.rub.syssec.saaf.application.manifest.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collection;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rub.syssec.saaf.analysis.steps.metadata.DOMManifestParser;
import de.rub.syssec.saaf.analysis.steps.metadata.ManifestParser;
import de.rub.syssec.saaf.analysis.steps.metadata.ManifestParserException;
import de.rub.syssec.saaf.analysis.steps.metadata.SimplePermissionChecker;
import de.rub.syssec.saaf.db.datasources.XMLPermissionDatasource;
import de.rub.syssec.saaf.model.application.manifest.ActivityInterface;
import de.rub.syssec.saaf.model.application.manifest.ManifestInterface;
import de.rub.syssec.saaf.model.application.manifest.ReceiverInterface;
import de.rub.syssec.saaf.model.application.manifest.ServiceInterface;

public class DOMManifestParserTest {

	private File testfile;
	private File testdir;
	private ManifestParser parser;

	@BeforeClass
	public static void setupBeforeClass() throws Exception{
		PropertyConfigurator.configure("conf/log4j.properties");
	}
	@Before
	public void setUp() throws Exception {
		this.parser = new DOMManifestParser(new SimplePermissionChecker(new XMLPermissionDatasource("conf/permissions.xml","conf/schema/permissions.xsd")));
		setupTestDir();
	}

	@After
	public void tearDown() throws Exception {
		this.parser = null;
		this.testfile.delete();
		testdir.delete();
	}


	private void setupTestFile(String filename) throws FileNotFoundException, IOException {
		BufferedInputStream in = new BufferedInputStream(this.getClass().getResourceAsStream(filename));
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		testfile = new File(testdir.getAbsolutePath() + System.getProperty("file.separator")+ filename);
		testfile.createNewFile();
		PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(testfile)));
	
		String s = br.readLine();
		while(s != null)
		{
			out.println(s);
			out.flush();
			s = br.readLine();
		}
		br.close();
		in.close();
		out.close();
	}

	private void setupTestDir() throws Exception {
		//set up a SAAF analysis directory structure
		testdir  = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator")+ System.currentTimeMillis());
		testdir.mkdir();		
	}

	@Test
	public void testAnalyzeFileNoActivites() throws Exception {
		setupTestFile("TestNoActivityAndroidManifest.xml");
		ManifestInterface info = parser.parse(testfile);
		assertTrue("No activities defined. Still ManifestInfo claims there are.", info.hasNoActivities());
	}

	@Test
	public void testNotNull() throws Exception {
		setupTestFile("TestAndroidManifest.xml");
		ManifestInterface manifest = parser.parse(testfile);
		assertNotNull("The result of analyzeFile may not be null", manifest);
	}
	
	@Test(expected=ManifestParserException.class)
	public void testNonExisiting() throws Exception {
		testfile = new File("GibtsGarnichtAndroidManifest.xml");
		ManifestInterface manifest = parser.parse(testfile);
		assertNotNull("The result of analyzeFile may not be null", manifest);
	}
	
	@Test
	public void testActivities() throws Exception {
		setupTestFile("TestAndroidManifest.xml");
		ManifestInterface manifest = parser.parse(testfile);
		assertEquals("Analysis claims no activities were defined!", 9, manifest.getNumberOfActivities());
		assertEquals("Wrong number of activties",9,manifest.getActivities().size());
	}
	
	
	@Test
	public void testActivtyIntentFilters() throws Exception {
		setupTestFile("TestActivityManifest.xml");
		ManifestInterface manifest = parser.parse(testfile);
		Collection<ActivityInterface> activties = manifest.getActivities();
		for(ActivityInterface a : activties)
		{
			assertNotNull("Intent-Filters not correct",a.getIntentFilters());
			//our test file contains an activity with one intent-filter
			assertFalse("List of Intent-Filters empty",a.getIntentFilters().isEmpty());
		}		
	}
	
	@Test
	public void testServiceIntentFilters() throws Exception {
		setupTestFile("TestServiceManifest.xml");
		ManifestInterface manifest = parser.parse(testfile);
		Collection<ServiceInterface> services = manifest.getServices();
		for(ServiceInterface service : services)
		{
			assertNotNull("Intent-Filters not correct",service.getIntentFilters());
			//our test file contains an activity with one intent-filter
			assertFalse("List of Intent-Filters empty",service.getIntentFilters().isEmpty());
		}		
	}
	
	@Test
	public void testReceiverIntentFilters() throws Exception {
		setupTestFile("TestReceiverManifest.xml");
		ManifestInterface manifest = parser.parse(testfile);
		Collection<ReceiverInterface> receivers = manifest.getReceivers();
		for(ReceiverInterface receiver : receivers)
		{
			assertNotNull("Intent-Filters not correct",receiver.getIntentFilters());
			//our test file contains an activity with one intent-filter
			assertFalse("List of Intent-Filters empty",receiver.getIntentFilters().isEmpty());
		}		
	}
	
	@Test
	public void testServices() throws Exception {
		setupTestFile("TestAndroidManifest.xml");
		ManifestInterface manifest = parser.parse(testfile);
		assertEquals("Analysis claims no services were defined",1, manifest.getNumberOfServices());
	}
	
	@Test
	public void testReceivers() throws Exception {
		setupTestFile("TestAndroidManifest.xml");
		ManifestInterface manifest = parser.parse(testfile);
		assertEquals("Analysis claims no receivers were defined",1, manifest.getNumberOfReceivers());
	}
	//assertFalse("Analysis claims the result to be aggregated",info.isAggregated());
	//assertEquals("The number of analyzed manifests must be 1", 1, info.getNumberOfAnalyzedManifests());
	
	@Test
	public void testPermissions() throws Exception {
		setupTestFile("TestAndroidManifest.xml");
		ManifestInterface manifest = parser.parse(testfile);
		assertEquals("Number of requested permissions is not correct", 110, manifest.getNumberOfPermissions());
	}
	@Test
	public void testAppDebuggable() throws Exception{
		setupTestFile("TestApplicationAndroidManifest.xml");
		ManifestInterface manifest = parser.parse(testfile);
		assertTrue("Application.isDebuggable was not set correctly",manifest.isAppDebuggable());
	}
	
	@Test
	public void testAppLabel() throws Exception{
		setupTestFile("TestApplicationAndroidManifest.xml");
		ManifestInterface manifest = parser.parse(testfile);
		assertEquals("Application.isDebuggable was not set correctly","TestApp",manifest.getAppLabel());
	}
	@Test
	public void testMainActivity() throws Exception{
		setupTestFile("TestActivityManifest.xml");
		ManifestInterface manifest = parser.parse(testfile);
		ActivityInterface a = manifest.getDefaultActivity();
		assertNotNull("Default Activity was not set!",a);
		assertTrue("A default activity must have the entrypoint flag set",a.isEntryPoint());
		
	}
	@Test
	public void testNoMainActivity() throws Exception{
		setupTestFile("TestNoActivityAndroidManifest.xml");
		ManifestInterface manifest = parser.parse(testfile);
		ActivityInterface a = manifest.getDefaultActivity();
		org.junit.Assert.assertNull("When there is no activity there cannot be a default activity!",a);
	}
	

}
