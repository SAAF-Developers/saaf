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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.application.instructions.Instruction;
import de.rub.syssec.saaf.application.methods.Method;
import de.rub.syssec.saaf.misc.ByteUtils;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.DetectionLogicError;
import de.rub.syssec.saaf.model.application.FieldInterface;
import de.rub.syssec.saaf.model.application.InstructionType;
import de.rub.syssec.saaf.model.application.MethodInterface;
import de.rub.syssec.saaf.model.application.PackageInterface;
import de.rub.syssec.saaf.model.application.SmaliClassError;

/**
 * This class represents a parsed SMALI file on the disk with all methods, fields and so on.
 * This class always represents one SMALI file with its parsed bytecode.
 */
public class SmaliClass implements ClassInterface {
	
	private final File smaliFile;
	private final ApplicationInterface app;
	private static final boolean DEBUG=Boolean.parseBoolean(System.getProperty("debug.slicing","false"));
	private PackageInterface javaPackage;

	private LinkedList<CodeLineInterface> codeLineList = new LinkedList<CodeLineInterface>();	
	
	private LinkedList<MethodInterface> methodList = new LinkedList<MethodInterface>();
	private LinkedList<MethodInterface> emptyMethodList = new LinkedList<MethodInterface>();
	
	private LinkedList<FieldInterface> fieldList = new LinkedList<FieldInterface>();
	
	private String fuzzyHash = null;	//TODO: Implement
	private int id = -1;		//ID from the table in db
	
	private static final byte[] IMPLEMENTS = ".implements ".getBytes();
	private static final byte[] SUPER = ".super ".getBytes();
	private static final byte[] CLASS = ".class ".getBytes();
	private static final byte[] SOURCE = ".source ".getBytes();
	
	private static final Logger LOGGER = Logger.getLogger(SmaliClass.class); 
	
	private String superClass = null;
	private String sourceFile = null;

	private final HashSet<String> implementedInterfaces = new HashSet<String>();
	
	private static final int MAXIMAL_SMALI_FILE_SIZE = 1024 * 1024 * 100; // 100mb
	private int size = 0; 
	
	private final int label;
	
	private boolean inAdFramework=false;
	private boolean changed;
	private boolean obfuscated;
	private double entropy;
	
