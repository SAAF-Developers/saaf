/**
 * 
 */
package de.rub.syssec.saaf.gui.editor;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.rub.syssec.saaf.gui.MainWindow;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.MethodInterface;

/**
 * Provides a tree of the methods of a class.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class OutlineView extends JPanel {

	private static final long serialVersionUID = -1744983593887093007L;

	class MethodNode {
		private MethodInterface method;
		private String methodName;
		private CodeLineInterface cl;

		public MethodNode(MethodInterface method, String name,
				CodeLineInterface cl) {
			this.method = method;
			methodName = name;
			this.cl = cl;
		}

		public int getLine() {
			return cl.getLineNr();
		}

		public MethodInterface getMethod() {
			return method;
		}

		// this is shown in the tree
		public String toString() {
			if (method == null)
				return methodName;
			return method.getName() + " (" + method.getParameterString() + ")"
					+ method.getReturnValueString();
		}
	}

	private final class InternalMouseAdapter extends MouseAdapter {
		ActionListener menuListener = new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				if (event.getActionCommand().startsWith("graph ")) {

					DefaultMutableTreeNode node = (DefaultMutableTreeNode) ((JTree) event
							.getSource()).getSelectionPath()
							.getLastPathComponent();
					Object nodeInfo = node.getUserObject();
					MethodNode node_object = (MethodNode) nodeInfo;

					MethodViewer methodViewer = new MethodViewer(
							node_object.getMethod());
					MainWindow.getDesktopPane().add(methodViewer);
					try {
						methodViewer.setSelected(true);
					} catch (java.beans.PropertyVetoException e) {
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

			// smali = node_object.methodName;
			MethodInterface m = node_object.getMethod();

			JPopupMenu popup = new JPopupMenu();

			JMenuItem item = new JMenuItem("graph " + m);
			item.addActionListener(menuListener);
			popup.add(item);

			popup.show(tree, x, y);
		}
	}

	private final class SelectionListener implements TreeSelectionListener {
		private final EditorView editor;

		private SelectionListener(EditorView editor2) {
			this.editor = editor2;
		}

		public void valueChanged(TreeSelectionEvent e) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath()
					.getLastPathComponent();

			Object nodeInfo = node.getUserObject();
			if (node.isLeaf()) {
				MethodNode nodeObject;
				try {
					nodeObject = (MethodNode) nodeInfo;
				} catch (ClassCastException cce) {
					// No file in this node
					// (empty directory)
					return;
				}

				try {
					editor.goToLine(nodeObject.getLine());
				} catch (Exception e1) {
					// logger.warn(
					// "Problem during tree construction",
					// e1);
				}
			}
		}
	}

	JTree outline;
	private EditorView editor;

	public OutlineView(ClassInterface smaliFile, final EditorView editor) {
		super();
		//setBackground(Color.GREEN);
		this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		// children
		DefaultMutableTreeNode file = new DefaultMutableTreeNode(
				new MethodNode(null, smaliFile.getClassName(), smaliFile
						.getAllCodeLines().getFirst()));
		for (MethodInterface m : smaliFile.getMethods()) {
			file.add(new DefaultMutableTreeNode(new MethodNode(m, m.getName()
					+ " (" + m.getParameterString() + ")"
					+ m.getReturnValueString(), m.getCodeLines().getFirst())));
		}
		this.outline = new JTree(file);
		this.outline.addMouseListener(new InternalMouseAdapter());
		this.outline.addTreeSelectionListener(new SelectionListener(editor));
		JScrollPane pane = new JScrollPane(outline);
		this.add(pane);
	}
	
	public OutlineView(final EditorView editor){
		super();
		this.editor = editor;
		setBackground(Color.GREEN);
		this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
	}

}
