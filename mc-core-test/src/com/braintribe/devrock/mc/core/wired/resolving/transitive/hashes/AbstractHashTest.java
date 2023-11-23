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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.hashes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.deploy.ArtifactDeployer;
import com.braintribe.devrock.mc.api.download.PartDownloadManager;
import com.braintribe.devrock.mc.api.download.PartDownloadScope;
import com.braintribe.devrock.mc.api.event.EntityEventListener;
import com.braintribe.devrock.mc.api.event.EventContext;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.model.repolet.event.instance.OnDownloadEvent;
import com.braintribe.devrock.model.repolet.event.instance.OnHashFileUploadEvent;
import com.braintribe.devrock.model.repolet.event.instance.OnUploadEvent;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.artifact.consumable.ArtifactResolution;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.resource.FileResource;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * abstract base for test that deal with hashes 
 * 
 * @author pit
 *
 */
@Category(KnownIssue.class)
public abstract class AbstractHashTest implements HasCommonFilesystemNode, EntityEventListener<GenericEntity> {
	protected File repo;
	protected File input;
	protected File output;
	protected File uploadSource;
	protected File uploadTarget;	
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/hashes");
		input = pair.first;
		output = pair.second;
	
		uploadSource = new File( input, "upload");
		
		repo = new File( output, "repo");
		uploadTarget = new File( output, "upload");
	
	}
		
	protected File initial = new File( input, "initial");	
	protected File settings = new File( input, "settings.xml");


	protected Launcher launcher;
	{
		launcher = Launcher.build()
				.repolet()
					.name("archive")
						.descriptiveContent()
							.descriptiveContent(archiveInput())
						.close()
						.uploadFilesystem()
							.filesystem( uploadTarget)
						.close()					
						// a has no hash in header, requires hash files 
						.hashes( "a-1.0.1.pom")
							.noHeaderSupport()
						.close()
						// b has wrong hashes in header 
						.hashes("b-1.0.1.pom")
						   .hash( "X-Checksum-Md5", "bla-md5-bla")
						   .hash( "X-Checksum-Sha1", "bla-sha1-bla")
						   .hash( "X-Checksum-Sha256", "bla-sha256-bla")
						.close()									
					.close()				
			.done();
	}
	
	
	protected void additionalTasks() {}
	
	protected RepoletContent archiveInput() { return RepoletContent.T.create();}
	
	protected Map<String,List<String>> downloadsNotified;
	protected Map<String, UploadData> updateDataNotified;
	
	@Before
	public void runBefore() {
		TestUtils.ensure(repo); 			

		downloadsNotified = new HashMap<>();
		updateDataNotified = new HashMap<>();
		launcher.addListener( OnDownloadEvent.T, this::onEvent);
		launcher.addListener( OnUploadEvent.T, this::onUploadEvent);
		launcher.addListener( OnHashFileUploadEvent.T, this::onUploadHashFileEvent);
		launcher.launch();		
		
		additionalTasks();
	}
	
	@After
	public void runAfter() {
		launcher.shutdown();
	}
	

	protected RepoletContent archiveInput(String definition) {
		File file = new File( input, definition);
		try {
			return RepositoryGenerations.unmarshallConfigurationFile(file);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot load parser file [" + file.getAbsolutePath() + "]" , IllegalStateException::new);
		} 
	}
	
	protected OverridingEnvironment buildVirtualEnvironement(Map<String,String> overrides) {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		if (overrides != null && !overrides.isEmpty()) {
			ove.setEnvs(overrides);						
		}
		ove.setEnv("repo", repo.getAbsolutePath());
		ove.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings.getAbsolutePath());
		ove.setEnv( "port", Integer.toString( launcher.getAssignedPort()));				
		return ove;		
	}
	
	protected boolean runDownload(CompiledPartIdentification cpi) {
		Maybe<ArtifactDataResolution> resolutionOptional = runDownloadReasoned(cpi);			
		if (resolutionOptional.isSatisfied()) {
			//System.out.println("present");
			return true;
		}
		else {
			//System.out.println("missing");
			return false;
		}
	}
	
	protected Maybe<ArtifactDataResolution> runDownloadReasoned(CompiledPartIdentification cpi) {
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
				.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))				
				.build();
				) {
			
			PartDownloadManager downloadManager = resolverContext.contract().dataResolverContract().partDownloadManager();
			
			PartDownloadScope partDownloadScope = downloadManager.openDownloadScope();
			
			return partDownloadScope.download(cpi, cpi).get();
		}
	}
	
	/**
	 * produces an artifact with the files passesd
	 * @param name - the {@link VersionedArtifactIdentification} as string
	 * @param directory - the directory with the files (parts)
	 * @return - the {@link Artifact} built from it 
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
	
	protected ArtifactResolution runUpload( Artifact artifact) {
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))				
					.build();
			) {
			
			RepositoryReflection repositoryReflection = resolverContext.contract().dataResolverContract().repositoryReflection();
			Repository repository = repositoryReflection.getRepository("archive");
			
			ArtifactDeployer deployer = resolverContext.contract().dataResolverContract().backendContract().artifactDeployer( repository);
			
			return deployer.deploy( artifact);				
		}
	}
	
	@Override
	public void onEvent(EventContext eventContext, GenericEntity event) {
		if (event instanceof OnDownloadEvent) {
			OnDownloadEvent oevent = (OnDownloadEvent) event;
			String source = oevent.getDownloadSource();
			String name = oevent.getSendingRepoletName();
			List<String> list = downloadsNotified.computeIfAbsent( name, k -> new ArrayList<>());
			list.add( source);
						
			//System.out.println("received download event from [" + name + "], source was [" + source + "]");
		}		
	}
	
	protected class UploadData {
		public String target;
		public Map<String, String> headerHashes;
		public Map<String,String> fileHashes;
	}
	
	private <E extends GenericEntity> void onUploadHashFileEvent(EventContext eventcontext1, E event) {
		OnHashFileUploadEvent uhev = (OnHashFileUploadEvent) event;
		String target = uhev.getHashFileName();
		String hash = uhev.getContent();
		int lastDot = target.lastIndexOf( '.');
		String hashTarget = target.substring(0, lastDot);
		String digest = target.substring( lastDot + 1);
		UploadData udata = updateDataNotified.computeIfAbsent( hashTarget, k -> new UploadData());
		if (udata.fileHashes == null)  {
			udata.fileHashes = new HashMap<>();
		}
		udata.fileHashes.put(digest, hash);		
	}
	private <E extends GenericEntity> void onUploadEvent(EventContext eventcontext1, E event) {
		
		OnUploadEvent uev = (OnUploadEvent) event;
		Map<String,String> hashes = uev.getHashes();
		String target = uev.getUploadTarget();
		UploadData udata = updateDataNotified.computeIfAbsent( target, k -> new UploadData());
		udata.target = target;
		udata.headerHashes = hashes;
		
	}
}
