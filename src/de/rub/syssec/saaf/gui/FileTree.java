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
package de.rub.syssec.saaf.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.StyleContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.log4j.Logger;

import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.MethodInterface;

/**
 * Display a file system in a JTree view (extended with lots of SAAF stuff).
 * 
 * <p>
 * Window to browse in the Android-App, viewing smali and java code. 
 * Called by {@link OpenApp#showOrOpenNewFrame}.
 * </p>
 * 
 * This class is based on FileTree.java from Ian Darwin.
 *
 * - Martin Ussath / 12.2011: Initial version.
 * - Johannes Hoffmann / 01.2012: Refactored and some improvements.
 *
 * 
 * @version $Id: FileTree.java,v 1.9 2004/02/23 03:39:22 ian Exp $
 * @author Ian Darwin
 * @see OpenApp
 *
 */
public class FileTree extends JPanel {
	
	private static final String MENU_ACTION_CFG = "Generate CFGs";

	private static final long serialVersionUID = -560054884407736589L;

	private File directory;
	private File smali;
	public FileNode lastClick;
	private JTree jTree;

	private final JTextPane editor;
	private final JTextArea lines;
	private final JScrollPane editorScrollPane;

	private JTree outlineTree;
	private JSplitPane outerSplitPane;

	private final static StyleContext STYLE_CONTEXT = new StyleContext();
	/** A cache for already parsed documents */
	private final static WeakHashMap<String, DefaultStyledDocument> DOCUMENT_MAP = new WeakHashMap<String, DefaultStyledDocument>();
	
	private final Vector<Vector<String>> history = new Vector<Vector<String>>();
	private final LinkEditorKit linkEditorKit;

	private final ApplicationInterface app;
	private OpenAnalysis openAna;
	private final Logger logger = Logger.getLogger(FileTree.class);
	
	private final ViewerStarter viewer = new ViewerStarter(ConfigKeys.VIEWER_IMAGES);
	
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
	
	private class MethodNode {
		private MethodInterface method;
		private String methodName;
		private CodeLineInterface cl;

		public MethodNode(MethodInterface method, String name, CodeLineInterface cl) {
			this.method = method;
			methodName = name;
			this.cl = cl;
		}

		// this is shown in the tree
		public String toString() {
			if (method == null)
				return methodName;
			return method.getName()+" ("+method.getParameterString()+")"+method.getReturnValueString();
		}

		public int getLine() {
			return cl.getLineNr();
		}
		
