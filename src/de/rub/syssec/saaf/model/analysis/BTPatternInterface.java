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
package de.rub.syssec.saaf.model.analysis;

import de.rub.syssec.saaf.model.Entity;

public interface BTPatternInterface extends Entity {

	/**
	 * returns the qualified name of the class the pattern applies to.
	 * 
	 * @return the qualifiedClassName
	 */
	public abstract String getQualifiedClassName();

	/**
	 * @param qualifiedClassName
	 *            the qualifiedClassName to set
	 */
	public abstract void setQualifiedClassName(String qualifiedClassName);

	/**
	 * return the name of the method the pattern applies to.
	 * 
	 * @return the methodName
	 */
	public abstract String getMethodName();

	/**
	 * @param methodName
	 *            the methodName to set
	 */
	public abstract void setMethodName(String methodName);

	/**
	 * returns a descriptive text for the pattern.
	 * 
	 * @return the description
	 */
	public abstract String getDescription();

	/**
	 * @param description
	 *            the description to set
	 */
	public abstract void setDescription(String description);

	/**
	 * @return the parameterSpecification or null as byte array
	 */
	public abstract byte[] getArgumentsTypes();

	/**
	 * @param parameterSpecification
	 *            the parameterSpecification to set
	 */
	public abstract void setParameterSpecification(String parameterSpecification);

	/**
	 * @return the parameterOfInterest
	 */
	public abstract int getParameterOfInterest();

	/**
	 * @param parameterOfInterest
	 *            the parameterOfInterest to set
	 */
	public abstract void setParameterOfInterest(int parameterOfInterest);
	
	/**
	 * 
	 * @return 
	 */
	public abstract boolean isActive();
	/**
	 * 
	 * @param active
	 */
	public abstract void setActive(boolean active);

}