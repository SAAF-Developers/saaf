/**
 * 
 */
package de.rub.syssec.saaf.analysis.steps.obfuscation;

/**
 * Just to keep the different types of entropy in one place.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class Entropy {
	
	/**
	 * A constructor just initializing the nameEntropy.
	 * @param ne
	 */
	public Entropy(double ne)
	{
		this.nameEntropy=ne;
	}
	
	public Entropy() {
		super();
	}

	/**
	 * The entropy calculated just from the name.
	 */
	public double nameEntropy;
	
	/**
	 * The entropy calculated from the concatenation of class- and method-names.
	 */
	public double CMEntropy;
	/**
	 * The entropy calculated from the concatenation of class-, method- and field-names.
	 */
	public double CMFEntropy;
	/**
	 * The entropy caculated as the average of the <b>separate entropies</b> of class-, method- and fieldnames.
	 */
	public double AverageEntropy;
	
}
