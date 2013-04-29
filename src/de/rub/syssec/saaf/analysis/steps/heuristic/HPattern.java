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
 *//* SAAF: A static analyzer for APK files.
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
package de.rub.syssec.saaf.analysis.steps.heuristic;

import de.rub.syssec.saaf.model.Entity;
import de.rub.syssec.saaf.model.analysis.HPatternInterface;
import de.rub.syssec.saaf.model.analysis.PatternType;

/**
 * This object contains all information for a pattern used by the new Heuristic logic
 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
 */
public class HPattern implements Comparable<HPatternInterface>, Entity, HPatternInterface {
	private String pattern;
	private String description;
	private int hvalue;
	private PatternType searchin;
	private int id = -1;		//ID from the table in db
	private boolean changed;
	private boolean active;
	private final static int STD_HVALUE = 0;

	public HPattern(String pattern, PatternType searchin, int hvalue, String description) {
		this.pattern = pattern;
		this.searchin = searchin;
		this.hvalue = hvalue;
		this.description = description;
		this.changed = true;
	}

	public HPattern(String pattern, PatternType searchin, int hvalue) {
		this(pattern, searchin, hvalue, "");
	}

	public HPattern(String pattern, PatternType searchin) {
		this(pattern, searchin, STD_HVALUE, "");
	}

	//################# getter and setter
	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.logicTier.HPatternInterface#getPattern()
	 */
	@Override
	public String getPattern() {
		return pattern;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.logicTier.HPatternInterface#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.logicTier.HPatternInterface#getHvalue()
	 */
	@Override
	public int getHvalue() {
		return hvalue;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.logicTier.HPatternInterface#getSearchin()
	 */
	@Override
	public PatternType getSearchin() {
		return searchin;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HPattern [" +
			"id=" + id + ", " +
			"pattern=" + pattern + ", " +
			"hvalue=" + hvalue + ", " +
			"searchin=" + searchin + ", " +
			"description=" + description + "]";
	}

	@Override
	public int compareTo(HPatternInterface o) {
		return this.pattern.compareTo(o.getPattern());
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.logicTier.HPatternInterface#setPattern(java.lang.String)
	 */
	@Override
	public void setPattern(String pattern) {
		this.pattern = pattern;
		setChanged(true);
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.logicTier.HPatternInterface#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
		setChanged(true);
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.logicTier.HPatternInterface#setHvalue(int)
	 */
	@Override
	public void setHvalue(int hvalue) {
		this.hvalue = hvalue;
		setChanged(true);
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.logicTier.HPatternInterface#setSearchin(de.rub.syssec.saaf.heuristic.PatternType)
	 */
	@Override
	public void setSearchin(PatternType searchin) {
		this.searchin = searchin;
		setChanged(true);
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
		
	}
}
