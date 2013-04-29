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
package de.rub.syssec.saaf.analysis.steps.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.rub.syssec.saaf.analysis.steps.hash.Hash;
import de.rub.syssec.saaf.model.application.Digest;

public class CertificateReader {
	
	String file = "";
	String fingerprint = null;
	X509Certificate cert = null;
	
	/**
	 * A reader, which reads the first Certificate out of a given apk File
	 * @param apkFile the apkFile
	 */
	public CertificateReader(File apkFile){
		file=apkFile.getAbsolutePath();
	}
	
	/**
	 * A reader, which reads the first Certificate out of a given apk File
	 * @param apkFile the path to the apkFile
	 */
	public CertificateReader(String apkFile){
		file=apkFile;
	}
	
	/**
	 * 
	 * @return
	 */
	private boolean readFile() throws FileNotFoundException{
		boolean s = true;
		if(cert == null){
			JarFile apk = null;
			try {
				if (!(new File(file).exists()))
					throw new FileNotFoundException();
				apk = new JarFile(file);
		
				JarEntry entry = apk.getJarEntry("classes.dex");
				InputStream is = apk.getInputStream(entry);
		        byte[] buffer = new byte[8192];
		        while (is.read(buffer, 0, buffer.length) != -1) {
		            // we just read. this will throw a SecurityException
		            // if  a signature/digest check fails.
		        }
		        is.close();
		        
		        //TODO: make more generic in case multiple certificates exist?
		        cert = (X509Certificate) entry.getCertificates()[0];  
			} catch (IOException e) {
				e.printStackTrace();
				s = false;
			}
			finally {
				try { if (apk != null) apk.close(); } catch (Exception ignored) { } 
			}
		}
		return s;
	}
	
	/**
	 * This method calculates a SHA1 hash over the certificate, which equals openssl's fingerprint option
	 * @return The SHA1 Fingerprint of this Certificate, which is identical to openssl's "-fingerprint" option  or null, in case it was not possible to generate a fingerprint
	 * @throws CertificateEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public String getSHA1() throws CertificateEncodingException, NoSuchAlgorithmException, FileNotFoundException, IOException{
		if(readFile()){
			byte[] encoded = cert.getEncoded();
			String sha1 = Hash.calculateHash(Digest.SHA1, encoded);
			return sha1;
		}
		return null;
	}

	/**
	 * Generates the MD5 Fingerprint of the read Certificate
	 * @return the MD5 Fingerprint  or null, in case it was not possible to generate the md5 fingerprint
	 * @throws CertificateEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public String getMD5() throws CertificateEncodingException, NoSuchAlgorithmException, FileNotFoundException, IOException{
		if(readFile()){
			byte [] encoded=cert.getEncoded();
			String sha1 = Hash.calculateHash(Digest.MD5, encoded);
			return sha1;
		}
		return null;
	}
	
	/**
	 * 
	 * @return the read X509Certificate  or null, in case it was not possible to read the certificate
	 */	
	public X509Certificate getCertificate(){
		try {
			readFile();
		} catch (FileNotFoundException e) {
			// FIXME: Log? Throw the exception?
			e.printStackTrace();
		}
		return cert;
	}
	
	/**
	 * Get the Fingerprint of the Certificate read from the given apk File
	 * @return the fingerprint (SHA 1) or null, in case it was not possible to generate a fingerprint
	 * @throws IOException 
	 * @throws FileNotFoundException If the path to the apk is wrong/File does not exist
	 * @throws NoSuchAlgorithmException 
	 * @throws CertificateEncodingException 
	 */
	public String getFingerprint() throws CertificateEncodingException, NoSuchAlgorithmException, FileNotFoundException, IOException{
		if(fingerprint==null){
			if(readFile()){
				fingerprint = getSHA1();
				return fingerprint;
			}
			return null;
		}
		return fingerprint;
	}
}
