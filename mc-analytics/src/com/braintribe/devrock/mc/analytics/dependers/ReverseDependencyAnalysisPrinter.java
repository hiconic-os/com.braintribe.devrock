// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.devrock.mc.analytics.dependers;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.cc.lcd.HashingComparator;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.processing.core.commons.EntityHashingComparator;

/**
 * lill' printer for the {@link DependerAnalysisNode}s.. 
 * even if main stay will most probably look at the {@link AnalysisArtifactResolution} (which can be produced as well), the 
 * output on the console is definitively easier using the classs
 * @author pit
 *
 */
public class ReverseDependencyAnalysisPrinter {
	private Set<EqProxy<DependerAnalysisNode>> visited = new HashSet<>();
	private static final String spacers = "\t\t\t\t\t\t\t\t\t\t\t\t\t";
	
	
	/**
	 * a comparator
	 */
	static final HashingComparator<DependerAnalysisNode> dependerAnalysisNode = EntityHashingComparator
			.build( DependerAnalysisNode.T)			
			.addPropertyPathField(DependerAnalysisNode.versionedArtifactIdentification, VersionedArtifactIdentification.groupId)
			.addPropertyPathField(DependerAnalysisNode.versionedArtifactIdentification, VersionedArtifactIdentification.artifactId)
			.addPropertyPathField(DependerAnalysisNode.versionedArtifactIdentification, VersionedArtifactIdentification.version)
			.done();
	
	public static void output(DependerAnalysisNode node) {
		ReverseDependencyAnalysisPrinter printer = new ReverseDependencyAnalysisPrinter();
		printer.show( node,0, false);
	}
	public static void detailedOutput(DependerAnalysisNode node) {	
		ReverseDependencyAnalysisPrinter printer = new ReverseDependencyAnalysisPrinter();
		printer.show( node,0, true);
		
	}	
	public static void outputToConsole(DependerAnalysisNode node) {
		ReverseDependencyAnalysisPrinter printer = new ReverseDependencyAnalysisPrinter();
		ConfigurableConsoleOutputContainer consoleOutputContainer = ConsoleOutputs.configurableSequence();
		printer.output( consoleOutputContainer, node, 0, false);
		ConsoleOutputs.print(consoleOutputContainer);
	}
	
	public static void detailedOutputToConsole(DependerAnalysisNode node) {
		ReverseDependencyAnalysisPrinter printer = new ReverseDependencyAnalysisPrinter();
		ConfigurableConsoleOutputContainer consoleOutputContainer = ConsoleOutputs.configurableSequence();
		printer.output( consoleOutputContainer, node, 0, true);
		ConsoleOutputs.print(consoleOutputContainer);
	}
	
	private void output(ConfigurableConsoleOutputContainer consoleOutputContainer, DependerAnalysisNode startingPoint, int offset, boolean showDependency) {
		String owner = spacers.substring(0, offset) + " " + startingPoint.asString();
		
		if (!showDependency) {
			consoleOutputContainer.append(owner);
		}
		else {			
			CompiledDependency referencingDependency = startingPoint.getReferencingDependency();
			consoleOutputContainer.append(owner  + (referencingDependency != null ? "-> " + referencingDependency.asString() : ""));
		}
		
		
		if (!visited.add( dependerAnalysisNode.eqProxy(startingPoint))) {
			consoleOutputContainer.append(" (already traversed)");
			return;
		}
		else {
			consoleOutputContainer.append("\n");			
		}
		List<DependerAnalysisNode> nodes = startingPoint.getNextNodes();
		for (DependerAnalysisNode node : nodes) {				
			output( consoleOutputContainer, node, offset+1, showDependency);
		}
		
	}
	
	private List<DependerAnalysisNode> sort( List<DependerAnalysisNode> nodes) {
		
		nodes.sort( new Comparator<DependerAnalysisNode>() {

			@Override
			public int compare(DependerAnalysisNode o1, DependerAnalysisNode o2) {			
				return o1.getVersionedArtifactIdentification().compareTo(o2.getVersionedArtifactIdentification());
			}			
		});
			
		// TODO: ask Dischi why I can't use the EntityHashingComparator from above
		//nodes.sort( (Comparator<? super DependerAnalysisNode>) dependerAnalysisNode);
		return nodes;
	}
		
	/**
	 * makes a little text output of the {@link DependerAnalysisNode}, starting from the starting-point
	 * @param startingPoint - the {@link DependerAnalysisNode} that should be the root of the display 
	 * @param offset - jst 
	 */
	private void show(DependerAnalysisNode startingPoint, int offset, boolean showDependency) {
		String owner = spacers.substring(0, offset) + " " + startingPoint.asString();
		
		if (!showDependency) {
			System.out.print(owner);
		}
		else {			
			CompiledDependency referencingDependency = startingPoint.getReferencingDependency();
			System.out.print(owner  + (referencingDependency != null ? "-> " + referencingDependency.asString() : ""));
		}
		
		
		if (!visited.add( dependerAnalysisNode.eqProxy(startingPoint))) {
			System.out.println(" (already traversed)");
			return;
		}
		else {
			System.out.println();			
		}
		List<DependerAnalysisNode> nodes = startingPoint.getNextNodes();
		for (DependerAnalysisNode node : sort(nodes)) {				
			show( node, offset+1, showDependency);
		}		
	}	
}
