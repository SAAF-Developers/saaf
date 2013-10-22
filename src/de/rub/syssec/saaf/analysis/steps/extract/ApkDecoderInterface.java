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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.log4j.Logger;

import brut.androlib.AndrolibException;
import brut.androlib.ApkDecoder;
import brut.androlib.err.CantFindFrameworkResException;
import brut.androlib.err.OutDirExistsException;
import brut.androlib.res.AndrolibResources;
import brut.androlib.res.util.ExtFile;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;


/**
 * Interface to the Android-APKtool
 * 
 * @author Martin Ussath
 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
 *
 */
public class ApkDecoderInterface {
	/**
	 * What to do if the apktool installed on the users system is older than ours.
	 * 
	 * @author Tilman Bender <tilman.bender@rub.de>
	 *
	 */
	public enum Treatment{
		DONT_TOUCH,
		RENAME,
		DELETE
	}

	private static final Logger LOGGER = Logger.getLogger(ApkDecoderInterface.class);

	/**
	 * Decodes an APK file
	 *
	 * <h4>Handles following problems:</h4>
	 * <p>
	 * Problem 1:	if SAAF uses an older version of APKTool than the user
	 * 				~/apktool/framework/1.apk
	 * Handling:	3 Options for the user ({@link Config})
	 * 				- default:	APKTool only decode the smali files, no Resources (incl. Manifest)
	 * 				- mv: 		rename the framework file, and complete decode
	 * 				- del:		delete the framework file, and complete decode
	 * </p>
	 *
	 * @param apk the apk
	 * @param destination the destination dir (if already existing, it will be removed firstly)
	 * @return false if the decoding crashes
	 */
	private static final Object  MUTEX= new Object();

	public static boolean decode(File apk, File destination) throws DecoderException {
		
		boolean decodeSucseccfull = false;
		synchronized(MUTEX){
			ApkDecoder localApkDecoder = new ApkDecoder();
			
	        try {
				// HL: The ApkTool can delete the Destination Folder on his own: (also faster)
				localApkDecoder.setForceDelete(true);
				
				localApkDecoder.setOutDir(destination);
				localApkDecoder.setApkFile(apk);
				//disable resource decoding
				localApkDecoder.setDecodeResources((short) 0x0100);				
				
				//apkdecoder constants
//		        public final static short DECODE_SOURCES_NONE = 0x0000;
//		        public final static short DECODE_SOURCES_SMALI = 0x0001;
//		        public final static short DECODE_SOURCES_JAVA = 0x0002;
//
//		        public final static short DECODE_RESOURCES_NONE = 0x0100;
//		        public final static short DECODE_RESOURCES_FULL = 0x0101;


				// localApkDecoder.setDecodeSources(ApkDecoder.DECODE_SOURCES_JAVA);  //HL: Not yet implemented by APKTool
	        	localApkDecoder.decode();
	        	
	        	//extract the manifest file, because disabling decoding resource also disables decoding of the manifest file
				AndrolibResources res = new AndrolibResources();
				ExtFile apkFile = new ExtFile(apk);
				res.decodeManifest(res.getResTable(apkFile,true), apkFile, destination);
      	
	        	decodeSucseccfull = true;
	        	
	        } catch (OutDirExistsException ex) {
	            // Should never occur, because setForceDelete(true) is called. 
	        	LOGGER.error(
	                "Destination directory (" + destination.getAbsolutePath() + ") " +
	                "already exists.",ex);
	        	throw new DecoderException(ex);
	        } catch (CantFindFrameworkResException ex) {
	            LOGGER.warn(
	                "Can't find framework resources for package of id: " +
	                String.valueOf(ex.getPkgId()) + ". You must install proper " +
	                "framework files, see Android-APKtool-project website for more info.");
	            throw new DecoderException(ex);
	        } catch (AndrolibException ex) {
	        	/**
	        	 * Handle the special type of AndrolibException caused by an outdated
	        	 * version of Android-APKTool (used by SAAF) in contrast to the version the user uses.
	        	 */
	        	if (ex.getMessage().startsWith("Multiple resources:")) {
	        		Treatment userOption = Treatment.valueOf(Config.getInstance().getConfigValue(ConfigKeys.APKTOOL_TREATMENT));
	        		
	        		if ( (userOption == Treatment.DELETE) 
	        			|| (userOption == Treatment.RENAME)) {
	        			
	        			//1. Rename or Delete current framework file
	        			final String frameworkDir = System.getProperty("user.home") + File.separatorChar + 
		            		"apktool" + File.separatorChar + "framework" + File.separatorChar;
	        			File apktool_framwork = new File(frameworkDir + "1.apk");
	        			if (userOption == Treatment.DELETE) {
	        				apktool_framwork.delete();
	        			} else {
	        				//case Config.RENAME_APKtool_FRAMEWORK_IF_TOO_OLD
	        				final String curDateTime = new SimpleDateFormat("yyyy-MM-dd_HHmm").format(Calendar.getInstance().getTime());
	        				apktool_framwork.renameTo(new File(frameworkDir + "1_mv_by_SAAF_on_" + curDateTime + ".apk"));
	        			}
	           			//2. Next try to decode
	        			try {
							localApkDecoder.decode();
						} catch (IOException e) {
							throw new DecoderException(e);
						} catch (AndrolibException e) {
							throw new DecoderException(e);
						}
	        			decodeSucseccfull = true;
	        			
	        			//3. Register created framework file to be deleted on the shutdown of SAAF
	        			File created_apktool_framwork = new File(frameworkDir + "1.apk");
	        			if ( created_apktool_framwork.exists()) {
	        				created_apktool_framwork.deleteOnExit();
	        			}
	        		} else {
	        			//case Config.DONT_TOUCH_APKtool_FRAMEWORK_IF_TOO_OLD
	        			
	        			//localApkDecoder.setDecodeResources(ApkDecoder.DECODE_RESOURCES_NONE);	//HL: Alternative, if APKTool crashes by Resources
	        			try {
	        				//localApkDecoder.setDecodeResources((short)0x0100);	//HL: Alternative, if APKTool crashes by Resources
							localApkDecoder.decode();
						} catch (IOException e) {
							throw new DecoderException(e);
						} catch (AndrolibException e) {
							throw new DecoderException(e);
						}
	        			
	        			LOGGER.error("Could not decode app correctly with APKtool, " +
	        					"because SAAF version is older than your APKtool. \nYou can " +
	        					"solve this problem by allowing SAAF in the config file " +
	        					"to move (mv) or to delete (del) \nthe framework file " +
	        					"(~/apktool/framwork/1.apk)." +
	        					"e.g. 'PERMISSION_FOR_APKtool_FRAMEWORK_TO=mv'");
	        			//TODO: Check if Logger crashes in headless mode
	        		}
	        	} else {
	        		throw new DecoderException(ex);
	        	}
	        }catch(Exception e){
	        	e.printStackTrace();
	        }
	    }
        return decodeSucseccfull;
	}

}
