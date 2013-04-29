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
package de.rub.syssec.saaf.misc;

import java.io.File;
import java.io.FileFilter;

/**
 * This class accepts files w/ some specific extension and directories
 * (with multiple possible directories).
 */
public class FileListFilter implements FileFilter {

	private String[] extensions = null;

	public FileListFilter(String... extensions) {
		this.extensions = extensions;
	}

	@Override
	public boolean accept(File file) {
		boolean fileOK = true;

		if (extensions != null) {
			boolean ok = false;
			for (String ext : extensions) {
				if (file.getName().toLowerCase().endsWith('.' + ext)
						|| file.isDirectory()) {
					ok = true;
					break;
				}
			}
			fileOK &= ok;
		}
		return fileOK;
	}
}