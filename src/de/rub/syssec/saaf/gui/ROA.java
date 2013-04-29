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
package de.rub.syssec.saaf.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;

import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;

/**
 * A small helper class which stores the path to recently opened apk files.
 */
public class ROA {
	
	private static final String RECENTLY_OPENED_APKS = "ruf";
	private static final String FILENAME = "roa.prop";
	
	private LinkedList<String> roaList = new LinkedList<String>();
	
	/**
	 * The delimiter for the property file. It will automatically be
	 * set to a value which is not allowed in a filename.
	 */
	private final String delimiter;
	
	public ROA() {
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			delimiter = "\\\\"; // escaped backslashes
		}
		else {
			delimiter = "//"; // Linux etc, but not Windows
		}
	}
	
	/**
	 * A a new APK to the beginning of the list.
	 * @param apkPath path to the apk
	 */
	public void addOpenedApk(String apkPath) {
		roaList.remove(apkPath);
		roaList.addFirst(apkPath);
		if (roaList.size() > 10) {
			roaList.removeLast();
		}
	}
	
	/**
	 * Return the actual list.
	 * The list itself is returned, so it can be modified.
	 * @return the list
	 */
	public LinkedList<String> getRoaList() {
		return roaList;
	}
	
	public void clear() {
		roaList.clear();
	}
	
	/**
	 * Load a stored list from property file.
	 * @throws IOException 
	 */
	public void loadList() throws IOException {
		Properties prop = new Properties();
		File f = new File(Config.getInstance().getFileConfigValue(ConfigKeys.DIRECTORY_HOME), FILENAME);
		if (!f.exists()) return;
		FileInputStream fin = new FileInputStream(f);
		prop.load(fin);
		fin.close();
		String p = prop.getProperty(RECENTLY_OPENED_APKS);
		
		if (p == null || p.trim().isEmpty()) return;
		
		String[] ruf = p.split(delimiter);
		for (int i = 0; i<ruf.length; i++) {
			if (i > 9) break;
			roaList.add(ruf[i]);
		}
	}
	
	/**
	 * Store the current list to a property file.
	 * @throws IOException
	 */
	public void storeList() throws IOException {
		StringBuilder sb = new StringBuilder();
		for (String s : roaList) {
			sb.append(s);
			if (!roaList.getLast().equals(s)) {
				sb.append(delimiter);
			}
		}
		Properties prop = new Properties();
		prop.setProperty(RECENTLY_OPENED_APKS, sb.toString());

		File f = new File(Config.getInstance().getFileConfigValue(ConfigKeys.DIRECTORY_HOME), FILENAME);
		FileOutputStream fos = new FileOutputStream(f);
		prop.store(fos, "Recently opened APKs for GUI");
		fos.close();
	}

}
