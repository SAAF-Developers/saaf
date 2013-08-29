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

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;

import de.rub.syssec.saaf.misc.Highlight;
import de.rub.syssec.saaf.model.application.BasicBlockInterface;
import de.rub.syssec.saaf.model.application.MethodInterface;

public class CFGGraph {
	MethodInterface method;
	mxGraph graph;
	mxGraphComponent graphComponent;
	
	HashMap<Integer, Object> vertices;
	public CFGGraph (MethodInterface m){
		method = m;
		vertices = new HashMap<Integer,Object>();
		graph = new mxGraph();	

		Object parent = graph.getDefaultParent();

		graph.getModel().beginUpdate();
		graph.setHtmlLabels(true);
		graph.setCellsDisconnectable(false);
		

		try	{
			graph.setAllowDanglingEdges(false);
			graph.setAutoSizeCells(true);
			
			graph.setCellsEditable(false);
			
			for(BasicBlockInterface bb: method.getBasicBlocks()){
				Object startVertex = null;
				int lineNr = bb.getCodeLines().getFirst().getLineNr();
				if(vertices.containsKey(lineNr)){
					startVertex = vertices.get(lineNr);
				} else {
					String b = highlight(bb.toString());
					startVertex = graph.insertVertex(parent, null, b, 0, 0, 80, 30);
					vertices.put(lineNr,startVertex);
				}

				graph.updateCellSize(startVertex);

				for( BasicBlockInterface target: bb.getNextBB()){
					Object targetVertex = null;
					lineNr = target.getCodeLines().getFirst().getLineNr();
					if(vertices.containsKey(lineNr)){
						targetVertex = vertices.get(lineNr);
					} else {
						String t = highlight(target.toString());
						targetVertex = graph.insertVertex(parent, null, t, 0, 0, 80, 30);
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

		graphComponent.scrollCellToVisible(graph.getDefaultParent());
		
		mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
        layout.setDisableEdgeStyle(false);
        


        layout.execute(graph.getDefaultParent());

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
