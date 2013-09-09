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


import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

import org.apache.log4j.Logger;



public class LinkEditorKit extends StyledEditorKit {
	private static final long serialVersionUID = -3788854220848840779L;

	// attribute on inline elements; if value is URL, will be used for the
	// HyperlinkEvent
	public static final Object LINK = new StringBuffer("link");

	// can be static because it picks up the editor from the MouseEvent
	private final MouseListener linkHandler;
	private final File byteCodeDirectory;
	private FileTree tree;
	
	/**
	 * FIXME: Can this result in a Nullpointer? When is it set in FileTree?
	 */
	private File lastClickedFile = null;
	
	private final Vector<Vector<String>> history;
	
	/**
	 * 
	 * @param history
	 * @param byteCodeDirectory the directory where SMALI files are found for the corresponding application
	 */
	public LinkEditorKit(Vector<Vector<String>> history, File byteCodeDirectory, FileTree t) {
		 linkHandler = new LinkHandler();
		 this.history = history;
		 this.byteCodeDirectory = byteCodeDirectory;
		 tree=t;
	}

	public void install(JEditorPane p) {
		super.install(p);
		p.addMouseListener(linkHandler);
	}

	public void deinstall(JEditorPane p) {
		p.removeMouseListener(linkHandler);
		super.deinstall(p);
	}

	private class LinkHandler extends MouseAdapter {
				
		private Logger logger=Logger.getLogger(LinkEditorKit.class);

		public LinkHandler() {
			
		}

		private Element characterElementAt(MouseEvent e) {
			JEditorPane p = (JEditorPane) e.getComponent();

			Position.Bias[] bias = new Position.Bias[1];
			int position = p.getUI().viewToModel(p, e.getPoint(), bias);

			if (bias[0] == Position.Bias.Backward && position != 0)
				--position;

			Element c = ((StyledDocument) p.getDocument())
					.getCharacterElement(position);

			// should test whether really inside
			return c;
		}

		public void mouseReleased(MouseEvent e) {

			if (!SwingUtilities.isLeftMouseButton(e))
				return;

			JEditorPane p = (JEditorPane) e.getComponent();

			if (p.isEditable())
				return;

			Element c = characterElementAt(e);

			if (c != null && c.getAttributes().getAttribute(LINK) != null) {
				try {
					// System.out.println(">> Link " +
					// p.getDocument().getText(c.getStartOffset(),
					// c.getEndOffset() - c.getStartOffset()));

					String class_name = p
							.getDocument()
							.getText(c.getStartOffset(),
									c.getEndOffset() - c.getStartOffset())
							.split(";")[0].substring(
							1,
							p.getDocument()
									.getText(
											c.getStartOffset(),
											c.getEndOffset()
													- c.getStartOffset())
									.split(";")[0].length());

					File datei = new File(byteCodeDirectory, File.separator+"smali"+File.separator+ class_name + ".smali");

					// zum abschneiden des Pfades
					File AppDir = byteCodeDirectory.getParentFile();

					if (p.getDocument()
							.getText(c.getStartOffset(),
									c.getEndOffset() - c.getStartOffset())
							.split(";")[1].length() >= 2) {

						// System.out.println(search);
						// System.out.println(BytecodeSearcher.searchStringinFile(datei,
						// search));

						if (history.size() == 0) {

							Vector<String> entry = new Vector<String>();
//							File click = app.getLastClickedFile();
//							entry.add(click.getAbsolutePath()
//									.substring(
//											AppDir.toString().length(),
//											click.getAbsolutePath().toString()
//													.length()));
//							entry.add(BytecodeSearcher.searchStringinFile(click,
//									search));

							entry.add(lastClickedFile.getAbsolutePath()
									.substring(
											AppDir.getAbsolutePath().length(),
											lastClickedFile.getAbsolutePath().toString()
													.length()));
//							entry.add(BytecodeSearcher.searchStringinFile(lastClickedFile,
//									search));

							history.add(entry);

						}


//						tree.searchNode(
//								datei.getAbsolutePath().substring(
//										AppDir.getAbsolutePath().length(),
//										datei.getAbsolutePath().toString()
//												.length()),
//								BytecodeSearcher.searchStringinFile(datei, search));

						Vector<String> entry = new Vector<String>();

						entry.add(datei.getAbsolutePath().substring(
								AppDir.getAbsolutePath().length(),
								datei.getAbsolutePath().toString().length()));
//						entry.add(BytecodeSearcher.searchStringinFile(datei, search));

						history.add(entry);

					} else {
						tree.searchNode(
								datei.getAbsolutePath().substring(
										AppDir.getAbsolutePath().length(),
										datei.getAbsolutePath().toString()
												.length()), "0");

						Vector<String> entry = new Vector<String>();

						entry.add(datei.getAbsolutePath().substring(
//								AppDir.toString().length(),
								datei.getAbsolutePath().toString().length()));
						entry.add("0");

						history.add(entry);
					}

					//System.out.println(c.getStartOffset());

					// System.out.println(p.getDocument().getText(c.getStartOffset(),
					// c.getEndOffset() -
					// c.getStartOffset()).split(";").length);
					// FileTree.searchNode(tableContent.get(viewRow).get(0),tableContent.get(viewRow).get(1));

				} catch (BadLocationException e1) {
					logger.warn("Problem with history",e1);
				} catch (Exception e1) {
					logger.warn("Problem with history",e1);
				}
			}
		}

	}
	
	/**
	 * Sets the last clicked file in the GUI.
	 * @param lastClickedFile
	 */
	protected void setLastClickedFile(File lastClickedFile) {
		this.lastClickedFile = lastClickedFile;
	}
}