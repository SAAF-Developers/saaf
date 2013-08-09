package de.rub.syssec.saaf.analysis.steps;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

public class DetectObfuscationStepTest {

	@Test
	public void testEntropyStringEmpty() {
		assertTrue(0.0==DetectObfuscationStep.entropy(""));
	}
	
	@Test
	public void testEntropyStringLength1() {
		assertTrue(0.0==DetectObfuscationStep.entropy("A"));
	}
	
	@Test
	public void testEntropyStringSingleChar() {
		assertTrue(0.0==DetectObfuscationStep.entropy("AA"));
	}

	@Test
	public void testMedian() {
		Double test = 23.0;
		ArrayList<Double> vals = new ArrayList<Double>();
		vals.add(test);
		assertTrue(test.doubleValue()==DetectObfuscationStep.median(vals));
	}

}
