package de.rub.syssec.saaf.db.dao;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.rub.syssec.saaf.db.dao.DAOFactory;
import de.rub.syssec.saaf.db.dao.mysql.MySQLDAOFactory;

public class DAOFactoryTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFactoryTypes() {
		DAOFactory factory = DAOFactory.getDAOFactory(DAOFactory.MYSQL_DIALECT);
		assertNotNull("DAOFactory must return a non null factory", factory);
		assertTrue("DAOFactory must return a MySQLDAOFacotry if the MYSQL_DIALECT was selected", factory instanceof MySQLDAOFactory);
	}

}
