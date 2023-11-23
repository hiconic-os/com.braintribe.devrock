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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.discovery;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.api.event.EntityEventListener;
import com.braintribe.devrock.mc.api.event.EventContext;
import com.braintribe.devrock.mc.api.repository.configuration.HasConnectivityTokens;
import com.braintribe.devrock.mc.api.repository.local.PartAvailability;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactPartResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.RepoletUsingTrait;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.model.repolet.event.instance.OnDownloadEvent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.devrock.repolet.launcher.LauncherTrait;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * tests how the 'discovery' method works, i.e. whether mc-core correctly identifies the possible origin of already existing files.<br/>
 * uses a 'fake' prepared local repository with data of a repository 'x', and then runs a resolution against the real 'x'. 
 * Expected is that a gets a part-availablitity file filled with data discovered via getHead rather than downloads. 
 * <br/>
 * 
 * two implementations: {@link StandardAccessPartavailabilityDiscoveryTest} and {@link RestAccessPartavailabilityDiscoveryTest}
 * 
 * @author pit
 *
 */
public abstract class AbstractDiscoveryTest implements LauncherTrait, HasCommonFilesystemNode, HasConnectivityTokens, EntityEventListener<GenericEntity>  {

	protected static final String COM_BRAINTRIBE_DEVROCK_TEST = "com.braintribe.devrock.test";
	
	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/discovery");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	
	protected File initial = new File( input, "initial");
	protected File offlineInitial = new File( input, "offline-initial");
	protected File settings = new File( input, "settings");
	protected File currentSettings = new File( settings, "basic-settings.xml");
	protected File repository = new File( output, "repo");
	
	protected File contentDef = new File( input, "content.definition.txt");
	protected File validationDef = new File( input, "content.validation.txt");
	protected Map<String,List<String>> downloadsNotified;
	
	protected TransitiveResolutionContext standardResolutionContext = TransitiveResolutionContext.build().done();
	
	protected RepoletContent content;
	{
		try {
			content = RepositoryGenerations.parseConfigurationFile( contentDef);		
		} catch (Exception e) {
			Assert.fail("cannot process first content");
			throw new IllegalStateException();
		}
	}
	
	// expectations for all tests 
	protected Map<PartIdentification,Boolean> partIdentifications = new HashMap<>();
	{  
		partIdentifications.put( PartIdentification.parse(":pom"), true);		
		partIdentifications.put( PartIdentification.parse(":jar"), true);
												
		partIdentifications.put( PartIdentification.parse("sources:jar"),true);
		partIdentifications.put( PartIdentification.parse("javadoc:jar"),true);
		partIdentifications.put( PartIdentification.parse("asset:man"),true);
		partIdentifications.put( PartIdentification.parse("notExpected:toBeThere"),false);
	}
	
	protected Launcher launcher;	
		
	protected LazyInitialized<CloseableHttpClient> httpClient = new LazyInitialized<>( RepoletUsingTrait::client);
	
	@Before
	public void runBefore() {
		TestUtils.ensure(output);
		repository.mkdirs();
		if (initial.exists()) {
			TestUtils.copy(initial, repository);
		}
		downloadsNotified = new HashMap<>();
		launcher.addListener( OnDownloadEvent.T, this::onEvent);
		
		launcher.launch();				
	}
	
	private String collate(List<String> strs) {
		return strs.stream().collect( Collectors.joining(","));
	}
	
	
	protected void validateDownloads( String repoletName, CompiledPartIdentification ... ids) {
		List<String> present = new ArrayList<>();
		List<String> missing = new ArrayList<>();
		List<String> list = downloadsNotified.get( repoletName);
		
		if (list == null) {
			list = java.util.Collections.emptyList();
		}
		

		for (CompiledPartIdentification id : ids) {
			String path = ArtifactAddressBuilder.build().compiledArtifact(id).toPath().toSlashPath();
			String partName = id.getArtifactId() + "-" + id.getVersion().asString();
			String suffix = id.getClassifier() != null ? id.getClassifier() + "." + id.getType() : "." + id.getType();
			String expectation = "/" + path + "/" + partName + "-" + suffix;
								
			if (list.contains( expectation)) {
				present.add( expectation);
			}
			else {
				missing.add( expectation);
			}					
			
		}
		List<String> excess = new ArrayList<>( list);
		excess.removeAll( present);
		
		Assert.assertTrue("missing [" + collate( missing) + "]", missing.size() == 0);
		Assert.assertTrue("excess [" + collate( excess) + "]", excess.size() == 0);		
	}
	
	
	
