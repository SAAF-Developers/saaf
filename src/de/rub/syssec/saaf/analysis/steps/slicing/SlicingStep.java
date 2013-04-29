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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.analysis.steps.AbstractStep;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.SAAFException;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.analysis.BTPatternInterface;
import de.rub.syssec.saaf.model.analysis.BTResultInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ConstantInterface;
import de.rub.syssec.saaf.model.application.DetectionLogicError;

/**
 * Triggers a backtracking search for parameters of interesting methods.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class SlicingStep extends AbstractStep {
	
	
	private List<BTPatternInterface> backtrackPatterns;
	private static final Logger LOGGER = Logger.getLogger(SlicingStep.class);
	
	public SlicingStep(Config cfg, List<BTPatternInterface> backtrackPatterns, boolean enabled)
	{
		this.config = cfg;
		this.name = "Backtracking";
		this.description = "Triggers a backtracking search for parameters of interesting methods";
		this.backtrackPatterns =(backtrackPatterns!=null)?backtrackPatterns:new ArrayList<BTPatternInterface>();
		this.enabled = enabled;
	}

	@Override
	public boolean doProcessing(AnalysisInterface analysis) throws AnalysisException {
		LOGGER.info("Start BackTrack...");
		try {
			ApplicationInterface app = analysis.getApp();
					List<BTResultInterface> bt_results = new LinkedList<BTResultInterface>();
					DetectionLogic dl = new DetectionLogic(app);
					List<SAAFException> nonCriticalExceptions = new ArrayList<SAAFException>();
					boolean exceptionInBackTrack=false;
					
					if (backtrackPatterns.isEmpty()) {
						LOGGER.warn("No backtracking-patterns to analyze. Stopping analysis");
						return false;
					}
					LOGGER.debug("Analyzing "+app.getApplicationName()+" using "+backtrackPatterns.size()+" backtracking-patterns.");

					for (BTPatternInterface p : backtrackPatterns) {
							if(p.isActive())
							{
								SlicingCriterion br = new SlicingCriterion(
										p.getQualifiedClassName(),
										p.getMethodName(),
										p.getArgumentsTypes(),
										p.getParameterOfInterest());
								dl.search(br);
								for (ConstantInterface c : br.getResults()) {
									LOGGER.debug("Adding const:\n"+c);
									bt_results.add(new BTResult(analysis,p,c,p.getParameterOfInterest()));
								}
								if (!br.isCleanAnalysis()) 
								{
									exceptionInBackTrack = true;
									for(Throwable t : br.getExceptionList())
									{
										nonCriticalExceptions.add(new SAAFException(t.getMessage(), t, analysis));
									}
								}
							}

					}
					LOGGER.info("Finished BackTrack search"
							+ " for Application " + app.getApplicationName()
							+ " with " + bt_results.size() + " Results"
							+ " " + (exceptionInBackTrack ? "[Finished with Exceptions]" : ""));
					analysis.setBTResults(bt_results);
					analysis.setNonCriticalExceptions(nonCriticalExceptions);
					return true;
		} catch (DetectionLogicError e) {
			throw new AnalysisException(e);
		}
	}
	


}
