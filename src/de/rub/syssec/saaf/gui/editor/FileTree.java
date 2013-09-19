/*
 * Copyright (c) Ian F. Darwin, http://www.darwinsys.com/, 1996-2002.
 * All rights reserved. Software written by Ian F. Darwin and others.
 * $Id: LICENSE,v 1.8 2004/02/09 03:33:38 ian Exp $
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * Java, the Duke mascot, and all variants of Sun's Java "steaming coffee
 * cup" logo are trademarks of Sun Microsystems. Sun's, and James Gosling's,
 * pioneering role in inventing and promulgating (and standardizing) the Java
 * language and environment is gratefully acknowledged.
 * 
 * The pioneering role of Dennis Ritchie and Bjarne Stroustrup, of AT&T, for
 * inventing predecessor languages C and C++ is also gratefully acknowledged.
 * 
 * Potential source:
 * - http://www.java2s.com/Code/Java/Swing-JFC/DisplayafilesysteminaJTreeview.htm
 */
package de.rub.syssec.saaf.gui.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.log4j.Logger;

import de.rub.syssec.saaf.gui.OpenAnalysis;
import de.rub.syssec.saaf.gui.ViewerStarter;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;

/**
 * Display a file system in a JTree view (extended with lots of SAAF stuff).
 * 
 * <p>
 * Window to browse in the Android-App, viewing smali and java code. Called by
 * {@link OpenApp#showOrOpenNewFrame}.
 * </p>
 * 
 * This class is based on FileTree.java from Ian Darwin.
 * 
 * - Martin Ussath / 12.2011: Initial version. - Johannes Hoffmann / 01.2012:
 * Refactored and some improvements.
 * 
 * 
 * @version $Id: FileTree.java,v 1.9 2004/02/23 03:39:22 ian Exp $
 * @author Ian Darwin
 * @see OpenApp
 * 
 */
public class FileTree extends JPanel implements PropertyChangeListener {

	private class FileCellRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 462485888657862971L;

