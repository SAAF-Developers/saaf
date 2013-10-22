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
package de.rub.syssec.saaf.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.application.manifest.permissions.Permission;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.application.PermissionType;

public class APICalls {
	
	
	private static File file;
	private static Logger logger;
	static List<APICall> calls = null;
	
	/*
	 * This class parses the apicall file
	 * 
	 */
	
	public static List<APICall> getCalls(){	
		if (calls == null)
			return readAPICalls();
		return calls;
	}
	
	public static List<APICall> readAPICalls() {
		calls = new ArrayList<APICall>();
		try {
			String fileValue = Config.getInstance().getConfigValue(ConfigKeys.FILE_APICALLS);
			if(fileValue == null){
				if(logger == null){
				logger = Logger.getLogger(APICalls.class);
				}
				//TODO show info window at gui start, if this happens
				logger.info("Could not match apicalls. Option " + ConfigKeys.FILE_APICALLS.toString()
						+ " not properly set in saaf.conf.");

			} else {
			file = new File(fileValue);
			
			
			BufferedReader in = new BufferedReader(new FileReader(file));
			
			String line = in.readLine();
			String description = null;

			while(line!=null){
				ArrayList<Permission> permissions = new ArrayList<Permission>();
			
			String [] split = line.split("\t");
			if(split.length==1)continue;//if empty line or non correct line
			String permission = split[1];
			String apiCall = split[0];
			if(split.length>=3)
				description = split[2];
			
			
			if(permission.toLowerCase().contains(" and ")){
				String [] ands;
				if(permission.contains(" and" )){
					ands = permission.split(" and ");
				}else {
					ands = permission.split(" AND ");
				}
				for(String p:ands){
					permissions.add(new Permission(p,PermissionType.UNKNOWN));
				}
			}

			
			if(permission.toLowerCase().contains(" or ")){
				String [] ors;
				if(permission.contains(" or" )){
					ors = permission.split(" or ");
				}else {
					ors = permission.split(" OR ");
				}
				for(String p:ors){
					//TODO: make this somehow correct(the type that is)
					permissions.add(new Permission(p,PermissionType.UNKNOWN));
				}
				}
				
				APICall call = new APICall(apiCall, permissions);
				if( description != null) call.setDescription(description);
					call.setPermissionString(permission);
					calls.add(call);
					line = in.readLine();
				}
			}
			} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return calls;
			
	}
}
