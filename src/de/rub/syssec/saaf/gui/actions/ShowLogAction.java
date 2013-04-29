/**
 * 
 */
package de.rub.syssec.saaf.gui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;

import javax.swing.AbstractAction;

import de.rub.syssec.saaf.gui.MainWindow;
import de.rub.syssec.saaf.gui.frame.LogFrame;

/**
 * Displays the logging window.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class ShowLogAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8950658355676412395L;
	private static LogFrame logframe;
	private String title;
	private MainWindow mainWindow;

	/**
	 * @param title
	 * @param mainWindow
	 */
	public ShowLogAction(String title, MainWindow mainWindow) {
		super(title);
		this.title = title;
		this.mainWindow = mainWindow;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (logframe == null || logframe.isClosed()) {
			if (logframe != null) {
				try {
					logframe.setClosed(false);
					logframe.setVisible(true);
				} catch (PropertyVetoException e) {
					e.printStackTrace();
				}
			} else {
				logframe = new LogFrame(); // make a new one
				MainWindow.getDesktopPane().add(logframe);
				logframe.setVisible(true); // show it
			}
		}
		if (logframe.isIcon()) {
			try {
				logframe.setIcon(false);
			} catch (PropertyVetoException e) {
				/* wtf? */
			}
		}
		logframe.show();
		logframe.toFront(); // move to front
	}

}
