/**
 * 
 */
package de.rub.syssec.saaf.db.dao.mysql;

import java.util.Set;
import java.util.TreeSet;

import de.rub.syssec.saaf.db.datasources.Datasource;
import de.rub.syssec.saaf.model.analysis.BTPatternInterface;

/**
 * A datasource that returns an empty set of BTPatterns.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class MockBTPatternDataSource implements Datasource<BTPatternInterface> {

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.db.datasources.Datasource#getData()
	 */
	@Override
	public Set<BTPatternInterface> getData() {
		return new TreeSet<BTPatternInterface>();
	}

}
