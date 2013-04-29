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
package de.rub.syssec.saaf.application.manifest.components;

import de.rub.syssec.saaf.model.application.manifest.ActionInterface;

/**
 * An action as defined in AndroidManifest.xml
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * @see http://developer.android.com/guide/topics/manifest/action-element.html
 */
public class Action implements Comparable<Action>, ActionInterface{

	private String name;

	public Action(String name) {
		super();
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.manifest.components.ActionInterface#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.manifest.components.ActionInterface#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Action other = (Action) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public int compareTo(Action o) {
		Action other;
		if(o instanceof Action)
		{
			other = (Action) o;
			if(this.name != null && other.name!=null)
			{
				return this.name.compareTo(other.name);
			}
			
		}
		return 0;
	}

}
