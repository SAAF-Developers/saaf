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

import java.util.TreeSet;

import de.rub.syssec.saaf.model.application.manifest.ActionInterface;
import de.rub.syssec.saaf.model.application.manifest.IntentFilterInterface;


/**
 * An Intent-Filter as defined in Manifest.xml
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * @see http://developer.android.com/guide/topics/manifest/intent-filter-element.html
 *
 */
public class IntentFilter implements Comparable, IntentFilterInterface{

	private TreeSet<ActionInterface> actions;

	private String label;

	private int priority;

	public IntentFilter() {
		this.actions= new TreeSet<ActionInterface>();
	}

	public IntentFilter(String name) {
		this.label = name;
		this.actions= new TreeSet<ActionInterface>();
	}
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.manifest.components.IntentFilterInterface#addAction(de.rub.syssec.saaf.application.manifest.components.Action)
	 */
	@Override
	public void addAction(ActionInterface a)
	{
		this.actions.add(a);
	}
	/* (non-Javadoc)
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
		IntentFilter other = (IntentFilter) obj;
		if (actions == null) {
			if (other.actions != null)
				return false;
		} else if (!actions.equals(other.actions))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.manifest.components.IntentFilterInterface#getActions()
	 */
	@Override
	public TreeSet<ActionInterface> getActions() {
		return actions;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.manifest.components.IntentFilterInterface#getLabel()
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.manifest.components.IntentFilterInterface#getPriority()
	 */
	@Override
	public int getPriority() {
		return priority;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actions == null) ? 0 : actions.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.manifest.components.IntentFilterInterface#setActions(java.util.TreeSet)
	 */
	@Override
	public void setActions(TreeSet<ActionInterface> actions) {
		this.actions = actions;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.manifest.components.IntentFilterInterface#setLabel(java.lang.String)
	 */
	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.manifest.components.IntentFilterInterface#setPriority(int)
	 */
	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public int compareTo(Object arg0) {
		IntentFilter other;
		if(arg0 instanceof IntentFilter)
		{
			other = (IntentFilter) arg0;
			if(this.label != null && other.label!=null)
			{
				return this.label.compareTo(other.label);
			}else{
				if(this.priority<other.priority)
				{
					return -1;
				}else if(this.priority>other.priority){
					return 1;
				}else{
					return 0;
				}
			}
			
		}
		return 0;
	}


}
