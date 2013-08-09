package de.rub.syssec.saaf.model.application;

/**
 * Defines methods to set and query if an object was obfuscated.
 * 
 * @author tbender
 *
 */
public interface Obfuscatable {

	/**
	 * Mark a class as (not) obfuscated.
	 * 
	 * At the time of writing "obfuscated" basically
	 * means mangling of names for classes, methods and members
	 * @param b
	 */
	public abstract void setObfuscated(boolean b);

	/**
	 * Indicates that a class has been obfuscated.
	 * 
	 * At the time of writing "obfuscated" basically
	 * means mangling of names for classes, methods and members
	 * 
	 * @return true - if class is obfuscated
	 */
	public abstract boolean isObfuscated();
	
	/**
	 * Set the entropy of the obfuscated object.
	 * 
	 * @param entropy
	 */
	public abstract void setEntropy(double entropy);
	/**
	 * get the entropy of the obfuscated object.
	 * 
	 * @return
	 */
	public abstract double getEntropy();
	

}