/**
 * 
 */
package de.rub.syssec.saaf.analysis.steps.obfuscation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.analysis.steps.AbstractStep;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.misc.config.ConfigKeys;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.FieldInterface;
import de.rub.syssec.saaf.model.application.MethodInterface;

/**
 * Detects obfuscated classes based on median length of members.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class LengthBasedDetectObfuscationStep extends AbstractStep {

	private static final int MAX_LENGTH = 11;
	private static final int MIN_LENGTH = 6;


	public static double median(ArrayList<Integer> allLengths) {
		Collections.sort(allLengths);
		int middle = allLengths.size() / 2;
		if (allLengths.size() % 2 == 1) {
			return allLengths.get(middle);
		} else {
			return (allLengths.get(middle - 1) + allLengths.get(middle)) / 2.0;
		}
	}

	public LengthBasedDetectObfuscationStep(Config cfg, boolean enabled) {
		this.logger = Logger.getLogger(getClass());
		this.config = cfg;
		this.name = "Obfuscation Check";
		this.description = "Detects obfuscation by checking the length of methods and field";
		this.enabled = enabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.analysis.steps.AbstractStep#doProcessing(de.rub.syssec
	 * .saaf.model.analysis.AnalysisInterface)
	 */
	@Override
	protected boolean doProcessing(AnalysisInterface analysis)
			throws AnalysisException {
		for (ClassInterface smaliClass : analysis.getApp().getAllSmaliClasss(
				true)) {
			analyze(smaliClass);
		}
		return true;
	}

	private void analyze(ClassInterface smaliClass) {
		ArrayList<Integer> allLengths = new ArrayList<Integer>();
		ArrayList<Integer> methodNameLengths = new ArrayList<Integer>();
		ArrayList<Integer> fieldNameLengths = new ArrayList<Integer>();

		//add the length of classname to the list of all name lengths
		allLengths.add(smaliClass.getClassName().length());
		//iterate over the methods
		int length=0;
		for(MethodInterface method : smaliClass.getMethods())
		{
			//get the length of the method name
			length=method.getName().length();
			//add it to the list of method name lengths
//			methodNameLengths.add(length);
			//add it to the list of all name lengths
			allLengths.add(length);
			//if it is not inside the interval
			if(length<MIN_LENGTH || length>MAX_LENGTH)
			{
				//mark method as obfuscated
				method.setObfuscated(true);
			}
		}
		//iterate over the fields
		for(FieldInterface field : smaliClass.getAllFields())
		{
			//get the length of the field name
			length = field.getFieldName().length();
			//add it to the list of field name lengths
			//fieldNameLengths.add(length);
			//add it to the list of all name lengths
			allLengths.add(length);
			//if it is not inside the interval mark field as obfuscated
			if(length<MIN_LENGTH || length>MAX_LENGTH)
			{
				//mark method as obfuscated
				field.setObfuscated(true);
			}
		}
		double median = median(allLengths);
		if(median<MIN_LENGTH || median>MAX_LENGTH)
		{
			smaliClass.setObfuscated(true);
		}
		
	}
}
