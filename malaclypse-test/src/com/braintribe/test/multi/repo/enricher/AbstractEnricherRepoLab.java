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
package com.braintribe.test.multi.repo.enricher;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.Assert;

import com.braintribe.artifacts.test.maven.framework.FakeLocalRepositoryProvider;
import com.braintribe.artifacts.test.maven.framework.FakeMavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.SolutionListPresence;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.SolutionPartReflectionContainerPersistenceExpert;
import com.braintribe.build.artifact.test.repolet.LauncherShell;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.build.artifact.test.repolet.Repolet;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDomain;
import com.braintribe.model.malaclypse.cfg.denotations.WalkKind;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ClashResolverDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.clash.OptimisticClashResolverDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.DependencyScope;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.test.multi.AbstractWalkLab;
import com.braintribe.test.multi.WalkDenotationTypeExpert;
import com.braintribe.utils.paths.PathList;

public abstract class AbstractEnricherRepoLab extends AbstractWalkLab {
	private static LauncherShell launcherShell;
	private static File contents = new File( "res/enricherLab/contents");
	protected static File localRepository = new File (contents, "repo");
	private static final File [] data = new File[] { 
			new File( contents, "archive.zip"),
	};

	protected static Map<String, Repolet> launchedRepolets;	
	

	protected static void before( File settings) {
		settingsPersistenceExpert = new FakeMavenSettingsPersistenceExpertImpl( settings);
		localRepositoryLocationProvider = new FakeLocalRepositoryProvider( localRepository);
		localRepository.mkdirs();
		int port = runBefore();
		
		// clean local repository
		TestUtil.delete(localRepository);
			
		// fire them up 
		launchRepolets( port);
	}

	private static void launchRepolets( int port) {
		Map<String, RepoType> map  = new HashMap<String, LauncherShell.RepoType>();
		map.put( "archive," + data[0].getAbsolutePath(), RepoType.singleZip);
		launcherShell = new LauncherShell( port);
		launchedRepolets = launcherShell.launch( map);
	}
	
	@AfterClass
	public static void after() {
		runAfter();
		launcherShell.shutdown();
	}
	
	protected abstract void validateStatus( String name, SolutionListPresence presence);
	
	protected void validateIndices( String repositoryId, String condensedName, String ... extensions) {
		Map<String, SolutionListPresence> map1 = extractIndexInformationForSolution( repositoryId, NameParser.parseCondensedSolutionName( condensedName));
		
		Set<String> foundTags = new HashSet<>();
		List<String> unexpectedTags = new ArrayList<>();
		
		
		// expected : no missings 
		for (Entry<String, SolutionListPresence> entry : map1.entrySet()) {
			validateStatus( entry.getKey(), entry.getValue());
			if (extensions != null) {
				boolean found = false;
				for (String extension : extensions) {
					if (entry.getKey().endsWith(extension)) {
						foundTags.add( extension);
						found = true;
						break;
					}
				}
				if (!found)
					unexpectedTags.add( entry.getKey());
			}			
		}	
		if (extensions != null) {
			if (foundTags.size() != extensions.length) {
				Assert.fail("no all expected files were found, found [" + toString(foundTags) + "], expected [" + toString( Arrays.asList( extensions)) + "]");
			}
			if (unexpectedTags.size() > 0) {
				Assert.fail("unexpected files were found : [" + toString(unexpectedTags));
			}
		}
	}
	
	
	private String toString( Collection<String> strings) {
		return strings.stream().collect(Collectors.joining(","));
	}
	
	public void runTest(String rule, String terminalAsString, String [] expectedNames, ScopeKind scopeKind, WalkKind walkKind, boolean skipOptional, DependencyScope ... additionalScopes) {
		try {
			Solution terminal = NameParser.parseCondensedSolutionName(terminalAsString);
			
			ClashResolverDenotationType clashResolverDt = OptimisticClashResolverDenotationType.T.create();
			WalkDenotationType walkDenotationType = WalkDenotationTypeExpert.buildCompileWalkDenotationType(  clashResolverDt, scopeKind, WalkDomain.repository, walkKind, skipOptional, additionalScopes);
			walkDenotationType.setTypeFilter( rule);
			
			Collection<Solution> result = test( "runTypeTest", terminal, walkDenotationType, expectedNames,1,0);
			testPresence(result, localRepository);
			testUpdates(result, localRepository);
			
		
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail( "exception [" + e + "] thrown");
		}
	}
	
	protected Map<String, SolutionListPresence> extractIndexInformationForSolution( String repositoryId, Solution solution) {

		Map<String, SolutionListPresence> indexInfo = new HashMap<>();
		String source = PathList.create().push( localRepository.getAbsolutePath()).push( solution.getGroupId().replace('.', '/')).push(solution.getArtifactId()).push( VersionProcessor.toString( solution.getVersion())).toSlashPath();
		List<String> indices = SolutionPartReflectionContainerPersistenceExpert.decode( new File(source), repositoryId);
		for (String index : indices) {
			if (index.startsWith( "!:")) {
				indexInfo.put(index, SolutionListPresence.missing);
			}
			else {
				indexInfo.put(index, SolutionListPresence.present);
			}
		}
		return indexInfo;		
	}

}