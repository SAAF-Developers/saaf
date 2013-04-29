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

import de.rub.syssec.saaf.application.manifest.permissions.Permission;
import de.rub.syssec.saaf.model.application.PermissionInterface;
import de.rub.syssec.saaf.model.application.PermissionType;

/**
 * Reads permission information from an XML File.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class XMLPermissionDatasource extends AbstractXMLDataSource<PermissionInterface> {

	private static final String XML_TAG_PERMISSION = "permission";

	private static final String XML_ATTRIBUTE_NAME = "name";

	private static final String XML_ATTRIBUTE_TYPE = "type";

	private static final String XML_ATTRIBUTE_DESCRIPTION = "description";
//	private Logger logger = Logger.getLogger(XMLPermissionDatasource.class);
	
	public XMLPermissionDatasource(String dataFile,String schema) {
		this.dataFile=dataFile;
		this.schemaFile=schema;
	}

	protected Set<PermissionInterface> doParse(Document doc) {
		Set<PermissionInterface> permissions = new TreeSet<PermissionInterface>();
		NodeList permNodes = doc.getElementsByTagName(XML_TAG_PERMISSION);
		Permission p=null;
		if (permNodes != null) {
			for (int nodeIndex = 0; nodeIndex < permNodes.getLength(); nodeIndex++) {
				Element permNode = (Element) permNodes.item(nodeIndex);

				Attr name = permNode.getAttributeNode(XML_ATTRIBUTE_NAME);
				if (name != null && !name.getValue().isEmpty()) {
					p=new Permission(name.getValue());
					
					Attr type = permNode.getAttributeNode(XML_ATTRIBUTE_TYPE);
					if (type != null && !type.getValue().isEmpty()) {
						p.setType(PermissionType.valueOf(type.getValue().toUpperCase()));
					}
					
					Attr descr = permNode.getAttributeNode(XML_ATTRIBUTE_DESCRIPTION);
					if (descr != null && !descr.getValue().isEmpty()) {
						p.setDescription(descr.getValue().toUpperCase());
					}
				}
				if(p!=null)
					permissions.add(p);
			}
		}
		return permissions;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "XMLPermissionDatasource [permissions=" + dataFile + "]";
	}

}
