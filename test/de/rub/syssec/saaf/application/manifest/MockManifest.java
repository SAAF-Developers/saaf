/**
 * 
 */
package de.rub.syssec.saaf.application.manifest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import de.rub.syssec.saaf.model.application.manifest.ActivityInterface;
import de.rub.syssec.saaf.model.application.manifest.ManifestInterface;
import de.rub.syssec.saaf.model.application.manifest.PermissionRequestInterface;
import de.rub.syssec.saaf.model.application.manifest.ReceiverInterface;
import de.rub.syssec.saaf.model.application.manifest.ServiceInterface;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class MockManifest implements ManifestInterface {

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#addActivity(de.rub.syssec.saaf.saaf.model.application.manifest.ActivityInterface)
	 */
	@Override
	public void addActivity(ActivityInterface activity) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#getActivities()
	 */
	@Override
	public Collection<ActivityInterface> getActivities() {
		// TODO Auto-generated method stub
		return new ArrayList<ActivityInterface>();
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#getNumberOfActivities()
	 */
	@Override
	public int getNumberOfActivities() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#hasNoActivities()
	 */
	@Override
	public boolean hasNoActivities() {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#hasPriorityBR()
	 */
	@Override
	public boolean hasPriorityBR() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#addPermissionRequest(de.rub.syssec.saaf.saaf.model.application.manifest.PermissionRequestInterface)
	 */
	@Override
	public void addPermissionRequest(PermissionRequestInterface p) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#getRequestedPermissions()
	 */
	@Override
	public Collection<PermissionRequestInterface> getRequestedPermissions() {
		// TODO Auto-generated method stub
		return new ArrayList<PermissionRequestInterface>();
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#getNumberOfPermissions()
	 */
	@Override
	public int getNumberOfPermissions() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#addService(de.rub.syssec.saaf.saaf.model.application.manifest.ServiceInterface)
	 */
	@Override
	public void addService(ServiceInterface service) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#getServices()
	 */
	@Override
	public Collection<ServiceInterface> getServices() {
		// TODO Auto-generated method stub
		return new ArrayList<ServiceInterface>();
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#getNumberOfServices()
	 */
	@Override
	public int getNumberOfServices() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#addReceiver(de.rub.syssec.saaf.saaf.model.application.manifest.ReceiverInterface)
	 */
	@Override
	public void addReceiver(ReceiverInterface receiver) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#getReceivers()
	 */
	@Override
	public Collection<ReceiverInterface> getReceivers() {
		// TODO Auto-generated method stub
		return new ArrayList<ReceiverInterface>();
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#getNumberOfReceivers()
	 */
	@Override
	public int getNumberOfReceivers() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#getPath()
	 */
	@Override
	public File getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#setPath(java.io.File)
	 */
	@Override
	public void setPath(File path) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#hasPermission(de.rub.syssec.saaf.saaf.model.application.PermissionInterface)
	 */
	@Override
	public boolean hasPermission(PermissionRequestInterface p) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#hasPermission(java.lang.String)
	 */
	@Override
	public boolean hasPermission(String perm) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#getVersionCode()
	 */
	@Override
	public int getVersionCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#setVersionCode(int)
	 */
	@Override
	public void setVersionCode(int versionCode) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#getVersionName()
	 */
	@Override
	public String getVersionName() {
		// TODO Auto-generated method stub
		return "test";
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#setVersionName(java.lang.String)
	 */
	@Override
	public void setVersionName(String versionName) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#getPackageName()
	 */
	@Override
	public String getPackageName() {
		// TODO Auto-generated method stub
		return "test";
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#setPackageName(java.lang.String)
	 */
	@Override
	public void setPackageName(String packageName) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#getMinSdkVersion()
	 */
	@Override
	public int getMinSdkVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#setMinSdkVersion(int)
	 */
	@Override
	public void setMinSdkVersion(int minSdkVersion) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#getAppLabel()
	 */
	@Override
	public String getAppLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#setAppLabel(java.lang.String)
	 */
	@Override
	public void setAppLabel(String appLabel) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#getAppLabelResolved()
	 */
	@Override
	public String getAppLabelResolved() {
		// TODO Auto-generated method stub
		return "test";
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#setAppLabelResolved(java.lang.String)
	 */
	@Override
	public void setAppLabelResolved(String appLabelResolved) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#isAppDebuggable()
	 */
	@Override
	public boolean isAppDebuggable() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.model.application.manifest.ManifestInterface#setAppDebuggable(boolean)
	 */
	@Override
	public void setAppDebuggable(boolean b) {
		// TODO Auto-generated method stub

	}

}
