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
package de.rub.syssec.saaf.misc.adchecker;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.rub.syssec.saaf.db.datasources.AbstractXMLDataSource;


/**
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class XMLAdnetworkDataSource extends AbstractXMLDataSource<AdNetwork> {


	private static final String XML_TAG_ADNETWORK = "ad-network";
	private static final String XML_ATTRIBUTE_PATH = "path-fragment";

	public XMLAdnetworkDataSource(String dataFile, String schemaFile) {
		super();
		this.dataFile = dataFile;
		this.schemaFile=schemaFile;
	}

	protected Set<AdNetwork> doParse(Document doc) {
		Set<AdNetwork> adNetworks = new TreeSet<AdNetwork>();
		NodeList adNodes = doc.getElementsByTagName(XML_TAG_ADNETWORK);
		if (adNodes != null) {
			for (int nodeIndex = 0; nodeIndex < adNodes.getLength(); nodeIndex++) {
				Element adNode = (Element) adNodes.item(nodeIndex);
				Attr name = adNode.getAttributeNode(XML_ATTRIBUTE_PATH);
				if (name != null && !name.getValue().isEmpty()) {

					adNetworks.add(new AdNetwork(name.getValue().replace("/", File.separator)));
				}
			}
		}
		return adNetworks;
	}

}
