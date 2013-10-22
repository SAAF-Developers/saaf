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
import java.util.ArrayList;

import de.rub.syssec.saaf.application.manifest.permissions.Permission;


public class APICall {
	//TODO: currently the calls are just saved in form of a string representation, change this to really represent the permissions needed (not that easy due to them being present in form of a boolean formula)
	
	String call = null;
	ArrayList<Permission> permissions = null;
	String permissionString = "";
	private String description = "";

	public APICall (String c){
		call = c;
		permissions = new ArrayList<Permission>();
	}
	
	public APICall (String c, Permission  p){
		call = c;
		permissions = new ArrayList<Permission>();
		permissions.add(p);
//		PermissionTree<String> permissionTree = new PermissionTree<String>();
	}

	
	public APICall (String c, ArrayList<Permission> p){
		call = c;
		permissions = p;
		
//		PermissionTree<String> permissionTree = new PermissionTree<String>();

	}
		
	public String getCall(){
		return call;
	}
	
	public String getDescription(){
		return description;
	}
	
	public String toString(){
		String ret = "";
		ret += "call: "+ call +" needs the permissions: ";
		ret += permissionString+ "   " ;
		ret += description;

		return ret;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setPermissionString(String perms) {
		this.permissionString = perms;
	}
	
	public String getPermissionString(){
		return permissionString;
	}

}
