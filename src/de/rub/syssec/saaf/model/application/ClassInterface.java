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

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import de.rub.syssec.saaf.model.Entity;

public interface ClassInterface extends Entity, Obfuscatable {

	public enum SearchType {
		INSTRUCTIONS_ONLY,
		NON_INSTRUCTIONS_ONLY,
		INSTRUCTIONS_AND_NON_INSTRUCTIONS;
	}

	/**
	 * Get the filename relative to the smali folder.
	 * 
	 * This method was specifically introduced for sitautions
	 * where you do not need or want the absolute path (e.g. reports)
	 * @return
	 */
	public abstract String getRelativeFile();
	
	/**
	 * Get the corresponding file on the filesystem.
	 * @return
	 */
	public abstract File getFile();

	/**
	 * Returns all methods w/ at least one BB.
	 * @return the methods
	 */
	public abstract LinkedList<MethodInterface> getMethods();

	/**
	 * Returns all methods w/ no BBs, eg, native and abstract methods.
	 * @return the methods
	 */
	public abstract LinkedList<MethodInterface> getEmptyMethods();

	public abstract LinkedList<CodeLineInterface> getAllCodeLines();

	/**
	 * Get all CodeLines which have a given opcode.
	 * TODO: Diese Methode auch anderen Stellen verwenden und Code Duplication zu vermeiden.
	 * @param type all the desired {@link InstructionType}s
	 * @return a list off all CodeLines having such an {@link InstructionType} 
	 */
	public abstract LinkedList<CodeLineInterface> getAllCodeLine(
			InstructionType... types);

	public abstract Collection<FieldInterface> getAllFields();

	public abstract int getLinesOfCode();

	/**
	 * Search for a pattern in a SMALI file.
	 * @param pattern the pattern to search for
	 * @param searchType where to search in the SMALI file
	 * @return a list containing all found CodeLines matching the pattern, the list might be empty
	 */
	public abstract LinkedList<CodeLineInterface> searchPattern(byte[] pattern,
			SearchType searchType);

	/**
	 * Search for a pattern in a SMALI file.
	 * @param pattern the pattern to search for
	 * @param types the instruction types for codelines which will be searched
	 * @return
	 */
	public abstract LinkedList<CodeLineInterface> searchPattern(byte[] pattern,
			InstructionType... types);

	/**
	 * @return the packageId
	 */
	public abstract int getPackageId();

	/**
	 * @param id the packageId to set (only once)
	 */
	public abstract void setPackageId(int packageId);

	/**
	 * Returns the package associated with this class.
	 * 
	 * @author Tilman Bender <tilman.bender@rub.de>
	 * @return the package associated or null
	 */
	public abstract PackageInterface getPackage();
	
	public abstract void setPackage(PackageInterface javaPackage);
	
	/**
	 * Get all implemented interfaces.
	 * @return A collection with the full class names, may be empty and the interfaces are divided by dots (if any)
	 */
	public abstract Collection<String> getImplementedInterfaces();

	/**
	 * Get the super class.
	 * @return the full class name of the super class, divided by dots
	 */
	public abstract String getSuperClass();

	/**
	 * Get the package name.
	 * @param useDots separate with dots or slashes
	 * @return the package name, divided by dots or slashes
	 */
	public abstract String getPackageName(boolean useDots);

	/**
	 * Get the class name without the .smali extension.
	 * @return
	 */
	public abstract String getClassName();

	/**
	 * Get the full class name.
	 * @param useDots separate with dots or slashes
	 * @return the full class name, divided by dots or slashes
	 */
	public abstract String getFullClassName(boolean useDots);

	public abstract void setSsdeepHash(String hash);

	public abstract String getSsdeepHash();

	/**
	 * @return the sourceFile
	 */
	public abstract String getSourceFile();

	/**
	 * @return the app
	 */
	public abstract ApplicationInterface getApplication();

	/**
	 * Get the amount of bytes of the parsed file.
	 * @return the size in bytes
	 */
	public abstract int getSize();

	/**
	 * Get the unique label of this SmaliClass.
	 * @return
	 */
	public abstract String getUniqueId();

	/**
	 * TODO Stub only
	 * Checks whether this file is located inside an ad framework's package path
	 * @return true if it is
	 */
	public abstract boolean isInAdFrameworkPackage();

	public abstract void setInAdFramework(boolean hasAd);

}