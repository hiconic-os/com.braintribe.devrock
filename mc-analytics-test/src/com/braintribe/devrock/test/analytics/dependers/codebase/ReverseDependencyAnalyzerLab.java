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
package com.braintribe.devrock.test.analytics.dependers.codebase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.analytics.dependers.DependerAnalysisNode;
import com.braintribe.devrock.mc.analytics.dependers.ReverseCodebaseDependencyAnalyzer;
import com.braintribe.devrock.mc.analytics.dependers.TransitiveResolverBasedReverseDependencyAnalyzer;
import com.braintribe.devrock.mc.core.configuration.StandaloneRepositoryConfigurationLoader;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.test.analytics.commons.utils.HasCommonFilesystemNode;
import com.braintribe.devrock.test.analytics.commons.utils.TestUtils;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

@Category( KnownIssue.class)
public class ReverseDependencyAnalyzerLab implements HasCommonFilesystemNode {
	
	protected static YamlMarshaller marshaller;
	static {
		marshaller = new YamlMarshaller();
		marshaller.setWritePooled(true);
	}
	protected File input;
	protected File output;
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/dependers/lab");
		input = pair.first;
		output = pair.second;			
	}
	
	protected File myRoot = new File( "f:/works/dev-envs/standard/git");
	protected File localRepository = new File("f:/repository");
	
	
	private Map<File, String> buildCodebases() {
		Map<File,String> codebases = new HashMap<>();
		codebases.put( new File( myRoot, "com.braintribe.devrock"), "${artifactId}");
		codebases.put( new File( myRoot, "com.braintribe.devrock.ant"), "${artifactId}");
		codebases.put( new File( myRoot, "com.braintribe.devrock.eclipse"), "${artifactId}");
		codebases.put( new File( myRoot, "tribefire.extension.artifact"), "${artifactId}");
		codebases.put( new File( myRoot, "tribefire.extension.setup"), "${artifactId}");
		codebases.put( new File( myRoot, "com.braintribe.gm"), "${artifactId}");
		codebases.put( new File( myRoot, "com.braintribe.common"), "${artifactId}");
		return codebases;
	}
	
	@Before
	public void before() {
		TestUtils.ensure(output);
	}

	//@Test
	public void tdrBasedTest() {
		TransitiveResolverBasedReverseDependencyAnalyzer rda = new TransitiveResolverBasedReverseDependencyAnalyzer();
		rda.setLocalRepository(localRepository);
		Map<File, String> codebases = buildCodebases();
		rda.setCodeBases(codebases);
		rda.setVerbose(true);
		
		String terminal = "com.braintribe.devrock:mc-core";
		Maybe<AnalysisArtifactResolution> maybe = rda.resolve(terminal);
		if (maybe.hasValue()) {
			File outputFile = new File( output, terminal.replace(':', '.') + ".dependers.yaml"); 
			try (OutputStream out = new FileOutputStream( outputFile)) {
				marshaller.marshall(out, maybe.value());
			}
			catch (Exception e) {
				e.printStackTrace();
				Assert.fail("resolution cannot be dumped:" + e.getMessage());
			}			
		}
		if (maybe.isUnsatisfied()) {
			Assert.fail("resolution failed : " + maybe.whyUnsatisfied().stringify());
		}
		
	}
	/**
	 * pass a {@link File} pointing to the local repo, and a {@link Map} with the codebase data
	 */
	//@Test
	public void runWithSpecificParametrization() {
		ReverseCodebaseDependencyAnalyzer rda = new ReverseCodebaseDependencyAnalyzer();
		rda.setLocalRepository(new File("f:/repository"));
		Map<File, String> codebases = buildCodebases();
		rda.setCodeBases(codebases);
		rda.setVerbose(true);
		
		String terminal = "com.braintribe.devrock:malaclypse";
		ArtifactIdentification artifactIdentification = ArtifactIdentification.parse( terminal);
		Maybe<List<DependerAnalysisNode>> nodesMaybe = rda.resolve( artifactIdentification);
		if (nodesMaybe.isSatisfied()) {
			List<DependerAnalysisNode> nodes = nodesMaybe.get();
			System.out.println("found " + nodes.size() + " chained referencers of:" + artifactIdentification.asString());
			AnalysisArtifactResolution resolution = rda.transpose(nodes);			
			File outputFile = new File( output, terminal.replace(':', '.') + ".dependers.yaml"); 
			try (OutputStream out = new FileOutputStream( outputFile)) {
				marshaller.marshall(out, resolution);
			}
			catch (Exception e) {
				e.printStackTrace();
				Assert.fail("resolution cannot be dumped:" + e.getMessage());
			}						
		}
		
	}

	/**
	 * pass a file pointing to the repository configuration
	 */
	@Test
	public void runWithYamlParametrization() {
		ReverseCodebaseDependencyAnalyzer rda = new ReverseCodebaseDependencyAnalyzer();		
		File cfgFile = new File( input, "repository-configuration.yaml");
		rda.setRepositoryConfigurationFile(cfgFile);
		rda.setVirtualEnvironment(buildOverridingEnvironment());		
		rda.setVerbose(true);
		
		String terminal = "com.braintribe.devrock:malaclypse";
		ArtifactIdentification artifactIdentification = ArtifactIdentification.parse( terminal);
		Maybe<List<DependerAnalysisNode>> nodesMaybe = rda.resolve( artifactIdentification);
		if (nodesMaybe.isSatisfied()) {
			List<DependerAnalysisNode> nodes = nodesMaybe.get();
			System.out.println("found " + nodes.size() + " chained referencers of:" + artifactIdentification.asString());
			AnalysisArtifactResolution resolution = rda.transpose(nodes);			
			File outputFile = new File( output, terminal.replace(':', '.') + ".dependers.yaml"); 
			try (OutputStream out = new FileOutputStream( outputFile)) {
				marshaller.marshall(out, resolution);
			}
			catch (Exception e) {
				e.printStackTrace();
				Assert.fail("resolution cannot be dumped:" + e.getMessage());
			}						
		}		
	}

	private OverridingEnvironment buildOverridingEnvironment() {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		ove.setEnv("repo", "f:/repository");
		ove.setEnv("sources", "f:/works/dev-envs/standard/git");
		return ove;
	}
	
	/**
	 * compile the repository configuration and pass it
	 */
	@Test
	public void runWithPassedConfigurationParametrization() {
		
		File cfgFile = new File( input, "repository-configuration.yaml");					
		StandaloneRepositoryConfigurationLoader loader = new StandaloneRepositoryConfigurationLoader();
		loader.setVirtualEnvironment(buildOverridingEnvironment());				
		Maybe<RepositoryConfiguration> maybe = loader.loadRepositoryConfiguration(cfgFile);
		if (maybe.isUnsatisfied()) {
			Assert.fail("can't compile [" + cfgFile.getAbsolutePath() + "] as " + maybe.whyUnsatisfied().stringify());
		}
		
		ReverseCodebaseDependencyAnalyzer rda = new ReverseCodebaseDependencyAnalyzer();
		rda.setRepositoryConfiguration( maybe.get());			
		rda.setVerbose(true);
		
		String terminal = "com.braintribe.devrock:malaclypse";
		ArtifactIdentification artifactIdentification = ArtifactIdentification.parse( terminal);
		Maybe<List<DependerAnalysisNode>> nodesMaybe = rda.resolve( artifactIdentification);
		if (nodesMaybe.isSatisfied()) {
			List<DependerAnalysisNode> nodes = nodesMaybe.get();
			System.out.println("found " + nodes.size() + " chained referencers of:" + artifactIdentification.asString());
			
			AnalysisArtifactResolution resolution = rda.transpose(nodes);			
			File outputFile = new File( output, terminal.replace(':', '.') + ".dependers.yaml"); 
			try (OutputStream out = new FileOutputStream( outputFile)) {
				marshaller.marshall(out, resolution);
			}
			catch (Exception e) {
				e.printStackTrace();
				Assert.fail("resolution cannot be dumped:" + e.getMessage());
			}						
		}		
	}

	
}
