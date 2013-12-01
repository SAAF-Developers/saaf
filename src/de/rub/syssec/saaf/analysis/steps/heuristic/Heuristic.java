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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.analysis.HPatternInterface;
import de.rub.syssec.saaf.model.analysis.HResultInterface;
import de.rub.syssec.saaf.model.analysis.PatternType;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.MethodInterface;
import de.rub.syssec.saaf.model.application.instruction.InstructionInterface;
import de.rub.syssec.saaf.model.application.instruction.InstructionType;
import de.rub.syssec.saaf.model.application.manifest.ManifestInterface;

/**
 * Class to quickly check an APK against certain things.
 * <p>
 * Two examples would be the search for invocations of certain methods and
 * whether some String can be found in the smali files.
 * </p>
 * 
 * TODO: This class needs a lot of love.
 * 
 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
 * @author Johannes Hoffmann <johannes.hofffmann@rub.de>
 * 
 */
public class Heuristic {
	private static final boolean DEBUG = Boolean.parseBoolean(System
			.getProperty("debug.heuristic", "false"));
	private static final String DEBUG_FILE = System.getProperty(
			"debug.heuristic.file", "perf.log.bak");
	private static final Logger LOGGER = Logger.getLogger(Heuristic.class);
	private List<HPatternInterface> patterns;

	public Heuristic(List<HPatternInterface> patterns) {
		this.patterns = patterns;
	}

