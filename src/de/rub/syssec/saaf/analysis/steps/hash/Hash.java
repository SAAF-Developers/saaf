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
package de.rub.syssec.saaf.analysis.steps.hash;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import de.rub.syssec.saaf.model.application.Digest;


public class Hash {
	
	public static final Digest DEFAULT_DIGEST = Digest.SHA1;

	/**
	 * This method calculates the hash for a given file
	 * @param digestAlgorithm the algorithm to use for hashing
	 * @param file the file to hash
	 * @return the hash based on the given algorithm for the given file
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static String calculateHash(Digest digestAlgorithm, File file)
			throws NoSuchAlgorithmException, IOException {
		
		// Fuzzy Hash (SSDeep) has its own implementation
		if (digestAlgorithm == Digest.FuzzyHash) {
			return SSDeep.calculateFuzzyHash(file);
		}

		MessageDigest md = MessageDigest.getInstance(digestAlgorithm.toString());
		
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		DigestInputStream dis = new DigestInputStream(bis, md);
		try {
			// read the file and update the hash calculation
			while (dis.read() != -1) {
				// do nothing
			}		
		}
		finally {
			try { dis.close(); } catch (Exception ignored) { }
		}
		

		// get the hash value as byte array
		byte[] hash = md.digest();

		return byteArray2Hex(hash);
	}
	
	/**
	 * This method calculates the hash for a given file
	 * @param digestAlgorithm the algorithm to use for hashing
	 * @param fileName the path of the file to hash
	 * @return the hash based on the given algorithm for the given file
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static String calculateHash(Digest digestAlgorithm, String fileName)
			throws NoSuchAlgorithmException, IOException {
		return calculateHash(digestAlgorithm, new File(fileName));
	}	
	
	/**
	 * This method calculates the hash for a given array of bytes and a given hashing algorithm.
	 * 
	 * This method will fail if used with Digest.FuzzyHash!
	 * 
	 * @param digestAlgorithm the algorithm to use for hashing
	 * @param bytes the bytes over which to calculate the hash
	 * @return the hash based on the given algorithm for the given array of bytes
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static String calculateHash(Digest digestAlgorithm, byte [] bytes)
			throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance(digestAlgorithm.toString());
		return byteArray2Hex(md.digest(bytes));
	}
		
	private static String byteArray2Hex(byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		String ret = formatter.toString();
		formatter.close();
		return ret;
	}
}
