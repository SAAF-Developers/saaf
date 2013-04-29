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


import de.rub.syssec.saaf.model.application.BasicBlockInterface;
import de.rub.syssec.saaf.model.application.DetectionLogicError;
import de.rub.syssec.saaf.model.application.MethodInterface;

/**
 * Depth first search. Label BasicBlocks in a method. The first BB is labeled 0.
 */
public class DFS {
	
//	private static final Logger logger = Logger.getLogger(DFS.class);
	private int label = 0;
	
	/**
	 * Assign labels (ints) to all BasicBlocks from a method.
	 * @param m
	 * @throws DetectionLogicError if not all vertexes are reached in the DFS
	 */
	public void labelAllBB(MethodInterface m) throws DetectionLogicError {
		if (m.isEmpty()) return;
		BasicBlockInterface firstBB = m.getFirstBasicBlock();
		firstBB.setLabel(label++); // First BB is labeled w/ 0
		label(firstBB); // The second w/ 1
//		// Test code:
		int cnt = 0;
		for (BasicBlockInterface bb : m.getBasicBlocks()) {
			if (bb.getLabel() < 0) {
				cnt++;
			}
		}
		if (cnt > 0) {
			//Method most likely patched, therefore:
			m.setHasUnlinkedBlocks(true);
		}
	}
	
	private void label(BasicBlockInterface bb) {
		for (BasicBlockInterface bbNext : bb.getNextBB()) {
			if (bbNext.getLabel() < 0) { // not set
				bbNext.setLabel(label++);
				label(bbNext); // recursive call
			}
			else {
				// is back edge
			}
		}
	}
}
