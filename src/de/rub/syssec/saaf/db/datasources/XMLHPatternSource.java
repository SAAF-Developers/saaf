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

import de.rub.syssec.saaf.analysis.steps.heuristic.HPattern;
import de.rub.syssec.saaf.model.analysis.HPatternInterface;
import de.rub.syssec.saaf.model.analysis.PatternType;


/**
 * Reads the definitions for heuristic patterns from an XML file.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class XMLHPatternSource extends AbstractXMLDataSource<HPatternInterface>{

	private static final String XML_TAG_PATTERN = "heuristic-pattern";
	private static final String XML_ATTR_PATTERN = "pattern";
	private static final String XML_ATTR_TYPE = "type";
	private static final String XML_ATTR_HVALUE = "hvalue";
	private static final String XML_ATTR_DESC = "description";
	private static final String XML_ATTR_ACTIVE = "active";
	

	/**
	 * @param dataFile
	 */
	public XMLHPatternSource(String patterns, String schema) {
		super();
		this.dataFile=patterns;
		this.schemaFile=schema;
	}


	protected Set<HPatternInterface> doParse(Document doc) {
		Set<HPatternInterface> patterns = new TreeSet<HPatternInterface>();
		NodeList patternNodes = doc.getElementsByTagName(XML_TAG_PATTERN);
		HPatternInterface p =null;
		if (patternNodes != null && patternNodes.getLength() >0 ) {
			logger.debug("Found "+patternNodes.getLength()+" nodes of type "+XML_TAG_PATTERN);
			for (int nodeIndex = 0; nodeIndex < patternNodes.getLength(); nodeIndex++) {
				Element patternNode = (Element) patternNodes.item(nodeIndex);
				Attr pattern = patternNode.getAttributeNode(XML_ATTR_PATTERN);
				Attr type = patternNode.getAttributeNode(XML_ATTR_TYPE);
				Attr hvalue = patternNode.getAttributeNode(XML_ATTR_HVALUE);
				Attr description = patternNode.getAttributeNode(XML_ATTR_DESC);
				Attr active = patternNode.getAttributeNode(XML_ATTR_ACTIVE);
				if(pattern!=null && type!=null && hvalue!=null && description!=null)
				{
					p = new HPattern(
							pattern.getValue(),
							PatternType.valueOf(type.getValue()),
							Integer.parseInt(hvalue.getValue()),
							description.getValue());
					p.setActive(Boolean.parseBoolean(active.getValue()));
					patterns.add(p);
				}

			}
		}else{
			logger.warn("No patterns found in file"+this.dataFile);
		}
		logger.debug("Found "+patterns.size()+" patterns");
		return patterns;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "XMLHPatternSource [dataFile=" + dataFile + "]";
	}

}
