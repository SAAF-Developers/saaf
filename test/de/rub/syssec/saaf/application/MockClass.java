package de.rub.syssec.saaf.application;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import de.rub.syssec.saaf.analysis.steps.obfuscation.Entropy;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.FieldInterface;
import de.rub.syssec.saaf.model.application.MethodInterface;
import de.rub.syssec.saaf.model.application.PackageInterface;
import de.rub.syssec.saaf.model.application.instruction.InstructionType;

public class MockClass implements ClassInterface {
	
	PackageInterface pkg;
	private int id;
	private String name;
	private boolean changed;

	public MockClass(String string, PackageInterface dummyPackage) {
		this.name = string;
		this.pkg = dummyPackage;
		this.changed=true;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public File getFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<MethodInterface> getMethods() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<MethodInterface> getEmptyMethods() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<CodeLineInterface> getAllCodeLines() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<CodeLineInterface> getAllCodeLine(InstructionType... types) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<FieldInterface> getAllFields() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLinesOfCode() {
		return 20;
	}


	@Override
	public LinkedList<CodeLineInterface> searchPattern(byte[] pattern,
			SearchType searchType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<CodeLineInterface> searchPattern(byte[] pattern,
			InstructionType... types) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPackageId() {
		return pkg.getId();
	}

	@Override
	public void setPackageId(int packageId) {
		this.pkg.setId(packageId);

	}

	@Override
	public Collection<String> getImplementedInterfaces() {
		ArrayList<String> interfaces = new ArrayList<String>();
		interfaces.add("Screwage");
		interfaces.add("Insultable");
		return interfaces;
	}

	@Override
	public String getSuperClass() {
		return "/bytecode/SuiConFo_3527961e3fb1134e1d3221c000879a90ff1022b6/bytecode/smali/com/magicsms/own/YourMama.smali";
	}

	@Override
	public String getPackageName(boolean useDots) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getClassName() {
		return this.name;
	}

	@Override
	public String getFullClassName(boolean useDots) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSsdeepHash(String hash) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getSsdeepHash() {
		return "a7f9b77c16a3aa80daa4e378659226f628326a95";
	}

	@Override
	public String getSourceFile() {
		return "/bytecode/SuiConFo_3527961e3fb1134e1d3221c000879a90ff1022b6/bytecode/smali/com/magicsms/own/Fuckyou.smali";
	}

	@Override
	public ApplicationInterface getApplication() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSize() {
		return 200;
	}

	@Override
	public String getUniqueId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isInAdFrameworkPackage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setInAdFramework(boolean hasAd) {
		// TODO Auto-generated method stub

	}

	@Override
	public PackageInterface getPackage() {
		return this.pkg;
	}

	@Override
	public void setPackage(PackageInterface javaPackage) {
		this.pkg=javaPackage;
		
	}

	@Override
	public void setChanged(boolean changed) {
		this.changed=changed;
	}

	@Override
	public boolean isChanged() {
		return this.changed;
	}

	@Override
	public String getRelativeFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setObfuscated(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isObfuscated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setEntropy(Entropy entropy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Entropy getEntropy() {
		// TODO Auto-generated method stub
		return null;
	}

}
