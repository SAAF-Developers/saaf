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
package de.rub.syssec.saaf.gui.frame;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import de.rub.syssec.saaf.gui.FilterTable;
import de.rub.syssec.saaf.gui.editor.FileTree;
import de.rub.syssec.saaf.misc.KMP;
import de.rub.syssec.saaf.model.APICall;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;

public class FoundAPICallsFrame extends JInternalFrame {


	private static final long serialVersionUID = -4413736917711312369L;
	private final JScrollPane jScrollPane;
	private static final String[] TABLE_COLUMNS = { "File", "Line", "Call", "Permission" }; 

	public FoundAPICallsFrame(final ApplicationInterface app, final FileTree fileTree) {
		super("Found APICalls - " + app.getApplicationName(), true, // resizable
				true, // closable
				true, // maximizable
				true);// iconifiable

		jScrollPane = new JScrollPane();
		
		HashMap<CodeLineInterface, APICall> matches = app.getMatchedCalls();
		List<CodeLineInterface> unmatched= new ArrayList<CodeLineInterface>();
		
		for(CodeLineInterface cl : app.getFoundCalls()){
			if(!matches.containsKey(cl)){
				unmatched.add(cl);
			}
		}
		
		final Set<CodeLineInterface> keys = matches.keySet();
		
		jScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);


		//TODO: maybe change the following two into sets
		//not working yet, because it is not application name but package name
		List<CodeLineInterface> internalCalls = new ArrayList<CodeLineInterface>();
		List<CodeLineInterface> systemCalls = new ArrayList<CodeLineInterface>();
		
		ArrayList<String> packageNames = new ArrayList<String>();
		for(ClassInterface c: app.getAllSmaliClasss(true)){
			packageNames.add(c.getFullClassName(false));
		}
		
		for(CodeLineInterface cl: app.getFoundCalls()){
			boolean added = false;
			for(String packageName: packageNames){
				if(KMP.indexOf(cl.getLine(), packageName.getBytes())>-1){
					internalCalls.add(cl);
					added = true;
				}
				
			}
			if(!added){
				systemCalls.add(cl);
				added = true;
			}
		}
		
		JTabbedPane jtbExample = new JTabbedPane();
		FilterTable t1 = new FilterTable(keys, TABLE_COLUMNS, fileTree);
		jtbExample.addTab("Match", t1);
		jtbExample.setSelectedIndex(0);
		FilterTable t3 = new FilterTable(unmatched, TABLE_COLUMNS, fileTree);
		jtbExample.addTab("No match", t3);
		
		FilterTable tableInternal = new FilterTable(internalCalls, TABLE_COLUMNS, fileTree);
		jtbExample.addTab("internal", tableInternal);
		
		FilterTable tableSystem = new FilterTable(systemCalls, TABLE_COLUMNS, fileTree);
		jtbExample.addTab("system", tableSystem);
		
		
		FilterTable t4 = new FilterTable(app.getFoundCalls(), TABLE_COLUMNS, fileTree);

		jtbExample.addTab("All", t4);

		setLayout(new GridLayout(1, 1));
		add(jtbExample);

		this.setPreferredSize(new Dimension(800, 400));
	}
	


}
