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
package de.rub.syssec.saaf.analysis.steps.extract;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class ApkUnzipper {

	private static final Logger LOGGER = Logger.getLogger(ApkUnzipper.class);

	/**
	 * Extracts the given archive into the given destination directory
	 * 
	 * @param archive
	 *            - the file to extract
	 * @param dest
	 *            - the destination directory
	 * @throws Exception
	 */
	public static void extractArchive(File archive, File destDir)
			throws IOException {

		if (!destDir.exists()) {
			destDir.mkdir();
		}

		ZipFile zipFile = new ZipFile(archive);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		byte[] buffer = new byte[16384];
		int len;
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();

			String entryFileName = entry.getName();

			File dir = buildDirectoryHierarchyFor(entryFileName, destDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			if (!entry.isDirectory()) {

				File file = new File(destDir, entryFileName);

				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(file));

				BufferedInputStream bis = new BufferedInputStream(
						zipFile.getInputStream(entry));

				while ((len = bis.read(buffer)) > 0) {
					bos.write(buffer, 0, len);
				}

				bos.flush();
				bos.close();
				bis.close();
			}
		}
		zipFile.close();
	}

	/**
	 * Copies the given file into the given destination directory
	 * 
	 * @param archive
	 *            - the file to copy
	 * @param dest
	 *            - the destination directory
	 * @throws IOException
	 */
	public static void copyApk(File archive, File dest) throws IOException {

		File destination = new File(dest, archive.getName());
		FileUtils.copyFile(archive, destination);
	}

	/**
	 * Extracts the given apk into the given destination directory
	 * 
	 * @param archive
	 *            - the file to extract
	 * @param dest
	 *            - the destination directory
	 * @throws IOException
	 */
	public static void extractApk(File archive, File dest) throws IOException {

		@SuppressWarnings("resource") // Closing it later results in an error
		ZipFile zipFile = new ZipFile(archive);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		BufferedOutputStream bos = null;
		BufferedInputStream bis = null;

		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();

			String entryFileName = entry.getName();

			byte[] buffer = new byte[16384];
			int len;

			File dir = buildDirectoryHierarchyFor(entryFileName, dest);// destDir
			if (!dir.exists()) {
				dir.mkdirs();
			}

			if (!entry.isDirectory()) {
				if (entry.getSize() == 0) {
					LOGGER.warn("Found ZipEntry \'" + entry.getName()
							+ "\' with size 0 in " + archive.getName()
							+ ". Looks corrupted.");
					continue;
				}

				try {
					bos = new BufferedOutputStream(new FileOutputStream(
							new File(dest, entryFileName)));// destDir,...

					bis = new BufferedInputStream(zipFile.getInputStream(entry));

					while ((len = bis.read(buffer)) > 0) {
						bos.write(buffer, 0, len);
					}
					bos.flush();
				} catch (IOException ioe) {
					LOGGER.warn("Failed to extract entry \'" + entry.getName()
							+ "\' from archive. Results for "
							+ archive.getName() + " may not be accurate");
				} finally {
					if (bos != null) bos.close();
					if (bis != null) bis.close();
					// if (zipFile != null) zipFile.close();
				}
			}

		}

	}

	private static File buildDirectoryHierarchyFor(String entryName,
			File destDir) {
		int lastIndex = entryName.lastIndexOf('/');

		String internalPathToEntry = entryName.substring(0, lastIndex + 1);
		return new File(destDir, internalPathToEntry);
	}
}
