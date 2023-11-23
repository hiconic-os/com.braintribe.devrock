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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.download;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.api.download.PartDownloadManager;
import com.braintribe.devrock.mc.api.download.PartDownloadScope;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.PartAvailabilityReflection;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.consumable.PartReflection;
import com.braintribe.processing.async.api.Promise;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * abstract base for tests for the {@link PartDownloadManager}, where its input comes from the *known* parts artifacts members of a dependency resolution, via
 * part-availablity-accesses. This one focuses solely on REST based part-availability
 * @author pit
 *
 */
public abstract class AbstractDownloadManagerTest implements HasCommonFilesystemNode {

	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/download");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	protected File settings = new File(input, "settings.xml");
	protected File initial = new File( input, "initial");
	
	protected Launcher launcher; 
	
	protected TransitiveResolutionContext lenientContext = TransitiveResolutionContext.build().lenient(true).done();
	
	protected void additionalTasks() {}
	
	@Before
	public void runBefore() {
		
		TestUtils.ensure(repo); 			
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
			return RepositoryGenerations.parseConfigurationFile(file);
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

	/**
	 * run a resolution first and then extract the locally known part availability 
	 * @param terminalAsString - the terminal for the resolution 
	 * @param availabilityTargetAsString - the availability target
	 * @return - a {@link Map} of the repoid to a {@link Set} of the found {@link CompiledPartIdentification}
	 */
	protected List<CompiledPartIdentification> run(String availabilityTargetAsString, String repo) {
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))				
					.build();
			) {
			
			// retrieve the part availability data
			CompiledArtifactIdentification compiledTargetIdentification = CompiledArtifactIdentification.parse(availabilityTargetAsString);
			PartAvailabilityReflection partAvailabilityReflection = resolverContext.contract().dataResolverContract().partAvailabilityReflection();
			
					
			
			List<PartReflection> allKnownPartsOf = partAvailabilityReflection.getAvailablePartsOf(compiledTargetIdentification);
			List<PartReflection> partsOfRepository = allKnownPartsOf.stream().filter( p -> repo.equals(p.getRepositoryOrigin())).collect( Collectors.toList());
			
			PartDownloadManager downloadManager = resolverContext.contract().dataResolverContract().partDownloadManager();
			
			PartDownloadScope partDownloadScope = downloadManager.openDownloadScope();
			
			if (partsOfRepository.size() != 0) {
				Map<CompiledPartIdentification, Promise<Maybe<ArtifactDataResolution>>> promises = new HashMap<>( partsOfRepository.size());
				
				List<String> assertions = new ArrayList<>( partsOfRepository.size());
				for (PartReflection pr : partsOfRepository) {
					CompiledPartIdentification cpi = CompiledPartIdentification.from(compiledTargetIdentification, pr);
					promises.put( cpi, partDownloadScope.download(cpi, cpi));
				}
				List<CompiledPartIdentification> successfullyDownloadedParts = new ArrayList<>( promises.size());
				
				for (Map.Entry<CompiledPartIdentification, Promise<Maybe<ArtifactDataResolution>>> entry : promises.entrySet()) {					
					Maybe<ArtifactDataResolution> optional = entry.getValue().get();
					if (optional.isUnsatisfied()) {
						assertions.add( "couldn't download [" + entry.getKey().asString() +"]");
					}
					else {
						successfullyDownloadedParts.add( entry.getKey());
					}
				}
				if (assertions.size() > 0) {
					Assert.fail( assertions.stream().collect(Collectors.joining("\t\n")));
				}
				
				return successfullyDownloadedParts;
			}
			
			return null;
											
		}
		catch( Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown [" + e.getLocalizedMessage() + "]");		
		}
		return null;
	}
	
	/**
	 * @param root
	 * @param compiledArtifactIdentification
	 * @param found
	 * @return
	 */
	protected boolean validate( File root, CompiledArtifactIdentification compiledArtifactIdentification, Set<CompiledPartIdentification> found) {
		List<String> assertions = new ArrayList<>();
		
		File artifactDirectory = ArtifactAddressBuilder.build().root(root.getAbsolutePath()).compiledArtifact(compiledArtifactIdentification).toPath().toFile();
		if (!artifactDirectory.exists()) {
			assertions.add( "expected directory [" + artifactDirectory.getAbsolutePath() + "] doesn't exist");
		}
		else {
			File [] files = artifactDirectory.listFiles();
			if (files == null || files.length == 0) {
				assertions.add( "directory [" + artifactDirectory.getAbsolutePath() + "] is unexpectedly empty");
			}
			else {
				Set<String> expectedNames = found.stream().map( cp -> cp.asFilename()).collect( Collectors.toSet());
				List<String> matching = new ArrayList<>( expectedNames.size());
				List<String> excess = new ArrayList<>( files.length);
				for (File file : files) {					
					String foundName = file.getName();
					
					// filter out metadata / part-availability
					if (!foundName.startsWith( compiledArtifactIdentification.getArtifactId() + "-" + compiledArtifactIdentification.getVersion().asString()))
						continue;
					
					if (expectedNames.contains( foundName)) {
						matching.add( foundName);
					}
					else {
						excess.add( foundName);
					}
				}
				List<String> missing = new ArrayList<>( expectedNames);
				missing.removeAll( matching);
				
				if (missing.size() > 0) {
					assertions.add( "missing [" + missing.stream().collect(Collectors.joining(",")));
				}
				if (excess.size() > 0) {
					assertions.add( "excess [" + excess.stream().collect( Collectors.joining( ",")));
				}
			}
		}
		
		if (assertions.size() > 0) {
			Assert.fail( assertions.stream().collect( Collectors.joining("\n")));
			return false;		
		}
		return true;
	}
	
	
}
