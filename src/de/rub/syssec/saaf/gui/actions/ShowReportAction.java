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

import de.rub.syssec.saaf.gui.MainWindow;
import de.rub.syssec.saaf.gui.ViewerStarter;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class ShowReportAction extends AbstractAction {

	private static final long serialVersionUID = -5587658993694202975L;
	private MainWindow mainWindow;
	private static final Logger LOGGER = Logger.getLogger(ShowReportAction.class);

	public ShowReportAction(String string, MainWindow mainWindow) {
		super(string);
		this.mainWindow = mainWindow;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		AnalysisInterface selectedAnalysis = mainWindow
				.getUserselectedAnalysisIfMultipleAreOpened();
		if (selectedAnalysis == null) {
			JOptionPane.showMessageDialog(mainWindow,
					"Please open an application first.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			if (selectedAnalysis.getReportFile() != null) {
				ViewerStarter viewer = new ViewerStarter(
						ConfigKeys.VIEWER_REPORTS);
				viewer.showFile(selectedAnalysis.getReportFile());
			} else {
				MainWindow.showInfoDialog(
						"No report file found. Did you run an analysis?",
						"Report not found");
			}

			/*
			 * TODO:change all the subclasses to use new heuristic here
			 * (probably just HeuristicResultsFrame and maybe one or two more)
			 */
		} catch (Exception e1) {
			LOGGER.error("Could not show report file.", e1);
			MainWindow.showErrorDialog(
					"An error occured, see the log fore more info.",
					"Error");
		}
	}

}
