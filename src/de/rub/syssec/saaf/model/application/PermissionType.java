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
package de.rub.syssec.saaf.model.application;

/**
 * A pattern type for the old and new heuristic.
 */
public enum PermissionType {
	// the Strings are the columns in the DB
	PLATFORM("platform"),
	FRAMEWORK("framework"),
	CUSTOM("custom"),
	UNKNOWN("unknown");

    private String name;

    /**
     * The type of pattern.
     * @param name the name of the pattern in the DB
     */
    private PermissionType(String paternName) {
    	this.name = paternName;
    }

    private PermissionType(int dings) {
    	this.name = "";
    }

    @Override
    public String toString() {
    	return name;
    }

    public int toInt() {
    	return this.ordinal();
    }
}
