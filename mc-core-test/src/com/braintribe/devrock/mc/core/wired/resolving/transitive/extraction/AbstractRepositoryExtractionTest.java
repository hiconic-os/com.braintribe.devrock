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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.extraction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.download.PartDownloadManager;
import com.braintribe.devrock.mc.api.download.PartDownloadScope;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.PartAvailabilityReflection;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.consumable.PartReflection;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.resource.Resource;
import com.braintribe.processing.async.api.Promise;
import com.braintribe.utils.IOTools;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * abstract base for repository extractions 
 * @author pit
 *
 */
public abstract class AbstractRepositoryExtractionTest implements HasCommonFilesystemNode{

	protected File repo;
	protected File input;
	protected File output;
	protected File dump;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/extraction");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");
		dump = new File( output, "dump");
	}
	protected File settings = new File(input, "settings.xml");
	protected File initial = new File( input, "initial");

	@Before
	public void runBefore() {
		
		TestUtils.ensure(repo); 			
		TestUtils.ensure(dump);
	}
	
	@After
	public void runAfter() {
		
	}
	
	protected TransitiveResolutionContext lenientContext = TransitiveResolutionContext.build()
			.includeImportDependencies(true)
			.includeParentDependencies(true)
			.includeRelocationDependencies(true)
			.includeStandardDependencies(true)
			.dependencyFilter( (d) -> !(d.getScope().equals("test") || d.getScope().equals( "provided") || d.getOptional()))
			.lenient(true).done();
	

	protected OverridingEnvironment buildVirtualEnvironement(Map<String,String> overrides) {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		if (overrides != null && !overrides.isEmpty()) {
			ove.setEnvs(overrides);						
		}
		ove.setEnv("M2_REPO", repo.getAbsolutePath());
		ove.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings.getAbsolutePath());		
				
		return ove;		
	}
	
	/**
	 * @param availabilityTargetAsString - the terminal as a string
	 * @return - a {@link List} of {@link CompiledPartIdentification}
	 */
	protected List<CompiledPartIdentification> listParts(String availabilityTargetAsString) {
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))				
					.build();
			) {
				
				CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parse(availabilityTargetAsString);
				TransitiveDependencyResolver resolver = resolverContext.contract().transitiveDependencyResolver();
				
				CompiledTerminal ct = CompiledTerminal.from(cdi);				
				AnalysisArtifactResolution resolution = resolver.resolve(lenientContext, ct);
				
				if (resolution.hasFailed()) {
					Assert.fail("resolution for [" + availabilityTargetAsString + "] has unexpectedly failed : " + resolution.getFailure().asFormattedText());
					return null;
				}
				
				List<CompiledPartIdentification> parts = new LinkedList<>();
				
				PartAvailabilityReflection partAvailabilityReflection = resolverContext.contract().dataResolverContract().partAvailabilityReflection();
				for (AnalysisArtifact solution : resolution.getSolutions()) {
					List<PartReflection> reflectedParts = partAvailabilityReflection.getAvailablePartsOf( solution.getOrigin());
					
					if ( reflectedParts.size() > 0 ) {
						for (PartReflection pf : reflectedParts) {
							CompiledPartIdentification cpi = CompiledPartIdentification.from( solution.getOrigin(), pf);
							parts.add( cpi);
						}
					}
				}
							
			return parts;
		}
	}
	
	/**
	 * actually downloads all parts 
	 * @param availabilityTargetAsString
	 * @param filter - a {@link Predicate} to filter out unwanted files
	 * @return - a {@link Pair} with two lists, first successful downloads, second unsuccessfull downloads 
	 */
	protected Pair<List<CompiledPartIdentification>, List<CompiledPartIdentification>> downloadParts(String availabilityTargetAsString, Predicate<PartIdentification> filter) {
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))				
					.build();
			) {
				
				CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parse(availabilityTargetAsString);
				TransitiveDependencyResolver resolver = resolverContext.contract().transitiveDependencyResolver();
				
				CompiledTerminal ct = CompiledTerminal.from(cdi);				
				AnalysisArtifactResolution resolution = resolver.resolve(lenientContext, ct);
				
				if (resolution.hasFailed()) {
					Assert.fail("resolution for [" + availabilityTargetAsString + "] has unexpectedly failed : " + resolution.getFailure().asFormattedText());
					return null;
				}					
				
				List<CompiledPartIdentification> parts = new LinkedList<>();
				
				PartAvailabilityReflection partAvailabilityReflection = resolverContext.contract().dataResolverContract().partAvailabilityReflection();
				for (AnalysisArtifact solution : resolution.getSolutions()) {
					List<PartReflection> reflectedParts = partAvailabilityReflection.getAvailablePartsOf( solution.getOrigin());
					
					if ( reflectedParts.size() > 0 ) {
						for (PartReflection pf : reflectedParts) {
							if (filter != null) {
								if (!filter.test(pf))
									continue;
							}
							CompiledPartIdentification cpi = CompiledPartIdentification.from( solution.getOrigin(), pf);
							parts.add( cpi);
						}
					}
				}
				
				PartDownloadManager downloadManager = resolverContext.contract().dataResolverContract().partDownloadManager();				
				PartDownloadScope partDownloadScope = downloadManager.openDownloadScope();
				Map<CompiledPartIdentification, Promise<Maybe<ArtifactDataResolution>>> promises = new HashMap<>();
				for (CompiledPartIdentification cpi : parts) {
					promises.put( cpi, partDownloadScope.download(cpi, cpi));
				}
				List<CompiledPartIdentification> successfullyDownloadedParts = new ArrayList<>( promises.size());
				List<CompiledPartIdentification> unsuccessfullyDownloadedParts = new ArrayList<>( promises.size());
				
				for (Map.Entry<CompiledPartIdentification, Promise<Maybe<ArtifactDataResolution>>> entry : promises.entrySet()) {					
					Maybe<ArtifactDataResolution> mayBe = entry.getValue().get();
					CompiledPartIdentification compiledPartIdentification = entry.getKey();
					if (mayBe.isUnsatisfied()) {
						unsuccessfullyDownloadedParts.add( compiledPartIdentification);
					}
					else {
						ArtifactDataResolution adr = mayBe.get();
						if (adr.isBacked()) {
							Resource resource = adr.getResource();
							try (OutputStream out = new FileOutputStream( new File( dump, compiledPartIdentification.asFilename()))) {
								IOTools.transferBytes(resource.openStream(), out);
							}
							catch( IOException e) {
								System.err.println("transfer error on [" + compiledPartIdentification.asString());
								unsuccessfullyDownloadedParts.add( compiledPartIdentification);
							}
							successfullyDownloadedParts.add( compiledPartIdentification);
						}
					}
				}
								
			return Pair.of( successfullyDownloadedParts, unsuccessfullyDownloadedParts);
		}
	}
			
}
