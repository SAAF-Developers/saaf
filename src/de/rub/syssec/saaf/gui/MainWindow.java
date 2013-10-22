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

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu.Separator;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.Main;
import de.rub.syssec.saaf.analysis.Analysis;
import de.rub.syssec.saaf.gui.actions.AboutAction;
import de.rub.syssec.saaf.gui.actions.CloseAnalysisAction;
import de.rub.syssec.saaf.gui.actions.DecompileToJavaAction;
import de.rub.syssec.saaf.gui.actions.DetectObfuscationAction;
import de.rub.syssec.saaf.gui.actions.DoAnalysisAction;
import de.rub.syssec.saaf.gui.actions.FoundAPICallsAction;
import de.rub.syssec.saaf.gui.actions.GenerateCFGsAction;
import de.rub.syssec.saaf.gui.actions.OpenAPKAction;
import de.rub.syssec.saaf.gui.actions.QuitAction;
import de.rub.syssec.saaf.gui.actions.SearchBytecodeAction;
import de.rub.syssec.saaf.gui.actions.SearchStringsAction;
import de.rub.syssec.saaf.gui.actions.ShowLogAction;
import de.rub.syssec.saaf.gui.actions.ShowReportAction;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;

public class MainWindow extends JFrame implements ActionListener {

	private static final long serialVersionUID = -7146327106175691998L;
	public static final Logger logger = Logger.getLogger(MainWindow.class);
	// private static final Logger LOGGER = Config.getLogger();
	private static MainWindow self;

	public ROA roaList = new ROA();

	/**
	 * The desktop pane is used to add frames to it
	 */
	private final JDesktopPane desktopPane;
	private final OpenAppsMgr openAppsMgr;

	public MainWindow() {
		super(Main.title + ": Static Android Analysis Framework");
		self = this;
		openAppsMgr = new OpenAppsMgr();
		Config.getInstance().setBooleanConfigValue(ConfigKeys.ANALYSIS_GENERATE_REPORT, true);
		try {
			// Load recently used application, will be used in the menu
			roaList.loadList();
		} catch (IOException e) {
			logger.error("Could not load recently used apk list: "
					+ e.getMessage());
		}

		// Make the big window be indented 35 pixels from each edge
		// of the screen.
		int inset = 35;
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		setBounds(inset, inset, ge.getDefaultScreenDevice().getDisplayMode()
				.getWidth()
				- (inset * 2), ge.getDefaultScreenDevice().getDisplayMode()
				.getHeight()
				- (inset * 2));

		// Set up the GUI.
		// TODO: Add some smart internal frame placement code:
		// http://www.java2s.com/Code/Java/Swing-JFC/InterestingthingsusingJInternalFramesJDesktopPaneandDesktopManager2.htm
		desktopPane = new JDesktopPane(); // a specialized layered pane
		// createFrame(); //create first "window"
		setContentPane(desktopPane);
		setJMenuBar(createMenuBar());
		desktopPane.setBackground(Color.gray);

		// Make dragging a little faster but perhaps uglier: will only move the
		// borders of the frame
		// desktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		
		setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);


	}

	private static final String CMD_SHOW_LOGS = "de.rub.syssec.saaf.cmd.check.show.logs";
	private static final String CMD_CLEAR_ROA = "de.rub.syssec.saaf.cmd.clear.recently.openend.apks";

	protected JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);

		JMenuItem menuItem = new JMenuItem(new OpenAPKAction("Open APK",
				openAppsMgr, this, true));
		menuItem.setMnemonic(KeyEvent.VK_O);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.ALT_MASK));
		// menuItem.setActionCommand(CMD_OPEN);
		// menuItem.addActionListener(this);
		fileMenu.add(menuItem);

		JMenu roa = new JMenu("Recently opened APKs");
		// menuItem.setMnemonic(KeyEvent.VK_R);
		// menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
		// ActionEvent.ALT_MASK));
		fileMenu.add(roa);
		boolean isListEmtpy = true;
		for (String apk : roaList.getRoaList()) {
			menuItem = new JMenuItem(new OpenAPKAction(apk, openAppsMgr, this,
					false));
			roa.add(menuItem);
			// menuItem.setActionCommand(CMD_ROA_APK+apk);
			// menuItem.addActionListener(this);
			isListEmtpy = false;
		}
		if (isListEmtpy) {
			menuItem = new JMenuItem("Emtpy");
			menuItem.setEnabled(false);
			roa.add(menuItem);
		}
		roa.addSeparator();
		menuItem = new JMenuItem("Clear");
		menuItem.setActionCommand(CMD_CLEAR_ROA);
		menuItem.addActionListener(this);
		if (isListEmtpy)
			menuItem.setEnabled(false);
		roa.add(menuItem);

		Separator sep1 = new Separator();
		fileMenu.add(sep1);

		menuItem = new JMenuItem(new CloseAnalysisAction("Close APK",
				openAppsMgr, this));
		menuItem.setMnemonic(KeyEvent.VK_C);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				ActionEvent.ALT_MASK));
		// menuItem.setActionCommand(CMD_CLOSE_APP);
		// menuItem.addActionListener(this);
		fileMenu.add(menuItem);

		Separator sep2 = new Separator();
		fileMenu.add(sep2);

		menuItem = new JMenuItem(new ShowLogAction("Show Logs",this));
		menuItem.setMnemonic(KeyEvent.VK_L);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				ActionEvent.ALT_MASK));
