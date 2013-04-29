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

import java.util.Vector;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import de.rub.syssec.saaf.application.search.StringSearcher;
import de.rub.syssec.saaf.application.search.StringSearcher.FoundString;
import de.rub.syssec.saaf.gui.FileTree;
import de.rub.syssec.saaf.model.application.ApplicationInterface;

public class FoundStringsFrame extends JInternalFrame {

	private static final long serialVersionUID = 8295968245777889533L;
	private final JScrollPane jScrollPane;
	private final JTable jTable;
	private Vector<FoundString> foundStringsVec;
	private static final String[] TABLE_COLUMNS = { "File", "Line", "String" }; 

	public FoundStringsFrame(final ApplicationInterface app, final FileTree fileTree) throws Exception {
		super("Found Strings - " + app.getApplicationName(), true, // resizable
				true, // closable
				true, // maximizable
				true);// iconifiable

		jScrollPane = new javax.swing.JScrollPane();
		jTable = new JTable();

		jScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		jTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent event) {
						int viewRow = jTable.getSelectedRow();
						if (viewRow >= 0) {
							if (event.getValueIsAdjusting() == false) {
								FoundString fs = foundStringsVec.get(viewRow);
								int appDirLength = app.getUnpackedDataDir().getParentFile().getAbsolutePath().length(); // cut the unnecessary part
								String fileName = fs.getCodeLine().getSmaliClass().getFile().getAbsolutePath().substring(appDirLength);
								
								fileTree.searchNode(fileName,
										""+fs.getCodeLine().getLineNr());
							}
						}
					}
				});

		foundStringsVec = StringSearcher.searchString(app);

		jTable.setModel(new SearchTableModel(foundStringsVec, TABLE_COLUMNS));
		JTable table = jTable;

		TableColumn column = null;
		for (int i = 0; i < 3; i++) {
			column = table.getColumnModel().getColumn(i);
			if (i == 1) {
				column.setMinWidth(50);
			} else {
				column.setMinWidth(350);
			}
		}

		jScrollPane.setViewportView(jTable);
		jTable.getColumnModel()
				.getSelectionModel()
				.setSelectionMode(
						javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addComponent(
				jScrollPane, javax.swing.GroupLayout.Alignment.TRAILING,
				javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addComponent(
				jScrollPane, javax.swing.GroupLayout.Alignment.TRAILING,
				javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE));
	}
	
	
	private class SearchTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 8834298385480526192L;
		
		private final Vector<FoundString> foundVec;
		private final String[] columnNames;
		
		public SearchTableModel(Vector<FoundString> foundVec, String[] columnNames) {
			this.foundVec = foundVec;
			this.columnNames = columnNames;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			FoundString fs = foundVec.get(row);
			switch (column) {
			case 0:
				// FIXME: Not so nice, but no way to tell whats the smali content dir?
				String s = fs.getCodeLine().getSmaliClass().getFile().getAbsolutePath();
				String split[] = s.split("/smali/", 2);
				return split[1];
			case 1:
				return fs.getCodeLine().getLineNr();
			case 2:
				return fs.getFoundString();
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
