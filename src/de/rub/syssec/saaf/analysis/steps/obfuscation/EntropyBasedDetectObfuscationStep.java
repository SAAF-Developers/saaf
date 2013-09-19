/**
 * 
 */
package de.rub.syssec.saaf.analysis.steps.obfuscation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.analysis.steps.AbstractStep;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.FieldInterface;
import de.rub.syssec.saaf.model.application.MethodInterface;

/**
 * Detects obfuscated classes by calculating the entropy.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class EntropyBasedDetectObfuscationStep extends AbstractStep {

	
	private static final double ENTROPY_CONSTANT = 2.25;
	private static List<String> ignored = Arrays.asList("<clinit>","<init>",
	"onActivityResult",
	"onBind",
	"onChange",
	"onClick",
	"onCreate",
	"onDestroy",
	"onDisabled",
	"onEnabled",
	"onNewIntent",
	"onOpen",
	"onReceive",
	"onStartCommand",
	"onTerminate",
	"onTransact",
	"onUnbind",
	"onUpgrade");

	/**
	 * Calculates the entropy of a character string.
	 * 
	 * @see <a href="http://en.wikipedia.org/wiki/Entropy_%28information_theory%29#Definition"> Definition</a>
	 * @param name
	 * @return
	 */
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
	

	public EntropyBasedDetectObfuscationStep(Config cfg,boolean enabled)
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
			entropy(smaliClass);
			logger.info("Entropy for class "+smaliClass.getClassName()+": "+smaliClass.getEntropy().CMFEntropy);

			if(smaliClass.getEntropy().CMFEntropy<ENTROPY_CONSTANT)
			{
				smaliClass.setObfuscated(true);
				logger.info("Class "+smaliClass.getClassName()+ " is potentially obfuscated");
			}			
		}
		return true;
	}
	
	/**
	 * Calculates the entropy of a class.
	 * 
	 * This method actually calculates several versions of the entropy.
	 * 
	 * <ul>
	 * 	<li>entropy1: calculated from concatenating the classname and all method names </li>
	 *  <li>entropy2: calculated from concatenating class-,method- and fieldnames </li>
	 *  <li>entropy3: calculated as the average over the entropies of class-, method- and fieldnames</li>
	 * </ul>
	 * 
	 * @param smaliClass
	 * @return
	 */
	public void entropy(ClassInterface smaliClass)
	{
		logger.info("Checking class "+smaliClass.getClassName()+" fo obfuscation");
		List<Double> entropies = new ArrayList<Double>();
		//this is used to produce one large string from names of the class,methods and fields
		StringBuilder allNames = new StringBuilder(smaliClass.getClassName());
		entropies.add(entropy(smaliClass.getClassName()));
		double entropy=0.0;
		for(MethodInterface method : smaliClass.getMethods())
		{
			//ignore methods that are never obfuscated and only distort the stats
			if(isIgnored(method))
			{
				continue;
			}
			allNames.append(method.getName());
			entropy = entropy(method.getName());
			method.setEntropy(new Entropy(entropy));
			if(entropy<ENTROPY_CONSTANT)
			{
				smaliClass.setObfuscated(true);
				method.setObfuscated(true);
				logger.info("Method "+method.getReadableJavaName()+ " is potentially obfuscated");
			}else{
				method.setObfuscated(false);
				smaliClass.setObfuscated(false);
			}
			entropies.add(entropy);
		}
		Entropy e = new Entropy();
		//calculate entropy of concatenation of class-name and all method names
		e.CMEntropy= entropy(allNames.toString());
		for(FieldInterface field : smaliClass.getAllFields())
		{
			allNames.append(field.getFieldName());
			entropies.add(entropy(field.getFieldName()));
		}
		//calculate entropy of concatenation of class-, method- and fieldnames
		e.CMFEntropy = entropy(allNames.toString());
		//calculate the average of all separate entropies
		e.AverageEntropy = mean(entropies);		
		smaliClass.setEntropy(e);
	}

	private boolean isIgnored(MethodInterface method) {
		if(ignored.contains(method.getName()))
		{
			logger.info("Ignoring method "+method.getName());
			return true;
		}
		return false;
	}

	public double mean(List<Double> entropies) {
		double sum = 0.0;
		for( Double entropy : entropies)
		{
			sum += entropy.doubleValue();
		}
		if(entropies.size()!=0)
		{
			return sum/entropies.size();
		}else
		{
			return entropies.size();
		}
		
	}
}
