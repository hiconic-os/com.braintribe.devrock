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
package com.braintribe.devrock.mc.core.resolver.workspace;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mc.api.commons.VersionInfo;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolver;
import com.braintribe.devrock.mc.core.commons.McConversions;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.mc.core.resolver.BasicArtifactDataResolution;
import com.braintribe.devrock.mc.core.resolver.BasicVersionInfo;
import com.braintribe.devrock.model.repository.WorkspaceRepository;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.marshaller.artifact.maven.metadata.DeclaredMavenMetaDataMarshaller;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.consumable.PartReflection;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.maven.meta.MavenMetaData;
import com.braintribe.model.artifact.maven.meta.Versioning;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.version.Version;
import com.braintribe.utils.lcd.LazyInitialized;

/**
 * {@link ArtifactDataResolver} for {@link WorkspaceRepository}
 * 
 * @author pit
 *
 */
public class WorkspaceArtifactDataResolver implements ArtifactDataResolver {
	private String repositoryId;
	private Map<EqProxy<ArtifactIdentification>, CodebaseArtifacts> artifacts = new HashMap<>();
	
	
	@Configurable
	@Required
	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}
	
	@Configurable
	@Required
	public void setArtifacts(Collection<Artifact> projects) {
		// transpose data internally 
		for (Artifact entry : projects) {		
			CompiledArtifactIdentification cai = CompiledArtifactIdentification.from(entry);
			EqProxy<ArtifactIdentification> key = HashComparators.artifactIdentification.eqProxy( cai);			
			CodebaseArtifacts codebaseArtifacts =  artifacts.computeIfAbsent( key, k -> {
				CodebaseArtifacts cas = new CodebaseArtifacts();
				cas.identification = ArtifactIdentification.from( entry);
				return cas;
			});			
			codebaseArtifacts.artifacts.put( HashComparators.version.eqProxy( cai.getVersion()), entry);
		}
		
	}
	
	
	private static class CodebaseArtifacts {
		ArtifactIdentification identification;
		Map<EqProxy<Version>, Artifact> artifacts = new HashMap<>();
		

		LazyInitialized<Resource> mavenMetaDataResource = new LazyInitialized<>(this::buildMavenMetaDataResource);
		
		private Resource buildMavenMetaDataResource() {
			MavenMetaData mavenMetaData = MavenMetaData.T.create();
			mavenMetaData.setGroupId(identification.getGroupId());
			mavenMetaData.setArtifactId(identification.getArtifactId());
			
			List<Version> versions = getVersions();
			
			Versioning versioning = Versioning.T.create();
			versioning.setVersions(versions);
			
			versioning.setLastUpdated(McConversions.formatMavenMetaDataDate(new Date()));
			
			if (!versions.isEmpty()) {
				Version latest = versions.get(versions.size() - 1);
				versioning.setLatest(latest);
			}
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
			
			DeclaredMavenMetaDataMarshaller.INSTANCE.marshall(baos, mavenMetaData);
			
			byte[] byteArray = baos.toByteArray();
			
			Resource resource = Resource.createTransient(() -> new ByteArrayInputStream(byteArray));
			resource.setName("maven-metadata.xml");
			resource.setFileSize((long)byteArray.length);
			resource.setMimeType("text/xml");

			return resource; 
		}
		
		public List<Version> getVersions() {
			return artifacts.keySet().stream().<Version>map(EqProxy::get).sorted(Version::compareTo).collect(Collectors.toList());
		}
	}
			
	@Override
	public List<VersionInfo> getVersions(ArtifactIdentification artifactIdentification) {
		EqProxy<ArtifactIdentification> key = HashComparators.artifactIdentification.eqProxy(artifactIdentification);
		
		CodebaseArtifacts codebaseArtifacts = artifacts.get(key);
		if (codebaseArtifacts == null) {
			return Collections.emptyList();
		}
		List<VersionInfo> versionInfos = codebaseArtifacts.getVersions().stream() //
			.map(v -> new BasicVersionInfo(v, Collections.singletonList(repositoryId))) //
			.collect(Collectors.toList());
		
		return versionInfos;
	}
	
	@Override
	public Maybe<List<VersionInfo>> getVersionsReasoned(ArtifactIdentification artifactIdentification) {
		return Maybe.complete(getVersions(artifactIdentification));
	}

	@Override
	public Maybe<ArtifactDataResolution> resolvePart(CompiledArtifactIdentification artifactIdentification, PartIdentification partIdentification, Version partVersionOverride) {
		Artifact codebaseArtifact = findArtifact(artifactIdentification);
		
		if (codebaseArtifact == null)
			return Maybe.empty(Reasons.build(NotFound.T).text( "cannot find " + partIdentification.toString()).toReason());
		
		Part part = codebaseArtifact.getParts().get( PartIdentification.asString( partIdentification));
		if (part == null) {
			CompiledPartIdentification cpi = CompiledPartIdentification.from(artifactIdentification, partIdentification);
			return Maybe.empty( Reasons.build(NotFound.T).text( "part " + cpi.asString() + "] cannot be found ").toReason());
		}
						
		BasicArtifactDataResolution res = new BasicArtifactDataResolution();
		res.setRepositoryId(repositoryId);
		res.setResource( part.getResource());
		return Maybe.complete(res);
	}

	private Artifact findArtifact(CompiledArtifactIdentification artifactIdentification) {
		EqProxy<ArtifactIdentification> key = HashComparators.artifactIdentification.eqProxy(artifactIdentification);
		
		CodebaseArtifacts codebaseArtifacts = artifacts.get(key);
		if (codebaseArtifacts == null) {
			return null;
		}
		
		Artifact codebaseArtifact = codebaseArtifacts.artifacts.get(HashComparators.version.eqProxy(artifactIdentification.getVersion()));
		return codebaseArtifact;
	}

	@Override
	public Maybe<ArtifactDataResolution> resolveMetadata(ArtifactIdentification artifactIdentification) {
		EqProxy<ArtifactIdentification> key = HashComparators.artifactIdentification.eqProxy(artifactIdentification);
		
		CodebaseArtifacts codebaseArtifacts = artifacts.get(key);
		if (codebaseArtifacts.getVersions().isEmpty())
			return Maybe.empty( Reasons.build(NotFound.T).toReason());
		
		BasicArtifactDataResolution resolution = new BasicArtifactDataResolution(codebaseArtifacts.mavenMetaDataResource.get());
		resolution.setRepositoryId(repositoryId);
		
		return Maybe.complete(resolution);
	}

	@Override
	public Maybe<ArtifactDataResolution> resolveMetadata(CompiledArtifactIdentification identification) {
		return Maybe.empty(Reasons.build(NotFound.T).toReason());
	}

	

	@Override
	public List<PartReflection> getPartsOf(CompiledArtifactIdentification compiledArtifactIdentification) {
		Artifact codebaseArtifact = findArtifact(compiledArtifactIdentification);
		if (codebaseArtifact == null) {
			return Collections.emptyList();
		}
		
		return new ArrayList<PartReflection>( codebaseArtifact.getParts().values());			
		
	}

	@Override
	public Maybe<ArtifactDataResolution> getPartOverview( CompiledArtifactIdentification compiledArtifactIdentification) {
		
		return Maybe.empty( Reasons.build(NotFound.T).toReason());
	}
	
}
