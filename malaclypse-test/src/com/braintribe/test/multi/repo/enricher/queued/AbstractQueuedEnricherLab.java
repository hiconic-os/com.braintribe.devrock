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
package com.braintribe.test.multi.repo.enricher.queued;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.junit.Assert;

import com.braintribe.artifacts.test.maven.framework.FakeLocalRepositoryProvider;
import com.braintribe.artifacts.test.maven.framework.FakeMavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.maven.settings.OverrideableVirtualEnvironment;
import com.braintribe.build.artifact.retrieval.multi.enriching.queued.QueueingMultiRepositorySolutionEnricher;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionHelper;
import com.braintribe.build.artifact.test.repolet.LauncherShell;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.build.artifact.test.repolet.Repolet;
import com.braintribe.build.artifacts.mc.wire.classwalk.ClasspathResolvers;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.ClasspathResolverExternalContract;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.ConfigurableClasspathResolverExternalSpace;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.Scopes;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.part.PartProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;
import com.braintribe.model.ravenhurst.Artifact;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.util.network.NetworkTools;
import com.braintribe.wire.api.context.WireContext;

public abstract class AbstractQueuedEnricherLab {

	protected static File contents = new File( "res/queuedEnricherLab/contents");
	protected static File repo = new File( contents, "repo");
	
	protected LauncherShell launcherShell;	
	protected Map<String, Repolet> launchedRepolets;	
	protected int port;
	protected OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
	
	protected void fireup(Map<String, RepoType> map) {
		TestUtil.ensure( repo);
		
		port = NetworkTools.getUnusedPortInRange(8080, 8100); 
		launcherShell = new LauncherShell( port);
		launchedRepolets = launcherShell.launch( map);
	
	}
	
	protected WireContext<ClasspathResolverContract> acquireClasspathWalkContext(File settings) {
		ConfigurableClasspathResolverExternalSpace cfg = new ConfigurableClasspathResolverExternalSpace();
		
		cfg.setScopes( Scopes.compileScopes());
		cfg.setSkipOptional(false);
		
		cfg.setResolvingInstant( ResolvingInstant.adhoc);
		
		
		ove.addEnvironmentOverride( "port", ""+ port);
		
		cfg.setVirtualEnvironment( ove);
		
		if (settings != null) {		
			FakeMavenSettingsPersistenceExpertImpl persistenceExpert = new FakeMavenSettingsPersistenceExpertImpl( settings);
			cfg.setOverrideSettingsPersistenceExpert(persistenceExpert);
		}
		
		if (repo != null) {
			FakeLocalRepositoryProvider localRepositoryProvider = new FakeLocalRepositoryProvider(repo);
			cfg.setOverrideLocalRepositoryExpert(localRepositoryProvider);
		}
		
		
		WireContext<ClasspathResolverContract> context = ClasspathResolvers.classpathResolverContext( b -> {  
			b.bindContract(ClasspathResolverExternalContract.class, cfg);	
		});
		
		return context;		
	}
	
		
	protected QueueingMultiRepositorySolutionEnricher acquireEnricher(WireContext<ClasspathResolverContract> context) {
		QueueingMultiRepositorySolutionEnricher enricher = new QueueingMultiRepositorySolutionEnricher();
		enricher.setRepositoryReflection( context.contract().repositoryReflection());		
		return enricher;
	}
	
	protected List<Solution> acquireSolutions( String ... condensedNames) {
		return Arrays.asList(condensedNames).stream().map( n -> {
			return NameParser.parseCondensedSolutionName(n);
		}).collect( Collectors.toList());				
	}
	
	protected PartTuple [] acquirePartTuples( String ... tuples) {
		PartTuple [] result = new PartTuple[ tuples.length];
		for (int i = 0; i < tuples.length;i++) {
			result[i] = PartTupleProcessor.fromString( tuples[i]);
		}
		return result;
	}
	
	protected void validate( List<Solution> solutions, List<PartTuple> tuples) {
		solutions.stream().forEach( s -> {
			Artifact rhArtifact = RepositoryReflectionHelper.ravenhurstArtifactFromSolution(s);
			String path = RepositoryReflectionHelper.getSolutionFilesystemLocation( repo.getAbsolutePath(), rhArtifact);
			tuples.stream().forEach( t -> {
				Part part = PartProcessor.createPartFromIdentification(s, s.getVersion(), t);
				String fileName = NameParser.buildFileName(part);
				File file = new File( path + "/" + fileName);
				Assert.assertTrue( "expected file [" + file.getAbsolutePath() + "] doesn't exist", file.exists());
			});
		});
	}
	
	protected void validate( Map<Solution, PartTuple []> map) {
		for (Entry<Solution, PartTuple[]> entry : map.entrySet()) {
			Solution solution = entry.getKey();
			Artifact rhArtifact = RepositoryReflectionHelper.ravenhurstArtifactFromSolution( solution);
			String path = RepositoryReflectionHelper.getSolutionFilesystemLocation( repo.getAbsolutePath(), rhArtifact);
			Arrays.asList( entry.getValue()).stream().forEach( t -> {
				Part part = PartProcessor.createPartFromIdentification(solution, solution.getVersion(), t);
				String fileName = NameParser.buildFileName(part);
				File file = new File( path + "/" + fileName);
				Assert.assertTrue( "expected file [" + file.getAbsolutePath() + "] doesn't exist", file.exists());
			});
		}
	}
	
	protected Map<Solution, PartTuple []> acquireEnrichmentInput(String ... expressions) {
		Map<Solution, PartTuple[]> result = new HashMap<>();
		Arrays.asList( expressions).stream().forEach( e -> {
			PartTuple [] tuples = null;
			String [] split = e.split( ";");
			Solution solution = NameParser.parseCondensedSolutionName(split[0]);
			if (split.length == 1) {
				tuples = new PartTuple[1];
				tuples[0] = PartTupleProcessor.fromString("pom");
			}
			else {
				String [] parts = split[1].split( ",");
				tuples = new PartTuple[ parts.length];
				for (int i = 0; i < parts.length; i++) {
					tuples[i] = PartTupleProcessor.fromString( parts[i]);
				}
			}
			result.put(solution, tuples);
		});
		return result;
	}
}
