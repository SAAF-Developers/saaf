/**
 * 
 */
package de.rub.syssec.saaf.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.ScrollPane;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.StyleContext;

import org.apache.commons.io.FileUtils;

import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class EditorView extends JPanel {
	
	private static final long serialVersionUID = 8404271707246217439L;
	private final Vector<Vector<String>> history;
	
	private final class LineNumberUpdater implements DocumentListener {
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
	}

	private final class InternalKeyListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent keyEvent) {
			/*
			 * Navigation works as follows: ESC: If you're in the middle of
			 * a smali b/c you clicked a link, you will jump back.
			 * 
			 * Backspace: Deletes the history and you cannot return anymore
			 * with ESC.
			 * 
			 * TODO: This can greatly be improved :)
			 */
			if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
	
				if (history.size() >= 2) {
					history.removeElementAt(history.size() - 1);
					fileTree.searchNode(history.get(history.size() - 1).get(0),
							history.get(0).get(1));
				}
			} else if (keyEvent.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
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
	}

	private final static StyleContext STYLE_CONTEXT = new StyleContext();
	/** A cache for already parsed documents */
	private final static WeakHashMap<String, DefaultStyledDocument> DOCUMENT_MAP = new WeakHashMap<String, DefaultStyledDocument>();


	
	private JScrollPane editorScrollPane;
	private JTextPane editor;
	private JTextArea lines;
	private ApplicationInterface app;
	private LinkEditorKit linkEditorKit;
	private final FileTree fileTree;
	private OutlineView outlineTree;

	public EditorView(ApplicationInterface app, FileTree fileTree) {
		super();
		this.app = app;
		this.fileTree = fileTree;
		this.history = fileTree.getHistory();
		this.linkEditorKit = fileTree.getLinkEditorKit();
		
		//setBackground(Color.CYAN);
		setLayout(new GridBagLayout());
		
		lines = new JTextArea("1");
		lines.setBackground(Color.LIGHT_GRAY);
		lines.setEditable(false);
		
		editor = new JTextPane();
	
		editor.setFont(lines.getFont());
		editor.setEditorKit(linkEditorKit);
		editor.setEditable(false);
		// /Key Listener
		editor.addKeyListener(new InternalKeyListener());
		editor.getDocument().addDocumentListener(new LineNumberUpdater());
		
		editorScrollPane = new JScrollPane();
		editorScrollPane.getViewport().add(editor);
		editorScrollPane.setRowHeaderView(lines);
		editorScrollPane
				.setVerticalScrollBarPolicy(JScrollPane .VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		Dimension minimumSize = new Dimension(300, 300);
		editorScrollPane.setMinimumSize(minimumSize);

		GridBagConstraints editorConstraints = new GridBagConstraints();
		editorConstraints.anchor = GridBagConstraints.NORTHWEST;
		editorConstraints.fill = GridBagConstraints.BOTH;
		editorConstraints.gridx = 0;
		editorConstraints.gridy = 0;
		editorConstraints.weightx = 0.75;
		editorConstraints.weighty = 1.0;
		this.add(editorScrollPane,editorConstraints);
	}
	
	private int determinePosition(String[] content, int oldCursor, int line) {
		// visible lines on a single page
		int numVisibleLines = (int) Math.floor(editor.getVisibleRect()
				.getHeight()
				/ editor.getFontMetrics(editor.getFont()).getHeight());

		int numCharsOnFirstPage = 0;

		for (int i = 0; i < (numVisibleLines); i++) {
			// length of line
			numCharsOnFirstPage = numCharsOnFirstPage + content[i].length();
			// for the new line
			numCharsOnFirstPage = numCharsOnFirstPage + 1;// change to
															// lineseperator
															// size

		}
		numCharsOnFirstPage++;

		int c1 = 0;
		int c2 = 0;
		int n1 = line - 1;
		c1 = byteCount(content, n1);

		n1 = line - 1 + numVisibleLines - 1 - 1;
		c2 = byteCount(content, n1);

		if (c1 >= oldCursor)
			return c2 + 1;
		else
			return c1 + 1;
	}
	
	/**
	 * return the # of bytes from the begining of the array until line number
	 * line
	 * 
	 * @param content
	 * @param line
	 * @return
	 */
	private int byteCount(String[] content, int line) {
		int c1 = 0;
		if (line >= content.length)
			line = content.length - 2;

		for (int i = 0; i < (line); i++) {
			// line length
			c1 = c1 + content[i].length();
			// new line
			c1 = c1 + 1;
		}
		c1++;
		return c1;
	}
	
	/**
	 * Get a String with line numbers.
	 * 
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

	/**
	 * Constructs a styled document for a given file. Only smali files are
	 * styled.
	 * 
	 * @param file
	 *            the file to read and style
	 * @return the styled document
	 * @throws IOException
	 * @throws BadLocationException
	 */
	private DefaultStyledDocument loadDocument(File file) throws IOException,
			BadLocationException {
		// Already cached?
		DefaultStyledDocument doc = DOCUMENT_MAP.get(file.getAbsolutePath());
		if (doc == null) {
			doc = new DefaultStyledDocument(STYLE_CONTEXT);
		}
		if (file.getName().endsWith(".smali")) {
			ClassInterface smali = app.getSmaliClass(file.getAbsoluteFile());
			StringBuilder builder = new StringBuilder();
			String separator = System.getProperty("line.separator");
			for (CodeLineInterface cl : smali.getAllCodeLines()) {
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

	public void open(File file) throws IOException, BadLocationException {
		DefaultStyledDocument doc = loadDocument(file);
		editor.setDocument(doc);
		lines.setText(getNumberedLine());
		editor.setCaretPosition(0);
		
		if (file.getName().endsWith("smali")) {
		ClassInterface smaliFile = app.getSmaliClass(file);
		this.outlineTree = new OutlineView(smaliFile, this);
		GridBagConstraints treeConstraints = new GridBagConstraints();
		treeConstraints.anchor = GridBagConstraints.NORTHWEST;
		treeConstraints.fill = GridBagConstraints.BOTH;
		treeConstraints.gridx = 1;
		treeConstraints.gridy = 0;
		treeConstraints.weightx = 0.25;
		treeConstraints.weighty = 1.0;
		this.add(new JScrollPane(outlineTree),treeConstraints);
		this.revalidate();
	}
	}

	public void goToLine(int lineNr) throws BadLocationException {
		String[] content = editor.getDocument()
				.getText(0, editor.getDocument().getLength())
				.split("\n");

		int oldCursor = editor.getCaretPosition();
		editor.setCaretPosition(determinePosition(content,
				oldCursor, lineNr));
		
	}
}
