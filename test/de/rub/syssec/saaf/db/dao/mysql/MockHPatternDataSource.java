package de.rub.syssec.saaf.db.dao.mysql;

import java.util.Set;
import java.util.TreeSet;

import de.rub.syssec.saaf.db.datasources.Datasource;
import de.rub.syssec.saaf.model.analysis.HPatternInterface;

/**
 * A datasource that returns an empty set of HPatterns.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class MockHPatternDataSource implements Datasource<HPatternInterface>{

	@Override
	public Set<HPatternInterface> getData() {
		return new TreeSet<HPatternInterface>();
	}

}
