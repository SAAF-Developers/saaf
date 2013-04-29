package de.rub.syssec.saaf.db.dao.mysql;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	MySQLApplicaitonDAOTest.class,
	MySQLAnalysisDAOTest.class, 
	MySQLBTPatternDAOTest.class, 
	MySQLBTResultDAOTest.class,
	MySQLDAOFactoryTest.class,
	MySQLHPatternDAOTest.class, 
	MySQLHResultDAOTest.class,
	MySQLPackageDAOTest.class, 
	MySQLClassDAOTest.class, 
	MySQLMethodDAOTest.class,
	MySQLExcpetionDAOTest.class})
public class AllMySQLDAOTests {

}
