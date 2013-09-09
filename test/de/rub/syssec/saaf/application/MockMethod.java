package de.rub.syssec.saaf.application;

import java.util.LinkedList;

import de.rub.syssec.saaf.analysis.steps.obfuscation.Entropy;
import de.rub.syssec.saaf.model.application.BasicBlockInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.DetectionLogicError;
import de.rub.syssec.saaf.model.application.FieldInterface;
import de.rub.syssec.saaf.model.application.MethodInterface;

public class MockMethod implements MethodInterface {
	
	int id;
	private String name;
	private ClassInterface classFile;
	private LinkedList<CodeLineInterface> codelines;
	private boolean changed;
	private String paramstring="";
	private String returnValue;
	private boolean probablyPatched = false;

	public MockMethod() {
	}
	

	/**
	 * @param name
	 * @param classFile
	 * @param hash
	 * @param codelines
	 */
	public MockMethod(String name, ClassInterface classFile,
			LinkedList<CodeLineInterface> codelines) {
		super();
		this.name = name;
		this.classFile = classFile;
		this.codelines = codelines;
		this.changed=true;
	}


	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setEmpty(boolean isEmpty) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateBBs() throws DetectionLogicError {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] getParameters() {
		return new byte[1];
	}

	@Override
	public BasicBlockInterface getFirstBasicBlock() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<BasicBlockInterface> getBasicBlocks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBasicBlocks(LinkedList<BasicBlockInterface> blocks) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public LinkedList<FieldInterface> getLocalFields() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLocalFields(LinkedList<FieldInterface> localFields) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean contains(byte[] instruction) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float arithOps() {
		return 0.01f;
	}

	@Override
	public LinkedList<CodeLineInterface> getCodeLines() {
		return this.codelines;
	}

	@Override
	public void setCodeLines(LinkedList<CodeLineInterface> lines) {
		this.codelines=lines;
		
	}

	@Override
	public ClassInterface getSmaliClass() {
		return this.classFile;
	}

	@Override
	public void setSmaliClass(ClassInterface smaliClass) {
		this.classFile = smaliClass;
	}

	@Override
	public boolean isStatic() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void isStatic(boolean isStatic) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[][] getCmp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getParameterString() {
		return this.paramstring; 
	}

	@Override
	public void setParameterString(String params) {
		this.paramstring=params;
	}

	@Override
	public int getLabel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setLabel(int label) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getUniqueLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUniqueLabel(String ulabel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getReadableJavaName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setReadableJavaName(String javaName) {
		// TODO Auto-generated method stub
		
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
	public void setChanged(boolean changed) {
		this.changed=changed;
		
	}


	@Override
	public boolean isChanged() {
		return this.changed;
	}


	@Override
	public byte[] getReturnValue() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getReturnValueString() {
		return this.returnValue; 
	}
	
	public void setReturnValueString(String bla)
	{
		this.returnValue=bla;
	}
	
	@Override
	public void setHasUnlinkedBlocks(boolean patched) {
		probablyPatched = patched;
	}
	
	@Override
	public boolean hasUnlinkedBBs() {
		return probablyPatched;
	}


	@Override
	public boolean isProbablyPatched() {
		return hasUnlinkedBBs();
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
