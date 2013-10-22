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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.print.PrinterException;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import de.rub.syssec.saaf.application.search.StringSearcher.FoundString;
import de.rub.syssec.saaf.gui.editor.FileTree;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;


public class FilterTable extends JPanel implements ComponentListener {

	private static final long serialVersionUID = 2409862993818211192L;
	private JTable table;
	private JScrollPane scrollPane;
    Set<CodeLineInterface> keys = null;
    List<CodeLineInterface> foundCalls = null; //TODO: merge all the different set and list and vector cases, this will make this class a lot better and everything easier
    Vector<FoundString> foundVec = null;
    FileTree fileTree = null;
    
    private TableRowSorter<SearchTableModel> sorter;
    
    
    //TODO: take similar stuff out of constructors
    public FilterTable(Vector<FoundString> foundVec, String[] columnNames, final FileTree fileTree){
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));//change layout
        this.foundVec = foundVec;
        
        this.fileTree = fileTree;
        //Create a table with a sorter.
        SearchTableModel model = new SearchTableModel(foundVec, columnNames);
        
        createPane(model);        
    }
    
    
    
    public FilterTable(List<CodeLineInterface> foundCalls, String[] columnNames, final FileTree fileTree){
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.foundCalls = foundCalls;
        
        this.fileTree = fileTree;
        //Create a table with a sorter.
        SearchTableModel model = new SearchTableModel(foundCalls, columnNames);
        
        createPane(model);
    }



	private void createPane(SearchTableModel model) {
		scrollPane = createGUI(model);
        
        JPanel input = createInput();
  
        //Add the scroll pane to this panel.
        add(scrollPane);
        add(input);
        this.addComponentListener(this);
	}   
    
    
	
	public FilterTable(Set<CodeLineInterface> keys, String[] tableColumns, final FileTree fileTree) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.keys = keys;

        this.fileTree = fileTree;
        //Create a table with a sorter.
        SearchTableModel model = new SearchTableModel(keys, tableColumns);
        
        createPane(model);        
	}



	private JPanel createInput() {
	      //Create a separate form for filterText and statusText
        JPanel form = new JPanel();
        JLabel l1 = new JLabel("Search Text (case sensitive and Java Regex possible):", SwingConstants.TRAILING);
        form.add(l1);
        final JTextField searchText = new JTextField();
        searchText.setPreferredSize(new Dimension(150,30));
        searchText.getDocument().addDocumentListener(
                new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) {
                        Filter(searchText);
                    }
                    public void insertUpdate(DocumentEvent e) {
                        Filter(searchText);
                    }
                    public void removeUpdate(DocumentEvent e) {
                        Filter(searchText);
                    }
                });
        l1.setLabelFor(searchText);
        form.add(searchText);
        JButton printTable = new JButton("print table");
        printTable.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e) {
    	        try {
					table.print();
				} catch (PrinterException e1) {
					e1.printStackTrace();
				}
        }});
        form.add(printTable);
        form.setPreferredSize(new Dimension(400,40));
        form.setMaximumSize(new Dimension(800,200));
        return form;
	}

    private void Filter(JTextField textField) {
        RowFilter<SearchTableModel, Object> rf = null;
        try {
        	//use filter on all columns, if a specific column is needed use second parameter, eg. rf = RowFilter.regexFilter(textField.getText(), columnIndex);
            rf = RowFilter.regexFilter(textField.getText());
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        sorter.setRowFilter(rf);
    }



	private JScrollPane createGUI(SearchTableModel model) {
	      sorter = new TableRowSorter<SearchTableModel>(model);
	        table = new JTable(model);
	        table.setRowSorter(sorter);
	        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
	        table.setFillsViewportHeight(true);

	        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	        
	        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

//	        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
	        
	        TableColumn column = null;
	        int width = table.getPreferredScrollableViewportSize().width / (table.getColumnCount()-1);
	        for (int i = 0; i < table.getColumnCount(); i++) {
	            column = table.getColumnModel().getColumn(i);
	            if (i == 1) {
	            	
	                column.setPreferredWidth(50); //third column is smaller
	            } else {
	                column.setPreferredWidth(width);
	            }
	        }

	        table.getSelectionModel().addListSelectionListener(
	                new ListSelectionListener() {
	                    public void valueChanged(ListSelectionEvent event) {
	                        int viewRow = table.getSelectedRow();
	                        if (viewRow >= 0) {
	                        	if(event.getValueIsAdjusting() == false){
	                        		int row = table.convertRowIndexToModel(viewRow);
		                            if(keys != null){
										CodeLineInterface cl = (CodeLineInterface) keys.toArray()[row];//ugly the list version is better
										
										ApplicationInterface app = cl.getMethod().getSmaliClass().getApplication();
										int appDirLength = app.getUnpackedDataDir().getParentFile().getAbsolutePath().length(); // cut the unnecessary part
										String fileName = cl.getSmaliClass().getFile().getAbsolutePath().substring(appDirLength);
										
										fileTree.searchNode(fileName,
												""+cl.getLineNr());
		                            } else if( foundCalls != null){
										CodeLineInterface cl = foundCalls.get(row);
										
										ApplicationInterface app = cl.getMethod().getSmaliClass().getApplication();
										int appDirLength = app.getUnpackedDataDir().getParentFile().getAbsolutePath().length(); // cut the unnecessary part
										String fileName = cl.getSmaliClass().getFile().getAbsolutePath().substring(appDirLength);
										
										fileTree.searchNode(fileName,
												""+cl.getLineNr());
		                            } else if(foundVec != null){
										FoundString fs = foundVec.get(row);
										ApplicationInterface app = fs.getCodeLine().getMethod().getSmaliClass().getApplication();
										
										int appDirLength = app.getUnpackedDataDir().getParentFile().getAbsolutePath().length(); // cut the unnecessary part
										String fileName = fs.getCodeLine().getSmaliClass().getFile().getAbsolutePath().substring(appDirLength);
										
										fileTree.searchNode(fileName,""+fs.getCodeLine().getLineNr());
		                            }
	                        	}
	                        }
	                    }
	                }
	        );
	        

	        //Create the scroll pane and add the table to it.
	        JScrollPane scrollPane = new JScrollPane(table);
	        
	        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	        scrollPane.setViewportView(table);//??
   
	        return scrollPane;
	}


	@Override
	public void componentHidden(ComponentEvent arg0) {
		
	}



	@Override
	public void componentMoved(ComponentEvent arg0) {
		
	}



	@Override
	public void componentResized(ComponentEvent arg0) {
        TableColumn column = null;
        int width = scrollPane.getWidth() / (table.getColumnCount()-1);
        for (int i = 0; i < table.getColumnCount(); i++) {
            column = table.getColumnModel().getColumn(i);
            if (i == 1) {
            	
                column.setPreferredWidth(50); //third column is smaller
            } else {
                column.setPreferredWidth(width);
                
            }
        }
	}



	@Override
	public void componentShown(ComponentEvent arg0) {
		
	}



	private class SearchTableModel extends AbstractTableModel {
		
		private Set<CodeLineInterface> foundCalls = null;
		private List<CodeLineInterface> foundList = null;
		private Vector<FoundString> foundVec = null;
		private final String[] columnNames;
		
		public SearchTableModel(Vector<FoundString> foundVec, String[] columnNames) {
			this.foundVec = foundVec;
			this.columnNames = columnNames;
		}
		
		public SearchTableModel(Set<CodeLineInterface> foundCalls, String[] columnNames) {
			this.foundCalls = foundCalls;
			this.columnNames = columnNames;
		}
		
		public SearchTableModel(List<CodeLineInterface> foundCalls, String[] columnNames) {
			this.foundList = foundCalls;
			this.columnNames = columnNames;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			if(foundCalls!=null){
			CodeLineInterface cl = (CodeLineInterface) foundCalls.toArray()[row];//ugly
			switch (column) {
			case 0:
				// FIXME: Not so nice, but no way to tell whats the smali content dir?
				String s = cl.getSmaliClass().getFile().getAbsolutePath().replace(File.separator, "/");//replace is due to windows file.separator being different which results in problems for the split
				String split[] = s.split("/smali/", 2);//split method? code 3 times, fix this shit
				return split[1];
			case 1:
				return cl.getLineNr();
			case 2:
				return new String(cl.getLine());
			case 3:
				if(cl.getPermission()!=null) 
					return cl.getPermission().getName();
				else {return "";}
			default:
				return "";
			}
			} else if(foundList!=null){
			CodeLineInterface cl = foundList.get(row);
			switch (column) {
			case 0:
				// FIXME: Not so nice, but no way to tell whats the smali content dir?
				String s = cl.getSmaliClass().getFile().getAbsolutePath().replace(File.separator, "/");//replace is due to windows file.separator being different which results in problems for the split
				String split[] = s.split("/smali/", 2);
				return split[1];
			case 1:
				return cl.getLineNr();
			case 2:
				return new String(cl.getLine());
			case 3:
				if(cl.getPermission()!=null) 
					return cl.getPermission().getName();
				else return "";
			default:
				return "";
			}
			}
			FoundString fs = foundVec.get(row);
			switch (column) {
			case 0:
				// FIXME: Not so nice, but no way to tell whats the smali content dir?
				String s = fs.getCodeLine().getSmaliClass().getFile().getAbsolutePath().replace(File.separator, "/");//replace is due to windows file.separator being different which results in problems for the split
				String split[] = s.split("/smali/", 2);
				return split[1];
			case 1:
				return fs.getCodeLine().getLineNr();
			case 2:
				return fs.getFoundString();
			case 3:
				if(fs.getCodeLine().getPermission()!=null) 
					return fs.getCodeLine().getPermission().getName();
				else return "";
			default:
				return "";
			}
		}

		@Override
		public int getRowCount() {
			if(foundCalls != null)
				return foundCalls.size();
			else if (foundList != null) 
				return foundList.size();
			else return foundVec.size();
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

