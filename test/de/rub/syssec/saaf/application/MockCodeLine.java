package de.rub.syssec.saaf.application;

import de.rub.syssec.saaf.application.manifest.permissions.Permission;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.MethodInterface;
import de.rub.syssec.saaf.model.application.instruction.InstructionInterface;

/**
 * MockClass that extends Codeline to call its protected constructor.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class MockCodeLine implements CodeLineInterface {

	private MethodInterface method;
	private ClassInterface classFile;
	private Permission permission;
	private byte[] line;
	private int linNr;

	public MockCodeLine(byte[] line, int lineNr, ClassInterface sf) {
		this.line = line;
		this.linNr = lineNr;
		this.classFile=sf;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.application.CodeLine#getMethod()
	 */
	@Override
	public MethodInterface getMethod() {
		return this.method;
	}

	@Override
	public byte[] getLine() {
		return line;
	}

	@Override
	public int getLineNr() {
		return this.linNr;
	}

	@Override
	public String getNrAndLine() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCode() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean startsWith(byte[] pattern) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean startsWith(String pattern) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(byte[] pattern) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(String pattern) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public InstructionInterface getInstruction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClassInterface getSmaliClass() {
		return this.classFile;
	}

	@Override
	public void setMethod(MethodInterface method) {
		this.method=method;
		
	}

	@Override
	public void setPermission(Permission perm) {
		this.permission = perm;
		
	}

	@Override
	public Permission getPermission() {
		return permission;
	}
	
	

}
