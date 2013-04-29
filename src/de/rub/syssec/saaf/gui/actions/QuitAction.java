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
package de.rub.syssec.saaf.gui.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;

import de.rub.syssec.saaf.gui.MainWindow;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class QuitAction extends AbstractAction {

	private static final long serialVersionUID = 1694589428767804565L;
	private MainWindow mainWindow;

	public QuitAction(String title, MainWindow frame) {
		super(title);
		this.mainWindow=frame;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		MainWindow.logger.info("SAAF will now exit.");
		try {
			// FIXME Clean up files? Issue #70
			mainWindow.roaList.storeList();
		} catch (IOException e1) {
			MainWindow.logger.error("Could not save recently used apk list: "+e1.getMessage());
		}
		System.exit(0);
	}

}