	@Override
	public void onEvent(EventContext eventContext, GenericEntity event) {
		if (event instanceof OnDownloadEvent) {
			OnDownloadEvent oevent = (OnDownloadEvent) event;
			String source = oevent.getDownloadSource();
			String name = oevent.getSendingRepoletName();
			List<String> list = downloadsNotified.computeIfAbsent( name, k -> new ArrayList<>());
			list.add( source);
						
			System.out.println("received download event from [" + name + "], source was [" + source + "]");
		}
		
	}



	@After
	public void runAfter() {
		if (launcher.isRunning()) {
			launcher.shutdown();
		}
	}

	protected OverridingEnvironment buildVirtualEnvironement(Map<String,String> overrides) {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		if (overrides != null && !overrides.isEmpty()) {
			ove.setEnvs(overrides);						
		}
		ove.setEnv("repo", repo.getAbsolutePath());
		ove.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", currentSettings.getAbsolutePath());
		ove.setEnv( "port", Integer.toString( launcher.getAssignedPort()));
				
		return ove;		
	}
		
	protected AnalysisArtifactResolution run(String terminal, TransitiveResolutionContext resolutionContext, boolean activate, Map<PartIdentification,Boolean> partExpectations) throws Exception {
		final Map<String,String> overrides = new HashMap<>();
		if (!activate) {							
			overrides.put(MC_CONNECTIVITY_MODE, MODE_OFFLINE);
		}
		
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement( overrides))				
					.build();
			) {
			
			TransitiveDependencyResolver transitiveDependencyResolver = resolverContext.contract().transitiveDependencyResolver();
			
			CompiledTerminal cdi = CompiledTerminal.from ( CompiledDependencyIdentification.parse( terminal));
			AnalysisArtifactResolution artifactResolution = transitiveDependencyResolver.resolve( resolutionContext, cdi);
			
			ArtifactPartResolver partResolver = resolverContext.contract().dataResolverContract().artifactResolver();
			if (partExpectations != null && !partExpectations.isEmpty()) {
				for (AnalysisArtifact aa : artifactResolution.getSolutions()) {
					
					boolean isTerminal = aa.getDependers().stream().filter(d -> d.getDepender() == null).findFirst().isPresent();

					if (isTerminal) {
						continue;
					}
					
					CompiledArtifact compiledArtifact = aa.getOrigin();
					for (Map.Entry<PartIdentification, Boolean> entry : partExpectations.entrySet()) {
						PartIdentification partIdentification = entry.getKey();
						Maybe<ArtifactDataResolution> resolvedPart = partResolver.resolvePart(compiledArtifact, partIdentification);
						if (resolvedPart.isSatisfied()) {
							Assert.assertTrue("Part [" + partIdentification.asString() + "] of [" + compiledArtifact.asString() + "] is present yet shouldn't be ", entry.getValue()); 
					
						}
						else {
							Assert.assertTrue("Part [" + partIdentification.asString() + "] of [" + compiledArtifact.asString() + "] isn't present yet should be ", !entry.getValue());
						}
					}
					// validated part availability - only complete after all's resolved					
					Pair<String,Map<EqProxy<PartIdentification>,PartAvailability>> partAvailabilityData = loadPartAvailabilityFile(compiledArtifact);					
					validatePartAvailability( compiledArtifact, partAvailabilityData.second, partExpectations);
					
				}
			}
									
			return artifactResolution;					
								
		}				
	}
	
		
	/**
	 * @param ca - the {@link CompiledArtifact} whose part-availability we want
	 * @return - a {@link Pair} of UUID (the key of the part-availability) and a {@link Map} of {@link PartIdentification} to {@link PartAvailability}
	 */
	protected abstract Pair<String,Map<EqProxy<PartIdentification>,PartAvailability>> loadPartAvailabilityFile(CompiledArtifact ca);	

	/**
	 * validate stuff only related to the online test - after running the resolution 
	 */
	protected void validateAdditionalOnlineAspects() {}	
	/**
	 * additional preparation for the online test - if required, prior to running the resolution 
	 */
	protected void prepareOnlineTests() {}
	
	/**
	 * validate stuff only related to the offline test - after running the resolution
	 */
	protected void validateAdditionalOfflineAspects() {}
	/**
	 * additional preparation for the offline test - prior to running the resolution
	 */
	protected void prepareOfflineTest() {}	


	/**
	 * validate all parts of an artifact
	 * @param ca
	 * @param found
	 * @param map
	 */
	protected void validatePartAvailability(CompiledArtifact ca, Map<EqProxy<PartIdentification>, PartAvailability> found, Map<PartIdentification, Boolean> map) {
		for (Map.Entry<PartIdentification, Boolean> entry : map.entrySet()) 
			validatePartAvailability(ca, found, entry);
	}
	
	/**
	 * validate a single part 
	 * @param ca - the owning {@link CompiledArtifact}
	 * @param found - found data (as read from file)
	 * @param entry - the data to check
	 */
	protected void validatePartAvailability(CompiledArtifact ca, Map<EqProxy<PartIdentification>, PartAvailability> found, Map.Entry<PartIdentification, Boolean> entry) {
		
		PartIdentification pi = entry.getKey();
		boolean expectedToBeAvailable = Boolean.TRUE.equals(entry.getValue());
		
		PartAvailability pa = found.get( HashComparators.partIdentification.eqProxy(pi));
		if (pa == null) {
			Assert.assertTrue("expected to find a part-availability for [" + pi.asString() + "] of [" + ca.asString() + "], but found nothing", !expectedToBeAvailable);
			return;
		}
		switch( pa) {
		case available:
			Assert.assertTrue( "expected [" + pi.asString() + "]  of [" + ca.asString() + "] to be present, yet it isn't", expectedToBeAvailable);
			break;
		case unavailable:
			Assert.assertTrue( "expected [" + pi.asString() + "]  of [" + ca.asString() +  "] not to be present, yet it is", !expectedToBeAvailable);
			break;
		case unknown:
			Assert.fail( "unknown is not an expected value for [" + pi.asString() + "]  of [" + ca.asString() + "]");
			break;
		default:
			Assert.assertTrue( "expected [" + pi.asString() + "]  of [" + ca.asString() + "] to be present (or known), yet it isn't", expectedToBeAvailable);
			break;			
		}			
	}
	

	/**
	 * run the online test 
	 */
	protected void runOnline() {
		AnalysisArtifactResolution resolution = null;
		try {
			resolution = run( COM_BRAINTRIBE_DEVROCK_TEST + ":t#1.0.1", standardResolutionContext, true, partIdentifications);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown " + e.getLocalizedMessage());
		}
		Validator validator = new Validator();
		validator.validateExpressive(validationDef, resolution);
		validator.assertResults();
		validateAdditionalOnlineAspects();
	}
		
	/**
	 * run the offline test
	 */
	protected void runOffline() {
		prepareOfflineTest();
		AnalysisArtifactResolution resolution = null;
		try {
			resolution = run( COM_BRAINTRIBE_DEVROCK_TEST + ":t#1.0.1", standardResolutionContext, false, partIdentifications);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown " + e.getLocalizedMessage());
		}
		Validator validator = new Validator();
		validator.validateExpressive(validationDef, resolution);
		validator.assertResults();

		validateAdditionalOfflineAspects();
	}
}
