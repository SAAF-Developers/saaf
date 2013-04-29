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
package de.rub.syssec.saaf.application.manifest.components;

import java.util.Set;
import java.util.TreeSet;

import de.rub.syssec.saaf.model.application.manifest.IntentFilterInterface;
import de.rub.syssec.saaf.model.application.manifest.IntentReceivingComponentInterface;


/**
 * Common superclass for all components that can define intent-filters
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class IntentReceivingComponent extends Component implements IntentReceivingComponentInterface{

	protected Set<IntentFilterInterface> filters;
	
	public IntentReceivingComponent() {
		super();
		this.filters = new TreeSet<IntentFilterInterface>();
	}
	
	public IntentReceivingComponent(String name) {
		super();
		this.name=name;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.manifest.components.IntentReceivingComponentInterface#getIntentFilters()
	 */
	@Override
	public Set<IntentFilterInterface> getIntentFilters() {
		return this.filters;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.manifest.components.IntentReceivingComponentInterface#addIntentFilter(de.rub.syssec.saaf.application.manifest.components.IntentFilter)
	 */
	@Override
	public void addIntentFilter(IntentFilterInterface filter) {
		this.filters.add(filter);		
	}

}