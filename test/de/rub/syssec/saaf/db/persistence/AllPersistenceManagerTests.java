package de.rub.syssec.saaf.db.persistence;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AnalysisEntityManagerTest.class,
				BTResultEntityManagerTest.class, 
				ClassEntityManagerTest.class, 
				PackageEntityManagerTest.class,
				ApplicationEntityManagerTest.class,
				HResultEntityManagerTest.class,
				MethodEntityManagerTest.class,
				HPatternEntityManagerTest.class,
				BTPatternEntityManagerTest.class})
public class AllPersistenceManagerTests {

}
