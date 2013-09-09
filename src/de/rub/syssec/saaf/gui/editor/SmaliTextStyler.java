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

import java.awt.Color;
import java.io.File;
import java.util.Vector;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import de.rub.syssec.saaf.misc.Highlight;
import de.rub.syssec.saaf.model.application.ApplicationInterface;

/**
 * TODO: Revise this. Faster, better ....
 */
public class SmaliTextStyler {

	private static StyleContext sc = new StyleContext();
	private static Style annotation;
	private static Style vars;
	private static Style jump;
	private static Style variablenStyle;
	private static Style opcode;
	private static MutableAttributeSet link;

	private String text;

	public SmaliTextStyler() {
		/* empty */
	}

	static {
		annotation = sc.addStyle("annotation",
				sc.getStyle(StyleContext.DEFAULT_STYLE));
		StyleConstants.setForeground(annotation, Color.gray);

		variablenStyle = sc.addStyle("VariablenStyle", null);
		StyleConstants.setForeground(variablenStyle, Color.green);

		opcode = sc.addStyle("opcode", null);
		StyleConstants.setForeground(opcode, Color.red);

		jump = sc.addStyle("jump", null);
		StyleConstants.setForeground(jump, Color.green);

		vars = sc.addStyle("vars", null);
		StyleConstants.setForeground(vars, Color.blue);

		link = new SimpleAttributeSet();
		link.addAttribute(LinkEditorKit.LINK, "true");
		link.addAttribute(StyleConstants.Underline, Boolean.TRUE);

	}

	/**
	 * FIXME: Remove app reference, highlighting is slow
	 * 
	 * @param app
	 * @param doc
	 * @param text_string
	 * @return
	 */
	protected void highlightStrings(ApplicationInterface app, DefaultStyledDocument doc, String text_string) {
		text = text_string;

		// System.out.println(Application.smali_dir.getAbsolutePath());
		// click stuff
		Vector<File> smaliClasss = app.getAllRawSmaliFiles(true);

		for (int i = 0; i < smaliClasss.size(); i++) {
			String class_name = smaliClasss
					.get(i)
					.getAbsolutePath()
					.substring(
							app.getBytecodeDirectory().getAbsolutePath().length(),
							smaliClasss.get(i).getAbsolutePath().length());
			class_name = " L"
					+ class_name.substring(1, class_name.length() - 6);
			linehighlight(doc, class_name, link);
			// System.out.println(smaliClasss.get(i).getAbsolutePath());
			// System.out.println(class_name);
		}

		// Highlight(doc, "Lorg/me", link);
		// Highlight(doc, ".end", annotation);
		// Highlight(doc, ".method", annotation);
		// Highlight(doc, "invoke-direct", opcode);
		// linehighlight(doc, ".local", annotation);
		// linehighlight(doc, ".parameter", annotation);
		// linehighlight(doc, ".prologue", annotation);
		// linehighlight(doc, ".line", annotation);

		Vector<String> commands = Highlight.OP_CODES;

		for (int i = 0; i < commands.size(); i++) {
			highlight(doc, commands.get(i), opcode);
		}

		commands = Highlight.ANNOTATIONS;

		for (int i = 0; i < commands.size(); i++) {
			linehighlight(doc, commands.get(i), annotation);
		}

		Vector<String> signs = new Vector<String>();

		signs.add("v");
		signs.add("p");

		for (int i = 0; i < signs.size(); i++) {
			for (int z = 100; z >= 0; z--) {
				highlight(doc, " " + signs.get(i) + z + "\n", vars);
				highlight(doc, " " + signs.get(i) + z + ",", vars);
				highlight(doc, " " + signs.get(i) + z + "}", vars);
				highlight(doc, "{" + signs.get(i) + z + ",", vars);
				highlight(doc, "{" + signs.get(i) + z + "}", vars);
				highlight(doc, "{" + signs.get(i) + z + " ..", vars);
			}
		}

		Vector<String> jumps = new Vector<String>();

		jumps.add(":cond_");
		jumps.add(":goto_");
		jumps.add(":catch_");
		jumps.add(":catchall_");

		for (int i = 0; i < jumps.size(); i++) {
			for (int z = 100; z >= 0; z--) {
				highlight(doc, " " + jumps.get(i) + z, jump);
			}
		}
	}


	private void linehighlight(DefaultStyledDocument doc, String pattern,
			MutableAttributeSet type) {

		int pos = 0;

		// Search for pattern
		while ((pos = text.indexOf(pattern, pos)) >= 0) {
			// Create highlighter using private painter and apply around pattern
			// pos +1 damit leerzeichen vorher net unterstrichen wird -> wichtig
			// jedoch für die Erkennung
			doc.setCharacterAttributes(pos + 1, text.indexOf("\n", pos) - pos,
					type, false);
			pos += pattern.length();
		}
	}


	private void highlight(DefaultStyledDocument doc, String pattern,
			MutableAttributeSet type) {

		int pos = 0;

		// Search for pattern
		while ((pos = text.indexOf(pattern, pos)) >= 0) {
			// Create highlighter using private painter and apply around pattern
			doc.setCharacterAttributes(pos, pattern.length(), type, false);
			pos += pattern.length();
		}
	}
}