		@Override
		public Component getTreeCellRendererComponent(JTree arg0, Object arg1,
				boolean selected, boolean expanded, boolean leaf, int arg5,
				boolean arg6) {
			Component c = super.getTreeCellRendererComponent(arg0, arg1,
					selected, expanded, leaf, arg5, arg6);
			if (leaf) {
				// TODO
				// obtain the class for the file
				FileNode f = (FileNode) ((DefaultMutableTreeNode)arg1).getUserObject();
				ClassInterface smaliClass = model.getCurrentApplication()
						.getSmaliClass(f.getFile());
				// check if its obfuscated
				if (smaliClass != null && smaliClass.isObfuscated()) {
					// set foreground to red if it is
					c.setForeground(Color.RED);
				} else {
					// otherwise set foreground to black
					c.setForeground(Color.BLACK);
				}
			}
			return c;
		}

	}

	private static final String MENU_ACTION_CFG = "Generate CFGs";
	private final Vector<Vector<String>> history;
	private final LinkEditorKit linkEditorKit;

	private static final long serialVersionUID = -560054884407736589L;

	private File directory;
	private File smali;
	public FileNode lastClick;
	private JTree fileTree;

	private final EditorView editor;
	private OpenAnalysis openAna;
	private final Logger logger = Logger.getLogger(FileTree.class);

	private final ViewerStarter viewer = new ViewerStarter(
			ConfigKeys.VIEWER_IMAGES);
	private EditorModel model;
	private OutlineView outlineTree;

	public EditorModel getModel() {
		return model;
	}

	public void setModel(EditorModel model) {
		this.model = model;
	}

	private final class SelectionListener implements TreeSelectionListener {

		public void valueChanged(TreeSelectionEvent e) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath()
					.getLastPathComponent();

			// if (node == null) { // at startup this will show the manifest
			// try {
			// editor.open(app.getManifestFile());
			// } catch (Exception e1) {
			// logger.warn("Problem during tree construction", e1);
			// }
			// return;
			// }

			Object nodeInfo = node.getUserObject();
			if (node.isLeaf()) {
				FileNode nodeObject;
				try {
					nodeObject = (FileNode) nodeInfo;
				} catch (ClassCastException cce) {
					// No file in this node (empty directory)
					return;
				}

				linkEditorKit.setLastClickedFile(nodeObject.getFile());

				try {
					// Load text from file
					model.setCurrentFile(nodeObject.getFile());
				} catch (Exception e1) {
					logger.warn("Problem during tree construction: "
							+ e1.getMessage());
				}
			}
		}
	}

	private class FileNode {
		private String fileName;
		private File file;

		public FileNode(String fileName_p, File file_p) {
			fileName = fileName_p;
			file = file_p;
		}

		// this is shown in the tree
		public String toString() {
			return fileName;
		}

		public File getFile() {
			return file;
		}
	}

	public FileTree(final ApplicationInterface app, File dir, OpenAnalysis open) {
		super();
		this.openAna = open;
		history = new Vector<Vector<String>>();
		linkEditorKit = new LinkEditorKit(history, app.getUnpackedDataDir(),
				this);

		directory = dir;
		//userful to debug layout issues
		//setBackground(Color.MAGENTA);
		setLayout(new GridBagLayout());

		this.model = new EditorModel(app);

		// we want to be notified if the file changes so we can reflect that in
		// the tree
		model.addPropertyChangeListener(this);

		// the tree that lists the files (top left)
		JTree tree = new JTree(addNodes(null, dir));
		tree.addMouseListener(ma);
		tree.addTreeSelectionListener(new SelectionListener());
		tree.setCellRenderer(new FileCellRenderer());
		fileTree = tree;

		GridBagConstraints treeConstraints = new GridBagConstraints();
		treeConstraints.fill = GridBagConstraints.BOTH;
		treeConstraints.gridheight = 1;
		treeConstraints.gridwidth = 1;
		treeConstraints.gridx = 0;
		treeConstraints.gridy = 0;
		treeConstraints.weightx = 0.20;
		treeConstraints.weighty = 1.0;
		treeConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		this.add(new JScrollPane(tree), treeConstraints);

		// the list of components (bottom left)
		EntryPointsView entrypoints = new EntryPointsView(model);
		model.addPropertyChangeListener(entrypoints);

		JScrollPane entryPointsScroller = new JScrollPane(entrypoints);
		GridBagConstraints entrypointConstraints = new GridBagConstraints();
		entrypointConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		entrypointConstraints.fill = GridBagConstraints.BOTH;
		entrypointConstraints.gridheight = 1;
		entrypointConstraints.gridwidth = 1;
		entrypointConstraints.gridx = 0;
		entrypointConstraints.gridy = 1;
		entrypointConstraints.weightx = 0.15;
		entrypointConstraints.weighty = 1.0;

		this.add(entryPointsScroller, entrypointConstraints);

		// the editor (contains the textview and the list of methods)
		this.editor = new EditorView(model, this);
		this.model.addPropertyChangeListener(this.editor);

		GridBagConstraints editorConstraints = new GridBagConstraints();
		editorConstraints.anchor = GridBagConstraints.NORTHWEST;
		editorConstraints.fill = GridBagConstraints.BOTH;
		editorConstraints.gridheight = 2;
		editorConstraints.gridwidth = 1;
		editorConstraints.gridx = 1;
		editorConstraints.gridy = 0;
		editorConstraints.weightx = 0.70;
		editorConstraints.weighty = 1.0;
		this.add(editor, editorConstraints);

		this.outlineTree = new OutlineView(this.model);
		model.addPropertyChangeListener("currentClass", outlineTree);

		GridBagConstraints outlineConstraints = new GridBagConstraints();
		outlineConstraints.anchor = GridBagConstraints.NORTHWEST;
		outlineConstraints.fill = GridBagConstraints.BOTH;
		outlineConstraints.gridwidth = 1;
		outlineConstraints.gridheight = 2;
		outlineConstraints.gridx = 2;
		outlineConstraints.gridy = 0;
		outlineConstraints.weightx = 0.15;
		outlineConstraints.weighty = 1.0;
		this.add(outlineTree, outlineConstraints);
	}

	public DefaultMutableTreeNode searchNode(String nodeStr, String lineNr) {

		DefaultMutableTreeNode node = null;
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> e = ((DefaultMutableTreeNode) fileTree
				.getModel().getRoot()).breadthFirstEnumeration();

		while (e.hasMoreElements()) {

			node = (DefaultMutableTreeNode) e.nextElement();
			String filepath = "";

			for (int i = 0; i < node.getPath().length; i++) {
				filepath = filepath + File.separator + node.getPath()[i];
			}
			if (filepath.startsWith(nodeStr)) {
				TreeNode[] nodes = node.getPath();
				TreePath path = new TreePath(nodes);
				fileTree.scrollPathToVisible(path);
				fileTree.setSelectionPath(path);
				if (lineNr != null) {
					try {
						editor.goToLine(Integer.parseInt(lineNr));
					} catch (Exception e1) {
						logger.warn("Problem during tree construction", e1);
					}
				}

				return node;
			}

		}
		return null;
	}

	/** Add nodes from under "dir" into curTop. Highly recursive. */
	DefaultMutableTreeNode addNodes(DefaultMutableTreeNode curTop, File dir) {

		// String curPath = dir.getPath();
		String curPath = dir.getName();
		DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(curPath);

		if (curTop != null) {
			// should only be null at root
			curTop.add(curDir);
		}

		// ignore some files
		IOFileFilter fileFilter = new NotFileFilter(
				new SuffixFileFilter(new String[] { ".class", ".dot", ".java",
						".DS_Store", ".yml" }));

		LinkedList<File> files = new LinkedList<File>(FileUtils.listFiles(dir,
				fileFilter, null));
		// FIXME: How the hell can directories be listed?!
		// LinkedList<File> directories = new
		// LinkedList<File>(FileUtils.listFiles(dir, FalseFileFilter.INSTANCE,
		// DirectoryFileFilter.INSTANCE));
		LinkedList<File> directories = new LinkedList<File>(Arrays.asList(dir
				.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY)));

		Collections.sort(files);
		Collections.sort(directories);

		// Recursively add directories
		for (File directory : directories) {
			addNodes(curDir, directory);
		}
		// Add files
		for (File file : files) {
			if (file.getAbsolutePath().endsWith(".png")
					&& file.getAbsolutePath().contains("/bytecode/smali")) {
				/*
				 * Skipping generated PNG CFGs in bytecode folder. Other PNG
				 * files will be shown in the tree, eg, /res/drawable-hdpi This
				 * might not be necessary b/c no PNGs are currently not created
				 * at startup.
				 */
				continue;
			}
			curDir.add(new DefaultMutableTreeNode(new FileNode(file.getName(),
					file)));
		}
		return curDir;
	}

	// public Dimension getMinimumSize() {
	// return new Dimension(1000, 400);
	// }
	//
	// public Dimension getPreferredSize() {
	// return new Dimension(1000, 600);
	// }

	MouseAdapter ma = new MouseAdapter() {
		private void myPopupEvent(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			JTree tree = (JTree) e.getSource();
			TreePath path = tree.getPathForLocation(x, y);
			if (path == null)
				return;

			tree.setSelectionPath(path);

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
					.getLastPathComponent();
			Object nodeInfo = node.getUserObject();
			FileNode node_object = (FileNode) nodeInfo;
			lastClick = node_object;

			smali = node_object.getFile();
			File myDir = new File(smali.getParent());
			File[] cfgFiles = myDir.listFiles();

			if (smali.getAbsolutePath().endsWith(".smali")) {

				JPopupMenu popup = new JPopupMenu();

				JMenuItem item = new JMenuItem(MENU_ACTION_CFG);
				item.addActionListener(menuListener);
				popup.add(item);

				popup.addSeparator();

				// smali File
				item = new JMenuItem(smali.getName());
				item.addActionListener(menuListener);
				popup.add(item);

				// Java File (need decompilation first)
				String javaFileName = smali.getName().substring(0,
						smali.getName().length() - 6)
						+ ".java";
				File javaFile = new File(smali.getParentFile(), javaFileName);
				item = new JMenuItem(javaFileName);
				item.addActionListener(menuListener);
				popup.add(item);
				if (!javaFile.exists()) {
					item.setEnabled(false);
				}
				popup.addSeparator();

				for (int t = 0; t < cfgFiles.length; t++) {
					if (cfgFiles[t].getName().endsWith(".png")
							&& cfgFiles[t]
									.getName()
									.startsWith(
											node_object
													.getFile()
													.getName()
													.substring(
															0,
															node_object
																	.getFile()
																	.getName()
																	.length() - 6)
													+ "_")) {
						item = new JMenuItem(cfgFiles[t].getName());
						item.addActionListener(menuListener);
						popup.add(item);
					}
				}

				popup.show(tree, x, y);
			}
		}

		ActionListener menuListener = new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				if (event.getActionCommand().equals(MENU_ACTION_CFG)) {
					if (openAna != null) {
						try {
							openAna.showOrOpenNewFrame(OpenAnalysis.AppFrame.CFGS);
						} catch (Exception e) {
							logger.error(e);
						}
					}

				}

				else if (event.getActionCommand().endsWith(".png")) {

					File cfg = new File(lastClick.getFile().getParent()
							+ File.separator + event.getActionCommand());
					cfg.deleteOnExit();

					try {

						viewer.showFile(cfg);

					} catch (IOException e) {
						logger.error(e);
					}
				}

				else { // load the file and display it (normally .smali)
					try {
						File f = new File(lastClick.getFile().getParent()
								+ File.separator + event.getActionCommand());
						model.setCurrentFile(f);
					} catch (Exception e) {
						logger.error(e);
					}
				}
			}
		};

		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger())
				myPopupEvent(e);
		}

		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger())
				myPopupEvent(e);
		}
	};

	public Vector<Vector<String>> getHistory() {
		return this.history;
	}

	public LinkEditorKit getLinkEditorKit() {
		return this.linkEditorKit;
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		// if the selected file changed update the tree selection
		if ("currentFile".equals(arg0.getPropertyName())) {
			File f = (File) arg0.getNewValue();
		}
	}

}
