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
package com.braintribe.devrock.mc.core.resolver;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolver;
import com.braintribe.devrock.mc.core.commons.PartReflectionCommons;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.consumable.PartReflection;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.version.Version;
import com.braintribe.utils.paths.UniversalPath;

/**
 * an implementation of a file-system based (remote) repository
 *  
 * @author pit 
 *
 */
public class FilesystemRepositoryArtifactDataResolver implements ArtifactVersionsResolverTrait, ArtifactDataResolver {
	private File root;
	private String repositoryId = "unknown";
	
	@Configurable @Required
	public void setRoot(File root) {
		this.root = root;
	}
	
	@Configurable
	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}
	
	/**
	 * @param file - the file to return
	 * @return - either {@link Optional} with {@link BasicArtifactDataResolution} or empty {@link Optional} 
	 */
	private Maybe<ArtifactDataResolution> process(File file) {
		if (file.exists()) {
			BasicArtifactDataResolution res = new BasicArtifactDataResolution();
			res.setRepositoryId(repositoryId);
			FileResource resource = FileResource.T.create();
			resource.setName(file.getName());
			resource.setPath(file.getAbsolutePath());
			resource.setFileSize(file.length());
			res.setResource(resource);
			return Maybe.complete(res);
		}
		else
			return Reasons.build(NotFound.T).toMaybe();
	}
	

	@Override
	public Maybe<ArtifactDataResolution> resolveMetadata(ArtifactIdentification identification) {
		File mavenMetadataFile = ArtifactAddressBuilder.build().root(root.getAbsolutePath()).artifact(identification).metaData().toPath().toFile();	
		return process(mavenMetadataFile);		
	}

	@Override
	public Maybe<ArtifactDataResolution> resolveMetadata(CompiledArtifactIdentification identification) {
		File mavenMetadataFile = ArtifactAddressBuilder.build().root(root.getAbsolutePath()).compiledArtifact(identification).metaData().toPath().toFile();	
		return process(mavenMetadataFile);		
	}

	@Override
	public Maybe<ArtifactDataResolution> resolvePart(CompiledArtifactIdentification identification, PartIdentification partIdentification, Version partVersionOverride) {
		File partFile;
		
		if (partVersionOverride == null) 
			partFile = ArtifactAddressBuilder.build().root(root.getAbsolutePath()).compiledArtifact(identification).part( partIdentification).toPath().toFile();
		else 
			partFile = ArtifactAddressBuilder.build().root(root.getAbsolutePath()).compiledArtifact(identification).part( partIdentification, partVersionOverride).toPath().toFile();
		
		return process(partFile);
	}
	@Override
	public Maybe<ArtifactDataResolution> getPartOverview( CompiledArtifactIdentification compiledArtifactIdentification) {
		return Reasons.build(NotFound.T).toMaybe();
	}
	@Override
	public Maybe<List<PartReflection>> getPartsOfReasoned(CompiledArtifactIdentification compiledArtifactIdentification) {		
		File artifactRepository = UniversalPath.empty().push( root.getAbsolutePath())
								.pushDottedPath( compiledArtifactIdentification.getGroupId())
								.push( compiledArtifactIdentification.getArtifactId())
								.push( compiledArtifactIdentification.getVersion().asString())
								.toFile();
		File [] files = artifactRepository.listFiles(); 
		if (files == null || files.length == 0) {
			return Maybe.complete(Collections.emptyList());
		}
		List<String> names = new ArrayList<>(files.length);
		for (int i = 0; i < files.length; i++) {
			names.add( files[i].getName());
		}
		
		return Maybe.complete(PartReflectionCommons.transpose(compiledArtifactIdentification, repositoryId, names));
	}
	
	
}
