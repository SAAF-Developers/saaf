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
package de.rub.syssec.saaf.gui.frame;

import javax.swing.JInternalFrame;

/**
 * TODO: Do some smart placement or get rid of this stub at all as it currently does nothing special
 */
public class InternalFrameStub extends JInternalFrame {
	private static final long serialVersionUID = 2641712754179070227L;
	static int openFrameCount = 1;
	static final int xOffset = 30, yOffset = 30;

	public InternalFrameStub(String titel) {
		super(titel, true, // resizable
				true, // closable
				true, // maximizable
				true);// iconifiable

		// set window size
		setSize(300, 300);

		// Set the window's location.
		setLocation(xOffset * openFrameCount, yOffset * openFrameCount);
		openFrameCount++;
	}
	
	@Override
	public void finalize() {
		openFrameCount--;
	}
}
