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
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ProgressMonitor;

import de.rub.syssec.saaf.analysis.steps.ProgressListener;
import de.rub.syssec.saaf.analysis.steps.decompile.DecompileToJavaStep;
import de.rub.syssec.saaf.gui.MainWindow;
import de.rub.syssec.saaf.gui.OpenAppsMgr;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;

/**
 * Triggers the decompilation of the complete application.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class DecompileToJavaAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4240488138238632093L;
	private OpenAppsMgr appsManager;
	private final MainWindow mainwindow;

	/**
	 * @param appsManager
	 * @param mainWindow
	 */
	public DecompileToJavaAction(String title, MainWindow mainwindow,
			OpenAppsMgr appsManager) {
		super(title);
		this.mainwindow = mainwindow;
		this.appsManager = appsManager;
		// this action is only enabled if the java decompiler is usable
		this.enabled = Config.getInstance().isValidExecutable(
				ConfigKeys.EXECUTABLE_JAD);
		if (this.enabled) {
			this.putValue(SHORT_DESCRIPTION,
					"Decompile apk to java source code");
		} else {
			this.putValue(SHORT_DESCRIPTION, "No decompiler available");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		Thread doit = new Thread() {
			public void run() {

				mainwindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				DecompileToJavaStep decompile = new DecompileToJavaStep(
						Config.getInstance(), true);
				

				List<AnalysisInterface> analyses = appsManager.getAllAnalyses();
				for (AnalysisInterface analysis : analyses) {
					final int numberOfClasses = analysis.getApp()
							.getAllClassFiles(true).size();
					final ProgressMonitor monitor = new ProgressMonitor(mainwindow,
							"Decompiling to Java ...", "Decompiling "
									+ analysis.getApp().getApplicationName(),
							0, numberOfClasses);
					
					decompile.addProgressListener(new ProgressListener() {
						
						@Override
						public void started() {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void setProgress(String note) {
							monitor.setNote(note);
							
						}
						
						@Override
						public void setProgress(int progress) {
							monitor.setProgress(progress);
							
						}
						
						@Override
						public void finished() {
							monitor.setProgress(numberOfClasses);
							
						}
						
						@Override
						public void canceled() {
							// TODO Auto-generated method stub
							
						}
					});
					try {
						decompile.process(analysis);
					} catch (AnalysisException e) {
						e.printStackTrace();
						MainWindow.showErrorDialog(
								"An error occured during decompilation of "
										+ analysis.getApp()
												.getApplicationName(),
								"Decompilation Error");
					}finally{
						mainwindow.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

					}
				}
			}
		};
		doit.start();

	}

}
