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
package de.rub.syssec.saaf.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.application.manifest.permissions.Permission;
import de.rub.syssec.saaf.misc.FileList;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.APICall;
import de.rub.syssec.saaf.model.APICalls;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.ClassOrMethodNotFoundException;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.DetectionLogicError;
import de.rub.syssec.saaf.model.application.Digest;
import de.rub.syssec.saaf.model.application.InstructionType;
import de.rub.syssec.saaf.model.application.MethodInterface;
import de.rub.syssec.saaf.model.application.SmaliClassError;
import de.rub.syssec.saaf.model.application.manifest.ComponentInterface;
import de.rub.syssec.saaf.model.application.manifest.ManifestInterface;

/**
 * This class represents a whole Android application or APK file.
 */
public class Application implements ApplicationInterface {

	private static final Logger LOGGER = Logger.getLogger(Application.class);
	/**
	 * The directory where .smali, .class and .java files are located
	 */
	private File bytecodeDirectory;
	private File appDirectory;
	/**
	 * the directory where all unpacked files from the apk reside
	 */
	private File decompiledContentDir;

	private String applicationName;
	private String fileExtension;

	private File apkFile;
	private File manifestFile;
	private ManifestInterface manifest;

	// directory in which the content of the .apk file is extracted
	private File apkContentDir;

	private int smaliClassLabel = 0;

	private int id;

	/**
	 * Directory where the imported app is located
	 */
	private File apkDirectory;
	private Config config;
	private HashMap<String,ClassInterface> smaliClassMap = new HashMap<String, ClassInterface
			>();
	/**
	 * This map stores all calculated message Digests for this application.
	 */
	private final EnumMap<Digest, String> digestMap = new EnumMap<Digest, String>(Digest.class);
	/**
	 * Mapping of this apps codelines to apicalls if they match
	 */
	HashMap<CodeLineInterface, APICall> matchedCalls = null;
	List<CodeLineInterface> foundCalls = new ArrayList<CodeLineInterface>();//maybe change this to treeset and make it comparable
	
	/**
	 * returns the calls which could be mapped to the permissions based on android permission map
	 */
	public HashMap<CodeLineInterface, APICall> getMatchedCalls(){ 
		if(matchedCalls != null)
			return matchedCalls;
		else {
//			matchedCalls = new HashMap<CodeLineInterface, APICall>();
			matchCalls();
			return matchedCalls;
		}
	}
	
	public List<CodeLineInterface> getFoundCalls(){
		return foundCalls;
	}
	
	
	/**
	 * match the codelines to known apicalls
	 */
	public void matchCalls(){
		if(matchedCalls == null){
			matchedCalls = new HashMap<CodeLineInterface, APICall>();
		
		
		//better var names
		for(ClassInterface s : getAllSmaliClasss(false)){
			for(CodeLineInterface c: s.getAllCodeLines()){
				if(c.getInstruction().getType().equals(InstructionType.INVOKE)||c.getInstruction().getType().equals(InstructionType.INVOKE_STATIC)){
					foundCalls.add(c);					
				}
			}
		}

		for(CodeLineInterface c: foundCalls){
			String className = new String (c.getInstruction().getCalledClassAndMethodWithParameter()[0]).replaceAll("/", ".");
			for(APICall call: APICalls.getCalls()){
				if(call.getCall().contains(className+"."+new String (c.getInstruction().getCalledClassAndMethodWithParameter()[1]))){
	
					//params need to be converted for matching....
					//params
					String params = new String (c.getInstruction().getCalledClassAndMethodWithParameter()[2]);

					if(call.getCall().contains(className+"."+new String (c.getInstruction().getCalledClassAndMethodWithParameter()[1])+"("+params+")")){
						c.setPermission(new Permission(call.getCall()));//TODO: this needs to also be saved in the smali data itself instead of just in this copy
						matchedCalls.put(c, call);
						
					} 


				}
			}
		}
		}		
	}
	
	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public File getUnpackedDataDir() {
		return decompiledContentDir;
	}
	
	@Override
	public String getMessageDigest(Digest digestAlgorithm) {
		return digestMap.get(digestAlgorithm);
	}
	
	@Override
	public void setMessageDigest(Digest digestAlgorithm, String digest) {
		digestMap.put(digestAlgorithm, digest);
	}

	@Override
	public File getManifestFile() {
		return this.manifestFile;
	}

	@Override
	public ManifestInterface getManifest() {
		return this.manifest;
	}

	@Override
	public File getBytecodeDirectory() {
		return bytecodeDirectory;
	}

	@Override
	public File getApplicationDirectory() {
		return appDirectory;
	}

