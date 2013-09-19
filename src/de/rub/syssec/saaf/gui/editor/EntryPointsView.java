package de.rub.syssec.saaf.gui.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jf.smali.smaliParser.super_spec_return;

import de.rub.syssec.saaf.gui.MainWindow;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.Obfuscatable;
import de.rub.syssec.saaf.model.application.manifest.ActivityInterface;
import de.rub.syssec.saaf.model.application.manifest.ComponentInterface;
import de.rub.syssec.saaf.model.application.manifest.ReceiverInterface;
import de.rub.syssec.saaf.model.application.manifest.ServiceInterface;

/**
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class EntryPointsView extends JPanel implements PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1085852368775999038L;

	ImageIcon activityIcon = new ImageIcon("images/activity.png");
	ImageIcon serviceIcon = new ImageIcon("images/service.png");
	ImageIcon receiverIcon = new ImageIcon("images/receiver.png");

	// renders the components (activity, receiver, service) with a different
	// icon
	private final class ComponentCellRenderer extends DefaultListCellRenderer
			implements ListCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6849033434303757425L;
		private ApplicationInterface app;

		public ComponentCellRenderer(ApplicationInterface app) {
			super();
			this.app = app;
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel guiComponent = (JLabel) super.getListCellRendererComponent(
					list, value, index, isSelected, cellHasFocus);

			de.rub.syssec.saaf.application.manifest.components.Component manifestComponent = (de.rub.syssec.saaf.application.manifest.components.Component) value;
			try {
				Obfuscatable o = app.getSmaliClass(manifestComponent);
				if (o != null && o.isObfuscated()) {
					guiComponent.setForeground(Color.RED);
				} else {
					guiComponent.setForeground(Color.BLACK);
				}
				if (value instanceof ActivityInterface) {
					guiComponent.setIcon(activityIcon);
				} else if (value instanceof ReceiverInterface) {
					guiComponent.setIcon(receiverIcon);
				} else if (value instanceof ServiceInterface) {
					guiComponent.setIcon(serviceIcon);
				}
			} catch (Exception e) {
				//now this is bad form :P
				e.printStackTrace();
			}
			guiComponent.setText(manifestComponent.getName());
			return guiComponent;
		}
	}

	private final class AdjustEditorContent implements ListSelectionListener {

		private ApplicationInterface app;

		public AdjustEditorContent(ApplicationInterface app) {
			super();
			this.app = app;
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			ListSelectionModel lsm = (ListSelectionModel) e.getSource();
			ComponentInterface component = (ComponentInterface) list.getModel()
					.getElementAt(lsm.getMinSelectionIndex());
			ClassInterface c = model.getCurrentApplication().getSmaliClass(component);
			model.setCurrentFile(c.getFile());

		}
	}

	private final class ComponentListModel extends AbstractListModel {

		private static final long serialVersionUID = 5220412137135665100L;
		private final ApplicationInterface app;
		List<ComponentInterface> components;

		private ComponentListModel(ApplicationInterface app) {
			this.app = app;
			components = app.getManifest().getComponents();
		}

		public int getSize() {
			return components.size();
		}

		public Object getElementAt(int index) {
			return components.get(index);
		}
	}

	private ComponentListModel listModel;
	private JList list;

	private EditorModel model;

	/**
	 * Create the panel.
	 */
	public EntryPointsView(EditorModel model) {
		this.model = model;
		setLayout(new GridBagLayout());
		ApplicationInterface app = model.getCurrentApplication();
		if (app.getManifest() != null
				&& app.getManifest().getComponents() != null) {
			this.list = new JList();
			this.listModel = new ComponentListModel(app);
			this.list.setModel(listModel);
			this.list.getSelectionModel().addListSelectionListener(
					new AdjustEditorContent(app));
			this.list.setCellRenderer(new ComponentCellRenderer(app));
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.anchor = GridBagConstraints.FIRST_LINE_START;
			constraints.fill = GridBagConstraints.BOTH;
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;
			add(list, constraints);
		} else {
			this.add(new JLabel(
					"Could not retrieve components from applications manifest."));
			MainWindow.showErrorDialog(
					"There was a problem retrieving the applicaiton entrypoints.\n"
							+ "Maybe the manifest is malfomed.\n"
							+ "Entrypoints will not be listed.",
					"Cannot display components");
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// if the current class is in our list adjust the selection
	}

}
