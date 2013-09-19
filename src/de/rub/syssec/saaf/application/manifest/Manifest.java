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
package de.rub.syssec.saaf.application.manifest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.rub.syssec.saaf.application.manifest.components.Component;
import de.rub.syssec.saaf.application.manifest.permissions.Permission;
import de.rub.syssec.saaf.application.manifest.permissions.PermissionRequest;
import de.rub.syssec.saaf.model.application.manifest.ActivityInterface;
import de.rub.syssec.saaf.model.application.manifest.ComponentInterface;
import de.rub.syssec.saaf.model.application.manifest.DuplicateEntryPointException;
import de.rub.syssec.saaf.model.application.manifest.IntentFilterInterface;
import de.rub.syssec.saaf.model.application.manifest.ManifestInterface;
import de.rub.syssec.saaf.model.application.manifest.PermissionRequestInterface;
import de.rub.syssec.saaf.model.application.manifest.ReceiverInterface;
import de.rub.syssec.saaf.model.application.manifest.ServiceInterface;

/**
 * Provides access to information from AndroidManifest.xml
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 * @author Hanno Lemoine <hanno.lemoine@gdata.de>
 */
public class Manifest implements ManifestInterface {

	private Set<PermissionRequestInterface> permissions;

	private HashMap<String, ActivityInterface> activities;

	private HashMap<String, ServiceInterface> services;

	private HashMap<String, ReceiverInterface> receivers;

	private File androidManifestXML;
	private int versionCode;
	private String versionName;
	private String packageName;
	private int minSdkVersion;
	private String appLabel;
	private String appLabelResolved; // Does not Contain any more @string/...
	private boolean appDebuggable = false; // True or False

	private ActivityInterface defaultActivity;

	private File tidyAndoirdManifestXML;

