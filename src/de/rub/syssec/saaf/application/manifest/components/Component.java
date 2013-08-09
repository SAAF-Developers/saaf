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

import de.rub.syssec.saaf.model.application.manifest.ComponentInterface;

/**
 * Common superclass for application components.
 * 
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class Component implements ComponentInterface {

	protected String name;
	private boolean isEntryPoint;

	public Component() {
		super();
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.manifest.components.ComponentInterface#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.manifest.components.ComponentInterface#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.name;
	}
	@Override
	public void setEntryPoint(boolean b) {
		this.isEntryPoint=b;
	}
	
	@Override
	public boolean isEntryPoint() {
		return this.isEntryPoint;
	}
	
}