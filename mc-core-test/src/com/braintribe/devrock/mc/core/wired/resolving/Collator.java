// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
