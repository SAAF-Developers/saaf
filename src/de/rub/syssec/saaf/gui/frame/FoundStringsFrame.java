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

import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JInternalFrame;

import de.rub.syssec.saaf.application.search.StringSearcher;
import de.rub.syssec.saaf.application.search.StringSearcher.FoundString;
import de.rub.syssec.saaf.gui.FilterTable;
import de.rub.syssec.saaf.gui.editor.FileTree;
import de.rub.syssec.saaf.model.application.ApplicationInterface;

public class FoundStringsFrame extends JInternalFrame {

	private static final long serialVersionUID = 8295968245777889533L;
	private Vector<FoundString> foundStringsVec;
	private static final String[] TABLE_COLUMNS = { "File", "Line", "String" }; 

	public FoundStringsFrame(final ApplicationInterface app, final FileTree fileTree) throws Exception {
		super("Found Strings - " + app.getApplicationName(), true, // resizable
				true, // closable
				true, // maximizable
				true);// iconifiable

		foundStringsVec = StringSearcher.searchString(app);
		FilterTable table = new FilterTable(foundStringsVec, TABLE_COLUMNS, fileTree);
		
		add(table);
		
		this.setPreferredSize(new Dimension(800, 400));
	}
	
}
