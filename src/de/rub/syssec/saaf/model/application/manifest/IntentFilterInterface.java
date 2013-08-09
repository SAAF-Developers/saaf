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
package de.rub.syssec.saaf.model.application.manifest;

import java.util.TreeSet;


public interface IntentFilterInterface {

	public abstract void addAction(ActionInterface a);

	/**
	 * @return the actions
	 */
	public abstract TreeSet<ActionInterface> getActions();

	/**
	 * @return the label
	 */
	public abstract String getLabel();

	/**
	 * @return the priority
	 */
	public abstract int getPriority();

	/**
	 * @param actions the actions to set
	 */
	public abstract void setActions(TreeSet<ActionInterface> actions);

	/**
	 * @param label the label to set
	 */
	public abstract void setLabel(String label);

	/**
	 * @param priority the priority to set
	 */
	public abstract void setPriority(int priority);

	public abstract boolean hasAction(String string);

}