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
package de.rub.syssec.saaf.gui.frame.logs;

import java.beans.PropertyVetoException;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;


public class LogFrame extends JInternalFrame {

	private static LogFrame self = null;

	private static final long serialVersionUID = -7483604950556000222L;
	private static JTextArea txtArea = new JTextArea();
	private JTextAreaAppender txtAreaAppender;
	/**
	 * Constructor.
	 */
	public LogFrame() {
		super("Logs", true, // resizable
				true, // closable
				true, // maximizable
				true);// iconifiable
		setSize(650, 200);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		txtAreaAppender = new JTextAreaAppender(txtArea);
		Logger.getRootLogger().addAppender(txtAreaAppender);
		txtArea.setEditable(false);
		JScrollPane sPane = new JScrollPane(txtArea);
		sPane.setAutoscrolls(true);
		add(sPane);

		if(txtArea.getText().equals("")){
			txtArea.append("GUI logger started\n\n");
		}
		txtArea.setCaretPosition(txtArea.getText().length());

	}

}
