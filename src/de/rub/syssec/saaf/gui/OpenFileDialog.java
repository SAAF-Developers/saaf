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

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

public class OpenFileDialog {
	
	private static final Logger logger = Logger.getLogger(OpenFileDialog.class);


	/**
	 * Create a File Open Dialog.
	 * @param parent The GUI component calling this.
	 * @param filter File extensions for easy filtering (may be null and . and * are ignored)
	 * @return
	 */
	public static File createOpenFileDialog(Component parent, String... fileExtension) {
		File seletcedfile = null;
		JFileChooser fc = new JFileChooser();
		
		// try to set up a filter, if it fails (weird null input...) set no filter at all
		try {
			for (int i=0; i<fileExtension.length; i++) {
				fileExtension[i] = fileExtension[i].replaceAll("\\.", "");
				fileExtension[i] = fileExtension[i].replaceAll("\\*", "");
			}
			
			FileNameExtensionFilter fileFilter = null;
			
			if (fileExtension != null && fileExtension.length == 1 && "apk".equals(fileExtension[0])) {
				fileFilter = new FileNameExtensionFilter(".apk (Android App)", fileExtension);
			}
			else if (fileExtension != null && fileExtension.length > 0) {
				StringBuilder sb = new StringBuilder();
				for (String s : fileExtension) {
					sb.append(s+" ");
				}
				sb.append("- Misc Filter");
				fileFilter = new FileNameExtensionFilter(sb.toString(), fileExtension);
			}
			if (fileFilter != null) fc.setFileFilter(fileFilter);
		}
		catch (Exception e) {
			logger.error("Improper call to to createOpenFileDialog()!",e);
		}
		
		int returnVal = fc.showOpenDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			seletcedfile = fc.getSelectedFile();
			if (seletcedfile.isFile() && seletcedfile.canRead()) { // the user can input arbitrary data...
//				System.out.println(f.getAbsolutePath());
				return seletcedfile;
			}
			else return null;
		}
		else return seletcedfile;
	}	
}
