/**
 * 
 */
package de.rub.syssec.saaf.model.application.manifest;

/**
 * Indicates that an manifest has two (or more) Activities for "android.intent.action.MAIN" defined.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class DuplicateEntryPointException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -568681570662231823L;

	public DuplicateEntryPointException(String message)
	{
		super(message);
	}
	
	public DuplicateEntryPointException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DuplicateEntryPointException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public DuplicateEntryPointException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
