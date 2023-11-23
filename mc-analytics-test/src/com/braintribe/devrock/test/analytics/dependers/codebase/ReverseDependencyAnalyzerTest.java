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
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.analytics.dependers.DependerAnalysisNode;
import com.braintribe.devrock.mc.analytics.dependers.ReverseCodebaseDependencyAnalyzer;
import com.braintribe.devrock.mc.analytics.dependers.ReverseDependencyAnalysisPrinter;
import com.braintribe.devrock.mc.core.configuration.StandaloneRepositoryConfigurationLoader;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.test.analytics.commons.utils.HasCommonFilesystemNode;
import com.braintribe.devrock.test.analytics.commons.utils.TestUtils;
import com.braintribe.devrock.test.analytics.commons.validator.Validator;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.config.ConfigurationEvaluationError;
import com.braintribe.gm.model.reason.config.UnresolvedProperty;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

public class ReverseDependencyAnalyzerTest implements HasCommonFilesystemNode {
	
	private static final String GRP = "com.braintribe.devrock.test";
	private static YamlMarshaller marshaller;
	static {
		marshaller = new YamlMarshaller();
		marshaller.setWritePooled(true);
	}
	protected File input;
	protected File output;
	protected File repo;
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/dependers/test");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repository");
	}
	
	protected File myRoot = new File( input, "sources");
	protected File localRepository = repo;
	protected File initial = new File( input, "initial");
	
	private List<DependerAnalysisNode> expectations = new ArrayList<>();
	{
		// aaa : start
		String grp_a = GRP + ".a";
		DependerAnalysisNode startingPoint = DependerAnalysisNode.T.create();
		startingPoint.setInitialArtifactIdentification( ArtifactIdentification.create( grp_a, "aaa"));
		expectations.add(startingPoint);
		
		// aa
		DependerAnalysisNode aa = DependerAnalysisNode.T.create();
		VersionedArtifactIdentification aaVai = VersionedArtifactIdentification.create(grp_a, "aa", "1.0.1");
		aa.setVersionedArtifactIdentification( aaVai);
		CompiledDependency dependency = CompiledDependency.from( CompiledDependencyIdentification.create(grp_a, "aaa", "1.0.1"));
		dependency.setType("jar");
		aa.setReferencingDependency( dependency);
		aa.setPreviousNode(startingPoint);
		expectations.add( aa);
		
		// a 
		DependerAnalysisNode a = DependerAnalysisNode.T.create();
		VersionedArtifactIdentification aVai = VersionedArtifactIdentification.create(grp_a, "a", "1.0.1");
		a.setVersionedArtifactIdentification( aVai);
		dependency = CompiledDependency.from( aaVai);
		dependency.setType("jar");
		a.setReferencingDependency( dependency);
		a.setPreviousNode(aa);
		expectations.add( a);
		
		
		//bbb
		String grp_b = GRP + ".b";
		DependerAnalysisNode bbb = DependerAnalysisNode.T.create();
		VersionedArtifactIdentification bbbVai = VersionedArtifactIdentification.create(grp_b, "bbb", "1.0.1");
		bbb.setVersionedArtifactIdentification( bbbVai);
		dependency = CompiledDependency.from( aVai);
		dependency.setType("jar");
		bbb.setReferencingDependency( dependency);
		bbb.setPreviousNode(a);
		expectations.add( bbb);
		
		// bb
		DependerAnalysisNode bb = DependerAnalysisNode.T.create();
		VersionedArtifactIdentification bbVai = VersionedArtifactIdentification.create(grp_b, "bb", "1.0.1");
		bb.setVersionedArtifactIdentification( bbVai);
		dependency = CompiledDependency.from( bbbVai);
		dependency.setType("jar");
		bb.setReferencingDependency( dependency);
		bb.setPreviousNode(bbb);
		expectations.add(bb);
		
		// b
		DependerAnalysisNode b = DependerAnalysisNode.T.create();
		VersionedArtifactIdentification bVai = VersionedArtifactIdentification.create(grp_b, "b", "1.0.1");
		b.setVersionedArtifactIdentification( bVai);
		dependency = CompiledDependency.from( bbVai);
		dependency.setType("jar");
		b.setReferencingDependency( dependency);
		b.setPreviousNode(bb);
		expectations.add(b);
		
		// ccc
		String grp_c = GRP + ".c";
		DependerAnalysisNode ccc = DependerAnalysisNode.T.create();
		VersionedArtifactIdentification cccVai = VersionedArtifactIdentification.create(grp_c, "ccc", "1.0.1");
		ccc.setVersionedArtifactIdentification( cccVai);
		dependency = CompiledDependency.from(bVai);
		dependency.setType("jar");
		ccc.setReferencingDependency( dependency);
		ccc.setPreviousNode(b);
		expectations.add( ccc);
		
		// cc
		DependerAnalysisNode cc = DependerAnalysisNode.T.create();
		VersionedArtifactIdentification ccVai = VersionedArtifactIdentification.create(grp_c, "cc", "1.0.1");
		cc.setVersionedArtifactIdentification( ccVai);
		dependency = CompiledDependency.from( cccVai);
		dependency.setType("jar");
		cc.setReferencingDependency( dependency);
		cc.setPreviousNode(ccc);
		expectations.add( cc);
		
		// c
		DependerAnalysisNode c = DependerAnalysisNode.T.create();
		VersionedArtifactIdentification cVai = VersionedArtifactIdentification.create(grp_c, "c", "1.0.1");
		c.setVersionedArtifactIdentification( cVai);
		dependency = CompiledDependency.from( ccVai);
		dependency.setType("jar");
		c.setReferencingDependency( dependency);
		c.setPreviousNode(c);
		expectations.add( c);
				
	}
	
	
	
	@Before
	public void before() {
		TestUtils.ensure(output);
		TestUtils.copy(initial, myRoot);
	}
	
	private OverridingEnvironment buildOverridingEnvironment() {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		ove.setEnv("repo", repo.getAbsolutePath());
		ove.setEnv("sources", myRoot.getAbsolutePath());
		return ove;
	}
	
	/**
	 * compile the repository configuration and pass it
	 */
	@Test
	public void runDependerAnalysisTest() {
		
		File cfgFile = new File( input, "repository-configuration.yaml");					
		StandaloneRepositoryConfigurationLoader loader = new StandaloneRepositoryConfigurationLoader();
		loader.setVirtualEnvironment(buildOverridingEnvironment());				
		Maybe<RepositoryConfiguration> maybe = loader.loadRepositoryConfiguration(cfgFile);
		
		if (!maybe.hasValue()) {
			Assert.fail("can't compile [" + cfgFile.getAbsolutePath() + "] as " + maybe.whyUnsatisfied().stringify());
			return;
		}
		else if (maybe.isIncomplete()) {
			// validate.. 			
			if (maybe.isUnsatisfiedBy(ConfigurationEvaluationError.T)) {
				Reason invalidationReason = maybe.whyUnsatisfied();
				boolean unexpectedReasons = false;
				for (Reason reason : invalidationReason.getReasons()) {
					if (reason instanceof UnresolvedProperty) {
						UnresolvedProperty unresolvedProperty = (UnresolvedProperty) reason;
						String propertyName = unresolvedProperty.getPropertyName();
						if (!propertyName.equals("artifactId")) {
							unexpectedReasons = true;
						}
					}
					else {
						unexpectedReasons = true;
					}
				}
				if (unexpectedReasons) {
					Assert.fail("Unexpected variable resolving issues :" + maybe.whyUnsatisfied().stringify());
					return;
				}
			}
			else {
				Assert.fail("Unexpected repository compilation issues" + maybe.whyUnsatisfied().stringify());
				return;
			}						
		}
		
		
		ReverseCodebaseDependencyAnalyzer rda = new ReverseCodebaseDependencyAnalyzer();
		rda.setRepositoryConfiguration( maybe.value());			
		rda.setVerbose(false);
		
		String terminal = "com.braintribe.devrock.test.a:aaa";
		ArtifactIdentification artifactIdentification = ArtifactIdentification.parse( terminal);
		Maybe<List<DependerAnalysisNode>> nodesMaybe = rda.resolve( artifactIdentification);
		
		// validate
		if (nodesMaybe.isUnsatisfied()) {
			Assert.fail("processing failed : " + nodesMaybe.whyUnsatisfied().stringify());
			return;
		}
		
		
		if (nodesMaybe.isSatisfied()) {
			List<DependerAnalysisNode> nodes = nodesMaybe.get();			
			System.out.println("found " + (nodes.size()-1) + " chained referencers of:" + artifactIdentification.asString());
			AnalysisArtifactResolution analysisArtifactResolution = rda.transpose( nodes);
						
			ReverseDependencyAnalysisPrinter.output( nodes.get(0));
			File outputFile = new File( output, terminal.replace(':', '.') + ".dependers.yaml"); 
			try (OutputStream out = new FileOutputStream( outputFile)) {
				marshaller.marshall(out, analysisArtifactResolution);
			}
			catch (Exception e) {
				e.printStackTrace();
				Assert.fail("resolution cannot be dumped:" + e.getMessage());
			}
			
			Validator validator = new Validator();			
			validator.validateReverseDependencies(nodes, expectations);			
			validator.assertResults();
		}		
	}
}
