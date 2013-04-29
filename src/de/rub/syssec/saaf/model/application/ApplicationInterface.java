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
package de.rub.syssec.saaf.model.application;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import de.rub.syssec.saaf.analysis.steps.hash.GenerateHashesStep;
import de.rub.syssec.saaf.model.Entity;
import de.rub.syssec.saaf.model.application.manifest.ManifestInterface;

public interface ApplicationInterface extends Entity {

	/**
	 * @return the directory where all unpacked files from the apk reside
	 */
	public abstract File getUnpackedDataDir();

	public abstract File getManifestFile();

	public abstract ManifestInterface getManifest();

	/**
	 * Get the directory where .smali, .class and .java files are located.
	 * 
	 * @return the directory
	 */
	public abstract File getBytecodeDirectory();

	/**
	 * Returns The directory where all files to this application are stored. 
	 * Extraction and decompilation take place in subdirectories of this folder.
	 * 
	 * @return the directory
	 */
	public abstract File getApplicationDirectory();

	/**
	 * Get the APK file.
	 * 
	 * @return the APK for this application
	 */
	public abstract File getApkFile();

	/**
	 * To work with smali classes, use the getSmaliClass method! this method
	 * returns the actual file on the file system.
	 * 
	 * @param includeFilesFromAdPackages
	 * @return
	 */
	public abstract Vector<File> getAllRawSmaliFiles(
			boolean includeFilesFromAdPackages);

	/**
	 * 
	 * @param includeFilesFromAdPackages
	 * @return
	 */
	public abstract Vector<File> getAllClassFiles(
			boolean includeFilesFromAdPackages);

	/**
	 * Get a already parsed SmaliClass.
	 * @param file the filename
	 * @return the SmaliClass or null if the file is not part of this application
	 */
	public abstract ClassInterface getSmaliClass(File file);

	/**
	 * Get all parsed Smali Files, access will be cached.
	 * 
	 * @param includeFilesFromAdPackages
	 * @return
	 */
	public abstract LinkedList<ClassInterface> getAllSmaliClasss(
			boolean includeFilesFromAdPackages);

	public abstract String getApplicationName();
	
	public abstract void setApplicationName(String name);
	
	/**
	 * This method calculates the total number of codelines of this application
	 * @param includeFilesFromAdPackages this parameter determines whether the 
	 * codelines of the adpackage should be included in the total number of codelines
	 * @return the number of Codelines this application consists of
	 */
	public abstract int getNumberOfCodelines(boolean includeFilesFromAdPackages);
	
	
	/**
	 * Search for a SMALI file corresponding to the className and search for the
	 * correct method
	 * 
	 * TODO: If parameterDeclaration is null, the old behavior is triggered.
	 * But this behavior is only a workaround and will be gone soon.
	 * 
	 * 
	 * @param className
	 *            the full package name of the class
	 * @param methodName
	 *            the name of the method
	 * @param parameterDeclaration
	 * 		the unparsed parameters of the method
	 * @param returnValue the return value of the method
	 * @return the method or null if the class or the method in unknown
	 */
	public abstract MethodInterface getMethodByClassAndName(String className,
			String methodName, byte[] parameterDeclaration,
			byte[] returnValue)
			throws ClassOrMethodNotFoundException;

	public abstract String getFileExtension();
	
	public abstract void setFileExtension(String extension);

	public abstract void setManifest(ManifestInterface manifest);

	public abstract void setManifestFile(File manifestFile);

	public abstract void setBytecodeDirectory(File bytecodeDirectory);

	public abstract void setApplicationDirectory(File appDirectory);

	public abstract void setDecompiledContentDir(File decompiledContentDir);

	public abstract File getDecompiledContentDir();

	public abstract void setApkDirectory(File apkDirectory);

	public abstract File getApkDirectory();

	public abstract void setApkContentDir(File apkContentDir);

	public abstract File getApkContentDir();

	public abstract void setAllSmaliClasss(
			HashMap<String, ClassInterface> smaliClassMap);

	public abstract int getSmaliClassLabel();

	public abstract void setSmaliClassLabel(int smaliClassLabel);

	/**
	 * Set the message digest for the APK file. This will overwrite any set
	 * digest value.
	 * 
	 * @param digestAlgorithm the algorithm
	 * @param digest the value
	 */
	public abstract void setMessageDigest(Digest digestAlgorithm, String digest);

	/**
	 * Get the message digest for the APK file. If the digest has not yet
	 * been set, this method will return null but it will never throw an
	 * exception.
	 * 
	 * Calculation should be done in the {@linkplain GenerateHashesStep} 
	 * step before any real operations start.
	 * 
	 * @param digestAlgorithm
	 * @return the digest or null
	 */
	public abstract String getMessageDigest(Digest digestAlgorithm);
}