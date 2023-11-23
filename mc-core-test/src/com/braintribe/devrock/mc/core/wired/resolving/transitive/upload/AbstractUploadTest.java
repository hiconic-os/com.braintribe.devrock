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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.deploy.ArtifactDeployer;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.resolver.ArtifactDataResolverModule;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.mc.reason.PartUploadFailed;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.artifact.consumable.ArtifactResolution;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.resource.FileResource;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * abstract common base for all tests working with the ArtifactDeployer
 * @author pit
 *
 */
public abstract class AbstractUploadTest implements HasCommonFilesystemNode {

	protected File repo;
	protected File fsRepo;
	protected File input;
	protected File output;
	
	protected File upload;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/upload");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");
		fsRepo = new File( output, "fs-repo");
		upload = new File( output, "upload");
	}
	
	private File settings = new File( input, "settings.xml");
	

	protected abstract RepoletContent archiveInput();	
	
	
	private Launcher launcher; 
	{
		launcher = Launcher.build()
				.repolet()
					.name("archive")
						.descriptiveContent()
							.descriptiveContent(archiveInput())
						.close()
						.uploadFilesystem()
							.filesystem( upload)
						.close()
						.uploadReturnValueOverrides()
							.code("c-1.0.2.pom", 409)
						.close()
					.close()
				
			.done();
	}

	@Before
	public void runBefore() {
		TestUtils.ensure(repo);
		TestUtils.ensure(upload);
		TestUtils.ensure(fsRepo);
		launcher.launch();
	}
	
	@After
	public void runAfter() {
		launcher.shutdown();
	}
	
	
	protected OverridingEnvironment buildVirtualEnvironement(Map<String,String> overrides) {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		if (overrides != null && !overrides.isEmpty()) {
			ove.setEnvs(overrides);						
		}
		ove.setEnv("M2_REPO", repo.getAbsolutePath());
		ove.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings.getAbsolutePath());
		ove.setEnv( "port", Integer.toString( launcher.getAssignedPort()));
		ove.setEnv( "fs-archive", "/" + fsRepo.getAbsolutePath().replace('\\', '/'));				
		return ove;		
	}
	
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
			Repository repository = resolverContext.contract().repositoryReflection().getRepository( repositoryId);
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
	
	/**
	 * upload a single artifact 
	 * @param terminal - the terminal's name (artifact id & version)
	 * @param directory - the directory that contains the files to upload
	 * @param repositoryId - the id of the target repository 
	 * @return - an {@link ArtifactResolution}
	 */
	protected ArtifactResolution runSingle(String terminal, File directory, String repositoryId) {
		Artifact artifact = generateArtifact(terminal, directory);
		return upload( repositoryId, Collections.singletonList( artifact));
	}
	
	/**
	 * @param artifactsToUpload - a Map of the artifactId and version, and the directory with files for each artifact 
	 * @param repositoryId - the id of the target repository 
	 * @return - an {@link ArtifactResolution}
	 */
	protected ArtifactResolution runMultiple(Map<String, File> artifactsToUpload, String repositoryId) {
		List<Artifact> artifacts = new ArrayList<>( artifactsToUpload.size());
		for (Map.Entry<String, File> entry : artifactsToUpload.entrySet()) {
			artifacts.add( generateArtifact( entry.getKey(), entry.getValue()));
		}				
		return upload( repositoryId,  artifacts);
	}
		
}
