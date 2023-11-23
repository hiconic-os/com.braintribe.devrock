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
package com.braintribe.devrock.mc.core.wired.resolving;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.devrock.model.repolet.content.Artifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

public class Collator {
	/**
	 * @param missing - a {@link List} of {@link com.braintribe.devrock.model.repolet.content.Dependency}
	 * @return - a concatenated string of all {@link com.braintribe.devrock.model.repolet.content.Dependency} in the list, separated by a comma
	 */
	public static String collateMissingDependencies( Collection<com.braintribe.devrock.model.repolet.content.Dependency> missing) {
		return collate( new ArrayList<VersionedArtifactIdentification>( missing));
	}	
	/**
	 * @param missing - a {@link List} of {@link Artifact}
	 * @return - a concatenated string of all {@link Artifact} in the list, separated by a comma
	 */
	public static String collateArtifacts( Collection<Artifact> missing) {
		return collate( new ArrayList<VersionedArtifactIdentification>( missing));
	}
	/**
	 * @param missing - a {@link List} of {@link VersionedArtifactIdentification}
	 * @return - a concatenated string of all {@link VersionedArtifactIdentification} in the list, separated by a comma
	 */
	public static String collate( Collection<VersionedArtifactIdentification> missing) {
		return missing.stream().map( d -> d.asString()).collect(Collectors.joining(","));
	}
	/**
	 * @param missing - a {@link List} of {@link AnalysisDependency}
	 * @return - a concatenated string of all {@link AnalysisDependency} in the list, separated by a comma
	 */
	public static String collateDependencies( Collection<AnalysisDependency> missing) {
		return missing.stream().map( d -> {
			String s = d.getGroupId() + ":" + d.getArtifactId() + "#" + d.getVersion();
			String suffix = d.getSolution() != null ? d.getSolution().asString() : "<n/a>";
			s += " -> " + suffix;			
			return s;
			}).collect(Collectors.joining(","));
	}
	/**
	 * @param excess - a {@link List} of {@link AnalysisArtifact}
	 * @return - a concatenated string of all {@link AnalysisArtifact} in the list, separated by a comma
	 */
	public static String collateAnalysisArtifacts( Collection<AnalysisArtifact> excess) {
		return excess.stream().map( d -> d.getGroupId() + ":" + d.getArtifactId() + "#" + d.getVersion()).collect(Collectors.joining(","));
	}

	/**
	 * @param excess - a {@link List} of {@link AnalysisArtifact}
	 * @return - a concatenated string of all {@link AnalysisArtifact} in the list, separated by a comma
	 */
	public static String collateUnexpectedAnalysisArtifacts( Collection<AnalysisArtifact> excess) {
		return excess.stream().map( d -> {
			String retval =  d.getGroupId() + ":" + d.getArtifactId() + "#" + d.getVersion();
			retval += "\n" + d.getFailure().stringify();
			return retval;
		}).collect(Collectors.joining(","));		
	}
	
	/**
	 * @param list - a {@link List} of {@link String}
	 * @return - a concatenated string of all {@link String} in the list, separated by a comma
	 */
	public static String collateNames( Collection<String> list) {
		return list.stream().collect( Collectors.joining(","));
	}
	
	/**
	 * @param list - a {@link Collection} of {@link EqProxy} of {@link PartIdentification}
	 * @return - a concatenated string of all {@link String} in the list, separated by a comma
	 */
	public static String collatePartProxies( Collection<EqProxy<PartIdentification>> list) {
		return list.stream().map( eq -> eq.get().asString()).collect( Collectors.joining(","));
	}
	/**
	 * @param list - a {@link Collection} of {@link PartIdentification}
	 * @return - a concatenated string of all {@link String} in the list, separated by a comma
	 */
	public static String collateParts( Collection<PartIdentification> list) {
		return list.stream().map(p -> p.asString()).collect( Collectors.joining(","));
	}
	
}