	/**
	 * The parsed SMALI file.
	 * 
	 * @param smaliFile the corresponding file
	 * @param app the app
	 * @param label the unique label of the SMALI file within an application
	 * @throws IOException is some IO error occurred
	 * @throws DetectionLogicError if the BBs could not be correctly labeled
	 * @throws SmaliClassError 
	 */
	public SmaliClass(File smaliFile, ApplicationInterface app, int label) throws IOException, DetectionLogicError, SmaliClassError {
		this.smaliFile = smaliFile;
		this.app = app;
		this.label = label;
		this.javaPackage = new JavaPackage(app);
		if (DEBUG) LOGGER.debug("Parsing SMALI code for file "+smaliFile.getName());
		parse();
		this.changed = true;
	}



	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#getFile()
	 */
	@Override
	public File getFile() {
		return smaliFile;
	}	
	
	
	/**
	 * Parse the codelines.
	 * @throws IOException 
	 * @throws DetectionLogicError if the BBs could not be correctly labeled
	 * @throws SmaliClassError 
	 */
	private void parse() throws IOException, DetectionLogicError, SmaliClassError {
	
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		try {
			int lineNr = 1;
			fis = new FileInputStream(smaliFile);
			bis = new BufferedInputStream(fis);
			byte[] line;
			while ((line = ByteUtils.parseLine(bis, 256000)) != null) { // 250k
				codeLineList.addLast(new CodeLine(line, lineNr++, this));
				size += line.length;
				if (size > MAXIMAL_SMALI_FILE_SIZE) throw new IOException("Maximum SMALI file size of "+MAXIMAL_SMALI_FILE_SIZE+" bytes exceeded!");
			}
		}
//		catch (Exception e) {
//			LOGGER.logError(SmaliClass.class, "Could not read file: "+smaliFile.getAbsolutePath()+": "+e.getMessage());
//			e.printStackTrace();
//		}
		finally {
			try { if (bis != null) bis.close(); } catch (Exception e) { /*ignore*/ }
			try { if (fis != null) fis.close(); } catch (Exception e) { /*ignore*/ }		
		}
		
		boolean insideMethod = false;

		LinkedList<CodeLineInterface> blockedCodeLines = new LinkedList<CodeLineInterface>();
		
		/**
		 * All codelines not belonging to a method.
		 * Fields, enums, Annotations etc
		 */
		LinkedList<CodeLineInterface> otherCL = new LinkedList<CodeLineInterface>();
		
		final byte[] START_METHOD = ".method ".getBytes();
		final byte[] END_METHOD = ".end method".getBytes();
		

		/*
		 * TODO:
		 * :array_0
		 * .array-data 0x1
		 *  0x78t <- is wrongly parsed b/c it is assumed to be an instruction, should also not be put into the BB
		 *  0x79t <- see above
		 *  0x7at <- see above
		 * .end array-data		
		 */
		int methodLabel = 0;
		for (CodeLineInterface cl : codeLineList) {
			if (cl.isEmpty()) continue; // skip empty lines
			if (insideMethod) {
				if (ByteUtils.startsWith(cl.getLine(), END_METHOD)) {
					//	append, store method
					blockedCodeLines.addLast(cl);
					Method m = new Method(blockedCodeLines, this, methodLabel++); // save
					if (DEBUG) LOGGER.debug("> Parsing instructions/opcodes for method '"+m.getName()+"'");
					for (CodeLineInterface mcl : blockedCodeLines) {
						mcl.setMethod(m); // set a reference to the method for later and faster access
						mcl.getInstruction().parseOpCode();
					}
					//TODO: do better, added generateBBs Method to  Method.java, which will now generate the BBs instead of directly
					//generating the blocks at construction time, this should be the only place where this call is currently necessary
					m.generateBBs();
					
					/*
					 *  This is a "fix" for empty methods. Otherwise, this happens:
					 *  Method.getFirstBasicBlock w/ this content
					 *  	.method public abstract PpNzwq9T()Ljava/util/List;
					 *  	.end method
					 *  produces a java.util.NoSuchElementException. 
					 */
					if (!m.getBasicBlocks().isEmpty()) methodList.addLast(m);
					else emptyMethodList.addLast(m);
					
					blockedCodeLines = new LinkedList<CodeLineInterface>(); // reset
					insideMethod = false;
				}
				else { // do not append .line to the method
					if(!ByteUtils.startsWith(cl.getLine(), ".line".getBytes()))
						blockedCodeLines.addLast(cl);
				}
			}
			else {
				if (ByteUtils.startsWith(cl.getLine(), START_METHOD)) {
					// new block and append
					blockedCodeLines.addLast(cl); // either still empty or reseted in END
					insideMethod = true;
				}
				else {
//					append to otherCL
					otherCL.addLast(cl);
				}
			}

		}
		
		fieldList = Field.parseAllFields(otherCL);
		
		// parse implements, class and super lines
		for (CodeLineInterface cl : otherCL) {
			if (cl.startsWith(SUPER)) { // .super Landroid/app/Activity;
				byte[] tmp = Instruction.split(cl.getLine()).getLast();
				superClass = new String(ByteUtils.subbytes(tmp, 1, tmp.length-1)).replace("/", ".");
			}
			else if (cl.startsWith(IMPLEMENTS)) { // .implements Ljava/io/Serializable;
				byte[] tmp = Instruction.split(cl.getLine()).getLast();
				implementedInterfaces.add(new String(ByteUtils.subbytes(tmp, 1, tmp.length-1)).replace("/", "."));
			}
			else if (cl.startsWith(CLASS)) { // .class public Ltest/android/AndroidTestActivity;
				byte[] tmp = Instruction.split(cl.getLine()).getLast();
				List<String> packageNames = new ArrayList<String>();
				String x[] = new String(ByteUtils.subbytes(tmp, 1, tmp.length-1)).split("/");
				for (int i = 0; i<x.length-1; i++) { // do not include the class name
					packageNames.add(x[i]);
				}
				this.javaPackage.setName(packageNames);
			} else if (cl.startsWith(SOURCE)) {// .source "MagicSMSActivity.java"
				byte[] tmp = Instruction.split(cl.getLine()).getLast();
				sourceFile = new String(ByteUtils.subbytes(tmp, 1, tmp.length-1));
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#getMethods()
	 */
	@Override
	public LinkedList<MethodInterface> getMethods() {
		return methodList;
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#getEmptyMethods()
	 */
	@Override
	public LinkedList<MethodInterface> getEmptyMethods() {
		return emptyMethodList;
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#getAllCodeLines()
	 */
	@Override
	public LinkedList<CodeLineInterface> getAllCodeLines() {
		return codeLineList;
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#getAllCodeLine(de.rub.syssec.saaf.application.instructions.InstructionMap.InstructionType)
	 */
	@Override
	public LinkedList<CodeLineInterface> getAllCodeLine(InstructionType ... types) {
		LinkedList<CodeLineInterface> ret = new LinkedList<CodeLineInterface>();
		for (CodeLineInterface cl : getAllCodeLines()) {
			for (InstructionType type : types) {
				if (cl.getInstruction().getType() == type) ret.addLast(cl);
			}
		}
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#getAllFields()
	 */
	@Override
	public Collection<FieldInterface> getAllFields() {
		return Collections.unmodifiableCollection(fieldList);
	}
	
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#getLinesOfCode()
	 */
	@Override
	public int getLinesOfCode() {
		return codeLineList.size();
	}
	
	
//	/* (non-Javadoc)
//	 * @see de.rub.syssec.saaf.application.ClassInterface#getSha1()
//	 */
//	@Override
//	public String getSha1() throws NoSuchAlgorithmException, IOException {
//		if (sha1Hash == null) sha1Hash = Hash.calculateHash(Digest.SHA1, smaliFile);
//		return sha1Hash;
//	}
	
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#searchPattern(byte[], de.rub.syssec.saaf.application.SmaliClass.SearchType)
	 */
	@Override
	public LinkedList<CodeLineInterface> searchPattern(byte[] pattern, SearchType searchType) {
		LinkedList<CodeLineInterface> results = new LinkedList<CodeLineInterface>();
		for (CodeLineInterface cl : codeLineList) {
			switch (searchType) {
			case INSTRUCTIONS_ONLY:
				if (!cl.isCode()) continue;
				break;
			case NON_INSTRUCTIONS_ONLY:
				if (cl.isCode()) continue;
				break;
			case INSTRUCTIONS_AND_NON_INSTRUCTIONS:
				if (cl.isEmpty()) continue;
				break;
			default:
				break;
			}
			if (cl.contains(pattern)) results.addLast(cl);
		}
		return results;
	}
		
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#searchPattern(byte[], de.rub.syssec.saaf.application.instructions.InstructionMap.InstructionType)
	 */
	@Override
	public LinkedList<CodeLineInterface> searchPattern(byte[] pattern, InstructionType ... types) {
		LinkedList<CodeLineInterface> results = new LinkedList<CodeLineInterface>();
		for (CodeLineInterface cl : codeLineList) {
			for (InstructionType type : types) {
				if (type == cl.getInstruction().getType()) {
					results.add(cl);
				}
			}
		}
		return results;
	}
	
//################# getter and setter ##################################
	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#getPackageId()
	 */
	@Override
	public int getPackageId() {
		return this.javaPackage.getId();
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#setPackageId(int)
	 */
	@Override
	public void setPackageId(int packageId) {
		this.javaPackage.setId(packageId);
	}


	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#getImplementedInterfaces()
	 */
	@Override
	public Collection<String> getImplementedInterfaces() {
		return Collections.unmodifiableCollection(implementedInterfaces);
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#getSuperClass()
	 */
	@Override
	public String getSuperClass() {
		return superClass;
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#getPackageName(boolean)
	 */
	@Override
	public String getPackageName(boolean useDots) {
		return this.javaPackage.getName(useDots);
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#getClassName()
	 */
	@Override
	public String getClassName() {
		return smaliFile.getName().replace(".smali", "");
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#getFullClassName(boolean)
	 */
	@Override
	public String getFullClassName(boolean useDots) {
		String separator;
		if (useDots) separator = ".";
		else separator = "/";
		return getPackageName(useDots) + separator + getClassName();
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#setHash_ssDeep(java.lang.String)
	 */
	@Override
	public void setSsdeepHash(String hash) {
		fuzzyHash=hash;
		setChanged(true);
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#getHash_ssDeep()
	 */
	@Override
	public String getSsdeepHash() {
		return fuzzyHash;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#getSourceFile()
	 */
	@Override
	public String getSourceFile() {
		return sourceFile;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#getApplication()
	 */
	@Override
	public ApplicationInterface getApplication() {
		return app;
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#getSize()
	 */
	@Override
	public int getSize() {
		return size;
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#getUniqueId()
	 */
	@Override
	public String getUniqueId() {
		return String.valueOf(label);
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#isInAdFrameworkPackage()
	 */
	@Override
	public boolean isInAdFrameworkPackage() {
//		LOGGER.debug("Stub: Ad Check not yet implemented!");
		return inAdFramework;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.ClassInterface#setInAdFramework(boolean)
	 */
	@Override
	public void setInAdFramework(boolean hasAd) {
		inAdFramework = hasAd;
		setChanged(true);
	}

	@Override
	public PackageInterface getPackage() {
		return this.javaPackage;
	}

	@Override
	public void setPackage(PackageInterface javaPackage) {
		this.javaPackage=javaPackage;
		setChanged(true);		
	}


	@Override
	public void setChanged(boolean changed) {
		this.changed = changed;
	}


	@Override
	public boolean isChanged() {
		return this.changed;
	}



	@Override
	public String getRelativeFile() {
		return this.smaliFile.getAbsolutePath().replaceFirst(app.getDecompiledContentDir().getAbsolutePath()+File.separator+"smali"+File.separator, "");
	}
	
	public String toString(){
		return getClassName();
	}



	@Override
	public void setObfuscated(boolean b) {
		this.obfuscated = b;
	}



	@Override
	public boolean isObfuscated() {
		return this.obfuscated;
	}
	
	@Override
	public void setEntropy(double entropy) {
		this.entropy=entropy;
	}


	@Override
	public double getEntropy() {
		return this.entropy;
	}
}