	/**
	 * Check the APK against all set patterns.
	 * 
	 * @param ana
	 *            the analysis holding representing the APK
	 * @return the results
	 */
	public LinkedList<HResultInterface> check(AnalysisInterface ana) {
		LinkedList<HResultInterface> hResults = new LinkedList<HResultInterface>();

		if (this.patterns.isEmpty()) {
			LOGGER.error("The list of heuristic patterns is empty. Not checking anything.");
			return new LinkedList<HResultInterface>();
		}

		// sort Pattern by PatternType
		LinkedList<HPatternInterface> manifestPattern = new LinkedList<HPatternInterface>();
		LinkedList<HPatternInterface> invokePattern = new LinkedList<HPatternInterface>();
		LinkedList<HPatternInterface> smaliPattern = new LinkedList<HPatternInterface>();
		LinkedList<HPatternInterface> methodPattern = new LinkedList<HPatternInterface>();
		LinkedList<HPatternInterface> superclassPattern = new LinkedList<HPatternInterface>();
		LinkedList<HPatternInterface> patchedCodePattern = new LinkedList<HPatternInterface>();

		for (HPatternInterface pattern : patterns) {
			if (pattern.isActive()) {
				switch (pattern.getSearchin()) {
				case MANIFEST:
					manifestPattern.add(pattern);
					break;
				case INVOKE:
					invokePattern.add(pattern);
					break;
				case SMALI:
					smaliPattern.add(pattern);
					break;
				case METHOD_MOD:
					methodPattern.add(pattern);
					break;
				case SUPERCLASS:
					superclassPattern.add(pattern);
					break;
				case PATCHED_CODE:
					patchedCodePattern.add(pattern);
					break;
				default:
					LOGGER.warn("Unknown pattern type detected, ignoring.");
				}
			}
		}

		Date d1 = new Date(System.currentTimeMillis());
		Date d2 = d1;
		Date d3 = d1;
		Date d4 = d1;
		Date d5 = d1;
		Date d6 = d1;
		Date d7 = d1;
		Date d8 = d1;
		Config conf = Config.getInstance();
		// check for MANIFEST-Pattern
		if (DEBUG)
			d1 = Calendar.getInstance().getTime();
		if (conf.getBooleanConfigValue(ConfigKeys.HEURISTIC_PATTERN_MANIFEST)) {
			LOGGER.debug("Start HeuristicSearch for " + manifestPattern.size()
					+ " Manifest-Pattern.");
			hResults.addAll(checkManifest(ana, manifestPattern));
		}

		// check for INVOKE-Pattern (only invokes given in a special format)
		if (DEBUG)
			d2 = Calendar.getInstance().getTime();
		if (conf.getBooleanConfigValue(ConfigKeys.HEURISTIC_PATTERN_INVOKE)) {
			LOGGER.debug("Start HeuristicSearch for " + invokePattern.size()
					+ " INVOKE-Pattern.");
			hResults.addAll(checkInvoke(ana, invokePattern));
		}

		// check for SMALI-Pattern (search only text in SMALI Files)
		if (DEBUG)
			d3 = Calendar.getInstance().getTime();
		if (conf.getBooleanConfigValue(ConfigKeys.HEURISTIC_PATTERN_SMALI)) {
			LOGGER.debug("Start HeuristicSearch for " + smaliPattern.size()
					+ " SMALI-Pattern.");
			hResults.addAll(checkSmali(ana, smaliPattern));
		}

		if (DEBUG)
			d4 = Calendar.getInstance().getTime();
		if (conf.getBooleanConfigValue(ConfigKeys.HEURISTIC_PATTERN_METHOD_MOD)) {
			LOGGER.debug("Start HeuristicSearch for " + methodPattern.size()
					+ " METHOD_MOD-Pattern.");
			hResults.addAll(checkMethodDeclaration(ana, methodPattern));
		}

		if (DEBUG)
			d5 = Calendar.getInstance().getTime();
		if (conf.getBooleanConfigValue(ConfigKeys.HEURISTIC_PATTERN_SUPERCLASS)) {
			LOGGER.debug("Start HeuristicSearch for "
					+ superclassPattern.size() + " SUPERCLASS-Pattern.");
			hResults.addAll(checkInheritance(ana, superclassPattern));
		}

		if (DEBUG)
			d6 = Calendar.getInstance().getTime();
		if (conf.getBooleanConfigValue(ConfigKeys.HEURISTIC_SEARCH_PATCHED_CODE)) {
			LOGGER.debug("Start HeuristicSearch for "
					+ patchedCodePattern.size() + " PATCHED_CODE-Pattern.");
			hResults.addAll(searchPatchedCode(ana, patchedCodePattern));
		}

		if (DEBUG)
			d7 = Calendar.getInstance().getTime();
		int sumHValue = sumHValue(hResults);
		ana.setHeuristicValue(sumHValue);
		LOGGER.debug("Finished HeuristicSearch" + " for Application "
				+ ana.getApp().getApplicationName() + " with "
				+ hResults.size() + " Results"
				+ " and summed HeuristicResult = " + sumHValue);
		if (DEBUG)
			d8 = Calendar.getInstance().getTime();

		if (DEBUG) {
			LOGGER.debug(ana.getApp().getApplicationName()
					+ " Manifest-Pattern time:    " + diffTime(d1, d2));
			LOGGER.debug(ana.getApp().getApplicationName()
					+ " INVOKE-Pattern time:      " + diffTime(d2, d3));
			LOGGER.debug(ana.getApp().getApplicationName()
					+ " SMALI-Pattern time:       " + diffTime(d3, d4));
			LOGGER.debug(ana.getApp().getApplicationName()
					+ " METHOD_MOD-Pattern time:  " + diffTime(d4, d5));
			LOGGER.debug(ana.getApp().getApplicationName()
					+ " SUPERCLASS-Pattern time:  " + diffTime(d5, d6));
			LOGGER.debug(ana.getApp().getApplicationName()
					+ " PATCHED_CODE-Pattern time:  " + diffTime(d6, d7));
			LOGGER.debug(ana.getApp().getApplicationName()
					+ " Sum&Convert Results time: " + diffTime(d7, d8));
			LOGGER.debug(ana.getApp().getApplicationName()
					+ " Total time:               " + diffTime(d1, d8));
		}

		// if (DEBUG) {
		// // Achtung es hängt sehr von der Reihenfolge der Aufrufe ab
		// // WENN checkINVOKE nach checkSMALI aufgerufen wird, braucht es im
		// // Schnitt nur 1 ms ansonsten 30ms
		// // TODO: Test mit größerem Sample und mehr INVOKE Pattern
		// String name = "INV";
		// Writer fw = null;
		// try {
		// fw = new FileWriter(DEBUG_FILE, true);
		// fw.write("Performance: \t " + name + "\t" + d1.toString()
		// + "\t" + (d2.getTime() - d1.getTime()) + "\t"
		// + (d3.getTime() - d2.getTime()) + "\t"
		// + (d4.getTime() - d3.getTime()) + "\t"
		// + (d5.getTime() - d4.getTime()) + "\t"
		// + (d5.getTime() - d1.getTime()) + "\n");
		// } catch (IOException e) {
		// LOGGER.info("Could not create file.");
		// } finally {
		// if (fw != null) try { fw.close(); } catch (IOException ignore) { }
		// }
		// }
		return hResults;
	}

