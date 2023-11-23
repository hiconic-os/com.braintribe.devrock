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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.upload.lab;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.deploy.ArtifactDeployer;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.resolver.ArtifactDataResolverModule;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.mc.reason.PartUploadFailed;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.artifact.consumable.ArtifactResolution;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.resource.FileResource;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

@Category(KnownIssue.class)
public class UploadLab implements HasCommonFilesystemNode {

	protected File repo;
	protected File fsRepo;
	protected File input;
	protected File output;
	
	protected File upload;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/upload.lab");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");
		fsRepo = new File( output, "fs-repo");
		upload = new File( output, "upload");
	}
	
	private File settings = new File( input, "settings.xml");

	/**
	 * generates an artifact with the files from the passed directory
	 * @param name - the {@link VersionedArtifactIdentification} as string
	 * @param directory - the directory that contains the files 
	 * @return - the {@link Artifact}
	 */
	protected Artifact generateArtifact(String name, File directory) {
		VersionedArtifactIdentification vai = VersionedArtifactIdentification.parse(name);
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId(vai.getGroupId());
		artifact.setArtifactId(vai.getArtifactId());
		artifact.setVersion(vai.getVersion());
		
		File [] files = directory.listFiles();
		
		if (files == null || files.length == 0)
			return artifact;
		
		String prefix = artifact.getArtifactId() + "-" + artifact.getVersion();
		for (File file : files) {
			String fileName = file.getName();
			if (!fileName.startsWith( prefix)) {
				continue;
			}
					 
			String suffix = fileName.substring( prefix.length());
			int dot = suffix.indexOf( '.');
			String extension = suffix.substring( dot + 1);		
			String rem = suffix.substring(0, dot);
			
			// check classifier
			String classifier = null;
			if (suffix.startsWith( "-")) {
				classifier = rem.substring(1);				
			}
			
			Part part = Part.T.create();
			part.setClassifier(classifier);
			part.setType(extension);
			FileResource fileResource = FileResource.T.create();
			fileResource.setPath(file.getAbsolutePath());
			part.setResource(fileResource);
			
			String key = classifier != null ? classifier + ":" + extension : extension;
			artifact.getParts().put( key, part);
		}
				
		return artifact;
	}
	
	protected OverridingEnvironment buildVirtualEnvironement(Map<String,String> overrides) {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		if (overrides != null && !overrides.isEmpty()) {
			ove.setEnvs(overrides);						
		}
		ove.setEnv("M2_REPO", repo.getAbsolutePath());
		ove.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings.getAbsolutePath());
		return ove;		
		
		//DEVROCK_TESTS_WRITE_USERNAME
		//DEVROCK_TESTS_WRITE_PASSWORD
		//DEVROCK_TESTS_REPOSITORY_BASE_URL
	}
	
	/**
	 * actually upload the artifacts 
	 * @param repositoryId - the id of the target repository 
	 * @param artifacts - the artifacts to upload 
	 * @return - the {@link ArtifactResolution}
	 */
	protected ArtifactResolution upload( String repositoryId, Collection<Artifact> artifacts) {
		try (				
				WireContext<ArtifactDataResolverContract> resolverContext = Wire.contextBuilder( ArtifactDataResolverModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))				
					.build();
			) {
			RepositoryReflection repositoryReflection = resolverContext.contract().repositoryReflection();
			Repository repository = repositoryReflection.getRepository( repositoryId);
			if (repository == null) {
				ArtifactResolution resolution = ArtifactResolution.T.create();
				resolution.setFailure( Reasons.build(PartUploadFailed.T).text("No repository with the id [" + repositoryId + "] exists").toReason());						
				return resolution;
			}
			ArtifactDeployer artifactDeployer = resolverContext.contract().backendContract().artifactDeployer( repository);

			ArtifactResolution resolution = artifactDeployer.deploy( artifacts);

			return resolution;
		}			
	}

	//@Test
	public void runUpload() {
		List<Artifact> artifacts = new ArrayList<>();
		//artifacts.add( generateArtifact("com.braintribe.devrock.test:a#1.0.2", new File( input, "a-1.0.2")));
		//artifacts.add( generateArtifact("com.braintribe.devrock:maven-metadata-marshaller#1.0.21-pc", new File( input, "maven-metadata-marshaller-1.0.21-pc")));
		artifacts.add( generateArtifact("com.braintribe.devrock:declared-maven-settings-model#1.0.22-pc", new File( input, "declared-maven-settings-model-1.0.22-pc")));
		
		ArtifactResolution resolution = upload( "devrock-tests", artifacts);
		System.out.println(resolution);
	}
}
