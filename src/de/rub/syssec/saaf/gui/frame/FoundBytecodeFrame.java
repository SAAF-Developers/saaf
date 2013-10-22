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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import de.rub.syssec.saaf.application.search.BytecodeSearcher;
import de.rub.syssec.saaf.gui.FilterTable;
import de.rub.syssec.saaf.gui.editor.FileTree;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;

/**
 * Calls the search for patterns in smali bytecode.
 */
public class FoundBytecodeFrame extends JInternalFrame {
	 
	private static final long serialVersionUID = -6243385366339287049L;
	private final JButton searchBtn;
	private final JScrollPane jScrollPane;
	private FilterTable table;
	private FileTree fileTree;
	private final JTextField patternField;
	private Vector<CodeLineInterface> resultVec = new Vector<CodeLineInterface>();
	private static final String[] TABLE_COLUMNS = { "File", "Line", "Content", "Is code?" }; 
	private final ApplicationInterface app;
	
	
	public FoundBytecodeFrame(final ApplicationInterface app, final FileTree fileTree) {
	    super("Search in SMALI Bytecode Files - "+ app.getApplicationName(), true, true, true, true);
    	this.app = app;
        patternField = new javax.swing.JTextField();
        searchBtn = new javax.swing.JButton();
        jScrollPane = new javax.swing.JScrollPane();
        
        this.fileTree = fileTree;
        
        searchBtn.setText("Search");
        searchBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	performSearch(evt);
            }	
        });

        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        table = new FilterTable(resultVec, TABLE_COLUMNS, fileTree);
        jScrollPane.setViewportView(table);
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(searchBtn)
                    .addComponent(patternField, javax.swing.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE))
                .addGap(31, 31, 31))
            .addComponent(jScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(patternField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(searchBtn)
                .addGap(12, 12, 12)
                .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE))
        );
	    
        JScrollPane scrollPane = new JScrollPane(table);
	    add(scrollPane);
	    
	    this.setPreferredSize(new Dimension(800, 400));
	 }

	protected void performSearch(ActionEvent evt) {
		String pattern = patternField.getText();
		if (pattern == null || pattern.trim().isEmpty()) return;
		
		resultVec = BytecodeSearcher.searchPattern(app, pattern);
		table = new FilterTable(resultVec, TABLE_COLUMNS, fileTree);
       
        jScrollPane.setViewportView(table);
	}
}