	/**
	 * Checks an APK against all patterns related to the Manifest file.
	 * 
	 * @param ana
	 *            the analysis representing the APK
	 * @param pattern
	 *            the pattern
	 * @return all found results
	 */
	private LinkedList<HResultInterface> checkManifest(AnalysisInterface ana,
			LinkedList<HPatternInterface> pattern) {
		LinkedList<HResultInterface> hResults = new LinkedList<HResultInterface>();
		ManifestInterface manifest = ana.getApp().getManifest();
		if (manifest != null) {
			for (HPatternInterface hPat : pattern) {
				boolean patternMatched = true;
				String[] patterns = hPat.getPattern().split("\\s+");
				for (int z = 0; z < patterns.length; z++) {
					if (patterns[z].trim().equals("noActivity")) {
						patternMatched &= manifest.hasNoActivities();
					} else if (patterns[z].trim().equals("priorityBR")) {
						patternMatched &= manifest.hasPriorityBR();
					} else {
						patternMatched &= manifest.hasPermission("android."
								+ patterns[z].trim());
					}
					if (!patternMatched) {
						/*
						 * b/c all checks are ANDed with patternMatched we can
						 * bail out if it is false at any time
						 */
						break;
					}
				}
				if (patternMatched) {
					hResults.add(new HResult(ana, hPat));
				}
			}
		}else{
			LOGGER.warn("Could not find a Manifest. Skipping manifest patterns.");
		}
		return hResults;
	}

	/**
	 * Checks an APK against all patterns related to invoke opcodes.
	 * 
	 * @param ana
	 *            the analysis representing the APK
	 * @param pattern
	 *            the pattern
	 * @return all found results
	 */
	private LinkedList<HResultInterface> checkInvoke(AnalysisInterface ana,
			LinkedList<HPatternInterface> pattern) {
		LinkedList<HResultInterface> hResults = new LinkedList<HResultInterface>();
		for (HPatternInterface hPat : pattern) {
			hResults.addAll(findInvokePattern(ana, hPat));
		}
		return hResults;
	}

	/**
	 * Checks an APK against all patterns related to smali bytecode.
	 * 
	 * @param ana
	 *            the analysis representing the APK
	 * @param pattern
	 *            the pattern
	 * @return all found results
	 */
	private LinkedList<HResult> checkSmali(AnalysisInterface ana,
			LinkedList<HPatternInterface> pattern) {
		LinkedList<HResult> hResults = new LinkedList<HResult>();
		LinkedList<ClassInterface> appFiles = ana.getApp().getAllSmaliClasss(
				Config.getInstance().getBooleanConfigValue(
						ConfigKeys.ANALYSIS_INCLUDE_AD_FRAMEWORKS));
		for (ClassInterface sf : appFiles) {
			LinkedList<CodeLineInterface> codeLines = sf.getAllCodeLines();
			for (CodeLineInterface cl : codeLines) {
				for (HPatternInterface hPat : pattern) {
					if (cl.contains(hPat.getPattern().getBytes()))
						hResults.add(new HResult(ana, hPat, cl));
				}
			}
		}
		return hResults;
	}

	/**
	 * Search for methods which contain dead code. This might be an indicator
	 * for a patched program as the compiler would normally not generate such
	 * code. See {@link BasicBlockInterface.hasDeadCode()} and {@link
	 * MethodInterface.hasUnlinkedBBs()} for more information about this.
	 * 
	 * @param ana
	 *            the analysis representing the APK
	 * @param pattern
	 *            pattern the pattern
	 * @return all found results
	 */
	private LinkedList<HResult> searchPatchedCode(AnalysisInterface ana,
			LinkedList<HPatternInterface> pattern) {
		LinkedList<HResult> hResults = new LinkedList<HResult>();
		LinkedList<ClassInterface> appFiles = ana.getApp().getAllSmaliClasss(
				Config.getInstance().getBooleanConfigValue(
						ConfigKeys.ANALYSIS_INCLUDE_AD_FRAMEWORKS));
		for (ClassInterface sf : appFiles) {
			for (MethodInterface method : sf.getMethods()) {
				if (method.isProbablyPatched()) {
					for (HPatternInterface hPat : pattern) {
						hResults.add(new HResult(ana, hPat, method
								.getCodeLines().getFirst()));
					}
				}
			}
		}
		return hResults;
	}

