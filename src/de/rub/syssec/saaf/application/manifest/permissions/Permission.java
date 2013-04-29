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
package de.rub.syssec.saaf.application.manifest.permissions;

import de.rub.syssec.saaf.model.application.PermissionInterface;
import de.rub.syssec.saaf.model.application.PermissionType;
import static de.rub.syssec.saaf.model.application.PermissionType.*;

/**
 * This class represents a Permission that is stored in SAAF.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class Permission implements PermissionInterface{

	/**
	 * @param name
	 * @param type
	 * @param description
	 * @param unknown
	 */
	private String name;
	private PermissionType type;
	private String description;
	private boolean changed;
	private int id;

	
	public Permission(String name)
	{
		this(name,UNKNOWN);
	}

	public Permission(String name, PermissionType type) {
		this(name,type,"");
	}
	
	public Permission(String name, PermissionType type, String description) {
		super();
		this.name = name;
		this.type = type;
		this.description = description;
		this.changed=true;
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.manifest.permissions.PermissionInterface#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.manifest.permissions.PermissionInterface#setName(java.lang.String)
	 */
	@Override
	public void setName(String permission) {
		this.name = permission;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Permission other = (Permission) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public int compareTo(PermissionInterface o) {
		return this.name.compareTo(o.getName());
	}

	@Override
	public PermissionType getType() {
		return type;
	}

	@Override
	public void setType(PermissionType type) {
		this.type = type;
	}

	@Override
	public String getDescription() {
		return description;
	}


	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the changed
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * @param changed the changed to set
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}
	
	

}
