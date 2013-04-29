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

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;

import javax.swing.JOptionPane;

import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;

/**
 * A small class to start different viewers for different file types
 * @author Christian Kröger
 *
 */
public class ViewerStarter {
	private ConfigKeys viewerString;
	private String placeholder = "%f";
	
	/**
	 * 
	 * @param viewerString the string to search in saaf.conf to find the viewer name
	 */
	public ViewerStarter(ConfigKeys viewerString){
		this.viewerString = viewerString;
	}
	
	/**
	 * 
	 * @param viewerString the string to search in saaf.conf to find the viewer name
	 * @param placeholder the placeholder to use for replacement
	 */
	public ViewerStarter(ConfigKeys viewerString, String placeholder){
		this.viewerString = viewerString;
		this.placeholder = placeholder;
	}
	
	/**
	 * change the viewer which will be started
	 * @param newViewer the new viewer
	 */
	public void changeViewer (ConfigKeys newViewer){
		viewerString = newViewer;
	}
	
	/**
	 * The string which is searched in the config
	 * @return the string searched in the config
	 */
	public ConfigKeys getViewer(){
		return viewerString;
	}
	
	/**
	 * Update the placeholder with a new value
	 * @param p the new placeholder
	 */
	public void setPlaceholder(String p){
		placeholder = p;
	}
	
	/**
	 * Returns the placeholder, which will be replaced by the filename, when the viewer is started
	 * @return the current placeholder
	 */
	public String getPlaceholder(){
		return placeholder;
	}
	
	/**
	 * Show the given file with the current image-viewer
	 * @param file the file to show in the current image-viewer
	 * @throws IOException
	 */
	public void showFile(File file) throws IOException {
		if (Config.getInstance().getConfigValue(
				viewerString) == null) {
			JOptionPane.showMessageDialog(null,"Showing "+file.getName()+" failed, because \"" + viewerString + " "+placeholder+ "\" is not set in saaf.conf");
		} else {
			String defaultViewer = Config.getInstance()
					.getConfigValue(viewerString);
			
			//if the placeholder exists, replace all occurences of it with the filename
			if (defaultViewer.contains(placeholder)) {
				defaultViewer = defaultViewer.replaceAll(
						placeholder, Matcher.quoteReplacement(file
								.getAbsolutePath()));
				//otherwise just concatenate the filename
			} else {
				defaultViewer += " "+ file.getAbsolutePath();
			}
			Runtime r = Runtime.getRuntime();
			r.exec(defaultViewer);
		}
	}
	
	
}
