/* SAAF: A static analyzer for APK files.
 * Copyright (C) 2013  syssec.rub.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.rub.syssec.saaf.db.datasources;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
/**
 * Dataasource that provides XML reading and optional validation for subclasses.
 * <br/>
 * The class provides a default implementation of the getData
 * method that tries to open the member <em>dataFile</em> and
 * apply schema validation if a schema was given in <em>schemafile</em>.
 * 
 * After opening (and optionally validating) the data-file the doParse-Method
 * is called. Subclasses must implement this to actually build a dataset
 * from the DOM.
 * <br/>
 * The code does <b>not</b> throw an exception if the schema is invalid
 * but merely logs it.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 * @param <T>
 */
public abstract class AbstractXMLDataSource<T> implements Datasource<T>{

	/**
	 * The path to the XML file that should be read into a DOM and passed to <em>doParse</em>
	 */
	protected String dataFile;
	/**
	 * The path to the XML-Schema file that should be used to validate <em>dataFile</em>
	 */
	protected String schemaFile;
	
	protected Logger logger = Logger.getLogger(AbstractXMLDataSource.class);

	public AbstractXMLDataSource() {
		super();
	}

	@Override
	public Set<T> getData() throws DataSourceException {
		Set<T> patterns = new TreeSet<T>();
		File patternFile = new File(this.dataFile);
		File schemaFile = new File(this.schemaFile);
		Document data;
		try {
			data = readXMLFile(patternFile);
			if(schemaFile.exists() && schemaFile.canRead())
			{
				logger.debug("Validating additional configuration...");
				Document schema = readXMLFile(schemaFile);
				if(isValid(data, schema))
				{
					logger.debug("Additional configuration valid.");
				}else
				{
					logger.warn("Configuration file invalid! This may cause errors down the line");
				}
				
			}else{
				logger.warn("No XML-Schema was found at "+schemaFile+" invalid additional configuration will not be detected");
			}
			patterns = doParse(data);
		} catch (InvalidXMLException e) {
			logger.error("Problem reading additional configuration "
					+ patternFile.getAbsolutePath(), e);
		}
		return patterns;
	}

	protected abstract Set<T> doParse(Document data);

	private Document readXMLFile(File file) throws InvalidXMLException {
		Document xmlFile;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			xmlFile = builder.parse(file);
		} catch (ParserConfigurationException e) {
			throw new InvalidXMLException(e);
		} catch (SAXException e) {
			throw new InvalidXMLException(e);
		} catch (IOException e) {
			throw new InvalidXMLException(e);
		}
		return xmlFile;
	}

	private boolean isValid(Document xmlDocument, Document xmlSchema) {
		boolean valid = false;
	
		try {
			DOMSource xmlSource = new DOMSource(xmlDocument);
			DOMSource schemaSource = new DOMSource(xmlSchema);
			SchemaFactory schemaFactory = SchemaFactory
					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(schemaSource);
			Validator validator = schema.newValidator();
			validator.validate(xmlSource);
			valid = true;
		} catch (SAXException e) {
			logger.warn("Validation error",e);
		} catch (IOException e) {
			// do nothing valid=false is failsafe default.
		}
		return valid;
	}

	/**
	 * @return the dataFile
	 */
	public String getDataFile() {
		return dataFile;
	}

	/**
	 * @param dataFile the dataFile to set
	 */
	public void setDataFile(String filename) {
		this.dataFile = filename;
	}

	/**
	 * @return the schemaFile
	 */
	public String getSchemaFile() {
		return schemaFile;
	}

	/**
	 * @param schemaFile the schemaFile to set
	 */
	public void setSchemaFile(String schemaFile) {
		this.schemaFile = schemaFile;
	}

	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * @param logger the logger to set
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

}