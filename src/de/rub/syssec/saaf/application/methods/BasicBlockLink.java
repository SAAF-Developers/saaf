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
package de.rub.syssec.saaf.application.methods;

public class BasicBlockLink {

	private BasicBlock to = null;

	private String label = null;

	// could be done with ints aswell
	// could save label names of targets or whatever
	public BasicBlockLink(BasicBlock to) {
		this.to = to;
	}

	public BasicBlockLink(BasicBlock to, String label) {
		this.to = to;
		this.label = label;
	}

	public BasicBlock getTo() {
		return to;
	}

	public String getLabel() {
		return label;
	}

}
