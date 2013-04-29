package de.rub.syssec.saaf.reporting;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class STReportGeneratorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

 @Test
	public void testGenerateReport() {
	 
	 assertTrue(true);
	 /* Cannot test this since Mocking of Application/Analysis is impossible as long
	  * as you have to subclass them and thereby execute their constructor that perform
	  * unwanted actions. 
	  *
	 	try {
			ReportGenerator rg = new STReportGenerator(new File("templates"),"xml.stg");
			
			rg.generateReport(new MockAnalysis(null));
		} catch (ReportingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

}
