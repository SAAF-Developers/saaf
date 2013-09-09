/**
 * 
 */
package de.rub.syssec.saaf.analysis.steps;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A helper class that handles updates to all ProgressListeners.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class ProgressHandler implements ProgressObservable {

	private CopyOnWriteArrayList<ProgressListener> listeners;

	
	public ProgressHandler() {
		super();
		this.listeners = new CopyOnWriteArrayList<ProgressListener>();
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.analysis.steps.ProgressObservable#addProgressListener(de.rub.syssec.saaf.analysis.steps.ProgressListener)
	 */
	@Override
	public void addProgressListener(ProgressListener p) {
		this.listeners.add(p);
	}

	public void notifyCanceled()
	{
		for( ProgressListener listener : listeners)
		{
			listener.canceled();
		}
	}

	public void notifyFinsihed() {
		for( ProgressListener listener : listeners)
		{
			listener.finished();
		}		
	}

	public void notifyMax(int maximum)
	{
		for( ProgressListener listener : listeners)
		{
			listener.setMaximum(maximum);
		}
	}

	public void notifyProgress(int progress)
	{
		for( ProgressListener listener : listeners)
		{
			listener.setProgress(progress);
		}
	}

	public void notifyProgress(String message) {
		for( ProgressListener listener : listeners)
		{
			listener.setProgress(message);
		}
	}

	public void notifyStarted() {
		for( ProgressListener listener : listeners)
		{
			listener.started();
		}
	}

}
