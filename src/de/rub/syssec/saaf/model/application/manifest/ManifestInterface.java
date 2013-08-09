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
package de.rub.syssec.saaf.model.application.manifest;

import java.io.File;
import java.util.Collection;

/**
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
 */
public interface ManifestInterface {

	public abstract void addActivity(ActivityInterface activity);

	/**
	 * @return the activities
	 */
	public abstract Collection<ActivityInterface> getActivities();

	/**
	 * Return number of activities defined in the manifest.
	 * 
	 * @return the number of activities
	 */
	public abstract int getNumberOfActivities();

	/**
	 * Test whether the manifest has no activities defined at all
	 * 
	 * @return true if the Manifest defines no activities
	 */
	public abstract boolean hasNoActivities();

	/**
	 * Checks whether there is a BroadcastReceiver with an IntentFilter setting
	 * its Priority.
	 * 
	 * @return true if there is a BR which changes his priority
	 */
	public abstract boolean hasPriorityBR();

	/**
	 * Add a permission to the set of requested permissions.
	 * 
	 * @param p
	 *            the permission to add.
	 */
	public abstract void addPermissionRequest(PermissionRequestInterface p);

	/**
	 * Return a list of all permissions requested in this Manifest.
	 * 
	 * @return collection of requested permissions
	 */
	public abstract Collection<PermissionRequestInterface> getRequestedPermissions();

	/**
	 * Return the number of permissions requested in the manifest.
	 * 
	 * @return the number of requested permissions
	 */
	public abstract int getNumberOfPermissions();

	/**
	 * Add a service to the list of defined services.
	 * 
	 * @param service
	 *            the service to add.
	 */
	public abstract void addService(ServiceInterface service);

	/**
	 * Return a collection of all services requested in this interface.
	 * 
	 * @return the services
	 */
	public abstract Collection<ServiceInterface> getServices();

	/**
	 * Return the number of services defined in this Manifest.
	 * 
	 * @return the number of defined services
	 */
	public abstract int getNumberOfServices();

	/**
	 * Add a receiver to the list of defined receivers.
	 * 
	 * @param receiver
	 *            the receiver to add
	 */
	public abstract void addReceiver(ReceiverInterface receiver);

	/**
	 * @return the receivers
	 */
	public abstract Collection<ReceiverInterface> getReceivers();

	/**
	 * Return a collection of all receivers defined in this interface.
	 * 
	 * @return the defined receivers.
	 */
	public abstract int getNumberOfReceivers();

	/**
	 * Return the absolute path of the Manifest represented by this object.
	 * 
	 * @return the path
	 */
	public abstract File getPath();

	/**
	 * Set the path of the Manifest.xml represented by this object.
	 * 
	 * @param path
	 *            the path to set
	 */
	public abstract void setPath(File path);

	/**
	 * Query if the Manifest requests a certain permission.
	 * 
	 * @param p
	 * @return
	 */
	public abstract boolean hasPermission(PermissionRequestInterface p);

	/**
	 * Query if the Manifest requests a certain permission.
	 * 
	 * @param perm
	 *            as String, like "android.permission.SEND_SMS"
	 * @return
	 */
	public abstract boolean hasPermission(String perm);

	// ################ getter and Setter #################
	/**
	 * @return the versionCode
	 */
	public int getVersionCode();

	/**
	 * @param versionCode
	 *            the versionCode to set
	 */
	public void setVersionCode(int versionCode);

	/**
	 * @return the versionName
	 */
	public String getVersionName();

	/**
	 * @param versionName
	 *            the versionName to set
	 */
	public void setVersionName(String versionName);

	/**
	 * @return the packageName
	 */
	public String getPackageName();

	/**
	 * @param packageName
	 *            the packageName to set
	 */
	public void setPackageName(String packageName);

	/**
	 * @return the minSdkVersion
	 */
	public int getMinSdkVersion();

	/**
	 * @param minSdkVersion
	 *            the minSdkVersion to set
	 */
	public void setMinSdkVersion(int minSdkVersion);

	/**
	 * @return the appLabel
	 */
	public String getAppLabel();

	/**
	 * @param appLabel
	 *            the appLabel to set
	 */
	public void setAppLabel(String appLabel);

	/**
	 * @return the appLabelResolved
	 */
	public String getAppLabelResolved();

	/**
	 * @param appLabelResolved
	 *            the appLabelResolved to set
	 */
	public void setAppLabelResolved(String appLabelResolved);

	/**
	 * @return the appDebuggable
	 */
	public boolean isAppDebuggable();

	/**
	 * @param b
	 *            the appDebuggable to set
	 */
	public void setAppDebuggable(boolean b);

	/**
	 * Returns the activity that is used to start the application.
	 * 
	 * @return
	 */
	public ActivityInterface getDefaultActivity();

	/**
	 * 
	 * @param activity
	 *            the name of the activity (relative the the Manifests package.
	 * 
	 */
	public void setDefaultActivity(ActivityInterface activity)
			throws DuplicateEntryPointException;
}