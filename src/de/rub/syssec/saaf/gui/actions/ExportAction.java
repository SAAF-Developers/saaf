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

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.mxgraph.view.mxGraph;

import de.rub.syssec.saaf.analysis.steps.cfg.ExportCFG;

public class ExportAction implements ActionListener {
private mxGraph graph;


	public ExportAction(mxGraph g) {
		graph = g;
	}
	

	public void actionPerformed(ActionEvent event) {
	if(event.getActionCommand().startsWith("Export")){
	    FileNameExtensionFilter filter = new FileNameExtensionFilter("All Files",/*"svg",*/ "png");
		JFileChooser exportDir = new JFileChooser();
		exportDir.setFileFilter(filter);
		exportDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = exportDir.showSaveDialog(null);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String file = exportDir.getSelectedFile().getAbsolutePath();
			
			String extension = file.substring(file.lastIndexOf(".") +1 );
			ExportCFG export = new ExportCFG(graph);
		    if (extension != null) {
		        if (extension.equals("png") ) {
					export.generatePNG(file); 
		        } /*else if (extension.equals("svg")){
					genSVG(file);
		        }*/else{//if not type chosen, use png
					export.generatePNG(file+".png");
		        }
		    }else{
				
			    }
			}
		}
	}

}