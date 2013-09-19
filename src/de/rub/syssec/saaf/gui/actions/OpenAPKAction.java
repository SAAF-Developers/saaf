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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ProgressMonitor;

import de.rub.syssec.saaf.Main;
import de.rub.syssec.saaf.analysis.Analysis;
import de.rub.syssec.saaf.analysis.steps.ProgressListener;
import de.rub.syssec.saaf.analysis.steps.SetupFileSystemStep;
import de.rub.syssec.saaf.analysis.steps.SetupLoggingStep;
import de.rub.syssec.saaf.analysis.steps.Step;
import de.rub.syssec.saaf.analysis.steps.extract.ExtractApkStep;
import de.rub.syssec.saaf.analysis.steps.hash.Hash;
import de.rub.syssec.saaf.application.Application;
import de.rub.syssec.saaf.gui.MainWindow;
import de.rub.syssec.saaf.gui.OpenAppsMgr;
import de.rub.syssec.saaf.gui.OpenFileDialog;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;

/**
 * Open an APK file and show a window.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class OpenAPKAction extends AbstractAction {

	private static final long serialVersionUID = -4238255395636201964L;
	private MainWindow mainWindow;
	private OpenAppsMgr openAppsMgr;
	private boolean prompt;
	private String title;

	/**
	 * @param openAppsMgr
	 * @param mainWindow
	 * @param b
	 */
	public OpenAPKAction(String title, OpenAppsMgr openAppsMgr,
			MainWindow mainWindow, boolean prompt) {
		super(title);
		this.title = title;
		this.mainWindow = mainWindow;
		this.openAppsMgr = openAppsMgr;
		this.prompt = prompt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		final ActionEvent event = arg0;
		final File apk;
		if (this.prompt) {
			apk = OpenFileDialog.createOpenFileDialog(mainWindow, "apk");
		} else {
			apk = new File(this.title);
		}

		if (apk != null) {
			final ProgressMonitor mon =  new ProgressMonitor(mainWindow, "Opening ...", "Opening "+apk.getName(), 0, 10);
			mon.setMillisToDecideToPopup(0);
			mon.setMillisToPopup(0);
			final ProgressListener listener = new MonitorBackedProgressListener(mon);
			
			Thread doit = new Thread() {

				@Override
				public void run() {
					mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					mainWindow.setTitle(Main.title + ": " + apk.getName());

					new ShowLogAction("foo", mainWindow).actionPerformed(event);
					try {
						Application app = new Application(apk, true);
						Config config = Config.getInstance();
						AnalysisInterface analysis = new Analysis(app);
						analysis.addProgressListener(listener);
						analysis.doPreprocessing();
						
						openAppsMgr.addAnalysis(analysis);
						// abort an app w/ the same hash is already opened
						if (!openAppsMgr.addNewAnalysis(analysis)) {
							MainWindow
									.showInfoDialog(
											"This application is already opened.\nHash="
													+ app.getMessageDigest(Hash.DEFAULT_DIGEST),
											"Open Application");
						} else {
							mainWindow.roaList.addOpenedApk(apk
									.getAbsolutePath());
							// show the filetree and the manifest as an default
							// operation
							openAppsMgr
									.openFrame(
											analysis,
											de.rub.syssec.saaf.gui.OpenAnalysis.AppFrame.FILETREE);
						}
					} catch (Exception e1) {
						MainWindow.showInfoDialog(
								"Could not create app object for file "
										+ apk.getAbsolutePath() + ".\n"
										+ e1.getMessage(),
								"Problem Opening Application");
						e1.printStackTrace();
					}finally
					{
						mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

					}
				}

			};
			doit.start();
		}
	}

}