	public Manifest(File analyzedPath) {
		super();
		this.permissions = new HashSet<PermissionRequestInterface>();
		this.activities = new HashMap<String, ActivityInterface>();
		this.services = new HashMap<String, ServiceInterface>();
		this.receivers = new HashMap<String, ReceiverInterface>();
		this.androidManifestXML = analyzedPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.application.manifest.ManifestInterface#addActivity(de.rub
	 * .syssec.application.manifest.components.Activity)
	 */
	@Override
	public void addActivity(ActivityInterface activity) {
		this.activities.put(activity.getName(), activity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.application.manifest.ManifestInterface#getActivities()
	 */
	@Override
	public Collection<ActivityInterface> getActivities() {
		return activities.values();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.application.manifest.ManifestInterface#getNumberOfActivities
	 * ()
	 */
	@Override
	public int getNumberOfActivities() {
		return this.activities.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.application.manifest.ManifestInterface#hasNoActivities()
	 */
	@Override
	public boolean hasNoActivities() {
		return (this.getNumberOfActivities() == 0) ? true : false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.application.manifest.ManifestInterface#hasPriorityBR()
	 */
	@Override
	public boolean hasPriorityBR() {
		for (ReceiverInterface receiver : this.receivers.values()) {
			for (IntentFilterInterface filter : receiver.getIntentFilters()) {
				if (filter.getPriority() >= 0) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.application.manifest.ManifestInterface#addPermission(de
	 * .rub.syssec.application.manifest.permissions.Permission)
	 */
	@Override
	public void addPermissionRequest(PermissionRequestInterface request) {
		this.permissions.add(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.application.manifest.ManifestInterface#getPermissions()
	 */
	@Override
	public Collection<PermissionRequestInterface> getRequestedPermissions() {
		return permissions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.application.manifest.ManifestInterface#getNumberOfPermissions
	 * ()
	 */
	@Override
	public int getNumberOfPermissions() {
		return this.permissions.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.application.manifest.ManifestInterface#addService(de.rub
	 * .syssec.application.manifest.components.Service)
	 */
	@Override
	public void addService(ServiceInterface service) {
		this.services.put(service.getName(), service);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.application.manifest.ManifestInterface#getServices()
	 */
	@Override
	public Collection<ServiceInterface> getServices() {
		return services.values();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.application.manifest.ManifestInterface#getNumberOfServices
	 * ()
	 */
	@Override
	public int getNumberOfServices() {
		return this.services.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.application.manifest.ManifestInterface#addReceiver(de.rub
	 * .syssec.application.manifest.components.Receiver)
	 */
	@Override
	public void addReceiver(ReceiverInterface receiver) {
		this.receivers.put(receiver.getName(), receiver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.application.manifest.ManifestInterface#getReceivers()
	 */
	@Override
	public Collection<ReceiverInterface> getReceivers() {
		return receivers.values();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.application.manifest.ManifestInterface#getNumberOfReceivers
	 * ()
	 */
	@Override
	public int getNumberOfReceivers() {
		return this.receivers.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rub.syssec.saaf.application.manifest.ManifestInterface#getPath()
	 */
	@Override
	public File getPath() {
		return androidManifestXML;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.application.manifest.ManifestInterface#setPath(java.io.
	 * File)
	 */
	@Override
	public void setPath(File path) {
		this.androidManifestXML = path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.application.manifest.ManifestInterface#hasPermission(de
	 * .rub.syssec.application.manifest.permissions.Permission)
	 */
	@Override
	public boolean hasPermission(PermissionRequestInterface p) {
		return permissions.contains(p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rub.syssec.saaf.application.manifest.ManifestInterface#hasPermission(java
	 * .lang.String)
	 */
	@Override
	public boolean hasPermission(String perm) {
		return hasPermission(new PermissionRequest(new Permission(perm)));
	}

	// ################ getter and Setter #################
	/**
	 * @return the versionCode
	 * 
	 */
	@Override
	public int getVersionCode() {
		return versionCode;
	}

	/**
	 * @param versionCode
	 *            the versionCode to set
	 */
	@Override
	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	/**
	 * @return the versionName
	 */
	@Override
	public String getVersionName() {
		return versionName;
	}

	/**
	 * @param versionName
	 *            the versionName to set
	 */
	@Override
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	/**
	 * @return the packageName
	 */
	@Override
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @param packageName
	 *            the packageName to set
	 */
	@Override
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	/**
	 * @return the minSdkVersion
	 */
	@Override
	public int getMinSdkVersion() {
		return minSdkVersion;
	}

	/**
	 * @param minSdkVersion
	 *            the minSdkVersion to set
	 */
	@Override
	public void setMinSdkVersion(int minSdkVersion) {
		this.minSdkVersion = minSdkVersion;
	}

	/**
	 * @return the appLabel
	 */
	public String getAppLabel() {
		return appLabel;
	}

	/**
	 * @param appLabel
	 *            the appLabel to set
	 */
	@Override
	public void setAppLabel(String appLabel) {
		this.appLabel = appLabel;
	}

	/**
	 * @return the appLabelResolved
	 */
	@Override
	public String getAppLabelResolved() {
		return appLabelResolved;
	}

	/**
	 * @param appLabelResolved
	 *            the appLabelResolved to set
	 */
	@Override
	public void setAppLabelResolved(String appLabelResolved) {
		this.appLabelResolved = appLabelResolved;
	}

	/**
	 * @return the appDebuggable
	 */
	@Override
	public boolean isAppDebuggable() {
		return appDebuggable;
	}

	/**
	 * @param appDebuggable
	 *            the appDebuggable to set
	 */
	@Override
	public void setAppDebuggable(boolean appDebuggable) {
		this.appDebuggable = appDebuggable;
	}
	
	@Override
	public ActivityInterface getDefaultActivity() {
		return defaultActivity;
	}

	@Override
	public void setDefaultActivity(ActivityInterface activity)throws DuplicateEntryPointException {
		if (this.defaultActivity == null) {
			this.defaultActivity = activity;
		} else {
			throw new DuplicateEntryPointException("Entrypoint already defined: "+this.defaultActivity.getName());
		}

	}

	@Override
	public List<ComponentInterface> getComponents() {
		ArrayList<ComponentInterface> components =  new ArrayList<ComponentInterface>();
		components.addAll(this.activities.values());
		components.addAll(this.services.values());
		components.addAll(this.receivers.values());		
		return components;
	}

	@Override
	public File getTidiedPath() {
		return this.tidyAndoirdManifestXML;
	}

	@Override
	public void setTidiedPath(File file) {
		this.tidyAndoirdManifestXML = file;
	}
}
