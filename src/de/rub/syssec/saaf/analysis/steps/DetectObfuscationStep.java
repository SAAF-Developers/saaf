/**
 * 
 */
package de.rub.syssec.saaf.analysis.steps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.FieldInterface;
import de.rub.syssec.saaf.model.application.MethodInterface;

/**
 * @author tbender
 *
 */
public class DetectObfuscationStep extends AbstractStep {

	
	private static final double ENTROPY_CONSTANT = 2.25;

	public static double entropy(String name) {
		 
        final Map<Character, Long> numberOfOccurences = new HashMap<Character, Long>();
        //count character frequency
        for (char c : name.toCharArray()) {
            Long occurrance = numberOfOccurences.get(c);
            numberOfOccurences.put(c, occurrance == null ? 1L : ++occurrance);
        }
 
        double combinedEntropy = 0.0d;
        double probability;
        for (Character c : numberOfOccurences.keySet()) {
        	//calculate probability of the symbol
            probability = numberOfOccurences.get(c) / (double) name.length();
            
            combinedEntropy += probability * (Math.log(probability) / Math.log(2));
        }
 
        return -combinedEntropy;
    }
	
	public static double median(List<Double> values) {
	    Collections.sort(values);
		int middle = values.size()/2;
	    if (values.size()%2 == 1) {
	        return values.get(middle);
	    } else {
	        return (values.get(middle-1) + values.get(middle)) / 2.0;
	    }
	}
	
	public DetectObfuscationStep(Config cfg,boolean enabled)
	{
		this.logger = Logger.getLogger(getClass());
		this.config = cfg;
		this.name = "Obfuscation Check";
		this.description = "Calculates String entropy of class and method names to detect obfuscation";
		this.enabled = enabled;
	}
	
	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.analysis.steps.AbstractStep#doProcessing(de.rub.syssec.saaf.model.analysis.AnalysisInterface)
	 */
	@Override
	protected boolean doProcessing(AnalysisInterface analysis)
			throws AnalysisException {
		logger.info("Calculating shannon entropy for all class names");

		for(ClassInterface smaliClass : analysis.getApp().getAllSmaliClasss(true))
		{
			double entropy = entropy(smaliClass);
			smaliClass.setEntropy(entropy);
			logger.info("Entropy for class "+smaliClass.getClassName()+": "+entropy);

			if(entropy<ENTROPY_CONSTANT)
			{
				smaliClass.setObfuscated(true);
				logger.info("Class "+smaliClass.getClassName()+ " is potentially obfuscated");
			}			
		}
		return true;
	}
	
	public double entropy(ClassInterface smaliClass)
	{
		logger.info("Checking class "+smaliClass.getClassName()+" fo obfuscation");
		List<Double> entropies = new ArrayList<Double>();
		double classNameEntropy = entropy(smaliClass.getClassName());
		entropies.add(classNameEntropy);
		for(MethodInterface method : smaliClass.getMethods())
		{
			double entropy = entropy(method);
			method.setEntropy(entropy);
			if(entropy<ENTROPY_CONSTANT)
			{
				smaliClass.setObfuscated(true);
				logger.info("Method "+smaliClass.getClassName()+ " is potentially obfuscated");
			}
			entropies.add(entropy);
		}
		double entropy = median(entropies);
		logger.info("Entropy for class "+smaliClass.getClassName()+" "+entropy);
		return  entropy;

	}
	
	public double entropy(FieldInterface field)
	{
		return entropy(field.getFieldName());
	}
	
	public double entropy(MethodInterface method)
	{
		double methodNameEntropy=entropy(method.getName());
		double fieldEntropy=0.0;
		List<Double> entropies = new ArrayList<Double>();
		entropies.add(methodNameEntropy);
		for(FieldInterface field : method.getLocalFields())
		{
			fieldEntropy = entropy(field);
			entropies.add(fieldEntropy);
			field.setEntropy(fieldEntropy);
		}
		return median(entropies);
	}

}
