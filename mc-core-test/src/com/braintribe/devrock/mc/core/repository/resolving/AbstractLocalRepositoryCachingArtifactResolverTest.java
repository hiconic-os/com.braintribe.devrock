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
package com.braintribe.devrock.mc.core.repository.resolving;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.commons.VersionInfo;
import com.braintribe.devrock.mc.api.repository.local.ArtifactPartResolverPersistenceDelegate;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.core.commons.ManagedFilesystemLockSupplier;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.filters.AllMatchingArtifactFilterExpert;
import com.braintribe.devrock.mc.core.repository.local.BasicArtifactPartResolverPersistenceDelegate;
import com.braintribe.devrock.mc.core.resolver.BasicVersionInfo;
import com.braintribe.devrock.mc.core.resolver.LocalRepositoryCachingArtifactResolver;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.testing.category.KnownIssue;


@Category(KnownIssue.class)
public abstract class AbstractLocalRepositoryCachingArtifactResolverTest implements HasCommonFilesystemNode{	
	//protected static final File res = new File( "res/repository");

	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("repository/" + getRoot());
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	protected List<String> standardParts = Arrays.asList( ":pom", ":jar", "sources:jar", "!javadoc:jar");
	
	protected abstract String getRoot();

	/**
	 * @param delegates - the delegates to add to the {@link LocalRepositoryCachingArtifactResolver}
	 * @return - a fully wired {@link LocalRepositoryCachingArtifactResolver}
	 */
	protected LocalRepositoryCachingArtifactResolver setup(List<ArtifactPartResolverPersistenceDelegate> delegates) {
		LocalRepositoryCachingArtifactResolver artifactResolver = new LocalRepositoryCachingArtifactResolver();
		
		artifactResolver.setLocalRepository( repo);
		artifactResolver.setLockProvider( new ManagedFilesystemLockSupplier());
		
		ArrayList<ArtifactPartResolverPersistenceDelegate> toAdd = new ArrayList<>( delegates);
		BasicArtifactPartResolverPersistenceDelegate localDelegate = BasicArtifactPartResolverPersistenceDelegate.createLocal();
		localDelegate.setArtifactFilter( AllMatchingArtifactFilterExpert.instance);
		toAdd.add( localDelegate);
		
		artifactResolver.setDelegates( toAdd);
		
		artifactResolver.postConstruct();
		
		return artifactResolver;		
	}
	
	private boolean matches( List<String> expected, List<String> found) {
		if (expected.size() != found.size())
			return false;
		for (String eri : expected) {
			if (!found.contains(eri))
				return false;
		}
		return true;
	}
	private boolean matches( VersionInfo expected, VersionInfo found) {
		if (expected.version().compareTo( found.version()) != 0) {
			return false;
		}
		return matches( expected.repositoryIds(), found.repositoryIds());				
	}
	
	private boolean matches( List<VersionInfo> expected, VersionInfo found) {	
		for (VersionInfo ei : expected) {
			if (matches( ei, found)) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * runs a test against the {@link LocalRepositoryCachingArtifactResolver} to retrieve versions (and repository origins)
	 * @param resolver - the {@link LocalRepositoryCachingArtifactResolver}
	 * @param cai - the {@link CompiledArtifactIdentification}
	 * @param expected - the expectation as {@link List} of {@link VersionInfo}
	 */
	protected void testVersionInfoResolving( LocalRepositoryCachingArtifactResolver resolver, ArtifactIdentification cai, List<VersionInfo> expected) {
		List<VersionInfo> resolvedVersionInfos = resolver.getVersions( cai);
		Assert.assertTrue("expected to find [" + expected.size() + "] version, found [" + resolvedVersionInfos.size() + "]" , resolvedVersionInfos.size() == expected.size());

		List<String> matching = new ArrayList<>();
		List<String> notMatching = new ArrayList<>();
		
		
		for (VersionInfo rVi : resolvedVersionInfos) {
		
			boolean match = matches( expected, rVi);
				
			if (match) {
				matching.add( ((BasicVersionInfo) rVi).asString());
			}
			else {
				notMatching.add( ((BasicVersionInfo)rVi).asString());
			}
		}
		List<String> expectedAsStrings = convert( expected);
		
		List<String> missing = new ArrayList<>( expectedAsStrings);		
		missing.removeAll( matching);
		
		List<String> excess = new ArrayList<>( convert( resolvedVersionInfos));		
		excess.removeAll( expectedAsStrings);
		
		Assert.assertTrue("expected [" + concat( expected) + "], found missing [" + concatStr( missing) + "]", missing.size() == 0);
		Assert.assertTrue("expected [" + concat( expected) + "], found excess [" + concatStr( excess) + "]", excess.size() == 0);
			
	}
	
	/**
	 * @param vis - the {@link List} of {@link VersionInfo} to turn into a {@link List} of {@link String}
	 * @return - a {@link List} of converted {@link String}
	 */
	private List<String> convert( List<VersionInfo> vis) {
		return vis.stream().map( v -> ((BasicVersionInfo)v).asString()).collect(Collectors.toList());
	}
	/**
	 * @param items - a {@link List} of {@link String} to concat
	 * @return - a single {@link String}
	 */
	private String concatStr(List<String> items) {
		return items.stream().collect(Collectors.joining(","));
	}
	
	/**
	 * @param expected - the {@link List} of {@link VersionInfo} to convert to a single {@link String}
	 * @return
	 */
	private String concat(List<VersionInfo> expected) {
		return concatStr( convert( expected));		
	}
	
	/**
	 * runs a part resolving and asserts the result 
	 * @param resolver - the {@link LocalRepositoryCachingArtifactResolver}
	 * @param cai - {@link CompiledArtifactIdentification}
	 * @param piStrings - a {@link List} of (enhanced) 
	 */
	protected void testPartResolving(LocalRepositoryCachingArtifactResolver resolver, CompiledArtifactIdentification cai, List<String> piStrings) {
		for (String piAsString : piStrings) {
			boolean absent = false;
			if (piAsString.startsWith( "!")) {
				piAsString = piAsString.substring(1);
				absent = true;
			}
			PartIdentification pi = PartIdentification.parse(piAsString);
			Maybe<ArtifactDataResolution> optional = resolver.resolvePart(cai, pi);
			if (absent) {
				Assert.assertTrue("expected not to find [" + pi.asString() + "] of [" + cai.asString() + "], yet found it", !optional.isSatisfied());
			}
			else {
				Assert.assertTrue("expected to find [" + pi.asString() + "] of [" + cai.asString() + "], yet did not find it", optional.isSatisfied());
			}
		}				
	}

	
	

	
	
}
