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

import java.util.LinkedList;

import de.rub.syssec.saaf.model.application.CodeLineInterface;

public class TryBlock {
	// the line numbers of begin and end of the code of this tryblock
	private int begin, end;
	
	/**
	 *  The truly last line of this tryBlock.
	 * meaning the last catch, which is always the end of a BasicBlock
	 */
	private CodeLineInterface blockEnd = null;

	/**
	 * The first lines of all corresponding catches.
	 */
	private LinkedList<CodeLineInterface> catches = null;

	// probably not all those sets necessary
	public int getBegin() {
		return begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public LinkedList<CodeLineInterface> getCatches() {
		return catches;
	}

	public void setCatches(LinkedList<CodeLineInterface> catches) {
		this.catches = catches;
	}

	public CodeLineInterface getBlockEnd() {
		return blockEnd;
	}

	public void setBlockEnd(CodeLineInterface blockEnd) {
		this.blockEnd = blockEnd;
	}

	public void addCatch(CodeLineInterface newCatch) {
		if (catches == null)
			catches = new LinkedList<CodeLineInterface>();
		catches.add(newCatch);
	}

	public TryBlock(int begin, int end, CodeLineInterface blockEnd) {
		super();
		this.begin = begin;
		this.end = end;
		this.blockEnd = blockEnd;
	}

	public TryBlock(int begin, int end, CodeLineInterface blockEnd,
			LinkedList<CodeLineInterface> catches) {
		super();
		this.begin = begin;
		this.end = end;
		this.blockEnd = blockEnd;
		this.catches = catches;
	}

	public TryBlock(int begin, int end, LinkedList<CodeLineInterface> catches) {
		super();
		this.begin = begin;
		this.end = end;
		this.catches = catches;
	}

	public TryBlock(int begin, int end) {
		super();
		this.begin = begin;
		this.end = end;
	}
}
