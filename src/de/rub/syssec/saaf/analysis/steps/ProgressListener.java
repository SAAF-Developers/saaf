/**
 * 
 */
package de.rub.syssec.saaf.analysis.steps;

/**
 * Defines methods that can be called by steps to inform about their progress.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 *
 */
public interface ProgressListener {
	
	/**
	 * Informs that the current progress it at a specific value.
	 * 
	 * Note that this is an absolute value and not meant to be a delta.
	 * 
	 * @param progress
	 */
	public void setProgress(int progress);
	
	/**
	 * Allows the caller to provide a description of the progress.
	 * 
	 * @param note
	 */
	public void setProgress(String note);
	
	/**
	 * Indicates the step has started.
	 */
	public void started();
	/**
	 * Indicates the step has finished.
	 */
	public void finished();
	/**
	 * Indicates the step was canceled before finishing.
	 * 
	 */
	public void canceled();

}
