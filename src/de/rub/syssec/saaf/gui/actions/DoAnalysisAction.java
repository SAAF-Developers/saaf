/* SAAF: A static analyzer for APK files.
 * Copyright (C) 2013  syssec.rub.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.rub.syssec.saaf.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.analysis.Analysis;
import de.rub.syssec.saaf.gui.MainWindow;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class DoAnalysisAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1410257192594360380L;
	private MainWindow mainWindow;
	private static final Logger LOGGER = Logger.getLogger(DoAnalysisAction.class);
	
	public DoAnalysisAction(String title, MainWindow mainWindow) {
		super(title);
		this.mainWindow=mainWindow;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Analysis selectedAnalysis = mainWindow.getUserselectedAnalysisIfMultipleAreOpened();
		if (selectedAnalysis == null) {
			JOptionPane.showMessageDialog(mainWindow,
					"Please open an application first.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			selectedAnalysis.doAnalysis();
			selectedAnalysis.doGenerateReport();
		} catch (Exception e1) {
			LOGGER.error("An error occured while running the analysis.", e1);
			MainWindow.showErrorDialog("Analysis failed, see the log for more info", "Error");
		}
	}

}
