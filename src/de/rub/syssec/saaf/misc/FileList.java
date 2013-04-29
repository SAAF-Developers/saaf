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
package de.rub.syssec.saaf.misc;


import java.io.File;
import java.util.Vector;

import de.rub.syssec.saaf.db.datasources.DataSourceException;
import de.rub.syssec.saaf.misc.config.Config;

/**
 * 
 * Scans the given file (directory) recursively for files matching the given filter string.
 */
public class FileList {

	public static final String CLASS_FILES = ".class";
	public static final String SMALI_FILES = ".smali";
	
	private final Vector<File> filterFiles = new Vector<File>();
	private final Vector<File> filterFilesADS = new Vector<File>();
	private final String suffix;
	private Config config;

	
	/**
	 * Get the used suffix to filter files. If no suffix was used, an empty string is returned.
	 * @return
	 */
	public String getUsedSuffix() {
		if (suffix == null) return "";
		else return suffix;
	}
	
	
	/** 
	 * Scan all files. This method will separate files from ad package on its own, see getAllFoundFiles(boolean includeFilesFromAdPackages).
	 * @param file the directory to scan or a single file to add
	 * @param suffix the filter for file names, only files ending with suffix are added (maybe null or empty and the check is NOT case sensitive)
	 */
	public FileList(File file, String suffix) {
		this.config = Config.getInstance();
		this.suffix = suffix;
		// use only lowercase suffix for comparison later, isAdPackage has to be false
		getAllFilesRecursive(file, suffix.toLowerCase(), false);
	}
	
	
	/**
	 * @param includeFilesFromAdPackages include files from known ad packages?
	 * @return all found files matching the filter
	 */
	public Vector<File> getAllFoundFiles(boolean includeFilesFromAdPackages) {
		if (!includeFilesFromAdPackages) return filterFiles;
		else {
			Vector<File> allFiles = new Vector<File>(filterFiles);
			allFiles.addAll(filterFilesADS);
			return allFiles;
		}
	}


	/**
	 * Fill the internal vector with all found files. This method calls itself recursively.
	 * @param file The file or directory to start with.
	 * @param suffix the suffix for files which shall be included
	 * @param isAdPackage denotes whether the file is from an ad package, must be false when called
	 */
	private void getAllFilesRecursive(File file, String suffix, boolean isAdPackage) {
		
		if (file.isFile() && file.getName().toLowerCase().endsWith(suffix)) {
			if (isAdPackage) {
				filterFilesADS.add(file);
			}
			else {
				filterFiles.add(file);
			}
		}

		/*
		 * XXX If malware hides inside a known "ad path", it is invisible if we exclude ads! 
		 */
		else if (file.isDirectory()) {
			File[] listOfFiles = file.listFiles();
			boolean hasAd=false;
			hasAd = config.getAdChecker().containsAnAd(file);
			if (listOfFiles != null) {
				for (int i = 0; i < listOfFiles.length; i++) {
					getAllFilesRecursive(listOfFiles[i], suffix, hasAd);
				}
			}
			else {
				/* nothing */
			}
		}
	}
}
