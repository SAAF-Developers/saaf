package de.rub.syssec.saaf.db.dao.mysql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rub.syssec.saaf.analysis.steps.slicing.BTPattern;
import de.rub.syssec.saaf.db.DatabaseHelper;
import de.rub.syssec.saaf.db.dao.exceptions.DAOException;
import de.rub.syssec.saaf.db.dao.exceptions.DuplicateEntityException;
import de.rub.syssec.saaf.db.dao.exceptions.NoSuchEntityException;
import de.rub.syssec.saaf.db.dao.mysql.MySQLBTPatternDAO;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.BTPatternInterface;

public class MySQLBTPatternDAOTest {

	private Connection connection;
	MySQLBTPatternDAO dao;
	private Logger logger=Logger.getLogger(MySQLBTPatternDAOTest.class);
	private DatabaseHelper helper;
	
	@BeforeClass
	public static void setupBeforeClass() throws Exception{
		PropertyConfigurator.configure("conf/log4j.properties");
	}
	
	@Before
	public void setUp() throws Exception {
		try {
			
			helper = new DatabaseHelper(Config.getInstance());
			helper.createDatabaseSchema();
			connection = helper.getConnection();
			//create the dao under test
			dao = new MySQLBTPatternDAO(connection);
		} catch (Exception e) {
			logger.error("Unable to connect to DB!",e);
		}
	}

	@After
	public void tearDown() throws Exception {
		helper.dropTables();
		connection.close();
	}

	@Test
	public void testCreate() throws DAOException, DuplicateEntityException {
		BTPatternInterface btpattern = new BTPattern("java/lang/String", "append", "Ljava/lang/String;",1,"test pattern");
		int id = dao.create(btpattern);
		assertTrue("The id returned by MySQLBTPatternDAO.create must be a positive integer",id>0);
	}
	
	@Test(expected=DAOException.class)
	public void testCreateWithNullClassName() throws Exception
	{
		BTPatternInterface btpattern = new BTPattern("java/lang/String", "append", "Ljava/lang/String;",1,"test pattern");
		btpattern.setQualifiedClassName(null);
		dao.create(btpattern);
	}
	
	@Test(expected=DAOException.class)
	public void testCreateWithNullMethodName() throws Exception
	{
		BTPatternInterface btpattern = new BTPattern("java/lang/String", "append", "Ljava/lang/String;",1,"test pattern");
		btpattern.setMethodName(null);
		dao.create(btpattern);
	}
	
	@Test(expected=DAOException.class)
	public void testCreateWithNullParamTypes() throws Exception
	{
		BTPatternInterface btpattern = new BTPattern("java/lang/String", "append", "Ljava/lang/String;",1,"test pattern");
		btpattern.setParameterSpecification(null);
		dao.create(btpattern);
	}
	
	@Test(expected=DAOException.class)
	public void testCreateWithNegativeParamOfInterest() throws Exception
	{
		BTPatternInterface btpattern = new BTPattern("java/lang/String", "append", "Ljava/lang/String;",1,"test pattern");
		btpattern.setParameterOfInterest(-1);
		dao.create(btpattern);
	}
	
	@Test(expected=DuplicateEntityException.class)
	public void testcreateSameTwice() throws DAOException, DuplicateEntityException
	{
		BTPatternInterface btpatternA = new BTPattern("java/lang/String", "append", "Ljava/lang/String;",1,"test pattern");
		dao.create(btpatternA);
		dao.create(btpatternA);
	}
	
	@Test
	public void testRead() throws Exception {
		//manually insert a record for a BTPattern and retrieve the id
		String className = "java/lang/Runtime";
		String methodName = "exec";
		String paramSpec = new String("Ljava/lang/String;".getBytes());
		int paramOfInterest = 0;
		String description = "test pattern";
				
		int inserted = createBTPattern(className, methodName, paramSpec, paramOfInterest,description);
		//try to read a BTPattern via the DAO an compare to the data used to create.
		BTPatternInterface read = dao.read(inserted);
		assertNotNull("There was no object read from the database",read);
		assertEquals("The id was not set correctly",1,read.getId());
	}
	
	@Test
	public void testReadNonExisting() throws Exception
	{
		assertNull(dao.read(1));
	}
	
