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


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import de.rub.syssec.saaf.application.search.BytecodeSearcher;
import de.rub.syssec.saaf.gui.FileTree;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;

/**
 * Calls the search for patterns in smali bytecode.
 */
public class FoundBytecodeFrame extends JInternalFrame {
	 
	private static final long serialVersionUID = -6243385366339287049L;
	private final JButton searchBtn;
	private final JScrollPane jScrollPane;
	private final JTable jTable;
	private final JTextField patternField;
	private Vector<CodeLineInterface> resultVec = new Vector<CodeLineInterface>();
	private static final String[] TABLE_COLUMNS = { "File", "Line", "Content", "Is code?" }; 
	private final ApplicationInterface app;
	
	
	public FoundBytecodeFrame(final ApplicationInterface app, final FileTree fileTree) {
	    super("Search in SMALI Bytecode Files - "+ app.getApplicationName(), true, true,	true, true);
    	this.app = app;
        patternField = new javax.swing.JTextField();
        searchBtn = new javax.swing.JButton();
        jScrollPane = new javax.swing.JScrollPane();
        jTable = new javax.swing.JTable();
        
        searchBtn.setText("Search");
        searchBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	performSearch(evt);
            }	
        });

        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
        jTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent event) {
                        int viewRow = jTable.getSelectedRow();
                      if (viewRow >= 0) {
                            if (event.getValueIsAdjusting() == false) {
                            	CodeLineInterface cl = resultVec.get(viewRow);
								int appDirLength = app.getUnpackedDataDir().getParentFile().getAbsolutePath().length(); // cut the unnecessary part
								String fileName = cl.getSmaliClass().getFile().getAbsolutePath().substring(appDirLength);
								fileTree.searchNode(fileName, ""+cl.getLineNr());
                            }
                        }
                    }
                }
        );
        
   	            
        jTable.setModel(new SearchTableModel(resultVec, TABLE_COLUMNS));
        jScrollPane.setViewportView(jTable);
        jTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);

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
	    
        JScrollPane scrollPane = new JScrollPane(jTable);
	    jTable.setFillsViewportHeight(true);
	    add(scrollPane);
	 }

	protected void performSearch(ActionEvent evt) {
		String pattern = patternField.getText();
		if (pattern == null || pattern.trim().isEmpty()) return;
		
		resultVec = BytecodeSearcher.searchPattern(app, pattern);
		jTable.setModel(new SearchTableModel(resultVec, TABLE_COLUMNS));
	       
        TableColumn column = null;
        for (int i = 0; i < 3; i++) {
            column = jTable.getColumnModel().getColumn(i);
            if (i == 1) {
                column.setMinWidth(150);
            } else {
            	column.setMinWidth(300);
            }
        }
       
        jScrollPane.setViewportView(jTable);
	}
	
	
	private class SearchTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 8834298385480526192L;
		
		private final Vector<CodeLineInterface> foundVec;
		private final String[] columnNames;
		
		public SearchTableModel(Vector<CodeLineInterface> foundVec, String[] columnNames) {
			this.foundVec = foundVec;
			this.columnNames = columnNames;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			CodeLineInterface cl = foundVec.get(row);
			switch (column) {
			case 0:
				// FIXME: Not so nice, but no way to tell whats the smali content dir?
				String s = cl.getSmaliClass().getFile().getAbsolutePath();
				String split[] = s.split("/smali/", 2);
				return split[1];
			case 1:
				return cl.getLineNr();
			case 2:
				return new String(cl.getLine());
			case 3:
				return cl.isCode();
			default:
				return "";
			}
		}

		@Override
		public int getRowCount() {
			return foundVec.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return columnNames[columnIndex];
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}
	}
}