	/**
	 * Searches for a pattern in all method declarations. Used, eg, to search
	 * for 'native' methods.
	 * 
	 * @param ana
	 *            ana the analysis representing the APK
	 * @param pattern
	 *            pattern the pattern
	 * @return all found results
	 */
	private LinkedList<HResultInterface> checkMethodDeclaration(
			AnalysisInterface ana, LinkedList<HPatternInterface> pattern) {
		LinkedList<HResultInterface> hResults = new LinkedList<HResultInterface>();
		LinkedList<ClassInterface> appFiles = ana.getApp().getAllSmaliClasss(
				Config.getInstance().getBooleanConfigValue(
						ConfigKeys.ANALYSIS_INCLUDE_AD_FRAMEWORKS));
		for (ClassInterface sf : appFiles) {
			for (HPatternInterface hPat : pattern) {
				for (MethodInterface m : sf.getEmptyMethods()) {
					if (m.getCodeLines().getFirst()
							.contains(hPat.getPattern().getBytes())) {
						hResults.add(new HResult(ana, hPat, m.getCodeLines()
								.getFirst()));
					}
				}
			}
		}
		return hResults;
	}

	/**
	 * This method checks against pattern which check whether some method is
	 * invoked in the APK or not.
	 * 
	 * @param ana
	 *            the analysis representing the APK
	 * @param pattern
	 *            pattern the pattern
	 * @return all found results
	 */
	private LinkedList<HResultInterface> findInvokePattern(
			AnalysisInterface ana, HPatternInterface pattern) {
		LinkedList<HResultInterface> hResults = new LinkedList<HResultInterface>();
		// Where to search
		LinkedList<ClassInterface> appFiles = ana.getApp().getAllSmaliClasss(
				Config.getInstance().getBooleanConfigValue(
						ConfigKeys.ANALYSIS_INCLUDE_AD_FRAMEWORKS));
		// What to search
		if (pattern.getSearchin() == PatternType.INVOKE) {
			byte[][] cm = new byte[2][];
			String patternString = pattern.getPattern();
			String[] pat = patternString.split("->");
			cm[0] = pat[0].getBytes(); // Java package with class
			cm[1] = pat[1].getBytes(); // Java method
			for (ClassInterface sf : appFiles) {
				LinkedList<CodeLineInterface> codeLines = sf.getAllCodeLines();
				for (CodeLineInterface cl : codeLines) {
					InstructionInterface i = cl.getInstruction();
					if (i.getType() == InstructionType.INVOKE
							|| i.getType() == InstructionType.INVOKE_STATIC) {
						if (Arrays
								.equals(i.getCalledClassAndMethod()[0], cm[0])
								&& Arrays.equals(
										i.getCalledClassAndMethod()[1], cm[1])) {
							// we found the method!
							hResults.add(new HResult(ana, pattern, cl));
						}
					}
				}
			}
		}
		return hResults;
	}

	/**
	 * Search for all classes which extend some given other class (the
	 * superclass).
	 * 
	 * @param ana
	 *            the analysis representing the APK
	 * @param pattern
	 *            pattern the pattern
	 * @return all found results
	 */
	private LinkedList<HResultInterface> checkInheritance(
			AnalysisInterface analysis, LinkedList<HPatternInterface> pattern) {
		LinkedList<HResultInterface> hResults = new LinkedList<HResultInterface>();
		LinkedList<ClassInterface> appFiles = analysis.getApp()
				.getAllSmaliClasss(
						Config.getInstance().getBooleanConfigValue(
								ConfigKeys.ANALYSIS_INCLUDE_AD_FRAMEWORKS));

		for (HPatternInterface p : pattern) {
			String superClass = p.getPattern(); // search for this super class
			for (ClassInterface sf : appFiles) {
				if (superClass.equalsIgnoreCase(sf.getSuperClass())) {
					// use the first codeline as it refers to the class which
					// extends the superclass in question
					hResults.add(new HResult(analysis, p, sf.getAllCodeLines()
							.getFirst()));
				}
			}
		}
		return hResults;

	}

	/**
	 * TODO: Heuristic values are currently set to 0. This method will therefore
	 * also only return 0.
	 * 
	 * @param hResults
	 * @return
	 */
	private int sumHValue(LinkedList<HResultInterface> hResults) {
		// int x = 0;
		// for (HResultInterface hResult : hResults) {
		// x += hResult.getPattern().getHvalue();
		// }
		// return x;
		return 0;
	}

	private String diffTime(Date d1, Date d2) {
		Date tmp;
		long diff, ms, s, m;
		if (d2.before(d1)) {
			tmp = d2;
			d2 = d1;
			d1 = tmp;
		}
		diff = d2.getTime() - d1.getTime();
		ms = diff % 1000;
		diff /= 1000;
		s = diff % 60;
		diff /= 60;
		m = diff % 60;
		diff /= 60;
		return String.format("%02d:%02d:%02d.%04d", diff, m, s, ms);// h:mm:ss.ms
	}
}
