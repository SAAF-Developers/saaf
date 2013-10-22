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
package de.rub.syssec.saaf.application;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.PackageInterface;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class JavaPackage implements PackageInterface {

	private static final String DOT = "\\.";
	private int id;
	private String fuzzyHash;
	private ApplicationInterface application;
	private List<String> name;
	private boolean changed;

	public JavaPackage(String name, ApplicationInterface app, String fuzzy) {
		this.name = new LinkedList<String>();
		this.name.addAll(safeSplit(name));
		this.application = app;
		this.fuzzyHash = fuzzy;
		this.changed = true;
	}

	public JavaPackage(List<String> name, ApplicationInterface app, String fuzzy) {
		this.name = name;
		this.application = app;
		this.fuzzyHash = fuzzy;
		this.changed = true;
	}

	public JavaPackage() {
		super();
	}


	public JavaPackage(ApplicationInterface app) {
		this.application=app;
	}


	@Override
	public int getId() {
		return this.id;
	}


	@Override
	public void setId(int id) {
		this.id=id;		
	}

	@Override
	public String getFuzzyHash() {
		return this.fuzzyHash;
	}

	@Override
	public void setFuzzyHash(String hash) {
		this.fuzzyHash=hash;
		this.setChanged(true);
	}

	@Override
	public ApplicationInterface getApplication() {
		return this.application;
	}

	@Override
	public void setApplication(ApplicationInterface app) {
		this.application=app;
		this.setChanged(true);
	}

	@Override
	public void setName(String string) {
		this.name = new LinkedList<String>();
		//this.name.addAll(Arrays.asList(string.split(".")));
		this.name.addAll(safeSplit(string));
		this.setChanged(true);
	}
	
	@Override
	public void setName(List<String> name) {
		this.name = name;
		this.setChanged(true);
	}
	
	private List<String> safeSplit(String list){
		LinkedList<String> l= new LinkedList<String>();
		
		if(list.contains(".")){
			l.addAll(Arrays.asList(list.split(DOT)));
		} else {
			l.add(list);
		}
		
		return l;
	}


	@Override
	public String getName(boolean useDots) {
		String name = "";
		for(String ll: this.name){
			if(useDots)
				name += ll+".";
			else
				name += ll+"/"; //using / instead of File.separator to get identical package names under windows and linux
		}
		//name can have a length of 0 if this is the default package
		if(name.length()>0)
			name = name.substring(0, name.length()-1);
		
		return name;
	}


	@Override
	public void setChanged(boolean changed) {
		this.changed = changed;
		
	}


	@Override
	public boolean isChanged() {
		return changed;
	}

}
