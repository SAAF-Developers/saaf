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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import de.rub.syssec.saaf.Main;

/**
 * @author Christian Kröger
 *
 */
public class AboutAction extends AbstractAction {

	private static final long serialVersionUID = -255616828279011376L;
	private static Properties props;

	public AboutAction(String title) {
		super(title);
		props = Main.getProperties();
		if( props == null ){
			 JOptionPane.showMessageDialog(null, "Could not parse properties file");
		}
	}

	
	private void openURL(URL url) {
	    Desktop desktop = null;
	    if(Desktop.isDesktopSupported()){
	    	desktop = Desktop.getDesktop();
	    }
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(url.toURI());
	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (URISyntaxException e) {
				e.printStackTrace();
			}
	    }
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

	    JEditorPane content = new JEditorPane("text/html", "<html><body>"
	            + props.getProperty("software.name") + " "
				+ props.getProperty("software.version")
				+ "</a><br/><br/>"
				+ "Institution:<br/>"
				+ "&nbsp Chair for Systems Security<br/>"
				+ "&nbsp Horst Görtz Institute for IT-Security<br/>"
				+ "&nbsp Ruhr-University Bochum, Germany<br/>"
				+ "<br/>Project page: <a href=\""+ props.getProperty("software.url")+"\">"+ props.getProperty("software.url")+"</a><br/>"
				+ "<br/>Credits: <br/>"
				+ "&nbsp Tilman Bender <br/>"
				+ "&nbsp Johannes Hoffmann<br/>"
				+ "&nbsp Christian Kröger <br/>"
				+ "&nbsp Hanno Lemoine <br/>"
				+ "&nbsp Martin Ussath"
	            + "</body></html>");
	    
	    content.setEditable(false);

	    content.addHyperlinkListener(new HyperlinkListener(){
	        @Override
	        public void hyperlinkUpdate(HyperlinkEvent e){
	            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)){
	            	openURL(e.getURL());
	            }
	        }
	    });
	
	    JOptionPane.showMessageDialog(null, content, "About", JOptionPane.PLAIN_MESSAGE);
	}

}
