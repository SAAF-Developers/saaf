package de.rub.syssec.saaf.db.datasources;

import static org.junit.Assert.*;

import java.util.Set;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rub.syssec.saaf.analysis.steps.heuristic.HPattern;
import de.rub.syssec.saaf.db.datasources.XMLHPatternSource;
import de.rub.syssec.saaf.model.analysis.HPatternInterface;
import de.rub.syssec.saaf.model.analysis.PatternType;

public class XMLHPatternSourceTest {

	private XMLHPatternSource ds;
	private HPatternInterface[] hardCodedPatterns = {
			//old Pattern
			new HPattern("android/telephony/TelephonyManager->getSubscriberId",PatternType.INVOKE,0,"IMSI"),
			new HPattern("android/telephony/TelephonyManager->getDeviceId",PatternType.INVOKE,0,"IMEI"),
			new HPattern("android/telephony/SmsManager->sendTextMessage",PatternType.INVOKE,0,"send SMS"),
			new HPattern("android/telephony/SmsManager->sendMultipartTextMessage",PatternType.INVOKE,0,"send multi-part SMS"),
			new HPattern("android/telephony/SmsManager->sendDataMessage",PatternType.INVOKE,0,"send data SMS"),
			new HPattern("query(Landroid/net/Uri;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor",PatternType.SMALI,0,"get Cursor for DB"),
			new HPattern("managedQuery(",PatternType.SMALI,0,"get Cursor for DB (deprecated)"),
			new HPattern("android/content/CursorLoader->CursorLoader",PatternType.INVOKE,0,"get Cursor for DB"),
			new HPattern("android.intent.action.CALL",PatternType.SMALI,0,"initiate call"),
			new HPattern("java/lang/System->loadLibrary",PatternType.INVOKE,-0,"load Java or JNI Libaries/Classes"),
			new HPattern("chmod",PatternType.SMALI,0,"change mode"),
			new HPattern("java/lang/Runtime->exec",PatternType.INVOKE,0,"execute command"),
			new HPattern("openConnection()Ljava/net/URLConnection",PatternType.SMALI,0,"open HTTP connection"),
			new HPattern("javax/crypto/Cipher->doFinal",PatternType.INVOKE,0,"use Java Crypto"),
			new HPattern("permission.INTERNET permission.READ_CONTACTS",PatternType.MANIFEST,0,"permissions: Internet and Read Contacts"),
			new HPattern("permission.READ_HISTORY_BOOKMARKS permission.INTERNET",PatternType.MANIFEST,-0,"permission: Read Bookmarks and Internet"),
			new HPattern("permission.SEND_SMS",PatternType.MANIFEST,0,"permission: Send SMS"),
			new HPattern("permission.CALL_PHONE",PatternType.MANIFEST,0,"permission: Call Phone"),
			new HPattern("permission.READ_CONTACTS permission.INTERNET permission.READ_PHONE_STATE",PatternType.MANIFEST,0,"permission: Internet, Read Contacts and Read Phone State"),
			new HPattern("permission.READ_SMS permission.INTERNET permission.READ_PHONE_STATE",PatternType.MANIFEST,0,"permission: Internet, Read SMS and Read Phone State"),
			new HPattern("noActivity permission.SEND_SMS",PatternType.MANIFEST,0,"permission + Activity: no Activity and Send SMS"),
			new HPattern("noActivity permission.READ_CONTACTS permission.INTERNET permission.READ_PHONE_STATE",PatternType.MANIFEST,0,""),
			new HPattern("noActivity permission.SEND_SMS permission.RECEIVE_SMS",PatternType.MANIFEST,0,""),
			new HPattern("noActivity permission.READ_SMS permission.INTERNET permission.READ_PHONE_STATE",PatternType.MANIFEST,0,""),
			//new Pattern
			new HPattern("android/telephony/gsm/SmsManager->sendTextMessage",PatternType.INVOKE,0,"send SMS"),
			new HPattern("android/telephony/gsm/SmsManager->sendMultipartTextMessage",PatternType.INVOKE,0,"send multi-part SMS"),
			new HPattern("android/telephony/gsm/SmsManager->sendDataMessage",PatternType.INVOKE,0,"send data SMS"),
			new HPattern("java/lang/Class->forName",PatternType.INVOKE,0,"Reflective class getter"),
			new HPattern("java/lang/Class->getMethod",PatternType.INVOKE,0,"Reflective method getter"),
			new HPattern("javax/net/ssl/SSLSocket->setEnabledCipherSuites",PatternType.INVOKE,0,"SSL Cipher Suites"),
			new HPattern("javax/net/ssl/SSLSocketFactory->createSocket",PatternType.INVOKE,0,"SSL Connections"),
			new HPattern("java/lang/Thread->sleep",PatternType.INVOKE,0,"Thread.sleep(x)"),
			new HPattern("android/os/SystemClock->sleep",PatternType.INVOKE,0,"SystemClock.sleep(x)"),
			new HPattern("android/support/v4/content/CursorLoader->CursorLoader",PatternType.INVOKE,0,"get Cursor for DB"),
			new HPattern("ACTION_CALL",PatternType.SMALI,0,"initiate call (const from android.content.intent"),
			new HPattern("android/app/AlarmManager->setRepeating", PatternType.INVOKE, 0, "alarm service, setRepeating()"),
			new HPattern("android/app/AlarmManager->setInexactRepeating", PatternType.INVOKE, 0, "alarm service, setInexactRepeating()"),
			new HPattern("android/app/AlarmManager->set", PatternType.INVOKE, 0, "alarm service, set()"),
			new HPattern(" native ", PatternType.METHOD_MOD, 0, "native invokes"), // spaces are relevant for search
			new HPattern("",PatternType.PATCHED_CODE,0,"Search for BBs w/ possible patched code"),
	};
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		PropertyConfigurator.configure("conf/log4j.properties");
	}

	@Before
	public void setUp() throws Exception {
		ds = new XMLHPatternSource("conf/heuristic-patterns.xml","conf/schema/heuristic-patterns.xsd");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNotNull() throws Exception {
		Set<HPatternInterface> patterns = ds.getData();
		assertNotNull(patterns);
	}
	@Test
	public void numberOfPatterns() throws Exception{
		Set<HPatternInterface> patterns = ds.getData();
		assertEquals("Number of parsed heurisitc patterns does not match.",46,patterns.size());
	}
	
	@Test
	public void testParsedPatterns() throws Exception
	{
		
		// check if all of the hardcoed patterns were also read from XML
		Set<HPatternInterface> parsedPatterns = ds.getData();
		for(int patternIndex=0;patternIndex < hardCodedPatterns.length;patternIndex++)
		{
			assertTrue("Pattern "+patternIndex + " "+hardCodedPatterns[patternIndex]+" was not found in the parsed patterns!",parsedPatterns.contains(hardCodedPatterns[patternIndex]));
		}
	}

}
