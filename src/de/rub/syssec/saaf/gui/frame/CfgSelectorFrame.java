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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.io.FileUtils;

import de.rub.syssec.saaf.analysis.steps.cfg.CFGGraph;
import de.rub.syssec.saaf.analysis.steps.cfg.ExportCFG;
import de.rub.syssec.saaf.application.methods.Method;
import de.rub.syssec.saaf.gui.MainWindow;
import de.rub.syssec.saaf.gui.editor.MethodViewer;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.MethodInterface;

/**
 * This frame shows all methods for one smali class for which CFGs can
 * be created and exported.
 */
public class CfgSelectorFrame extends JInternalFrame {

	private static final long serialVersionUID = -1922752821043510095L;
	private final ClassInterface smaliClass;
	private JTable list = new JTable();
	private final AbstractTableModel listModel;

	private JButton generateButton;
	private JButton showButton;
	private JCheckBox saveCfgCheckBox;

	private static final String GENERATE_OPERATION = "Generate";
	private static final String SHOW_OPERATION = "Show";
	
	
	/**
	 * A frame to generate and open Control Flow Graphs. CFGs are created using mxGraph
	 * and are saved as PNGs.
	 * 
	 * @param smaliClass the smali class to select methods from
	 */
	public CfgSelectorFrame(ClassInterface smaliClass) {
		super(smaliClass.getFullClassName(true), true, true, true, true);
		this.smaliClass = smaliClass;

		listModel = new MethodTableModel(smaliClass);
		list.setModel(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		list.getSelectionModel().addListSelectionListener(
			new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent event) {
					int viewRow = list.getSelectedRow();
					if (viewRow <= 0) {
						showButton.setEnabled(false);
					} else {
						showButton.setEnabled(true);
					}
				}
		});

		JScrollPane listScrollPane = new JScrollPane(list);
		
		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals(SHOW_OPERATION)) {
					generateAndShowCfg(true);
				}
				else if (e.getActionCommand().equals(GENERATE_OPERATION)) {
					generateAndShowCfg(false);
				}
			}
		};

		generateButton = new JButton(GENERATE_OPERATION);
		generateButton.setActionCommand(GENERATE_OPERATION);
		generateButton.addActionListener(al);
		showButton = new JButton(SHOW_OPERATION);
		showButton.setEnabled(false);
		showButton.addActionListener(al);

		saveCfgCheckBox = new JCheckBox("Save a copy");
		saveCfgCheckBox.setToolTipText("Save a copy in the configured CFG folder upon generation.");

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.add(generateButton);
		buttonPane.add(saveCfgCheckBox);
		buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JPanel filler = new JPanel();
		filler.setPreferredSize(new Dimension(10, 10));
		buttonPane.add(filler);
		buttonPane.add(showButton);

		add(listScrollPane, BorderLayout.CENTER);
		add(buttonPane, BorderLayout.PAGE_END);

		this.setPreferredSize(new Dimension(500, 250));
		this.pack();
		this.setVisible(true);
	}

	/**
	 * Show a CFG in external viewer. CFG will be created on-the-fly if it
	 * does not exists. This method will determine the selected method(s)
	 * from the shown table.
	 * 
	 * @param showCfg Generate AND show CFG?
	 */
	private void generateAndShowCfg(boolean showCfg) {
		boolean save = saveCfgCheckBox.isSelected();
		
		int index = list.getSelectedRow();
		if (index == 0) { // Generate all
			for (MethodInterface method : smaliClass.getMethods()) {
				File cfg = generateAndShowCfg(method, false);
				if (save) {
					safeCopy(cfg);
				}
			}
		}
		else if (index > 0) {
			Method method = (Method) listModel.getValueAt(index, 0); // one-dimensional
			File cfg = generateAndShowCfg(method, showCfg);
			if (save) {
				safeCopy(cfg);
			}
		}
	}

	/**
	 * generates a cfg and may show it
	 * 
	 * @param showCfg Generate AND show CFG?
	 * @return the file representing the CFG as PNG file
	 */
	private File generateAndShowCfg(MethodInterface method, boolean showCfg) {
		String targetDir = smaliClass.getApplication().getBytecodeDirectory()
				.getAbsolutePath();
		
		CFGGraph c = new CFGGraph(method);
		ExportCFG ex = new ExportCFG(c.getGraph(), targetDir);
		ex.export(method);
		File cfg = new File(ex.getLastExportedFile());
		if (showCfg) {
				MethodViewer cfgViewer = new MethodViewer(method);
				MainWindow.getDesktopPane().add(cfgViewer);
				try {
					cfgViewer.setSelected(true);
				} catch (java.beans.PropertyVetoException e) {
				}
		}
		return cfg;
	}


	/**
	 * Copies the generated PNG to the specified directory. The directory must be
	 * specified in the config.
	 * 
	 * @param cfg the CFG to copy
	 */
	private void safeCopy(File cfg) {
		String cfgValue = Config.getInstance().getConfigValue(ConfigKeys.DIRECTORY_CFGS);
		File externalDir = null;
		boolean error = false;
		if (cfgValue != null) {
			externalDir = new File(cfgValue);
			if (!externalDir.exists() || !externalDir.isDirectory() || !externalDir.canWrite()) {
				error = true;
			}
		}
		else {
			error = true;
		}
		if (error) {
			MainWindow.showErrorDialog("Could not save CFG(s). Option " + ConfigKeys.DIRECTORY_CFGS.toString()
					+ " not properly set in saaf.conf.", "Error");
			return;
		}
		
		String name = smaliClass.getApplication().getApplicationDirectory().getName();
		String bytecodeDir = smaliClass.getApplication().getBytecodeDirectory()
				.getName();


		int posByteCodeName = cfg.getAbsolutePath().lastIndexOf(bytecodeDir);
		String path = cfg.getAbsolutePath().substring(posByteCodeName+bytecodeDir.length()+1);
		File targetFile = new File(externalDir+File.separator+name+File.separator+path.replace(File.separator, "."));
		 
		try {
			if (!targetFile.exists() || (cfg.length() != targetFile.length())) {
				FileUtils.copyFile(cfg, targetFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
			MainWindow.showErrorDialog("Could not save CFG(s): "+e.getMessage(), "Error");
		}

	}

	/**
	 * An internal helper class to make use of a JTable.
	 */
	private class MethodTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 8834298385480526192L;

		private final LinkedList<MethodInterface> methods = new LinkedList<MethodInterface>();
		private final String[] columnNames = { "Shortened method names" };

		public MethodTableModel(ClassInterface smaliClass) {
			/*
			 * First one is the 'All' list item, it is no real method but starts
			 * the CFG creation for all methods in one class.
			 */
			for (MethodInterface method : smaliClass.getMethods()) {
				methods.add(method);
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (row == 0) {
				return "Create CFGs for all methods";
			}
			return methods.get(row - 1); // first one is null/"All"
		}

		@Override
		public int getRowCount() {
			return methods.size() + 1;
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
			return Object.class;
		}
	}
}
