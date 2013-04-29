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

import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.PermissionInterface;
import de.rub.syssec.saaf.model.application.PermissionType;
import de.rub.syssec.saaf.model.application.manifest.PermissionRequestInterface;

/**
 * Represents an applications declaration to use a certain Permission.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class PermissionRequest implements PermissionRequestInterface {

	private int id;
	private boolean changed;
	private PermissionInterface permission;
	private AnalysisInterface analysis;
	private boolean valid;

	public PermissionRequest(PermissionInterface permission) {
		this.permission = permission;
		this.valid=permission.getType()!=PermissionType.UNKNOWN;
		this.changed=true;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.model.application.manifest.PermissionRequestInterface#getRequestedPermission()
	 */
	@Override
	public PermissionInterface getRequestedPermission() {
		return this.permission;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.model.application.manifest.PermissionRequestInterface#setRequestedPermission(de.rub.syssec.saaf.model.application.PermissionInterface)
	 */
	@Override
	public void setRequestedPermission(PermissionInterface permission) {
		this.permission = permission;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.model.application.manifest.PermissionRequestInterface#isValid()
	 */
	@Override
	public boolean isValid() {
		return this.valid;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.model.application.manifest.PermissionRequestInterface#setValid(boolean)
	 */
	@Override
	public void setValid(boolean valid) {
		this.valid = valid;
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
	public void setChanged(boolean changed) {
		this.changed = changed;
		
	}

	@Override
	public boolean isChanged() {
		return this.changed;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((permission == null) ? 0 : permission.hashCode());
		result = prime * result + (valid ? 1231 : 1237);
		return result;
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
		PermissionRequest other = (PermissionRequest) obj;
		if (permission == null) {
			if (other.permission != null)
				return false;
		} else if (!permission.equals(other.permission))
			return false;
		if (valid != other.valid)
			return false;
		return true;
	}

	@Override
	public AnalysisInterface getAnalysis() {
		return this.analysis;
	}

	@Override
	public void setAnalysis(AnalysisInterface analysis) {
		this.analysis=analysis;
	}

	
}