//		menuItem.setActionCommand(CMD_SHOW_LOGS);
//		menuItem.addActionListener(this);
		fileMenu.add(menuItem);

		Separator sep3 = new Separator();
		fileMenu.add(sep3);

		menuItem = new JMenuItem(new QuitAction("Exit", this));
		menuItem.setMnemonic(KeyEvent.VK_X);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				ActionEvent.ALT_MASK));
		// menuItem.setActionCommand(CMD_EXIT);
		// menuItem.addActionListener(this);
		fileMenu.add(menuItem);

		//

		JMenu analysisMenu = new JMenu("Analysis");
		analysisMenu.setMnemonic(KeyEvent.VK_A);
		menuBar.add(analysisMenu);

		menuItem = new JMenuItem(new DoAnalysisAction(
				"Perform Analysis", this));
		menuItem.setMnemonic(KeyEvent.VK_P);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				ActionEvent.ALT_MASK));
		// menuItem.setActionCommand(CMD_PROGRAM_SLICING);
		// menuItem.addActionListener(this);
		analysisMenu.add(menuItem);

		Separator sep4 = new Separator();
		analysisMenu.add(sep4);

		menuItem = new JMenuItem(new ShowReportAction("Show Report", this));
		menuItem.setMnemonic(KeyEvent.VK_R);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				ActionEvent.ALT_MASK));
		// menuItem.setActionCommand(CMD_SHOW_REPORT);
		// menuItem.addActionListener(this);
		analysisMenu.add(menuItem);


		JMenu miscMenu = new JMenu("Misc");
		miscMenu.setMnemonic(KeyEvent.VK_M);
		menuBar.add(miscMenu);

		menuItem = new JMenuItem(new SearchBytecodeAction("Search Bytecode...",
				openAppsMgr, this));
		menuItem.setMnemonic(KeyEvent.VK_B);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
				ActionEvent.ALT_MASK));
		// menuItem.setActionCommand(CMD_SEARCH_BYTECODE);
		// menuItem.addActionListener(this);
		miscMenu.add(menuItem);

		menuItem = new JMenuItem(new SearchStringsAction("Show Strings...",
				openAppsMgr, this));
		menuItem.setMnemonic(KeyEvent.VK_S);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.ALT_MASK));
		// menuItem.setActionCommand(CMD_FIND_STRINGS);
		// menuItem.addActionListener(this);
		miscMenu.add(menuItem);
		
		menuItem = new JMenuItem(new FoundAPICallsAction("Show APIcalls...",
				openAppsMgr, this));
