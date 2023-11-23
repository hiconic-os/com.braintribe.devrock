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
package com.braintribe.devrock.test.analytics.dependers.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.analytics.dependers.DependerAnalysisNode;
import com.braintribe.devrock.mc.analytics.dependers.ReverseLocalCacheDependencyAnalyzer;
import com.braintribe.devrock.test.analytics.commons.utils.HasCommonFilesystemNode;
import com.braintribe.devrock.test.analytics.commons.utils.TestUtils;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

@Category( KnownIssue.class)
public class ReverseDependencyAnalyzerOnRealRepoLab implements HasCommonFilesystemNode {
	
	protected static YamlMarshaller marshaller;
	static {
		marshaller = new YamlMarshaller();
		marshaller.setWritePooled(true);
	}
	protected File input;
	protected File output;
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/dependers/cache/lab");
		input = pair.first;
		output = pair.second;			
	}
	
	@Before
	public void before() {
		TestUtils.ensure(output);
	}
	
	private OverridingEnvironment buildOverridingEnvironment() {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		ove.setEnv("repo", "f:/repository");		
		return ove;
	}
		
	/**
	 * pass a file pointing to the repository configuration
	 */
	@Test
	public void runWithYamlParametrizationOnRealRepo() {
		
		ReverseLocalCacheDependencyAnalyzer rda = new ReverseLocalCacheDependencyAnalyzer();		
		File cfgFile = new File( input, "repository-configuration.yaml");				
		rda.setRepositoryConfigurationFile(cfgFile);
		File indexFile = new File( input, "index.yaml");
		rda.setIndexFile(indexFile);
		
		rda.setVirtualEnvironment( buildOverridingEnvironment());		
		rda.setVerbose(true);
		
		//String terminal = "org.abego.treelayout:org.abego.treelayout.core#1.0.1";
		String terminal = "org.abego.treelayout:org.abego.treelayout.core#1.0.1";
		VersionedArtifactIdentification artifactIdentification = VersionedArtifactIdentification.parse( terminal);
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