		public MethodInterface getMethod(){
			return method;
		}
	}
	
	
	protected FileTree(final ApplicationInterface app, File dir, OpenAnalysis open) {
		super(new GridLayout(1, 0));
		this.openAna = open;
		linkEditorKit = new LinkEditorKit(history, app.getUnpackedDataDir(), this);
		directory = dir;
		JTree tree = new JTree(addNodes(null, dir));
		
		jTree = tree;
		
		outlineTree = new JTree();
		outlineTree.addTreeSelectionListener(new TreeSelectionListener() {
			
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) e
						.getPath().getLastPathComponent();

				Object nodeInfo = node.getUserObject();
				if (node.isLeaf()) {
					FileNode nodeObject; 
					try {
						nodeObject = (FileNode) nodeInfo;
					}
					catch (ClassCastException cce) {
						// No file in this node (empty directory)
						return;
					}
					
					linkEditorKit.setLastClickedFile(nodeObject.getFile());

					try {
						// Load text from file
						DefaultStyledDocument doc = loadDocument(nodeObject.getFile());						
						editor.setDocument(doc);
						lines.setText(getNumberedLine());
						editor.setCaretPosition(0);
						if (nodeObject.getFile().getName().endsWith("smali")) {
							smali = nodeObject.getFile();
						}
					} catch (Exception e1) {
						logger.warn("Problem during tree construction", e1);
					}
				}
			}
		});
		
		
		tree.addMouseListener(ma);
		setLayout(new BorderLayout());
		this.app = app;

		// Add a listener
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) e
						.getPath().getLastPathComponent();

				if (node == null) { // at startup this will show the manifest
					try {
						editor.setText(FileUtils.readFileToString(app.getManifestFile()));
					} catch (IOException e1) {
						logger.warn("Problem during tree construction", e1);
					}
					return;
				}

				Object nodeInfo = node.getUserObject();
				if (node.isLeaf()) {
					FileNode nodeObject; 
					try {
						nodeObject = (FileNode) nodeInfo;
					}
					catch (ClassCastException cce) {
						// No file in this node (empty directory)
						return;
					}
					
					linkEditorKit.setLastClickedFile(nodeObject.getFile());

					try {
						// Load text from file
						DefaultStyledDocument doc = loadDocument(nodeObject.getFile());						
						editor.setDocument(doc);
						lines.setText(getNumberedLine());
						editor.setCaretPosition(0);

						//TODO: improve updating of the MethodTree and just remove it, if a non smali file is selected (now updating is done via removing and adding of the tree)
						outerSplitPane.remove(outlineTree);

						if (nodeObject.getFile().getName().endsWith("smali")) {
							smali = nodeObject.getFile();
							//root element
							ClassInterface smaliFile = app.getSmaliClass(smali);

							//children
							DefaultMutableTreeNode file = new DefaultMutableTreeNode( new MethodNode(null, smaliFile.getClassName(),smaliFile.getAllCodeLines().getFirst()));
							for( MethodInterface m : smaliFile.getMethods()){
								 file.add(new DefaultMutableTreeNode(new MethodNode(m, m.getName()+" ("+m.getParameterString()+")"+m.getReturnValueString() ,m.getCodeLines().getFirst())));
							}
							 
							outlineTree = new JTree(file);
							outlineTree.addMouseListener(new MouseAdapter(){
								
								public void mousePressed(MouseEvent e) {
									if (e.isPopupTrigger())
										myPopupEvent(e);
								}

								public void mouseReleased(MouseEvent e) {
									if (e.isPopupTrigger())
										myPopupEvent(e);
								}
								
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
									MethodNode node_object = (MethodNode) nodeInfo;
									

									//smali = node_object.methodName;
									MethodInterface m = node_object.getMethod();

										JPopupMenu popup = new JPopupMenu();
										
										JMenuItem item = new JMenuItem("graph "+m);
										item.addActionListener(menuListener);
										popup.add(item);

										

										popup.show(tree, x, y);
									}
								

				
								ActionListener menuListener = new ActionListener() {
									public void actionPerformed(ActionEvent event) {
								
								if (event.getActionCommand().startsWith("graph ")) {

									DefaultMutableTreeNode node = (DefaultMutableTreeNode) outlineTree.getSelectionPath()
											.getLastPathComponent();
									Object nodeInfo = node.getUserObject();
									MethodNode node_object = (MethodNode) nodeInfo;
									
									new MethodViewer(node_object.getMethod());
								}
									}
								};
								
								
							});
							outlineTree.addTreeSelectionListener(new TreeSelectionListener() {
								
								public void valueChanged(TreeSelectionEvent e) {
									DefaultMutableTreeNode node = (DefaultMutableTreeNode) e
											.getPath().getLastPathComponent();

									Object nodeInfo = node.getUserObject();
									if (node.isLeaf()) {
										MethodNode nodeObject; 
										try {
											nodeObject = (MethodNode) nodeInfo;
										}
										catch (ClassCastException cce) {
											// No file in this node (empty directory)
											return;
										}

										try {

											String[] content = editor.getDocument()
													.getText(0, editor.getDocument().getLength())
													.split("\n");


											int oldCursor = editor.getCaretPosition();
											editor.setCaretPosition(determinePosition(content, oldCursor, nodeObject.getLine()));
											
										} catch (Exception e1) {
											logger.warn("Problem during tree construction", e1);
										}
									}
								}

							});

							outerSplitPane.setRightComponent(outlineTree);
							outerSplitPane.setDividerLocation(750);
						}
					} catch (Exception e1) {
						logger.warn("Problem during tree construction", e1);
					}
				}
			}
		});

		// Lastly, put the JTree into a JScrollPane.
		JScrollPane treeScrollPane = new JScrollPane();
		treeScrollPane.getViewport().add(tree);

		editorScrollPane = new JScrollPane();
		
		editor = new JTextPane() {
			private static final long serialVersionUID = -2940577917898370537L;

			public void setSize(Dimension d) {
				if (d.width < getParent().getSize().width)
					d.width = getParent().getSize().width;
				super.setSize(d);
			}

			public boolean getScrollableTracksViewportWidth() {
				return false;
			}
		};
		
		lines = new JTextArea("1");
		
		editor.setFont(lines.getFont());

		lines.setBackground(Color.LIGHT_GRAY);
		lines.setEditable(false);

		editor.setEditorKit(linkEditorKit);
		editor.setEditable(false);

		// /Key Listener
		KeyListener keyListener = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent keyEvent) {
				/*
				 * Navigation works as follows:
				 * ESC: If you're in the middle of a smali b/c you clicked a link,
				 * you will jump back.
				 * 
				 * Backspace: Deletes the history and you cannot return anymore
				 * with ESC.
				 * 
				 * TODO: This can greatly be improved :)
				 * 
				 */
				if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {

					if (history.size() >= 2) {
						history.removeElementAt(history.size() - 1);
						searchNode(history.get(history.size() - 1).get(0), history.get(0).get(1));
					}
				}
				else if (keyEvent.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
					history.clear();
				}
			}

			@Override
			public void keyReleased(KeyEvent keyEvent) {
				// nothing
			}

			@Override
			public void keyTyped(KeyEvent keyEvent) {
				// nothing
			}
		};

		editor.addKeyListener(keyListener);

		editor.getDocument().addDocumentListener(new DocumentListener() {
			
			public String getText() {
				return getNumberedLine();
			}

			@Override
			public void changedUpdate(DocumentEvent de) {
				lines.setText(getText());
			}

			@Override
			public void insertUpdate(DocumentEvent de) {
				lines.setText(getText());
			}

			@Override
			public void removeUpdate(DocumentEvent de) {
				lines.setText(getText());
			}

		});

		editorScrollPane.getViewport().add(editor);
		editorScrollPane.setRowHeaderView(lines);
		editorScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		//Add the scroll panes to a split pane.
		JSplitPane innersplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		innersplitPane.setLeftComponent(treeScrollPane);

		innersplitPane.setRightComponent(editorScrollPane);

		Dimension minimumSize = new Dimension(300, 300);
		editorScrollPane.setMinimumSize(minimumSize);
		treeScrollPane.setMinimumSize(minimumSize);
		innersplitPane.setDividerLocation(300);
		innersplitPane.setPreferredSize(new Dimension(750, 300));
		
		outerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		outerSplitPane.setPreferredSize(new Dimension(800, 300));
		
		outerSplitPane.setLeftComponent(innersplitPane);
		outerSplitPane.setDividerLocation(750);

		// Add the split pane to this panel.
		add(outerSplitPane);

		try {
			editor.setText(FileUtils.readFileToString(new File(directory
					.getAbsolutePath() + File.separator + "AndroidManifest.xml")));
			editor.setCaretPosition(0);
		} catch (IOException e1) {
			logger.warn("Problem building file tree", e1);
		}

	}
	
	/**
	 * return the # of bytes from the begining of the array until line number line
	 * @param content
	 * @param line
	 * @return
	 */
	private int byteCount(String[] content, int line) {
		int c1 = 0;
		if ( line >= content.length)
			line = content.length-2;

		for (int i = 0; i < (line); i++) {
			//line length
			c1 = c1 + content[i].length();
			//new line
			c1 = c1 + 1;
		}
		c1++;
		return c1;
	}
	
	private int determinePosition(String[] content, int oldCursor, int line) {
		//visible lines on a single page
		int numVisibleLines = (int)Math.floor(editor.getVisibleRect().getHeight() / editor.getFontMetrics(editor.getFont()).getHeight());

		int numCharsOnFirstPage = 0;
		
		for (int i = 0; i < (numVisibleLines); i++) {
			//length of line
			numCharsOnFirstPage = numCharsOnFirstPage + content[i].length();
			//for the new line
			numCharsOnFirstPage = numCharsOnFirstPage + 1;//change to lineseperator size

		}
		numCharsOnFirstPage++;

		int c1=0;
		int c2=0;
		int n1 = line-1;
		c1 = byteCount(content, n1);

		n1 = line-1 + numVisibleLines - 1     -1;
		c2 = byteCount(content, n1);

		if(c1>=oldCursor )
			return c2+1;
		else
			return c1+1;
	}
	
	public DefaultMutableTreeNode searchNode(String nodeStr, String lineNr) {
		
		DefaultMutableTreeNode node = null;
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> e = ((DefaultMutableTreeNode) jTree.getModel().getRoot())
				.breadthFirstEnumeration();
		
		while (e.hasMoreElements()) {

			node = (DefaultMutableTreeNode) e.nextElement();
			String filepath = "";

			for (int i = 0; i < node.getPath().length; i++) {
				filepath = filepath + File.separator + node.getPath()[i];
			}
			if (filepath.startsWith(nodeStr)) {
				TreeNode[] nodes = node.getPath();
				TreePath path = new TreePath(nodes);
				jTree.scrollPathToVisible(path);
				jTree.setSelectionPath(path);
				if (lineNr != null) {
					try {
						String[] content = editor.getDocument()
								.getText(0, editor.getDocument().getLength())
								.split("\n");

						int oldCursor = editor.getCaretPosition();
						editor.setCaretPosition(determinePosition(content, oldCursor, Integer.parseInt(lineNr)));	
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
		IOFileFilter fileFilter = new NotFileFilter(new SuffixFileFilter(new String[] { ".class", ".java", ".DS_Store" }));
		
		LinkedList<File> files = new LinkedList<File>(FileUtils.listFiles(dir, fileFilter, null));
		// FIXME: How the hell can directories be listed?!
//		LinkedList<File> directories = new LinkedList<File>(FileUtils.listFiles(dir, FalseFileFilter.INSTANCE, DirectoryFileFilter.INSTANCE));
		LinkedList<File> directories = new LinkedList<File>(Arrays.asList(dir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY)));
		
		Collections.sort(files);
		Collections.sort(directories);
		
		// Recursively add directories
		for (File directory : directories) {
			addNodes(curDir, directory);
		}
		// Add files
		for (File file : files) {
			if (file.getAbsolutePath().endsWith(".png") &&
				file.getAbsolutePath().contains("/bytecode/smali")) {
				/* 
				 * Skipping generated PNG CFGs in bytecode folder.
				 * Other PNG files will be shown in the tree, eg, /res/drawable-hdpi
				 * This might not be necessary b/c no PNGs are currently not
				 * created at startup.
				 */
				continue;
			}
			curDir.add(new DefaultMutableTreeNode(new FileNode(file.getName(), file)));
		}
		return curDir;
	}
	

	public Dimension getMinimumSize() {
		return new Dimension(1000, 400);
	}

	public Dimension getPreferredSize() {
		return new Dimension(1000, 600);
	}


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
							&& cfgFiles[t].getName().startsWith(
									node_object.getFile().getName().substring(0,
									node_object.getFile().getName().length() - 6) + "_")) {
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
				
				if(event.getActionCommand().equals(MENU_ACTION_CFG)) {
					if(openAna != null){
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
						File f = new File(
								lastClick.getFile().getParent()
								+ File.separator
								+ event.getActionCommand());
						DefaultStyledDocument doc = loadDocument(f);
						editor.setDocument(doc);
						editor.setCaretPosition(0);
						lines.setText(getNumberedLine());
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
	
	/**
	 * Get a String with line numbers.
	 * @return
	 */
	private String getNumberedLine() {
		int caretPosition = editor.getDocument().getLength();
		Element root = editor.getDocument().getDefaultRootElement();
		StringBuilder sb = new StringBuilder();
		String lineSep = System.getProperty("line.separator");
		sb.append(1);
		sb.append(lineSep);
		for (int i = 2; i < root.getElementIndex(caretPosition) + 2; i++) {
			sb.append(i);
			sb.append(lineSep);
		}
		return sb.toString();
	}


	protected ClassInterface getSelectedSmaliClass() {
		return app.getSmaliClass(smali);
	}

	/**
	 * Constructs a styled document for a given file. Only smali files are styled.
	 * @param file the file to read and style
	 * @return the styled document
	 * @throws IOException
	 * @throws BadLocationException
	 */
	private DefaultStyledDocument loadDocument(File file) throws IOException, BadLocationException {
		// Already cached?
		DefaultStyledDocument doc = DOCUMENT_MAP.get(file.getAbsolutePath());
		if (doc == null) {
			doc = new DefaultStyledDocument(STYLE_CONTEXT);
		}
		if (file.getName().endsWith(".smali")) {
			ClassInterface smali = app.getSmaliClass(file.getAbsoluteFile());
			StringBuilder builder = new StringBuilder();
			String separator = System.getProperty("line.separator");
			for(CodeLineInterface cl : smali.getAllCodeLines()){
				builder.append(new String(cl.getLine()));
				builder.append(separator);
			}
			
			doc.insertString(0, builder.toString(), null);
			
			SmaliTextStyler ts = new SmaliTextStyler();
			ts.highlightStrings(app, doc, builder.toString());

		} else {

		
		/*
		 * TODO: Some files, such as images, cannot properly be displayed.
		 * Maybe open an external program or display them in a hex-editor?
		 */
		
		String fileAsString = FileUtils.readFileToString(file); 
		doc.insertString(0, fileAsString, null);
		}

		return doc;
	}
}
