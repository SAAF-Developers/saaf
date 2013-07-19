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
package de.rub.syssec.saaf.analysis.steps.slicing;

import de.rub.syssec.saaf.model.analysis.BTPatternInterface;

/**
 * This object contains all information for a pattern used by the BackTrack
 * logic.
 * 
 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
 * @see de.rub.syssec.saaf.analysis.steps.slicing.SlicingCriterion
 */
public class BTPattern implements Comparable<BTPattern>, BTPatternInterface {
	private String qualifiedClassName;
	private String methodName;
	private String description;
	private String parameterSpecification;
	private int parameterOfInterest; // the numbers of the parameterOfInterest
										// of the function, in which we are
										// interested in
	private int id = -1; // ID from the table in db
										private boolean changed;
										private boolean active;

	public BTPattern(String qualifiedClass, String methodName,
			String paramSpec, int paramOfInterest, String description) {
		this.qualifiedClassName = qualifiedClass;
		this.methodName = methodName;
		this.parameterOfInterest = paramOfInterest;
		this.description = description;
		this.parameterSpecification = paramSpec;
		this.active=true;
		this.changed=true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.analysis.BTPatternInterface#getQualifiedClassName()
	 */
	@Override
	public String getQualifiedClassName() {
		return qualifiedClassName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.analysis.BTPatternInterface#getMethodName()
	 */
	@Override
	public String getMethodName() {
		return methodName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.analysis.BTPatternInterface#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
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
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + id;
		result = prime * result
				+ ((methodName == null) ? 0 : methodName.hashCode());
		result = prime * result + parameterOfInterest;
		result = prime
				* result
				+ ((parameterSpecification == null) ? 0
						: parameterSpecification.hashCode());
		result = prime
				* result
				+ ((qualifiedClassName == null) ? 0 : qualifiedClassName
						.hashCode());
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
		BTPattern other = (BTPattern) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id != other.id)
			return false;
		if (methodName == null) {
			if (other.methodName != null)
				return false;
		} else if (!methodName.equals(other.methodName))
			return false;
		if (parameterOfInterest != other.parameterOfInterest)
			return false;
		if (parameterSpecification == null) {
			if (other.parameterSpecification != null)
				return false;
		} else if (!parameterSpecification.equals(other.parameterSpecification))
			return false;
		if (qualifiedClassName == null) {
			if (other.qualifiedClassName != null)
				return false;
		} else if (!qualifiedClassName.equals(other.qualifiedClassName))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.analysis.BTPatternInterface#getArgumentsTypes()
	 */
	@Override
	public byte[] getArgumentsTypes() {
		if (parameterSpecification == null)
			return null;
		return parameterSpecification.getBytes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.analysis.BTPatternInterface#getParameterOfInterest()
	 */
	@Override
	public int getParameterOfInterest() {
		return parameterOfInterest;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BT_pattern [parameterOfInterest=" + parameterOfInterest
				+ ", description=" + description + ", methodName=" + methodName
				+ ", qualifiedClassName=" + qualifiedClassName + "]";
	}

	@Override
	public int compareTo(BTPattern arg0) {
		if (this.qualifiedClassName.compareTo(arg0.qualifiedClassName) == 0) {
			if (this.methodName.compareTo(arg0.methodName) == 0) {
				if (this.parameterSpecification
						.compareTo(arg0.parameterSpecification) == 0) {
					return Integer.valueOf(this.parameterOfInterest).compareTo(
							arg0.parameterOfInterest);
				}
				{
					return this.parameterSpecification
							.compareTo(arg0.parameterSpecification);
				}
			} else {
				return this.methodName.compareTo(arg0.methodName);
			}
		} else {
			return this.qualifiedClassName.compareTo(arg0.qualifiedClassName);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.analysis.BTPatternInterface#setQualifiedClassName(java
	 * .lang.String)
	 */
	@Override
	public void setQualifiedClassName(String qualifiedClassName) {
		this.qualifiedClassName = qualifiedClassName;
		this.setChanged(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.analysis.BTPatternInterface#setMethodName(java.lang.String
	 * )
	 */
	@Override
	public void setMethodName(String methodName) {
		this.methodName = methodName;
		this.setChanged(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.analysis.BTPatternInterface#setDescription(java.lang.String
	 * )
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
		this.setChanged(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.analysis.BTPatternInterface#setParameterSpecification(
	 * java.lang.String)
	 */
	@Override
	public void setParameterSpecification(String parameterSpecification) {
		this.parameterSpecification = parameterSpecification;
		this.setChanged(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.analysis.BTPatternInterface#setParameterOfInterest(int)
	 */
	@Override
	public void setParameterOfInterest(int parameterOfInterest) {
		this.parameterOfInterest = parameterOfInterest;
		this.setChanged(true);
	}

	@Override
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	@Override
	public boolean isChanged() {
		return this.changed;
	}

	@Override
	public boolean isActive() {
		return this.active;
	}

	@Override
	public void setActive(boolean active) {
		this.active=active;
		this.changed=true;		
	}
}
