package de.rub.syssec.saaf.gui.actions;

import javax.swing.ProgressMonitor;

import de.rub.syssec.saaf.analysis.steps.ProgressListener;

final class MonitorBackedProgressListener implements
		ProgressListener {
	private final ProgressMonitor mon;

	MonitorBackedProgressListener(ProgressMonitor mon) {
		this.mon = mon;
	}

	@Override
	public void started() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProgress(String note) {
		mon.setNote(note);
		
	}

	@Override
	public void setProgress(int progress) {
		mon.setProgress(progress);			
	}

	@Override
	public void finished() {
		mon.setProgress(mon.getMaximum());
		
	}

	@Override
	public void canceled() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMaximum(int maximum) {
		mon.setMaximum(maximum);
		
	}
}