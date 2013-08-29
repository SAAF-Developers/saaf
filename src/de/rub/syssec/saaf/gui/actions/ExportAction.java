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
package de.rub.syssec.saaf.gui.actions;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.png.mxPngEncodeParam;
import com.mxgraph.util.png.mxPngImageEncoder;
import com.mxgraph.view.mxGraph;

import de.rub.syssec.saaf.analysis.steps.hash.Hash;
import de.rub.syssec.saaf.model.application.Digest;
import de.rub.syssec.saaf.model.application.MethodInterface;

public class ExportAction implements ActionListener {
private static final int MAX_FILE_LENGTH = 250;
private mxGraph graph;
private String folder;
private String lastExportedFile;

	//TODO: split this whole thing into two classes, the action, and an export class which is simply called from the action
	public ExportAction(mxGraph g) {
		graph = g;
	}
	
	public ExportAction(mxGraph g, String folder) {
		graph = g;
		this.folder = folder;
	}
	private String fixLength(String className, String originalMethodName, String originalParameters, String originalReturn, String extension){
		StringBuilder retBuilder = new StringBuilder();
		if(originalMethodName.length()+originalParameters.length()+originalReturn.length()+extension.length() >= MAX_FILE_LENGTH){
			try {
				String hash = Hash.calculateHash(Digest.MD5, (originalParameters+originalReturn).getBytes());
				int HASH_LENGTH = hash.length();
				StringBuilder method = new StringBuilder();
				method.append(originalMethodName);
			if(className.length()+1+originalMethodName.length()+extension.length() >= MAX_FILE_LENGTH - HASH_LENGTH){
				retBuilder.append(className);
				retBuilder.append("_");
				retBuilder.append(method.substring(0, MAX_FILE_LENGTH -className.length() - 1 - extension.length() - HASH_LENGTH));
			} else if (className.length()+1+originalMethodName.length()+originalParameters.length()+extension.length() >= MAX_FILE_LENGTH - HASH_LENGTH){
				method.append(originalParameters);
				retBuilder.append(className);
				retBuilder.append("_");
				retBuilder.append(method.substring(0, MAX_FILE_LENGTH -className.length() - 1 - extension.length() - HASH_LENGTH));
			} else if (className.length()+1+originalMethodName.length()+originalParameters.length()+originalReturn.length()+extension.length() >= MAX_FILE_LENGTH- HASH_LENGTH){
				method.append(originalParameters);
				method.append(originalReturn);
				retBuilder.append(className);
				retBuilder.append("_");
				retBuilder.append(method.substring(0, MAX_FILE_LENGTH -className.length() - 1 - extension.length() - HASH_LENGTH));
			}
			retBuilder.append(hash);
			retBuilder.append(extension);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			retBuilder.append(className);
			retBuilder.append("_");
			retBuilder.append(originalMethodName);
			retBuilder.append(originalParameters);
			retBuilder.append(originalReturn);
			retBuilder.append(extension);
		}
		return retBuilder.toString();
	}
	
	public String getLastExportedFile(){
		return lastExportedFile;
	}
	
	public String export(String className, String separator, String methodName, String parameters, String returnValues, String fileExtension, String packageName){

		String filename = className+separator+methodName+parameters+returnValues+fileExtension;
		String returnName = filename;

		returnName = fixLength(className, methodName, parameters, returnValues, fileExtension);
		String file = folder+File.separator+packageName+File.separator+returnName;

		if (fileExtension.equals(".png") ) {
			File dir = new File(folder+File.separator+packageName);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			generatePNG(file);
		}/* else if (fileExtension.equals(".svg")){
			genSVG(file);
		}*/else{
			generatePNG(file+".png");
		}
		lastExportedFile = file;
		return returnName;
	}

	public void actionPerformed(ActionEvent event) {
	if(event.getActionCommand().startsWith("Export")){
	    FileNameExtensionFilter filter = new FileNameExtensionFilter("All Files",/*"svg",*/ "png");
		JFileChooser exportDir = new JFileChooser();
		exportDir.setFileFilter(filter);
		exportDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = exportDir.showSaveDialog(null);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String file = exportDir.getSelectedFile().getAbsolutePath();
			
			String extension = file.substring(file.lastIndexOf(".") +1 );
			
		    if (extension != null) {
		        if (extension.equals("png") ) {
					generatePNG(file); 
		        } /*else if (extension.equals("svg")){
					genSVG(file);
		        }*/else{//if not type chosen, use png
					generatePNG(file+".png");
		        }
		    }else{
				
			    }
		    lastExportedFile = file;
			}
		}
	}
/*
	private void genSVG(String file) {
		mxSvgCanvas canvas = (mxSvgCanvas) mxCellRenderer
				.drawCells(graph, null, 1, null,
						new CanvasFactory()	{
							public mxICanvas createCanvas(
									int width, int height){
								mxSvgCanvas canvas = new mxSvgCanvas(
										mxDomUtils.createSvgDocument(
												width, height));
								canvas.setEmbedded(true);

								return canvas;
							}
						});
		
		try {
			mxUtils.writeFile(mxXmlUtils.getXml(canvas.getDocument()),
					file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
	
	
	private void generatePNG(String file){
		// Creates the image for the PNG file
		BufferedImage image = mxCellRenderer.createBufferedImage(graph,
				null, 1, new Color(255, 255, 255), false, null);
		mxPngEncodeParam param = mxPngEncodeParam.getDefaultEncodeParam(image);

		try {
			FileOutputStream outputStream = new FileOutputStream(new File(file));
			
			try	{
				mxPngImageEncoder encoder = new mxPngImageEncoder(outputStream,	param);
	
				if (image != null){
					encoder.encode(image);
				}else{
					//could not create the image
				}
	
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally	{
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}  
		
	}
	
	public void export(MethodInterface method){
		export(folder, method);
	}
	
	public void export(String targetDir, MethodInterface method){
		BufferedWriter outputFile= null;				
		try {
			if(!new File(targetDir).exists()){
				new File(targetDir).mkdirs();
			}
			outputFile = new BufferedWriter(new FileWriter(targetDir+File.separator+"changed method names.txt", true));
		} catch (IOException e3) {
			e3.printStackTrace();
		}

		String parameters = "("+method.getParameterString().replaceAll("/", "_")+")";//TODO: maybe do this in method.getParameterString, or at least the "(" and ")"
		String methodName = method.getName().replace("<", "_").replace(">", "_");//This is due to windows not supporting < and > in file names
		StringBuilder realFileName = new StringBuilder();
		realFileName.append(method.getSmaliClass().getClassName());
		realFileName.append("_");
		realFileName.append(methodName);
		realFileName.append(parameters);
		realFileName.append(method.getReturnValueString());
		realFileName.append(".png");
								
		String newFileName = export(method.getSmaliClass().getClassName(), "_",methodName,parameters,method.getReturnValueString(),".png",method.getSmaliClass().getPackageName(false));
		
		if(!realFileName.toString().equals(newFileName)){
			try {
				if(new File(targetDir+File.separator+"changed method names.txt").length() == 0){
						outputFile.write("This file contains all the method names, that have been changed, due to the classname plus methodname plus its parameters being longer than 255 characters. For those files the end of it is replaced by the md5 hash of the string.");
						outputFile.newLine();
						outputFile.newLine();
						outputFile.newLine();
				}
				outputFile.write("Generated Filename:");
				outputFile.newLine();
				outputFile.write(newFileName);
				outputFile.newLine();
				outputFile.write("Real Filename:");
				outputFile.newLine();
				//prepend the package where this class + method resides
				realFileName.insert(0, method.getSmaliClass().getFullClassName(false));
				outputFile.write(realFileName.toString());
				outputFile.newLine();
				outputFile.newLine();
				outputFile.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
	}
	
	public void setGraph (mxGraph graph){
		this.graph = graph;
	}
}