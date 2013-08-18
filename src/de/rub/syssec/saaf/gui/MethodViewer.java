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
package de.rub.syssec.saaf.gui;

import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.mxgraph.view.mxGraph;

import de.rub.syssec.saaf.gui.actions.ExportAction;
import de.rub.syssec.saaf.misc.CFGGraph;
import de.rub.syssec.saaf.model.application.MethodInterface;

public class MethodViewer extends JFrame{

	
	private static final long serialVersionUID = 3931840262426289268L;
	MethodInterface method;
	mxGraph graph;
	
	HashMap<Integer, Object> vertices;

	public MethodViewer(MethodInterface method){
		super(method.getName()+"("+method.getParameterString()+")"+method.getReturnValueString());
		this.method = method;

		CFGGraph c = new CFGGraph(method);
		getContentPane().add(c.getGraphComponent());
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Export");
		menuBar.add(menu);
		JMenuItem menuItem = new JMenuItem("Export");
		menuItem.addActionListener(new ExportAction(c.getGraph()));
		menu.add(menuItem);
		
		
		
		this.setJMenuBar(menuBar);
		
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(800, 620);//TODO: change to fullscreen
		this.setVisible(true);
	}

}
