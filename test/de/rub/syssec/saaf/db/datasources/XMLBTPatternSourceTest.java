package de.rub.syssec.saaf.db.datasources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rub.syssec.saaf.analysis.steps.slicing.BTPattern;
import de.rub.syssec.saaf.db.datasources.AbstractXMLDataSource;
import de.rub.syssec.saaf.db.datasources.DataSourceException;
import de.rub.syssec.saaf.db.datasources.XMLBTPatternSource;
import de.rub.syssec.saaf.model.analysis.BTPatternInterface;

public class XMLBTPatternSourceTest {

	AbstractXMLDataSource ds;
	private BTPatternInterface[] hardCodedPatterns = {
			new BTPattern("android/telephony/SmsManager", "sendTextMessage",
				"Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/app/PendingIntent;Landroid/app/PendingIntent;",
				0, "Number and Text of a SMS Message"),
			new BTPattern("android/telephony/SmsManager", "sendTextMessage",
						"Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/app/PendingIntent;Landroid/app/PendingIntent;",
						2, "Number and Text of a SMS Message"),				
			new BTPattern("android/telephony/gsm/SmsManager", "sendTextMessage",
				"Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/app/PendingIntent;Landroid/app/PendingIntent;",
				0, "Number and Text of a SMS Message"),
			new BTPattern("android/telephony/gsm/SmsManager", "sendTextMessage",
						"Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/app/PendingIntent;Landroid/app/PendingIntent;",
						2, "Number and Text of a SMS Message"),				
			new BTPattern("java/lang/Runtime", "exec", "Ljava/lang/String;", 0, "Execute external propgram"),
			new BTPattern("java/lang/System", "loadLibrary", "Ljava/lang/String;", 0, "Load and link library"),
			
			new BTPattern("android/content/IntentFilter", "<init>", "Ljava/lang/String;", 0, "IntentFilter action"), // TODO: overloaded constructor
			new BTPattern("android/content/IntentFilter", "<init>", "Ljava/lang/String;Ljava/lang/String;", 0, "IntentFilter action"), // TODO: overloaded constructor
			
			new BTPattern("java/lang/Class", "forName", "Ljava/lang/String;", 0, "Reflective class getter"),
			new BTPattern("java/lang/Class", "getMethod", "Ljava/lang/String;", 0, "Reflective method getter"),
			new BTPattern("javax/net/ssl/SSLSocket", "setEnabledCipherSuites", "[Ljava/lang/String;", 0, "SSL Cipher Suites"),
			new BTPattern("javax/net/ssl/SSLSocketFactory", "createSocket", "Ljava/lang/String;I", 0, "SSL Connections"),
			new BTPattern("javax/net/ssl/SSLSocketFactory", "createSocket", "Ljava/lang/String;I", 1, "SSL Connections"),
			new BTPattern("android/net/Uri", "parse", "Ljava/lang/String;",0, "Android URI Parser"), // Content provider etc
			// Apache HTTP client stuff
			new BTPattern("java/net/URI", "<init>", "Ljava/lang/String;", 0, "Apache URI Parser"), // Apache URI Parser			
			new BTPattern("org/apache/http/client/methods/HttpGet", "<init>", "Ljava/lang/String;", 0, "Apache HTTP GET"),
			new BTPattern("org/apache/http/client/methods/HttpGet", "setURI", "Ljava/lang/String;", 0, "Apache HTTP GET"),			
			new BTPattern("org/apache/http/client/methods/HttpPost", "<init>", "Ljava/lang/String;", 0, "Apache HTTP POST"),
			new BTPattern("org/apache/http/client/methods/HttpPost", "setURI", "Ljava/lang/String;", 0, "Apache HTTP POST"),
			new BTPattern("org/apache/http/client/methods/HttpHead", "<init>", "Ljava/lang/String;", 0, "Apache HTTP HEAD"),
			new BTPattern("org/apache/http/client/methods/HttpHead", "setURI", "Ljava/lang/String;", 0, "Apache HTTP HEAD"),
			new BTPattern("org/apache/http/client/methods/HttpPut", "<init>", "Ljava/lang/String;", 0, "Apache HTTP PUT"),
			new BTPattern("org/apache/http/client/methods/HttpPut", "setURI", "Ljava/lang/String;", 0, "Apache HTTP PUT"),
			new BTPattern("org/apache/http/client/methods/HttpDelete", "<init>", "Ljava/lang/String;", 0, "Apache HTTP DELETE"),
			new BTPattern("org/apache/http/client/methods/HttpDelete", "setURI", "Ljava/lang/String;", 0, "Apache HTTP DELETE"),
			// Webview URL
			new BTPattern("android/webkit/WebView", "loadUrl", "Ljava/lang/String;",0, "Webview URL"),
			new BTPattern("android/webkit/WebView", "loadUrl", "Ljava/lang/String;Ljava/util/Map;", 0, "Webview URL"),
			
			/*
			 * '*' (wildcard, will match all classes) b/c Activities, Services etc are of the type Context and inherit this method
			 */
			new BTPattern("*", "getSystemService", "Ljava/lang/String;", 0, "Android System Provider Getter"), 
			
			/*
			 * Read the time of sleep methods. J is long and long is two registers wide! So a call to the first function in SMALI
			 * looks like this: invoke-static {v1, v2}, Ljava/lang/Thread;->sleep(J)V. v2 is the second half of the long.
			 */
			new BTPattern("java/lang/Thread", "sleep", "J", 0, "Thread.sleep(long)"),
			new BTPattern("java/lang/Thread", "sleep", "JI", 0, "Thread.sleep(long, int)"),
			};
	
	@BeforeClass
	public static void setupBeforeClass() throws Exception{
		PropertyConfigurator.configure("conf/log4j.properties");
	}
	
	@Before
	public void setUp() throws Exception {
		ds = new XMLBTPatternSource("conf/backtracking-patterns.xml","conf/backtracking-patterns.xsd");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNotNull() throws DataSourceException {
		Set<BTPatternInterface> patterns = ds.getData();
		assertNotNull(patterns);
	}
	
	@Test
	public void testNumberOfPatterns() throws DataSourceException
	{
		Set<BTPatternInterface> patterns = ds.getData();
		assertTrue("Number of parsed patterns does not match",38 <= patterns.size());

	}

	@Test
	public void testParsedPatterns() throws DataSourceException
	{
		Set<BTPatternInterface> parsedPatterns = ds.getData();
		for(int patternIndex=0;patternIndex < hardCodedPatterns.length;patternIndex++)
		{
			assertTrue("Pattern "+patternIndex + " "+hardCodedPatterns[patternIndex]+" was not found in the parsed patterns!",parsedPatterns.contains(hardCodedPatterns[patternIndex]));
		}	
	}
}
