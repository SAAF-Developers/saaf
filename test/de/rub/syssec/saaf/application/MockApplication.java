/**
 * 
 */
package de.rub.syssec.saaf.application;

import java.io.File;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.ClassOrMethodNotFoundException;
import de.rub.syssec.saaf.model.application.Digest;
import de.rub.syssec.saaf.model.application.MethodInterface;
import de.rub.syssec.saaf.model.application.manifest.ComponentInterface;
import de.rub.syssec.saaf.model.application.manifest.ManifestInterface;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class MockApplication implements ApplicationInterface {

	private int id;
	
	private final EnumMap<Digest, String> digestMap = new EnumMap<Digest, String>(Digest.class);
	
	private String name = "SuiConFo.apk";
	private String extension = ".apk";
	private boolean changed = true;

	private ManifestInterface manifest;
	
	public MockApplication() {
		digestMap.put(Digest.MD5, "0cbc6611f5540bd0809a388dc95a615b");
		digestMap.put(Digest.SHA1, "640ab2bae07bedc4c163f679a746f7ab7fb5d1fa");
		digestMap.put(Digest.SHA256, "532eaabd9574880dbf76b9b8cc00832c20a6ec113d682299550d7a6e0f345e25");
		digestMap.put(Digest.FuzzyHash, "3527961e3fb1134e1d3221c000879a90ff1022b6");
	}
	

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.db.Entity#getId()
	 */
	@Override
	public int getId() {
		return this.id;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.db.Entity#setId(int)
	 */
	@Override
	public void setId(int id) {
		this.id=id;

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.application.ApplicationInterface#getUnpackedDataDir()
	 */
	@Override
	public File getUnpackedDataDir() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.application.ApplicationInterface#getManifestFile()
	 */
	@Override
	public File getManifestFile() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.application.ApplicationInterface#getManifest()
	 */
	@Override
	public ManifestInterface getManifest() {
		// TODO Auto-generated method stub
		return this.manifest;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.application.ApplicationInterface#getBytecodeDirectory()
	 */
	@Override
	public File getBytecodeDirectory() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.application.ApplicationInterface#getApplicationDirectory()
	 */
	@Override
	public File getApplicationDirectory() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.application.ApplicationInterface#getApkFile()
	 */
	@Override
	public File getApkFile() {
		return new File(name);
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.application.ApplicationInterface#getAllRawSmaliFiles(boolean)
	 */
	@Override
	public Vector<File> getAllRawSmaliFiles(boolean includeFilesFromAdPackages) {
		return new Vector<File>();
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.application.ApplicationInterface#getAllClassFiles(boolean)
	 */
	@Override
	public Vector<File> getAllClassFiles(boolean includeFilesFromAdPackages) {
		return new Vector<File>();
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.application.ApplicationInterface#getSmaliFile(java.io.File)
	 */
	@Override
	public ClassInterface getSmaliClass(File file) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.application.ApplicationInterface#getApplicationName()
	 */
	@Override
	public String getApplicationName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.application.ApplicationInterface#getMethodByClassAndName(java.lang.String, java.lang.String, byte[])
	 */
	@Override
	public MethodInterface getMethodByClassAndName(String className,
			String methodName, byte[] parameterDeclaration, byte[] returnValue)
			throws ClassOrMethodNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.application.ApplicationInterface#getFileExtension()
	 */
	@Override
	public String getFileExtension() {
		return extension;
	}

	@Override
	public void setApplicationName(String name) {
		this.name=name;
		
	}

	@Override
	public void setFileExtension(String extension) {
		this.extension=extension;		
	}

	@Override
	public int getNumberOfCodelines(boolean includeFilesFromAdPackages) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setBytecodeDirectory(File bytecodeDirectory) {
	}

	public void setApplicationDirectory(File appDirectory) {
	}

	public File getAppDirectory() {
		return null;
	}

	public void setDecompiledContentDir(File decompiledContentDir) {
	}

	public File getDecompiledContentDir() {
		return null;
	}

	public void setApkDirectory(File apkDirectory) {
	}

	public File getApkDirectory() {
		return null;
	}

	public void setApkContentDir(File apkContentDir) {
	}

	public File getApkContentDir() {
		return null;
	}

	public void setSmaliFileLabel(int smaliFileLabel) {
	}

	@Override
	public void setManifest(ManifestInterface manifest) {
		this.manifest=manifest;
	}

	@Override
	public void setManifestFile(File manifestFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getSmaliClassLabel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setChanged(boolean changed) {
		this.changed = changed;
		
	}

	@Override
	public boolean isChanged() {
		// TODO Auto-generated method stub
		return this.changed;
	}

	@Override
	public LinkedList<ClassInterface> getAllSmaliClasss(
			boolean includeFilesFromAdPackages) {
		return new LinkedList<ClassInterface>();
	}

	@Override
	public void setAllSmaliClasss(HashMap<String, ClassInterface> smaliClassMap) {		
	}

	@Override
	public void setSmaliClassLabel(int smaliClassLabel) {
	}

	@Override
	public void setMessageDigest(Digest digestAlgorithm, String digest) {
		digestMap.put(digestAlgorithm, digest);
		
	}

	@Override
	public String getMessageDigest(Digest digestAlgorithm) {
		return digestMap.get(digestAlgorithm);
	}


	@Override
	public ClassInterface getSmaliClass(ComponentInterface component) {
		// TODO Auto-generated method stub
		return null;
	}
}
