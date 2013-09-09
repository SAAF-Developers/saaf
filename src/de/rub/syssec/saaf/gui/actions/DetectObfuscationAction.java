package de.rub.syssec.saaf.gui.actions;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;

import de.rub.syssec.saaf.analysis.steps.obfuscation.DetectObfuscationStep;
import de.rub.syssec.saaf.gui.MainWindow;
import de.rub.syssec.saaf.gui.OpenAppsMgr;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;

public class DetectObfuscationAction extends AbstractAction {

	private MainWindow mainwindow;
	private OpenAppsMgr appsManager;

	public DetectObfuscationAction(String title, MainWindow mainWindow,
			OpenAppsMgr openAppsMgr) {
		super(title);
		this.mainwindow = mainWindow;
		this.appsManager = openAppsMgr;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Thread doit = new Thread() {

			public void run() {
				mainwindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				DetectObfuscationStep detector = new DetectObfuscationStep(Config.getInstance(), true);
				List<AnalysisInterface> analyses = appsManager.getAllAnalyses();
				for (AnalysisInterface analysis : analyses) {
					try {
						detector.process(analysis);
					} catch (AnalysisException e) {
						e.printStackTrace();
						MainWindow.showErrorDialog(
								"An error occured "+e.getMessage(),
								"Decompilation Error");
					}
				}
				mainwindow.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

			}
		};
		doit.start();

	}
}
