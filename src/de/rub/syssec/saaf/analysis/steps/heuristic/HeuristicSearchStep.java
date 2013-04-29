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
package de.rub.syssec.saaf.analysis.steps.heuristic;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.analysis.steps.AbstractStep;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.analysis.HPatternInterface;
import de.rub.syssec.saaf.model.analysis.HResultInterface;

/**
 * Searches for heuristic patterns.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class HeuristicSearchStep extends AbstractStep {
	
	private List<HPatternInterface> patterns;
	
	public HeuristicSearchStep(Config cfg, List<HPatternInterface> patterns,boolean enabled)
	{
		this.logger = Logger.getLogger(getClass());
		this.config = cfg;
		this.name = "Heuristic Search";
		this.description = "Triggers heuristic search for interesting patterns.";
		this.patterns = (patterns!=null)?patterns:new ArrayList<HPatternInterface>();
		this.enabled = enabled;
		
		//TODO Split patterns into sets of different types
		//register sets of patterns in a Map<Type,List<HPattern>
		//create subststeps for different kinds of patterns
		//register different substeps in a Map<Type,AnalysisStep> 
	}

	/* (non-Javadoc)
	 * @see steps.analysis.AbstractAnalysisStep#process(de.rub.syssec.saaf.model.analysis.AnalysisInterface)
	 */
	@Override
	public boolean doProcessing(AnalysisInterface analysis) throws AnalysisException {
		
		if (patterns.isEmpty()) {
			logger.warn("No heuristic-patterns to analyze. Stopping analysis");
			return false;
		}
		
		
		Heuristic myHeu = new Heuristic(patterns);
		//TODO could we merge the Heuristic Class into this step?
		logger.debug("Analyzing "+analysis.getApp().getApplicationName()+" using "+patterns.size()+" heuristic patterns.");
		List<HResultInterface>h_results = myHeu.check(analysis);
		logger.debug("Found "+h_results.size()+" results.");
		analysis.setHResults(h_results);
		return true;
	}
	
	
}
