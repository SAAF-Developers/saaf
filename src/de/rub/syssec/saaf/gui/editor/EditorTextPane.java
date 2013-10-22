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
package de.rub.syssec.saaf.gui.editor;

import java.awt.event.MouseEvent;
import java.util.Set;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;

import de.rub.syssec.saaf.model.application.CodeLineInterface;

public class EditorTextPane extends JTextPane {
	private EditorModel model;

	private static final long serialVersionUID = -2940577917898370537L;
		

	public EditorTextPane(EditorModel editorModel){
		super();
		model = editorModel;
	}
		
		
	/**
	 * return the # of bytes from the beginning of the array until line number
	 * line
	 * 
	 * @param content
	 * @param line
	 * @return
	 */
	private int byteCount(String[] content, int line) {
		int c1 = 0;
		if (line >= content.length)
			line = content.length - 2;
		
	
		line -= 1;
		

		for (int i = 0; i < (line); i++) {
			// line length
			c1 = c1 + content[i].length();
			// new line
			c1 = c1 + 1;
		}
		c1++;
		return c1;
	}
	
	
	//reduce # returns in this method
	public String getToolTipText(MouseEvent event){
		int offset = viewToModel(event.getPoint());

		      
		String text = "";
		try {
			if ( model.getCurrentFile() != null){
			text = this.getText(offset, 1);
			text = model.getCurrentFile().getAbsolutePath();
			
			//match into function
			Set <CodeLineInterface> keys = model.getCurrentApplication().getMatchedCalls().keySet();
			for(CodeLineInterface cl: keys){
				if(text.contains(cl.getSmaliClass().getFullClassName(false))){
					String [] content = this.getDocument()
							.getText(0, this.getDocument().getLength())
							.split("\n");
					int lineStart = byteCount(content, cl.getLineNr())-2;
					int lineEnd = lineStart+cl.getLine().length+1;
					if(offset > lineStart && offset < lineEnd){
						text = "<html>"+model.getCurrentApplication().getMatchedCalls().get(cl).getPermissionString()+ "<br>"+model.getCurrentApplication().getMatchedCalls().get(cl).getDescription()+"</html>";
						return text;
					} else {
						continue;
					}
				}
			}
			return null;
			
			}
			else 
				return null;
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		return text;
	}
		

}
