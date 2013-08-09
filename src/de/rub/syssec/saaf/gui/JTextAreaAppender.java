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
package de.rub.syssec.saaf.gui;

import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class JTextAreaAppender extends AppenderSkeleton  {

	private JTextArea textArea = new JTextArea();
	
	public JTextAreaAppender (JTextArea area){
		textArea = area;
		DefaultCaret c = (DefaultCaret) textArea.getCaret();
		c.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	}

	protected void append(LoggingEvent arg0) {
		textArea.append(arg0.getMessage().toString()+"\n");
		//textArea.setCaretPosition(textArea.getText().length() - 1);
	}


	@Override
	public void close() { }


	@Override
	public boolean requiresLayout() {
		return false;
	}
}