	@Override
	public File getApkFile() {
		return apkFile;
	}


	/**
	 * TODO: Add support for temporary apps which are unpacked into a temp
	 * directory and which are not inserted into the DB.
	 * 
	 * @param apk
	 * @param warnIfDuplicate
	 *            Show a info dialog if an app with the same hash is already in
	 *            the DB old param generateJava -> now set in
	 *            Config.GENERATE_JAVA If this is true java code is generated
	 *            out of the .class files
	 * @throws Exception
	 */
	public Application(File apk, boolean warnIfDuplicate) {
		// Set up some names, files, etc
		this();
		this.applicationName = apk.getName().substring(0,
				apk.getName().length() - 4);

		this.fileExtension = apk.getName().substring(
				apk.getName().length() - 3);
		
		this.apkFile = apk;
	}

	public Application() {
		this.config = Config.getInstance();
		this.changed=true;
	}

	private FileList allSmaliClasss = null;
	private FileList allClassFiles = null;

	@Override
	public Vector<File> getAllRawSmaliFiles(boolean includeFilesFromAdPackages) {
		if (allSmaliClasss == null)
			allSmaliClasss = new FileList(bytecodeDirectory,
					FileList.SMALI_FILES);
		return allSmaliClasss.getAllFoundFiles(includeFilesFromAdPackages);
	}

	@Override
	public Vector<File> getAllClassFiles(boolean includeFilesFromAdPackages) {
		if (allClassFiles == null)
			allClassFiles = new FileList(bytecodeDirectory,
					FileList.CLASS_FILES);
		return allClassFiles.getAllFoundFiles(includeFilesFromAdPackages);
	}


	private boolean changed;

	@Override
	public ClassInterface getSmaliClass(File file) {
		ClassInterface sf = smaliClassMap.get(file.getAbsolutePath());
		if (sf == null ){
			boolean inAdFramework=false;
			inAdFramework = config.getAdChecker().containsAnAd(file);
			try {
				sf = new SmaliClass(file, this, smaliClassLabel++);
				sf.setInAdFramework(inAdFramework);
				smaliClassMap.put(file.getAbsolutePath(), sf);
			} catch (IOException e) {
				LOGGER.error("Could not create SmaliClass object", e);
			} catch (DetectionLogicError e) {
				LOGGER.error("Could not create SmaliClass object", e);
			} catch (SmaliClassError e) {
				LOGGER.error("Could not create SmaliClass object", e);
			}
		}
		return sf;//smaliClassMap.get(file.getAbsolutePath());
	}

	@Override
	public LinkedList<ClassInterface> getAllSmaliClasss(
			boolean includeFilesFromAdPackages) {
		LinkedList<ClassInterface> sfList = new LinkedList<ClassInterface>();
		for (File f : getAllRawSmaliFiles(includeFilesFromAdPackages)) {
			ClassInterface sf = getSmaliClass(f);
			if (sf == null) {
				boolean inAdFramework=false;
				inAdFramework = config.getAdChecker().containsAnAd(f);
				try {
					sf = new SmaliClass(f, this, smaliClassLabel++);
					sf.setInAdFramework(inAdFramework);
					smaliClassMap.put(f.getAbsolutePath(), sf);
				} catch (IOException e) {
					LOGGER.error("Could not create SmaliClass object", e);
				} catch (DetectionLogicError e) {
					LOGGER.error("Could not create SmaliClass object", e);
				} catch (SmaliClassError e) {
					LOGGER.error("Could not create SmaliClass object", e);
				}
			}
			if (sf != null){sfList.addLast(sf);}
		}
		return sfList;
	}

	@Override
	public void setAllSmaliClasss(HashMap<String, ClassInterface> smaliClassMap) {
		this.smaliClassMap = smaliClassMap;
	}


	@Override
	public String getApplicationName() {
		return applicationName;
	}


	@Override
	public int getNumberOfCodelines(boolean includeFilesFromAdPackages) {
		int nr = 0;
		LinkedList<ClassInterface> files = getAllSmaliClasss(includeFilesFromAdPackages);
		for (ClassInterface f : files) {
			nr += f.getLinesOfCode();
		}
		return nr;
	}


