/**
 * 
 */
package de.rub.syssec.saaf.analysis.steps.slicing;

import de.rub.syssec.saaf.model.analysis.BTPatternInterface;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class MockBTPattern implements BTPatternInterface {

	private boolean changed;

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setId(int id) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getQualifiedClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMethodName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getArgumentsTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getParameterOfInterest() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setQualifiedClassName(String qualifiedClassName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMethodName(String methodName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDescription(String description) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setParameterSpecification(String parameterSpecification) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setParameterOfInterest(int parameterOfInterest) {
		// TODO Auto-generated method stub

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
	public boolean isActive() {
		return true;
	}

	@Override
	public void setActive(boolean active) {
		// TODO Auto-generated method stub

	}

}
