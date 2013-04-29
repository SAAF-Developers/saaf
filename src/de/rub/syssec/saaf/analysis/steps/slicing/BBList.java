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

import java.util.HashSet;
import java.util.LinkedList;

import de.rub.syssec.saaf.model.application.BasicBlockInterface;

public class BBList {
	
	
	private LinkedList<BasicBlockInterface> path = new LinkedList<BasicBlockInterface>();
	/**
	 * Holds information about the current path, eg, what register is tracked where in the path
	 */
	private LinkedList<byte[]> pathState = new LinkedList<byte[]>();
	private LinkedList<BasicBlockInterface> previousPath = null;
	private HashSet<BasicBlockInterface> visited = new HashSet<BasicBlockInterface>();
	private boolean firstTime = true;
	private final boolean isBackwardSearch;
	
	/**
	 * Create a new list of BBs which automatically handles the path for each BB which is found by a depth-first search.
	 * 
	 * @param firstBB the first BB of the new path, it will be returned on the first call of getNextBB()
	 * @param pathState Can be used to denote the tracked register for the current BB, it can be overridden if it changes while handling new BBs
	 * @param isBackwardSearch are BBs searched forward (next) of backwards (previous)
	 */
	public BBList(BasicBlockInterface firstBB, byte[] pathState, boolean isBackwardSearch) {
		path.add(firstBB);
		this.pathState.add(pathState);
		this.isBackwardSearch = isBackwardSearch;
	}

	/**
	 * Create a new list of BBs which automatically handles the path for each BB which is found by a depth-first search. This method will
	 * store the previous path and will search for other BBs from the last BB in the path.
	 * 
	 * @param previousPath the actual BB is the last BB in the given path, only the last BB will be returned on the first call to getNextBB().
	 * @param pathState Can be used to denote the tracked register for the current BB, it can be overridden if it changes while handling new BBs
	 * @param isBackwardSearch are BBs searched forward (next) or backwards (previous)
	 */
	public BBList(LinkedList<BasicBlockInterface> previousPath, byte[] pathState, boolean isBackwardSearch) {
		this.previousPath = previousPath;
		this.isBackwardSearch = isBackwardSearch;
		// the last one is the starting BB
		BasicBlockInterface last = this.previousPath.removeLast();
		path.add(last);
		this.pathState.add(pathState);

		/*
		 *  Avoid loops.
		 *  This next command is not ok as the previous path might be a->b->c and from c it
		 *  goes back to b where something of interest can be found:
		 *  if (isBackwardSearch) visited.addAll(this.previousPath);
		 *  But we have to add 'last' to the visited list as a BB might reference itself.
		 */
		visited.add(last);
	}
	
	/**
	 * Create a new list of BBs which automatically handles the path for each BB which is found by a depth-first search.
	 * The search is conducted backwards.
	 * 
	 * @param firstBB the first BB of the new path, it will be returned on the first call of getNextBB()
	 * @param pathState Can be used to denote the tracked register for the current BB, it can be overridden if it changes while handling new BBs
	 */
	public BBList(BasicBlockInterface firstBB, byte[] pathState) {
		this(firstBB, pathState, true);
	}
	
	/**
	 * Create a new list of BBs which automatically handles the path for each BB which is found by a depth-first search. This method will
	 * store the previous path and will search for other BBs from the last BB in the path. The search is conducted backwards.
	 * 
	 * @param previousPath the actual BB is the last BB in the given path, only the last BB will be returned on the first call to getNextBB().
	 * @param pathState Can be used to denote the tracked register for the current BB, it can be overridden if it changes while handling new BBs
	 */
	public BBList(LinkedList<BasicBlockInterface> previousPath, byte[] pathState) {
		this(previousPath, pathState, true);
	}
	
	
	/**
	 * Returns BBs which are found during the DFS until no new BBs can be found. The first BB will always be
	 * returned on the first call, see constructor. The path is automatically stored and the current one can be
	 * retrieved with the getPathForLastBB() method.
	 * 
	 * @return the next BB according to the DFS, no BB will be returned twice
	 */
	public BasicBlockInterface getNextBb() {
		// return the first BB which was supplied in the constructor for the first call
		if (firstTime) {
			firstTime = false;
			return path.getFirst(); // there is only one
		}
				
		BasicBlockInterface current;
		BasicBlockInterface retBB = null; // the bb to be returned
		
		while (!path.isEmpty() && retBB == null) { // search until retBB is found or no more BBs are available
			current = path.getLast();
			LinkedList<BasicBlockInterface> blocks;
			if (isBackwardSearch) blocks = current.getPreviousBB();
			else blocks = current.getNextBB();
			for (BasicBlockInterface bb : blocks) { // search next successor
				if (!visited.contains(bb)) {
					retBB = bb; // this is the next in the DFS and will be returned
					visited.add(bb);
					path.add(bb); // add the current BB to the path
					pathState.add(pathState.getLast()); // save the state
					break;
				}
			}
			if (retBB == null) { // no successor found, go backwards in path
				// remove the last and search for a new one in the last BB
				removeLastBBFromList();
			}
		}
		return retBB;
	}
	
	/**
	 * Remove the last BB from the current path and search for the next BB according to the DFS.
	 */
	public void removeLastBBFromList() {
		if (!path.isEmpty()) {
			path.removeLast();
			pathState.removeLast();
		}
	}
	
	/**
	 * Add a new state to the current BB in the path, it will be propagated to the next (previous) BBs in the path.
	 * @param state
	 */
	public void setNewStateforCurrentBB(byte[] state) {
		pathState.pop();
		pathState.addLast(state);
	}
	
	/**
	 * Get the saved state for the current BB.
	 */
	public byte[] getState() {
		return pathState.getLast();
	}
	
	/**
	 * Get the path, which is always a new object.
	 * @return the path
	 */
	public LinkedList<BasicBlockInterface> getPathForLastBB() {
		LinkedList<BasicBlockInterface> ret = new LinkedList<BasicBlockInterface>();
		if (previousPath != null) ret.addAll(previousPath);
		if (!firstTime) { // as long as getNextBB() is not called, there is no path, but the initial BB is already added
			ret.addAll(path);
		}
		return ret;
	}
	
	/**
	 * Prints the path in a semi readable manner :)
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PATH:        ");
		for (BasicBlockInterface bb : path) {
			sb.append(bb.getLabel());
			sb.append("\t");
		}
		sb.append("\nSTATE:     ");
		for (byte[] bb : pathState) {
			sb.append(new String(bb));
			sb.append("\t");
		}
		sb.append("\nPREV PATH: ");
		for (BasicBlockInterface bb : previousPath) {
			sb.append(bb.getLabel());
			sb.append("\t");
		}
		sb.append("\nisBackwards: ");
		sb.append(isBackwardSearch);
		sb.append("\npathState: ");
		return sb.toString();
	}
}
