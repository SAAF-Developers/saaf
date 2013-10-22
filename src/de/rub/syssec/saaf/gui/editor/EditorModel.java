/**
 * 
 */
package de.rub.syssec.saaf.gui.editor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;

import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.ClassInterface;
import de.rub.syssec.saaf.model.application.MethodInterface;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 * 
 */
public class EditorModel {

	private ApplicationInterface currentApplication;
	private MethodInterface currentMethod;
	private File currentFile;
	private int currentLine;
	private PropertyChangeSupport propSupport;
	private ClassInterface currentClass;

	public EditorModel(ApplicationInterface app) {
		super();
		this.propSupport = new PropertyChangeSupport(this);
		this.currentApplication = app;
		this.currentFile = app.getManifestFile();

	}

	public int getCurrentLine() {
		return currentLine;
	}

	public void setCurrentLine(int currentLine) {
		int old = this.currentLine;
		this.currentLine = currentLine;
		this.propSupport.firePropertyChange("currentLine", old, currentLine);
	}

	public ApplicationInterface getCurrentApplication() {
		return currentApplication;
	}

	public void setCurrentApplication(ApplicationInterface currentApplication) {
		// needed to correctly signal property change
		ApplicationInterface old = this.currentApplication;

		this.currentApplication = currentApplication;
		this.propSupport.firePropertyChange("currentApplication", old,
				currentApplication);

		// opening a new application also causes the current file to be the
		// Manifest
		this.setCurrentFile(currentApplication.getManifestFile());
	}

	public MethodInterface getCurrentMethod() {
		return currentMethod;
	}

	public void setCurrentMethod(MethodInterface currentMethod) {
		// needed to correctly signal property change
		MethodInterface old = this.currentMethod;

		this.currentMethod = currentMethod;
		this.propSupport
				.firePropertyChange("currentMethod", old, currentMethod);

		// opening a new method will also cause the currentline be set to the
		// first line in the method
		this.setCurrentLine(currentMethod.getCodeLines().get(0).getLineNr());

	}

	public File getCurrentFile() {
		return currentFile;
	}

	public void setCurrentFile(File currentFile) {
		// needed to correctly signal property change
		File old = this.currentFile;

		this.currentFile = currentFile;
		//if we changed to a smali file, we need to reflect that with the currentClass property
		if (currentFile.getName().endsWith(".smali")) {
			
			ClassInterface newclass = this.currentApplication
					.getSmaliClass(currentFile);
			ClassInterface oldClass = this.currentClass;
			this.currentClass = newclass;
			this.propSupport.firePropertyChange("currentClass", oldClass, newclass);
		}else{
			ClassInterface oldClass = this.currentClass;
			this.currentClass = null;
			this.propSupport.firePropertyChange("currentClass", oldClass, null);
		}
		this.propSupport.firePropertyChange("currentFile", old, currentFile);

	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		this.propSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.propSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.propSupport.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		this.propSupport.removePropertyChangeListener(propertyName, listener);
	}

	public ClassInterface getCurrentClass() {
		return this.currentClass;
	}

	public void setCurrentClass(ClassInterface currentClass) {
		this.currentClass = currentClass;
	}

}
