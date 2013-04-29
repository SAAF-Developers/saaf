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
package de.rub.syssec.saaf.analysis.steps.metadata;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.rub.syssec.saaf.application.manifest.Manifest;
import de.rub.syssec.saaf.application.manifest.components.Action;
import de.rub.syssec.saaf.application.manifest.components.Activity;
import de.rub.syssec.saaf.application.manifest.components.IntentFilter;
import de.rub.syssec.saaf.application.manifest.components.IntentReceivingComponent;
import de.rub.syssec.saaf.application.manifest.components.Receiver;
import de.rub.syssec.saaf.application.manifest.components.Service;
import de.rub.syssec.saaf.application.manifest.permissions.Permission;
import de.rub.syssec.saaf.application.manifest.permissions.PermissionRequest;
import de.rub.syssec.saaf.model.application.manifest.IntentFilterInterface;
import de.rub.syssec.saaf.model.application.manifest.ManifestInterface;
import de.rub.syssec.saaf.model.application.manifest.PermissionRequestInterface;

/**
 * This class reads the AndroidManifest.xml using DOM and XPath
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
 */
public class DOMManifestParser implements ManifestParser {

	private static final String ANDROID_ATTR_NS = "http://schemas.android.com/apk/res/android";
	private static final Logger LOGGER = Logger
			.getLogger(DOMManifestParser.class);

	public DOMManifestParser(PermissionChecker checker) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.nongui.ManifestParser#parse(java.io.File)
	 */
	@Override
	public ManifestInterface parse(File manifest)
			throws ManifestParserException {
		LOGGER.info("Analyzing Manifest: " + manifest.getAbsolutePath());
		ManifestInterface parsedManifest = new Manifest(manifest);
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder;
		Document doc;
		try {
			builder = domFactory.newDocumentBuilder();
			doc = builder.parse(manifest);
		} catch (ParserConfigurationException e) {
			throw new ManifestParserException(e);
		} catch (SAXException e) {
			throw new ManifestParserException(e);
		} catch (IOException e) {
			throw new ManifestParserException(e);
		}

		parseManifest(doc, parsedManifest);
		parseActivities(doc, parsedManifest);
		parseServices(doc, parsedManifest);
		parseReceivers(doc, parsedManifest);
		parsePermissions(doc, parsedManifest);
		LOGGER.info("found in " + parsedManifest.getPath() + ": "
				+ parsedManifest.getNumberOfActivities() + " Activities" + ", "
				+ parsedManifest.getNumberOfReceivers() + " Receivers" + ", "
				+ parsedManifest.getNumberOfServices() + " Services" + " and "
				+ parsedManifest.getNumberOfPermissions() + " Permissions");
		LOGGER.info("Finished analyzing Manifest: "
				+ manifest.getAbsolutePath());
		return parsedManifest;
	}

	/**
	 * Parse the Manifest for basic tags, like 'manifest', 'uses-sdl',
	 * 'application', etc., which only occur one time.
	 * 
	 * @param doc
	 *            XML-document-object (INPUT).
	 * @param stats
	 *            Manifest-Class to save results (OUTPUT).
	 */
	private void parseManifest(Document doc, ManifestInterface stats) {
		// ####### <manifest ..>
		NodeList manifests = doc.getElementsByTagName("manifest");
		if (manifests.getLength() == 1) {
			Element manifestsNode = (Element) manifests.item(0);
			Attr packageName = manifestsNode.getAttributeNode("package");
			if (packageName != null) {
				LOGGER.debug("package: " + packageName.getValue());
				stats.setPackageName(packageName.getValue());
			}
			Attr versionCode = manifestsNode
					.getAttributeNode("android:versionCode");
			if (versionCode != null) {
				LOGGER.debug("version code: " + versionCode.getValue());
				try {
					stats.setVersionCode(Integer.parseInt(versionCode
							.getValue()));
				} catch (NumberFormatException e) {
					LOGGER.warn("version code could not be parsed correctly");
				}
			}
			Attr versionName = manifestsNode
					.getAttributeNode("android:versionName");
			if (versionName != null) {
				LOGGER.debug("version name: " + versionName.getValue());
				stats.setVersionName(versionName.getValue());
			}
		} else {
			LOGGER.error("There is more than one <manifest> in the Manifests: "
					+ manifests);
		}
		// ####### <uses-sdk ..>
		NodeList usesSDK = doc.getElementsByTagName("uses-sdk");
		if (usesSDK.getLength() == 1) {
			Element usesSDKNode = (Element) usesSDK.item(0);
			Attr minSdkVersion = usesSDKNode.getAttributeNode("minSdkVersion");
			if (minSdkVersion != null) {
				LOGGER.info("MinSdkVersion: " + minSdkVersion.getValue());
				try {
					stats.setMinSdkVersion(Integer.parseInt(minSdkVersion
							.getValue()));
				} catch (NumberFormatException nfe) {
					LOGGER.warn("MinSdkVersion could not be parsed correctly");
				}
			}
		} else if (usesSDK.getLength() > 0) {
			LOGGER.warn("There is more than one <uses-sdk> in the usesSDK: "
					+ usesSDK.getLength());
		}
		// ####### <application ..>
		NodeList application = doc.getElementsByTagName("application");
		if (application.getLength() == 1) {
			Element applicationNode = (Element) application.item(0);
			Attr appLabel = applicationNode.getAttributeNode("android:label");
			Attr appDebuggable = applicationNode
					.getAttributeNode("android:debuggable");
			if (appDebuggable != null) {
				LOGGER.debug("appDebuggable: " + appDebuggable.getValue());
				stats.setAppDebuggable(Boolean.parseBoolean(appDebuggable
						.getValue()));
			}
			if (appLabel != null) {
				LOGGER.debug(" appLabel: " + appLabel.getValue());
				stats.setAppLabel(appLabel.getValue());
			}

		} else {
			LOGGER.error("There is more than one <application> in the application: "
					+ application);
		}
	}

