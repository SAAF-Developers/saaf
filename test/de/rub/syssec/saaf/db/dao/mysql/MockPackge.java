package de.rub.syssec.saaf.db.dao.mysql;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.PackageInterface;

public class MockPackge implements PackageInterface {

	int id;
	private List<String> name;
	String fuzzyHash;
	ApplicationInterface app;
	private boolean changed;
	
	/**
	 * @param id
	 * @param name
	 * @param fuzzyHash
	 * @param app
	 */
	public MockPackge(String name, String fuzzyHash,
			ApplicationInterface app) {
		super();
		this.name = new LinkedList<String>();
		this.name.addAll(Arrays.asList(name.split("\\.")));
		this.fuzzyHash = fuzzyHash;
		this.app = app;
		this.changed = true;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public void setName(String string) {
		this.name = new LinkedList<String>();
		name.addAll(Arrays.asList(string.split("\\.")));
		this.setChanged(true);
	}
	
	@Override
	public void setName(List<String> name) {
		this.name = name;
		this.setChanged(true);
	}


	@Override
	public String getName(boolean useDots) {
		String name = "";
		for(String ll: this.name){
			if(useDots)
				name += ll+".";
			else
				name += ll+File.separator;
		}
		//name can have a length of 0 if this is the default package
		if(name.length()>0)
			name = name.substring(0, name.length()-1);
		
		return name;
	}

	@Override
	public String getFuzzyHash() {
		return fuzzyHash;
	}

	@Override
	public void setFuzzyHash(String hash) {
		this.fuzzyHash = hash;
		setChanged(true);
	}

	@Override
	public ApplicationInterface getApplication() {
		return app;
	}

	@Override
	public void setApplication(ApplicationInterface app) {
		this.app = app;
		setChanged(true);
	}

	@Override
	public void setChanged(boolean changed) {
		this.changed=changed;		
	}

	@Override
	public boolean isChanged() {
		return this.changed;
	}

}
