package de.rub.syssec.saaf.gui;

import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.mxgraph.view.mxGraph;

import de.rub.syssec.saaf.gui.actions.ExportAction;
import de.rub.syssec.saaf.misc.CFGGraph;
import de.rub.syssec.saaf.model.application.MethodInterface;

public class MethodViewer extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3931840262426289268L;
	//ClassInterface smali;
	MethodInterface method;
	mxGraph graph;
	
	HashMap<Integer, Object> vertices;

	public MethodViewer(MethodInterface method){
		super(method.getName()+"("+method.getParameterString()+")"+method.getReturnValueString());
		this.method = method;

		CFGGraph c = new CFGGraph(method);
		getContentPane().add(c.getGraphComponent());
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Export");
		menuBar.add(menu);
		JMenuItem menuItem = new JMenuItem("Export");
		menuItem.addActionListener(new ExportAction(c.getGraph()));
		menu.add(menuItem);
		
		
		
		this.setJMenuBar(menuBar);
		
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(800, 620);//TODO: change to fullscreen
		this.setVisible(true);
	}

}
