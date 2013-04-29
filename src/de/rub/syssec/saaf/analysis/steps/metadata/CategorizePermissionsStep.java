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
package de.rub.syssec.saaf.analysis.steps.metadata;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.analysis.steps.AbstractStep;
import de.rub.syssec.saaf.db.datasources.DataSourceException;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.manifest.PermissionRequestInterface;

/**
 * Categorizes the PermissionRequest based on set of currently known Permissions.
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class CategorizePermissionsStep extends AbstractStep {

	private PermissionChecker permissionChecker;
	private static final Logger logger=Logger.getLogger(CategorizePermissionsStep.class);
	
	

	/**
	 * @param permissionChecker
	 * @param logger
	 * @param enabled TODO
	 */
	public CategorizePermissionsStep(Config conf, boolean enabled) {
		super();
		this.name="Categorize Permissions";
		this.description="Categorizes the PermissionRequest based on set of currently known Permissions.";
		this.permissionChecker = new SimplePermissionChecker(conf.getPermissionSource());
		this.enabled=enabled;
	}



	@Override
	protected boolean doProcessing(AnalysisInterface analysis)
			throws AnalysisException {
		logger.info("Analyzing permissions of application "+analysis.getApp());
		List<PermissionRequestInterface> requests = new ArrayList<PermissionRequestInterface>(
				analysis.getApp().getManifest().getRequestedPermissions());
		for (PermissionRequestInterface request : requests) {
			// if the application requests a permission we do not know
			// mark the permission as unknown
			request.setAnalysis(analysis);
			try {
				permissionChecker.check(request);
			} catch (DataSourceException e) {
				throw new AnalysisException(e);
			}
			logger.debug("Requested permission "+request.getRequestedPermission()+" is of type "+request.getRequestedPermission().getType());
		}
		return true;
	}

}