	@Test
	public void testReadAll() throws Exception
	{
		BTPattern testPattern = new BTPattern("java/lang/Runtime", "exec", new String("Ljava/lang/String;".getBytes()), 0, "test pattern");
		dao.create(testPattern);
		BTPattern testPattern2 = new BTPattern("java/lang/Runtime", "getRuntime", new String("Ljava/lang/String;".getBytes()), 0, "test pattern");
		dao.create(testPattern2);
		List<BTPatternInterface> read= dao.readAll();
		assertEquals("Number of read patterns is not correct",2,read.size());
		for(BTPatternInterface p : read)
		{
			assertTrue("A pattern must have an id >0",p.getId()>0);
		}
	}
	
	@Test
	public void testUpdate() throws Exception {
		BTPattern testPattern = new BTPattern("java/lang/Runtime", "exec", new String("Ljava/lang/String;".getBytes()), 0, "test pattern");
		//save it
		int id = dao.create(testPattern);
		testPattern.setId(id);
		//modify it
		String changedDesc = "changed description";
		testPattern.setDescription(changedDesc);
		//update database
		boolean updated = dao.update(testPattern);
		assertTrue("BTBatternDAO.update did not return true",updated);
		//read back
		BTPatternInterface read = dao.read(testPattern.getId());
		//test if the record has actually been changed
		assertEquals("Descriiption has not been changed",changedDesc,read.getDescription());
	}
	
	@Test(expected=NoSuchEntityException.class)
	public void testUpdateNonExisting()throws Exception
	{
		BTPattern unsavedPattern =new BTPattern("java/lang/Runtime", "exec", new String("Ljava/lang/String;".getBytes()), 0, "test pattern");
		dao.update(unsavedPattern);
	}

	@Test
	public void testDelete() throws Exception {
		BTPattern testPattern = new BTPattern("java/lang/Runtime", "exec", new String("Ljava/lang/String;".getBytes()), 0, "test pattern");
		//save it
		int id = dao.create(testPattern);
		testPattern.setId(id);
		//remove using dao
		assertTrue(dao.delete(testPattern));
		//test if the record is actually gone
		assertNull("After BTBatternDAO.delete reading a deleted Pattern should return null",dao.read(id));
	}
	
	@Test
	public void testDeleteAll() throws Exception
	{
		BTPattern testPattern = new BTPattern("java/lang/Runtime", "exec", new String("Ljava/lang/String;".getBytes()), 0, "test pattern");
		dao.create(testPattern);
		BTPattern testPattern2 = new BTPattern("java/lang/Runtime", "getRuntime", new String("Ljava/lang/String;".getBytes()), 0, "test pattern");
		dao.create(testPattern2);
		assertTrue(dao.deleteAll()==2);
	}
	
	@Test(expected=NoSuchEntityException.class)
	public void testDeleteNonExisiting()throws Exception
	{
		BTPattern unsavedPattern =new BTPattern("java/lang/Runtime", "exec", new String("Ljava/lang/String;".getBytes()), 0, "test pattern");
		dao.delete(unsavedPattern);
	}
	
	/**
	 * Creates a DB pattern in the database _without_ using the DAO
	 * 
	 * @return
	 * @throws Exception 
	 */
	private int createBTPattern(String className, String methodName, String paramSpec, int paramOfinterest,String description) throws Exception
	{
		int id;
		String insertStmt = "INSERT INTO backtrack_pattern(" +
				"qualified_class, " +
				"method_name, " +
				"parameter_types, " +
				"param_of_interest, " +
				"description,"+
				"active) VALUES(?,?,?,?,?,?)";
				
				
		PreparedStatement insert = connection.prepareStatement(insertStmt, Statement.RETURN_GENERATED_KEYS);
		insert.setString(1, className);
		insert.setString(2, methodName);
		insert.setString(3, paramSpec);
		insert.setInt(4, paramOfinterest);
		insert.setString(5, description);
		insert.setBoolean(6, true);
		insert.executeUpdate();
		ResultSet rs = insert.getGeneratedKeys();
		if(rs.next())
		{
			id=rs.getInt(1);
		}else{
			throw new Exception("Autogenerated keys could not be retrieved!");
		}
		
		return id;
	}

}
