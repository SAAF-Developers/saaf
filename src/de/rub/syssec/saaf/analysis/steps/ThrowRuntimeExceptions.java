package de.rub.syssec.saaf.analysis.steps;

import java.util.NoSuchElementException;

import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;

/**
 * Throw multiple RuntimeExceptions to test Exception handling! Should only be
 * used for debug purpose!
 *
 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
 *
 */
public class ThrowRuntimeExceptions extends AbstractStep {

	public ThrowRuntimeExceptions(Config cfg, boolean enabled)
	{
		this.config = cfg;
		this.name = "ThrowRuntimeExceptions";
		this.description = "Throw multiple RuntimeExceptions to test Exception handling";
		this.enabled = enabled;
	}

	@Override
	protected boolean doProcessing(AnalysisInterface analysis)
			throws AnalysisException {
		java.util.Random ran = new java.util.Random();
		int r = ran.nextInt(3);
		logger.error("Runtime Exception Generator is activated! Throws exception nr " + r);
		switch (r) {
			case 0:
				// NullPointer
				throw new NullPointerException("catch me if you can");
			case 1:
				// ArrayOutOfBounds
				throw new ArrayIndexOutOfBoundsException("catch me if you can");
			case 2:
				// NoSuchElement
				throw new NoSuchElementException("catch me if you can");
		}
		return false;
	}

}
