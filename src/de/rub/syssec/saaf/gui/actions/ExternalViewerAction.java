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
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import de.rub.syssec.saaf.analysis.steps.cfg.CFGGraph;
import de.rub.syssec.saaf.analysis.steps.cfg.ExportCFG;
import de.rub.syssec.saaf.gui.MainWindow;
import de.rub.syssec.saaf.gui.ViewerStarter;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.application.MethodInterface;

public class ExternalViewerAction implements ActionListener {
	private CFGGraph graph;
	private MethodInterface method;

	private final ViewerStarter viewer = new ViewerStarter(ConfigKeys.VIEWER_IMAGES);

	public ExternalViewerAction(MethodInterface method, CFGGraph g) {
		graph = g;
		this.method = method;
	}
	

	public void actionPerformed(ActionEvent event) {
		String targetDir = method.getSmaliClass().getApplication().getBytecodeDirectory()
				.getAbsolutePath();
		ExportCFG ex = new ExportCFG(graph.getGraph(), targetDir);
		ex.export(method);
		File cfg = new File(ex.getLastExportedFile());

		try {
			viewer.showFile(cfg);
		} catch (IOException e) {
			e.printStackTrace();
			MainWindow.showErrorDialog("Could not generate CFG.", "Error");
		}
		
	}
	
	public void setGraph(CFGGraph graph){
		this.graph = graph;
	}

}