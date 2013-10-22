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

import de.rub.syssec.saaf.gui.MainWindow;
import de.rub.syssec.saaf.gui.OpenAppsMgr;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;


public class FoundAPICallsAction extends AbstractAction {


	private static final long serialVersionUID = -2894903457682760390L;
	private MainWindow mainWindow;
	private OpenAppsMgr openAppsMgr;

	public FoundAPICallsAction(String title, OpenAppsMgr openAppsMgr,
			MainWindow mainWindow) {
		super(title);
		this.mainWindow = mainWindow;
		this.openAppsMgr = openAppsMgr;
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
		} else {
			try {
				openAppsMgr.openFrame(selectedAnalysis,
						de.rub.syssec.saaf.gui.OpenAnalysis.AppFrame.CALLS);
			} catch (Exception e1) {
				e1.printStackTrace();
				MainWindow.showErrorDialog(
						"Problem showing apicalls for application "
								+ selectedAnalysis.getApp().getApkFile()
										.getAbsolutePath() + "\n"
								+ e1.getMessage(), "Error");
			}
		}
	}

}
