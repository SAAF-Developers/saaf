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
package de.rub.syssec.saaf.analysis.steps.cfg;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxCellOverlay;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;

import de.rub.syssec.saaf.misc.Highlight;
import de.rub.syssec.saaf.model.APICall;
import de.rub.syssec.saaf.model.application.BasicBlockInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.MethodInterface;

public class CFGGraph {
	MethodInterface method;
	mxGraph graph;
	mxGraphComponent graphComponent;
	
	HashMap<Integer, Object> vertices;
	HashMap<mxCell, BasicBlockInterface> cells;
	HashMap<BasicBlockInterface, String> overlays;
	
	ImageIcon permissionIcon;
	
	public CFGGraph (MethodInterface m){
		this(m, false);
	}
	
	public CFGGraph (MethodInterface m, boolean showAPICallasDOTcomment){
		method = m;
		vertices = new HashMap<Integer,Object>();
		cells = new HashMap<mxCell,BasicBlockInterface>();
		overlays = new HashMap<BasicBlockInterface, String>();
		graph = new mxGraph();
		
		Object parent = graph.getDefaultParent();

		graph.getModel().beginUpdate();
		graph.setHtmlLabels(true);
		graph.setCellsDisconnectable(false);
		
    	URL imageURL = getClass().getResource("/images/permission.png");
   		permissionIcon = new ImageIcon(imageURL);
		

		HashMap<CodeLineInterface, APICall> matches = method.getSmaliClass().getApplication().getMatchedCalls();

		
		try	{
			graph.setAllowDanglingEdges(false);
			graph.setAutoSizeCells(true);
			
			graph.setCellsEditable(false);
			Collection<mxCell> c = new ArrayList<mxCell>();
			for(BasicBlockInterface bb: method.getBasicBlocks()){
				c.clear();
				
				Object startVertex = null;
				int lineNr = bb.getCodeLines().getFirst().getLineNr();
				if(vertices.containsKey(lineNr)){
					startVertex = vertices.get(lineNr);
				} else {
					startVertex = graph.insertVertex(parent, null, generateBasicBlockString(
							showAPICallasDOTcomment, matches, bb), 0, 0, 80, 30);
					vertices.put(lineNr,startVertex);
				}

				graph.updateCellSize(startVertex);
				
				for( BasicBlockInterface target: bb.getNextBB()){
					Object targetVertex = null;
					lineNr = target.getCodeLines().getFirst().getLineNr();
					if(vertices.containsKey(lineNr)){
						targetVertex = vertices.get(lineNr);
					} else {
						targetVertex = graph.insertVertex(parent, null, generateBasicBlockString(
								showAPICallasDOTcomment, matches, target), 0, 0, 80, 30);
						vertices.put(lineNr,targetVertex);
						graph.updateCellSize(targetVertex);
					}

				graph.insertEdge(parent, null, "", startVertex, targetVertex);
				}	            
			}
			
			graph.setCellStyles(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_LEFT, vertices.values().toArray() );

		}
		finally	{
		   graph.getModel().endUpdate();
		}
		
		graphComponent = new mxGraphComponent(graph);

		for(CodeLineInterface line: matches.keySet()){
			if(line.getMethod().equals(method)){

				//TODO: maybe add a line.getBB method or a method.getBBforLine
				for(BasicBlockInterface bb: method.getBasicBlocks()){
					if(bb.containsLineNr(line.getLineNr())){
						StringBuilder currentOverlayText = new StringBuilder();
						currentOverlayText.append("found APICall match in line: ");
						currentOverlayText.append(line.getNrAndLine());
						currentOverlayText.append("<br>");
						currentOverlayText.append(matches.get(line).getPermissionString());
						currentOverlayText.append("<br>");
						currentOverlayText.append(matches.get(line).getDescription());
						

						//if a bb contains multiple apicalls
						if(overlays.containsKey(bb)){
							StringBuilder oldOverlayText = new StringBuilder();
							oldOverlayText.append(overlays.get(bb));
							oldOverlayText.append("<br><br>");
							oldOverlayText.append(currentOverlayText);
							overlays.put(bb, oldOverlayText.toString());
						}else{
							overlays.put(bb, currentOverlayText.toString());
						}
						
						StringBuilder overlayText = new StringBuilder();
						overlayText.append("<html>");
						overlayText.append(overlays.get(bb));
						overlayText.append("</html>");
						//TODO: fix imageicon location
						mxCellOverlay overlay =  new mxCellOverlay(permissionIcon, overlayText.toString());
						//if new overlay, add it, if an old one exists replace it
						graphComponent.addCellOverlay(vertices.get(bb.getCodeLines().getFirst().getLineNr()), overlay);
//						mxCell cell = (mxCell)(vertices.get(bb.getCodeLines().getFirst().getLineNr()));
//						cell.setStyle("fillColor=red");
//						graph.setCellStyle("fillColor=blue", new Object[]{vertices.get(bb.getCodeLines().getFirst().getLineNr())}); 
						graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, "gray", new Object[]{vertices.get(bb.getCodeLines().getFirst().getLineNr())}); 
					}
				}
				
			}
		}
		