	private void parseActivities(Document doc, ManifestInterface stats) {
		// count activities
		LOGGER.debug("Analyzing activity definitions...");
		NodeList activities = doc.getElementsByTagName("activity"); //$NON-NLS-1$
		for (int nodeIndex = 0; nodeIndex < activities.getLength(); nodeIndex++) {
			Element activityNode = (Element) activities.item(nodeIndex);
			Attr attr = activityNode
					.getAttributeNodeNS(ANDROID_ATTR_NS, "name");
			if (attr != null) {
				Activity activity = new Activity(attr.getValue());

				// look for intent-filters and add them
				parseIntentFilters(activityNode, activity);

				stats.addActivity(activity);
				LOGGER.debug("Found activity: " + activity);
			}
		}
		LOGGER.debug("Finished analyzing activity definitions. "
				+ stats.getNumberOfActivities() + " activities found");
	}

	/**
	 * Look for intent-filters as children of the given element and adds them to
	 * the specified component.
	 * 
	 * @param node
	 *            the DOM-Node whose descendants we will check for
	 *            intent-filters.
	 * @param component
	 *            the component we will add the intent-filters to.
	 * @throws NumberFormatException
	 */
	private void parseIntentFilters(Element node,
			IntentReceivingComponent component) throws NumberFormatException {
		Attr attr;
		NodeList filters = node.getElementsByTagName("intent-filter");
		for (int filterIndex = 0; filterIndex < filters.getLength(); filterIndex++) {
			Element filterNode = (Element) filters.item(filterIndex);
			IntentFilter filter = new IntentFilter();
			attr = filterNode.getAttributeNodeNS(ANDROID_ATTR_NS, "label");
			if (attr != null) {
				filter.setLabel(attr.getValue());
			}
			attr = filterNode.getAttributeNodeNS(ANDROID_ATTR_NS, "priority");
			if (attr != null) {
				try{
				filter.setPriority(Integer.parseInt(attr.getValue()));
				}catch(NumberFormatException nfe)
				{
					LOGGER.warn("could not parse priority for intent");
				}
			}
			parseFilterActions(filterNode, filter);
			component.addIntentFilter(filter);
		}
	}

	private void parseFilterActions(Element node, IntentFilterInterface filter) {
		Attr attr;
		NodeList filters = node.getElementsByTagName("action");
		for (int filterIndex = 0; filterIndex < filters.getLength(); filterIndex++) {
			Element filterNode = (Element) filters.item(filterIndex);
			attr = filterNode.getAttributeNodeNS(ANDROID_ATTR_NS, "name");
			if (attr != null) {
				filter.addAction(new Action(attr.getValue()));
			}
		}
	}

	private void parseServices(Document doc, ManifestInterface stats) {
		// count activities
		LOGGER.debug("Analyzing service definitions...");
		NodeList services = doc.getElementsByTagName("service"); //$NON-NLS-1$
		for (int nodeIndex = 0; nodeIndex < services.getLength(); nodeIndex++) {
			Element serviceNode = (Element) services.item(nodeIndex);
			Attr attr = serviceNode.getAttributeNodeNS(ANDROID_ATTR_NS, "name");
			Service service = new Service();

			if (attr != null) {
				service.setName(attr.getValue());
			}

			// look for intent-filters
			parseIntentFilters(serviceNode, service);

			stats.addService(service);
			LOGGER.debug("Found service: " + service);
		}
		LOGGER.debug("Finished analyzing service definitions. "
				+ stats.getNumberOfServices() + " services found.");
	}

	private void parseReceivers(Document doc, ManifestInterface stats) {
		// count activities
		LOGGER.debug("Analyzing receiver definitions...");
		NodeList receivers = doc.getElementsByTagName("receiver"); //$NON-NLS-1$
		for (int nodeIndex = 0; nodeIndex < receivers.getLength(); nodeIndex++) {
			Element receiverNode = (Element) receivers.item(nodeIndex);
			Attr attr = receiverNode
					.getAttributeNodeNS(ANDROID_ATTR_NS, "name");

			Receiver receiver = new Receiver();
			if (attr != null) {
				receiver.setName(attr.getValue());
			}
			// look for intent-filters
			parseIntentFilters(receiverNode, receiver);

			stats.addReceiver(receiver);
			LOGGER.debug("Found receiver: " + receiver);
		}
		LOGGER.debug("Finished analyzing receiver definitions. "
				+ stats.getNumberOfReceivers() + " receivers found.");
	}

	private void parsePermissions(Document doc, ManifestInterface manifest) {
		NodeList requestedPermissions = doc
				.getElementsByTagName("uses-permission"); //$NON-NLS-1$
		Element permission;
		Attr name;
		LOGGER.debug("Analyzing requested permissions...");

		// iterate over all permissions requested in the manifest
		for (int permissionNr = 0; permissionNr < requestedPermissions
				.getLength(); permissionNr++) {
			permission = (Element) requestedPermissions.item(permissionNr);
			name = permission.getAttributeNodeNS(ANDROID_ATTR_NS, "name");
			PermissionRequestInterface request = new PermissionRequest(
					new Permission(name.getValue()));

			manifest.addPermissionRequest(request);
			LOGGER.debug("Requested permission: "
					+ request.getRequestedPermission());
		}
		LOGGER.debug("Finished analyzing requested permissions. "
				+ manifest.getNumberOfPermissions() + " permissions requested.");
	}

}
