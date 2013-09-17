/**
 * 
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

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
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
public class OutlineView extends JPanel implements PropertyChangeListener {

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
		private final EditorModel editor;

		private SelectionListener(EditorModel editor2) {
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
					editor.setCurrentLine(nodeObject.getLine());
				} catch (Exception e1) {
					// logger.warn(
					// "Problem during tree construction",
					// e1);
				}
			}
		}
	}

	private class MethodNameCellRenderer extends DefaultTreeCellRenderer {
	
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
				MethodNode m = (MethodNode) ((DefaultMutableTreeNode)arg1).getUserObject();
				MethodInterface method = m.getMethod();
				// check if its obfuscated
				if (method != null && method.isObfuscated()) {
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

	JTree outline;

	public OutlineView(final EditorModel model) {
		super();
		setBackground(Color.GREEN);
		this.setLayout(new GridBagLayout());
		
			
		this.outline = new JTree();
		this.outline.setCellRenderer(new MethodNameCellRenderer());
		ClassInterface smaliClass = model.getCurrentClass();
		if(smaliClass!=null)
		{
			this.outline.setModel(new DefaultTreeModel(buildTree(smaliClass)));
		}else{
			this.setVisible(false);
		}
		
		this.outline.addMouseListener(new InternalMouseAdapter());
		this.outline.addTreeSelectionListener(new SelectionListener(model));
		JScrollPane pane = new JScrollPane(outline);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		this.add(pane,constraints);
	}

	/**
	 * @param smaliClass
	 * @return
	 */
	private DefaultMutableTreeNode buildTree(ClassInterface smaliClass) {
		// children
		DefaultMutableTreeNode file = new DefaultMutableTreeNode(
				new MethodNode(null, smaliClass.getClassName(), smaliClass
						.getAllCodeLines().getFirst()));

		for (MethodInterface m : smaliClass.getMethods()) {
			file.add(new DefaultMutableTreeNode(new MethodNode(m, m.getName()
					+ " (" + m.getParameterString() + ")"
					+ m.getReturnValueString(), m.getCodeLines().getFirst())));
		}
		return file;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// if we are looking at a new class the method tree must be updated
		if ("currentClass".equals(evt.getPropertyName())) {
			if (evt.getNewValue() != null) {
				ClassInterface c = (ClassInterface) evt.getNewValue();
				TreeNode t = this.buildTree(c);
				this.outline.setModel(new DefaultTreeModel(t));
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						setVisible(true);
						getParent().repaint();
					}
				});
			}else
			{
				//we are not editing a class but something else (manifest,resource etc.)
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						//hide the panel and repaint the parent.
						setVisible(false);
						getParent().repaint();					
					}
				});

			}
		}
	}
}