		graphComponent.scrollCellToVisible(graph.getDefaultParent());
		
		
//		graphComponent.setToolTips(true);
	
		mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
        layout.setDisableEdgeStyle(false);

        layout.execute(graph.getDefaultParent());
	}

	private String generateBasicBlockString(boolean showAPICallasDOTcomment,
			HashMap<CodeLineInterface, APICall> matches, BasicBlockInterface bb) {
		String highlighted="";
		if(showAPICallasDOTcomment){
			ArrayList<CodeLineInterface> callLines = new ArrayList<CodeLineInterface>();
			//TODO: Change this, this code is in very similar form used later on to find positions for overlays
			for(CodeLineInterface line: matches.keySet()){
				if(line.getMethod().equals(method)){
						//This BB contains at least one APICall, so we have to build the string ourselfs
					if(bb.containsLineNr(line.getLineNr())){
						callLines.add(line);
					}
				}
			}
			StringBuilder out = new StringBuilder();
			for(CodeLineInterface currentLine: bb.getCodeLines()){
				if(callLines.contains(currentLine)){
//					out.append(".");
//					out.append("found APICall match in line: ");
//					out.append(currentLine.getNrAndLine());
//					out.append("\n");
					
					out.append(".");
					out.append(matches.get(currentLine).getPermissionString());
					out.append("\n");
					out.append(".");
					out.append(matches.get(currentLine).getDescription());
					out.append("\n");
					
				} 
				out.append(currentLine.getNrAndLine());
				out.append("\n");
				
			}
			
			highlighted = highlight(out.toString());
			
		} else {
			highlighted = highlight(bb.toString());
		}
		return highlighted;
	}

	
	public mxGraph getGraph(){
		return graph;
	}
	
	public mxGraphComponent getGraphComponent(){
		return graphComponent;
	}

	private String highlight(String zeile){
		Pattern p = Pattern.compile("[vp][0-9]+");
		
		// commands
		Vector<String> commands = Highlight.OP_CODES;

		for (String cmd : commands) {
			zeile = zeile.replace(cmd, "<FONT COLOR=\"red\">" + cmd
					+ "</FONT> ");
		}

		StringTokenizer st = new StringTokenizer(zeile, " ,}{", true);
		String reg = new String();
		while (st.hasMoreTokens()) {
			String temp = st.nextToken();
			Matcher m = p.matcher(temp);
			if (m.matches())
				reg += "<FONT COLOR=\"blue\">" + temp + "</FONT>";
			else
				reg += temp;
		}
		zeile = reg;

		// replace
		zeile = zeile.trim().replace("-&gt;",
				"<FONT COLOR=\"blue\">-&gt;</FONT>");

		// jumps
		Vector<String> jumps = new Vector<String>();

		jumps.add(":cond_");
		jumps.add(":goto_");
		jumps.add(":catch_");
		jumps.add(":catchall_");

		for (int i = 0; i < jumps.size(); i++) {
			for (int z = 100; z >= 0; z--) {
				zeile = zeile.trim().replace(
						jumps.get(i) + z + "",
						"<FONT COLOR=\"#009900\">" + jumps.get(i) + z
								+ "</FONT>");
			}
		}

		// Annotations
		if (zeile.trim().startsWith(".") || zeile.trim().startsWith("#")
				|| zeile.trim().startsWith(":")) {
			zeile = "<FONT COLOR=\"#5F5F5F\">" + zeile + "</FONT>";
		}

		return zeile;
		
	}
	
}
