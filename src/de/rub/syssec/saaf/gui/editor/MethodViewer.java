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
package de.rub.syssec.saaf.gui.editor;

import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import de.rub.syssec.saaf.analysis.steps.cfg.CFGGraph;
import de.rub.syssec.saaf.gui.actions.ExportAction;
import de.rub.syssec.saaf.gui.actions.ExternalViewerAction;
import de.rub.syssec.saaf.gui.actions.SwitchDotCommentAction;
import de.rub.syssec.saaf.model.application.MethodInterface;

public class MethodViewer extends JInternalFrame{

	private static final long serialVersionUID = 3931840262426289268L;
	MethodInterface method;
	ExportAction export;
	ExternalViewerAction external;
	CFGGraph graph;
	
	
	HashMap<Integer, Object> vertices;

	public MethodViewer(final MethodInterface method){
		super(method.getName() 
				+ "(" + method.getParameterString() + ")"
				+ method.getReturnValueString(),
				true, //resizable
		        true, //closable
		        true, //maximizable
		        true);//iconifiable
		this.method = method;

		graph = new CFGGraph(method);
		export = new ExportAction(graph);
		external = new ExternalViewerAction(method, graph);
		getContentPane().add(graph.getGraphComponent());
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menuBar.add(menu);
		JMenuItem menuItem = new JMenuItem("Export");
		menuItem.addActionListener(export);
		menu.add(menuItem);
		menuItem = new JMenuItem("Show in external viewer");
		menuItem.addActionListener(external);
		menu.add(menuItem);
		
		SwitchDotCommentAction switchAction = new SwitchDotCommentAction(this, method);
		
		menuItem = new JMenuItem("Show APICall information as dot comment");
		menuItem.addActionListener(switchAction);
		menu.add(menuItem);
		

		
		this.setJMenuBar(menuBar);
		
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(800, 620);//TODO: change to fullscreen
		this.setVisible(true);
	}
	
	public void setGraph(CFGGraph graph){
		this.graph = graph;
		export.setGraph(graph);
		external.setGraph(graph);
	}
	

}
