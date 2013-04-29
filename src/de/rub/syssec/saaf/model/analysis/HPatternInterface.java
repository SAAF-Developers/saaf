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

public interface HPatternInterface extends Entity {

	/**
	 * @return the description
	 */
	public abstract String getDescription();

	/**
	 * @param description
	 *            the description to set
	 */
	public abstract void setDescription(String description);

	/**
	 * @return the hvalue
	 */
	public abstract int getHvalue();

	/**
	 * @param hvalue
	 *            the hvalue to set
	 */
	public abstract void setHvalue(int hvalue);

	/**
	 * @return the pattern
	 */
	public abstract String getPattern();

	/**
	 * @param pattern
	 *            the pattern to set
	 */
	public abstract void setPattern(String pattern);

	/**
	 * @return the searchin
	 */
	public abstract PatternType getSearchin();

	/**
	 * @param searchin
	 *            the searchin to set
	 */
	public abstract void setSearchin(PatternType searchin);

	/**
	 * 
	 * @return true if pattern is active
	 */
	public abstract boolean isActive();

	/**
	 * 
	 * @param active
	 */
	public abstract void setActive(boolean active);

}