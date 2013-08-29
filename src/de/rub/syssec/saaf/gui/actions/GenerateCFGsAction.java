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
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import de.rub.syssec.saaf.analysis.steps.cfg.CFGGraph;
import de.rub.syssec.saaf.analysis.steps.cfg.ExportCFG;
import de.rub.syssec.saaf.gui.MainWindow;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.Digest;
import de.rub.syssec.saaf.model.application.MethodInterface;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class GenerateCFGsAction extends AbstractAction {

	private static final long serialVersionUID = 6922817709698827424L;
	private MainWindow mainWindow;

	public GenerateCFGsAction(String title, MainWindow mainWindow) {
		super(title);
		this.mainWindow = mainWindow;
		this.putValue(SHORT_DESCRIPTION, "Generates control flow grapsh for all methods.");		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		final AnalysisInterface selectedAnalysis = mainWindow
				.getUserselectedAnalysisIfMultipleAreOpened();
		if (selectedAnalysis == null) {
			JOptionPane.showMessageDialog(mainWindow,
					"Please open an application first.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		final int answer = JOptionPane.showConfirmDialog(null,
				"This might take a while and can consume\n"
						+ "several hundred megabytes of disk space.\n"
						+ "depending on the application size.\n"
						+ "\nAre you sure?",
				"Generate all Control Flow Graphs", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (answer == JOptionPane.YES_OPTION) {
			Thread doit = new Thread() {

				public void run() {
					mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					LinkedList<ClassInterface> files = selectedAnalysis
							.getApp().getAllSmaliClasss(true);
					
					int processed = 0;
					ProgressMonitor monitor = new ProgressMonitor(mainWindow, "Generating Graphs..", "Generating graphs for all methods in all classes", processed, files.size());
					
					File outDir = new File(Config.getInstance().getValue(ConfigKeys.DIRECTORY_CFGS)+File.separator+selectedAnalysis.getApp().getApplicationName()+"_"+selectedAnalysis.getApp().getMessageDigest(Digest.MD5));
					
					for (ClassInterface file : files) {// every file of the
														// current apk		
						monitor.setNote("Generating graphs for methods of class "+file);
						LinkedList<MethodInterface> methods = file.getMethods();
						for (MethodInterface method : methods) {// every method
																// in the
																// file
		

								CFGGraph c = new CFGGraph(method);
								ExportCFG ex = new ExportCFG(c.getGraph(), outDir.toString());
								ex.export(method);

						}
							 

						processed++;
						monitor.setProgress(processed);
					}
					
					mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					
				}
			};
			doit.start();
		}
	}

}
