/**
 * 
 */
package de.rub.syssec.saaf.application.heuristic;

import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.ConstantInterface;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class MockConstant implements ConstantInterface {

	private String description;
	private int arrayDimension;
	private Type type;
	private String identifier;
	private String value;
	private CodeLineInterface codeline;
	private int fuzzylevel;
	private boolean fuzzy;
	private String path;
	private boolean isAdframework;
	private int searchId;
	private VariableType vType;



	/**
	 * @param description
	 * @param arrayDimension
	 * @param type
	 * @param identifier
	 * @param value
	 * @param codeline
	 * @param fuzzylevel
	 * @param fuzzy
	 * @param path
	 * @param isAdframework
	 * @param searchId
	 */
	public MockConstant(String description, 
			int arrayDimension, 
			Type type,
			VariableType vType,
			String identifier, 
			String value, 
			CodeLineInterface codeline,
			int fuzzylevel, 
			boolean fuzzy, 
			String path, 
			boolean isAdframework,
			int searchId) {
		super();
		this.description = description;
		this.arrayDimension = arrayDimension;
		this.type = type;
		this.vType = vType;
		this.identifier = identifier;
		this.value = value;
		this.codeline = codeline;
		this.fuzzylevel = fuzzylevel;
		this.fuzzy = fuzzy;
		this.path = path;
		this.isAdframework = isAdframework;
		this.searchId = searchId;
	}

	@Override
	public String getTypeDescription() {
		return description;
	}

	@Override
	public int getArrayDimension() {
		return arrayDimension;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public CodeLineInterface getCodeLine() {
		return codeline;
	}

	@Override
	public int getFuzzyLevel() {
		return fuzzylevel;
	}

	@Override
	public boolean wasFuzzySearch() {
		return fuzzy;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public boolean isInAdFrameworkPackage() {
		return isAdframework;
	}

	@Override
	public int getSearchId() {
		return searchId;
	}

	@Override
	public VariableType getVariableType() {
		return vType;
	}
}
