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
package com.braintribe.devrock.mc.core.resolver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.braintribe.devrock.mc.api.commons.VersionInfo;
import com.braintribe.devrock.mc.api.resolver.ArtifactVersionsResolver;
import com.braintribe.devrock.mc.api.resolver.DependencyResolver;
import com.braintribe.devrock.model.mc.reason.UnresolvedDependencyVersion;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;

/**
 * basic implementation of the {@link DependencyResolver}
 * @author pit/dirk
 *
 */
public class BasicDependencyResolver implements DependencyResolver {

	private ArtifactVersionsResolver versionsResolver;
	
	
	
	/**
	 * @param versionsResolver - the {@link ArtifactVersionsResolver} to use 
	 */
	public BasicDependencyResolver(ArtifactVersionsResolver versionsResolver) {
		super();
		this.versionsResolver = versionsResolver;
	}

	@Override
	public Maybe<CompiledArtifactIdentification> resolveDependency(CompiledDependencyIdentification dependencyIdentification) {
		VersionExpression versionExpression = dependencyIdentification.getVersion();
		
		// in case the dependency has a concrete version we can directly imply a CompiledArtifactIdentification with no further resolutions 
		if (versionExpression instanceof Version) {
			return Maybe.complete(CompiledArtifactIdentification.from(dependencyIdentification, (Version)versionExpression));
		}
		
		// retrieve the available versions for the dependency
		List<VersionInfo> versionInfos = versionsResolver.getVersions(dependencyIdentification);

		// if there is no version at all we can return eagerly with an adequate reason
		if (versionInfos.isEmpty())
			return TemplateReasons.build(UnresolvedDependencyVersion.T).enrich(r -> r.setVersion(versionExpression)).toMaybe();

		// filter matches
		Optional<VersionInfo> match = versionInfos.stream().sorted(versionInfoComparator).filter(i -> versionExpression.matches(i.version())).findFirst();
		
		if (match.isPresent()) {
			return Maybe.complete(CompiledArtifactIdentification.from(dependencyIdentification, match.get().version())); 
		}
		
		
//		// find first matching version (list is sorted ascending) 
//		ListIterator<VersionInfo> iterator = versionInfos.listIterator( versionInfos.size());
//
//		while (iterator.hasPrevious()) {
//			VersionInfo versionInfo = iterator.previous();			
//			Version version = versionInfo.version();
//			if (versionExpression.matches( version)) {
//				return Maybe.complete(CompiledArtifactIdentification.from(dependencyIdentification, version));
//			}			
//		}
		
		Map<String, List<Version>> versionsPerRepo = new LinkedHashMap<>();
		
		for (VersionInfo info: versionInfos) {
			for (String repoId: info.repositoryIds()) {
				List<Version> versions = versionsPerRepo.computeIfAbsent(repoId, k -> new ArrayList<>());
				versions.add(info.version());
			}
		}
		
		List<Version> availableVersions = versionInfos.stream().map(VersionInfo::version).collect(Collectors.toList());
		
		return TemplateReasons.build(UnresolvedDependencyVersion.T) //
				.enrich(r -> { //
					r.setAvailableVersions(availableVersions); //
					r.setVersion(versionExpression); //
				}).toMaybe();
	}
	
	private static int compareDominance(VersionInfo i1, VersionInfo i2) {
		Integer p1 = i1.dominancePos();
		Integer p2 = i2.dominancePos();
		
		if (p1 == p2)
			return 0;
		
		if (p1 == null)
			return 1;
		
		if (p2 == null)
			return -1;
		
		return p1.compareTo(p2);
	}
	
	private static Comparator<VersionInfo> versionInfoComparator;
	
	static {
		Comparator<VersionInfo> c1 = BasicDependencyResolver::compareDominance;
		Comparator<VersionInfo> c2 = VersionInfo::compareTo;
		
		versionInfoComparator = c1.thenComparing(c2.reversed());
	}

	private String formatAvailableVersions(Map<String, List<Version>> versionsPerRepo) {
		StringBuilder builder = new StringBuilder();
		
		boolean first = true;
		for (Map.Entry<String, List<Version>> entry: versionsPerRepo.entrySet()) {
			if (first)
				first = false;
			else 
				builder.append(" | ");
			
			String repoId = entry.getKey();
			builder.append(repoId);
			builder.append(": ");
			
			builder.append(entry.getValue().stream().map(Version::asString).collect(Collectors.joining(", ")));
		}
		
		
		String availableVersionsStr = builder.toString();
		return availableVersionsStr;
	}

}