//		menuItem.setMnemonic(KeyEvent.VK_S);
//		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
//				ActionEvent.ALT_MASK));
		miscMenu.add(menuItem);
		
		menuItem = new JMenuItem(new DetectObfuscationAction("Check for obfuscation",this,openAppsMgr));
		miscMenu.add(menuItem);
		
		Separator sep5 = new Separator();
		miscMenu.add(sep5);

		menuItem = new JMenuItem(new GenerateCFGsAction("Generate all CFGs", this));
		// menuItem.setMnemonic(KeyEvent.VK_C);
		// menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
		// ActionEvent.ALT_MASK));
		// menuItem.setActionCommand(CMD_GENERATE_CFGS);
		// menuItem.addActionListener(this);
		miscMenu.add(menuItem);

		menuItem = new JMenuItem(new DecompileToJavaAction("Decompile to Java",this,
				openAppsMgr));
		miscMenu.add(menuItem);
		
		Separator sep6 = new Separator();
		miscMenu.add(sep6);
		
		menuItem = new JMenuItem(new AboutAction("About"));
		miscMenu.add(menuItem);

		return menuBar;
	}

	// React to menu selections.
	public void actionPerformed(ActionEvent e) {

		if (CMD_CLEAR_ROA.equals(e.getActionCommand())) {
			roaList.clear();
			setJMenuBar(createMenuBar());
			getRootPane().updateUI();
		} else {
			logger.error("Unknown cmd found. This should not happen. Cmd="
					+ e.getActionCommand());
		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	public void createAndShowGUI() {

		// Set up Look and Feel, possible: null (use the default), "Metal",
		// "System", "Motif", and "GTK"
		boolean useDefaultLaF = false;
		String laf = null;
		try {
			String os = System.getProperty("os.name").toLowerCase();
			// linux or unix
			if (os.indexOf("linux") >= 0) {
				// Set GTK L&F, it is somehow not used on my system, but instead
				// the Metal L&F
				// is the default one.
				laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
			} else {
				laf = UIManager.getSystemLookAndFeelClassName();
			}
			UIManager.setLookAndFeel(laf);
		} catch (Exception e) {
			useDefaultLaF = true;
			laf = "Metal"; // default and cross platform
			logger.error("Problem setting look-and-feel");
		}
		// fallback
		if (useDefaultLaF) {
			try {
				UIManager.setLookAndFeel(laf);
			} catch (Exception e) {
				/* ignore */
			}
		}

		// System.out.println("Using L&F "+laf); // debug

		// Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);

		// Create and set up the window.
		final MainWindow frame = new MainWindow();
		// Do nothing by default when 'X' is pressed and set our own handle for
		// this event
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				new QuitAction("Exit", frame).actionPerformed(null);
			}
		});
		// Display the window.
		frame.setVisible(true);
	}

	/**
	 * Display an Info Dialog in the GUI. THIS METHOD SHOULD ONLY BE CALLED FROM
	 * THE LOGGER COMPONENT.
	 * 
	 * @param msg
	 *            the message to display
	 * @param title
	 *            the title of the dialog
	 */
	public static void showInfoDialog(String msg, String title) {
		JOptionPane.showMessageDialog(self, msg, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Display an Error Dialog in the GUI. THIS METHOD SHOULD ONLY BE CALLED
	 * FROM THE LOGGER COMPONENT.
	 * 
	 * @param msg
	 *            the message to display
	 * @param title
	 *            the title of the dialog
	 */
	public static void showErrorDialog(String msg, String title) {
		JOptionPane.showMessageDialog(self, msg, title,
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * If multiple applications are opened, the user is asked to select one.
	 * This has to be done for several operations which require a
	 * "target application". If only one application is opened, this application
	 * is returned w/o required user interaction.
	 * 
	 * @return the selected application, the only application, or null if none
	 *         is opened
	 */
	public Analysis getUserselectedAnalysisIfMultipleAreOpened() {
		if (openAppsMgr.getOpenedAnalysisCnt() == 0)
			return null; // nothing opened
		else if (openAppsMgr.getOpenedAnalysisCnt() == 1) {
			return openAppsMgr.getAllOpenedAnalysis().firstElement()
					.getAnalysis();
		} else { // > 1
			Vector<OpenAnalysis> v = openAppsMgr.getAllOpenedAnalysis();
			Vector<Analysis> av = new Vector<Analysis>(v.size());
			for (OpenAnalysis ao : v) {
				av.add(ao.getAnalysis());
			}
			AnalysisInterface[] apps = (AnalysisInterface[]) av.toArray(new Analysis[av.size()]);
			Analysis selectedAnalysis = (Analysis) JOptionPane.showInputDialog(
					null, "Chose an application for this operation.",
					"Multiple Opened Applications",
					JOptionPane.QUESTION_MESSAGE, null, apps, apps[0]);
			return selectedAnalysis;
		}
	}

	/**
	 * 
	 * @return the desktop pane to add frames etc
	 */
	public static JDesktopPane getDesktopPane() {
		return self.desktopPane;
	}
}
