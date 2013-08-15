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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import de.rub.syssec.saaf.gui.MainWindow;
import de.rub.syssec.saaf.misc.CFGGraph;
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

	/**
	 * 
	 */
	private static final long serialVersionUID = 6922817709698827424L;
	private MainWindow mainWindow;

	public GenerateCFGsAction(String title, MainWindow mainWindow) {
		super(title);
		this.mainWindow = mainWindow;
		this.enabled = Config.getInstance().isValidExecutable(
				ConfigKeys.EXECUTABLE_DOT);
		if (this.enabled) {
			this.putValue(SHORT_DESCRIPTION,
					"Generates control flow grapsh for all methods.");
		} else {
			this.putValue(SHORT_DESCRIPTION,
					"The dot executable is not available.");
		}
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

					BufferedWriter outputFile= null;	
					try {
						if(!outDir.exists()){
							outDir.mkdirs();
						}
						outputFile = new BufferedWriter(new FileWriter(outDir+File.separator+"names.txt"));

					} catch (IOException e3) {
						e3.printStackTrace();
					}
					
					
					for (ClassInterface file : files) {// every file of the
														// current apk		
						monitor.setNote("Generating graphs for methods of class "+file);
						LinkedList<MethodInterface> methods = file.getMethods();
						for (MethodInterface method : methods) {// every method
																// in the
																// file
		
								//TODO: wrap all this in its own method
								CFGGraph c = new CFGGraph(method);
								ExportAction ex = new ExportAction(c.getGraph(), outDir.toString());
								String parameters = "("+method.getParameterString().replaceAll("/", "_")+")";//TODO: maybe do this in method.getParameterString, or at least the "(" and ")"
								
								StringBuilder realFileName = new StringBuilder();
								realFileName.append(method.getSmaliClass().getClassName());
								realFileName.append("_");
								realFileName.append(method.getName());
								realFileName.append(parameters);
								realFileName.append(method.getReturnValueString());
								realFileName.append(".png");
														
								String newFileName = ex.export(method.getSmaliClass().getClassName(), "_",method.getName(),parameters,method.getReturnValueString(),".png",method.getSmaliClass().getPackageName(false));
								
								if(!realFileName.toString().equals(newFileName)){
									try {
										outputFile.write("Generated Filename:");
										outputFile.newLine();
										outputFile.write(newFileName);
										outputFile.newLine();
										outputFile.write("Real Filename");
										outputFile.newLine();
										outputFile.write(realFileName.toString());
										outputFile.newLine();
										outputFile.newLine();
										
									} catch (IOException e1) {
										e1.printStackTrace();
									}
									
								}

						}
							 

						processed++;
						monitor.setProgress(processed);
					}
					
					mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					try {
						outputFile.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					} 
					
				}
			};
			doit.start();
		}
	}

}
