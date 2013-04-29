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

import java.util.Set;
import java.util.TreeSet;


import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.rub.syssec.saaf.analysis.steps.slicing.BTPattern;
import de.rub.syssec.saaf.model.analysis.BTPatternInterface;


/**
 * Reads the definitions for backtrack patterns from an XML file.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class XMLBTPatternSource extends AbstractXMLDataSource<BTPatternInterface>{

	private static final String XML_TAG_PATTERN = "backtracking-pattern";
	private static final String XML_ATTRIBUTE_PKG = "class";
	private static final String XML_ATTR_METHOD = "method";
	private static final String XML_ATTR_DESC = "description";
	private static final String XML_ATTR_IMPORTANT = "interesting";
	private static final String XML_ATTR_PARAMS = "parameters";
	private static final String XML_ATTR_ACTIVE = "active";
	/**
	 * @param dataFile
	 */
	public XMLBTPatternSource(String filename, String schemaFile) {
		super();
		this.dataFile = filename;
		this.schemaFile = schemaFile;
	}

	protected Set<BTPatternInterface> doParse(Document doc) {
		Set<BTPatternInterface> patterns = new TreeSet<BTPatternInterface>();
		NodeList patternNodes = doc.getElementsByTagName(XML_TAG_PATTERN);
		BTPatternInterface pattern = null;
		if (patternNodes != null && patternNodes.getLength() > 0) {
			logger.debug("Found " + patternNodes.getLength()
					+ " nodes of type " + XML_TAG_PATTERN);
			for (int nodeIndex = 0; nodeIndex < patternNodes.getLength(); nodeIndex++) {
				Element patternNode = (Element) patternNodes.item(nodeIndex);

				// obtain package,method and description
				Attr pkg = patternNode.getAttributeNode(XML_ATTRIBUTE_PKG);
				Attr method = patternNode.getAttributeNode(XML_ATTR_METHOD);
				Attr descr = patternNode.getAttributeNode(XML_ATTR_DESC);
				Attr paramSpec = patternNode.getAttributeNode(XML_ATTR_PARAMS);
				Attr paramOfInterest = patternNode.getAttributeNode(XML_ATTR_IMPORTANT);
				Attr active = patternNode.getAttributeNode(XML_ATTR_ACTIVE);
				if (pkg != null && method != null && descr != null && active!=null) {
					logger.debug("Adding pattern " + nodeIndex);
					pattern = new BTPattern(
							 pkg.getValue(), 
							 method.getValue(),
							 paramSpec.getValue(),
							 Integer.parseInt(paramOfInterest.getValue()),
							 descr.getValue());
					pattern.setActive(Boolean.parseBoolean(active.getValue()));
					patterns.add(pattern);
				} else {
					logger.warn("Something went wrong parsing backtracking pattern "
							+ nodeIndex);
				}
			}
		} else {
			logger.warn("No patterns found in file" + this.dataFile);
		}
		logger.debug("Found " + patterns.size() + " patterns");
		return patterns;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "XMLBTPatternSource [dataFile=" + dataFile + "]";
	}

}
