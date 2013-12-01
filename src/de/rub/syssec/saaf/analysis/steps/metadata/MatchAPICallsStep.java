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
package de.rub.syssec.saaf.analysis.steps.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.analysis.steps.AbstractStep;
import de.rub.syssec.saaf.application.manifest.permissions.Permission;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.APICall;
import de.rub.syssec.saaf.model.APICalls;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.instruction.InstructionType;

/**
 * Read in APICalls.txt during startup and match apicalls onto apk.
 * 
 */
public class MatchAPICallsStep extends AbstractStep {

	private static final Logger logger = Logger
			.getLogger(MatchAPICallsStep.class);

	/**
	 * @param enabled
	 *            TODO
	 */
	public MatchAPICallsStep(Config cfg, boolean enabled) {
		super();
		this.config = cfg;
		this.name = "Match Permissions";
		this.description = "Matches the APIcalls onto a set of currently known Permissions.";
		this.enabled = enabled;
	}

	@Override
	protected boolean doProcessing(AnalysisInterface analysis)
			throws AnalysisException {
		logger.info("Searching apicalls of application " + analysis.getApp());
		matchCalls(analysis.getApp());
		return true;
	}

	/**
	 * This method tries to match all the codelines in the app onto known 
	 * permissions based on the android permission map and returns the 
	 * resulting mappings.
	 */
	public void matchCalls(ApplicationInterface app) {

		HashMap<CodeLineInterface, APICall> matchedCalls = new HashMap<CodeLineInterface, APICall>();

		List<CodeLineInterface> foundCalls = findCalls(app);
		app.setFoundCalls(foundCalls);

		for (CodeLineInterface c : foundCalls) {
			String className = new String(c.getInstruction()
					.getCalledClassAndMethodWithParameter()[0]).replaceAll("/",
					".");
			for (APICall apiCall : APICalls.getCalls()) {
				//check if the called method is matched by this APICall
				String method = new String(c.getInstruction().getCalledClassAndMethodWithParameter()[1]);
				if (apiCall.getCall().contains(className+ "." + method)) {

					// params need to be converted for matching....
					// params
					String params = new String(c.getInstruction()
							.getCalledClassAndMethodWithParameter()[2]);
				
					//build a string from class,method and params and match it agains the APICall
					if (apiCall.getCall().contains(className
											+ "."
											+ new String(
													method)
											+ "(" + params + ")")) {
						/*
						 * TODO: this needs to also be saved in the smali data
						 * itself instead of just in this copy
						 */
						c.setPermission(new Permission(apiCall.getCall()));
						matchedCalls.put(c, apiCall);

					}

				}
			}
			app.setMatchedCalls(matchedCalls);
		}
	}

	/**
	 * @param app
	 * @return
	 */
	private List<CodeLineInterface> findCalls(ApplicationInterface app) {
		List<CodeLineInterface> foundCalls = new ArrayList<CodeLineInterface>();
		// better var names
		for (ClassInterface smaliClass : app.getAllSmaliClasss(false)) {
			for (CodeLineInterface codeline : smaliClass.getAllCodeLines()) {
				if (codeline.getInstruction().getType().equals(InstructionType.INVOKE) || 
					codeline.getInstruction().getType().equals(InstructionType.INVOKE_STATIC)) 
				{
					foundCalls.add(codeline);
				}
			}
		}
		return foundCalls;
	}

}