	@Override
	public MethodInterface getMethodByClassAndName(String className,
			String methodName, byte[] parameterDeclaration,
			byte[] returnValue)
			throws ClassOrMethodNotFoundException {
		File f = new File(bytecodeDirectory, className + ".smali");
		if (!f.exists()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Lost track, class unknown: ");
			sb.append(className);
			sb.append("->");
			sb.append(methodName);
			sb.append("(");
			sb.append(new String(parameterDeclaration));
			sb.append(")");
			throw new ClassOrMethodNotFoundException(sb.toString());
		} else {
			ClassInterface sf = getSmaliClass(f);
			for (MethodInterface m : sf.getMethods()) {
				if (m.getName().equals(methodName)) {
					if (parameterDeclaration != null
						&& Arrays.equals(parameterDeclaration, m.getParameters())
						&& Arrays.equals(returnValue, m.getReturnValue())) {
						return m;
					}
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Lost track, method unknown: ");
		sb.append(className);
		sb.append("->");
		sb.append(methodName);
		sb.append("(");
		sb.append(new String(parameterDeclaration));
		sb.append(")");
		throw new ClassOrMethodNotFoundException(sb.toString());
	}

	@Override
	public String getFileExtension() {
		return fileExtension;
	}

	@Override
	public void setApplicationName(String name) {
		this.applicationName = name;

	}

	@Override
	public void setFileExtension(String extension) {
		this.fileExtension = extension;

	}

	@Override
	public void setManifest(ManifestInterface manifest) {
		this.manifest = manifest;
		
	}

	@Override
	public void setManifestFile(File manifestFile) {
		this.manifestFile=manifestFile;		
	}

	/**
	 * @return the apkDirectory
	 */
	@Override
	public File getApkDirectory() {
		return apkDirectory;
	}

	/**
	 * @param apkDirectory the apkDirectory to set
	 */
	@Override
	public void setApkDirectory(File apkDirectory) {
		this.apkDirectory = apkDirectory;
	}

	/**
	 * @return the decompiledContentDir
	 */
	@Override
	public File getDecompiledContentDir() {
		return decompiledContentDir;
	}

	/**
	 * @param decompiledContentDir the decompiledContentDir to set
	 */
	@Override
	public void setDecompiledContentDir(File decompiledContentDir) {
		this.decompiledContentDir = decompiledContentDir;
	}

	/**
	 * @param appDirectory the appDirectory to set
	 */
	@Override
	public void setApplicationDirectory(File appDirectory) {
		this.appDirectory = appDirectory;
	}

	/**
	 * @param bytecodeDirectory the bytecodeDirectory to set
	 */
	@Override
	public void setBytecodeDirectory(File bytecodeDirectory) {
		this.bytecodeDirectory = bytecodeDirectory;
	}

	@Override
	public void setApkContentDir(File apkContentDir) {
		this.apkContentDir=apkContentDir;
		
	}

	@Override
	public File getApkContentDir() {
		return this.apkContentDir;
	}

	/**
	 * @return the smaliClassLabel
	 */
	public int getSmaliClassLabel() {
		return smaliClassLabel;
	}

	/**
	 * @param smaliClassLabel the smaliClassLabel to set
	 */
	@Override
	public void setSmaliClassLabel(int smaliClassLabel) {
		this.smaliClassLabel = smaliClassLabel;
	}

	@Override
	public void setChanged(boolean changed) {
		this.changed = changed;
		
	}

	@Override
	public boolean isChanged() {
		return changed;
	}

	@Override
	public String toString() {
		return "Application [applicationName=" + applicationName + "]";
	}
	
	/**
	 * @param app
	 */
	public static boolean isAPKFile(File apk) {
			FileInputStream fis = null;
		try {
			if (apk.length() <= 2) {
				LOGGER.info("File too small. Aborting.");
				return false;
			}
			if (!apk.canRead()) {
				LOGGER.info("File not readable. Aborting.");
				return false;
			}
			fis = new FileInputStream(apk);
			byte[] fileHead = new byte[8];
			int read = fis.read(fileHead);
			if (read <= 2) {
				LOGGER.info("Could not read file: "+apk.getName()+". Aborting.");
				return false;
			}
			if (
				fileHead[0] != 'P' ||
				fileHead[1] != 'K'
			) {
				LOGGER.info("Magic bytes do not match! Aborting.");
				return false;
			}
		} catch (IOException e) {
			LOGGER.info("Could not check file, aborting. Message: "+e.getMessage());
			return false;
		}
		finally {
			if (fis != null) {
				try { fis.close(); } catch (Exception e) { /* ignore */ }
			}
		}
		return true;
	}

	@Override
	public ClassInterface getSmaliClass(ComponentInterface component) {
		String path = component.getName();
		if(component.getName().startsWith("."))
		{
			path = this.getManifest().getPackageName()+path;
		}
		path = path.replace('.', '/');
		path = this.getDecompiledContentDir() + File.separator + "smali" + File.separator + path + ".smali";
		return this.getSmaliClass(new File(path));
	}
}
