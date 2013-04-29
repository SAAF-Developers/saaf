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
package de.rub.syssec.saaf.misc.config;

public interface ConfigInterface {

	/**
	 * Get a string value using a predefined key
	 * 
	 * @param key
	 *            the key whose value to get
	 * @return the value or null if the key is not set
	 */
	public abstract String getValue(ConfigKeys key);

	/**
	 * Set a configuration parameter of type string
	 * 
	 * @param key
	 * @param value
	 */
	public abstract void setConfigValue(ConfigKeys key, String value);

	/**
	 * Get a string value using predefined key and specifing a default value
	 * 
	 * @param key
	 *            the key whose value to get
	 * @param defaultValue
	 *            the value that will be used if retrieval fails
	 * @return the value or defaultValue if key is not set
	 */
	public abstract String getConfigValue(ConfigKeys key, String defaultValue);

	/**
	 * Get an integer value using a string key
	 * 
	 * @param key
	 *            the name of the value to get
	 * @param defaultValue
	 *            the value that will be used if retrieval fails
	 * @return the value or defaultValue if key is not set
	 */
	public abstract int getIntConfigValue(ConfigKeys key, int defaultValue);

	/**
	 * Set an integer value using a predefined key
	 * 
	 * @param key
	 * @param value
	 */
	public abstract void setIntConfigValue(ConfigKeys key, int value);

	/**
	 * Set  a boolean value using a predefined key
	 * @param key
	 * @param value
	 */
	public abstract void setBooleanConfigValue(ConfigKeys key, boolean value);

}