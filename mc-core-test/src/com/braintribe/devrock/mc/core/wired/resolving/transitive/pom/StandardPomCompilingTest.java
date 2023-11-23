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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.pom;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.version.VersionExpression;

public class StandardPomCompilingTest extends AbstractTransitiveResolverPomCompilingTest {

	protected RepoletContent archiveInput(String repoId) {
		File file = new File( input, repoId + ".content.definition.txt");
		try {
			return RepositoryGenerations.parseConfigurationFile(file);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot load parser file [" + file.getAbsolutePath() + "]" , IllegalStateException::new);
		} 
	}
	
	
	
	@Override
	protected void runAdditionalBeforeSteps() {
		// copy initial data (mimic local repository)
		if (initial.exists()) {
			TestUtils.copy( initial, repo);
		}
	}



	/**
	 * tests an expression like ${project.parent.version}
	 */
	@Test
	public void runParentVersionRetrievalTest() {
		String grpId = "com.braintribe.devrock.test";
		String artId = "a";
		String vrs = "1.0.1-1.0.2";
		CompiledArtifact ca = runResolve(grpId + ":" + artId + "#" + vrs);
		
		Assert.assertTrue("expected group id to be [" + grpId + "] yet it is [" + ca.getGroupId() + "]", grpId.equals(ca.getGroupId()));
		Assert.assertTrue("expected artifact id to be [" + artId + "] yet it is [" + ca.getArtifactId() + "]", artId.equals(ca.getArtifactId()));
		Assert.assertTrue("expected version to be [" + vrs + "] yet it is [" + ca.getVersion().asString() + "]", vrs.equals(ca.getVersion().asString()));
				
	}
	
	/**
	 * tests auto instrumentation (finding out missing groupid and version from the parent)
	 */
	@Test
	public void runAutoInstrumentationPerParentTest() {
		String grpId = "com.braintribe.devrock.test";
		String artId = "t";
		String vrs = "1.0.2";
		CompiledArtifact ca = runResolve(grpId + ":" + artId + "#" + vrs);
		
		Assert.assertTrue("expected group id to be [" + grpId + "] yet it is [" + ca.getGroupId() + "]", grpId.equals(ca.getGroupId()));
		Assert.assertTrue("expected artifact id to be [" + artId + "] yet it is [" + ca.getArtifactId() + "]", artId.equals(ca.getArtifactId()));
		Assert.assertTrue("expected version to be [" + vrs + "] yet it is [" + ca.getVersion().asString() + "]", vrs.equals(ca.getVersion().asString()));
				
	}
	
	
	/**
	 * tests dependency management look up with ALL coordinates (groupId, artifactId, classifier, type)
	 */
	@Test
	public void runDependencyManagementTest() {
		String grpId = "com.braintribe.devrock.test";
		String artId = "t";
		String vrs = "1.0.1";
		
		List<CompiledDependency> expectations = new ArrayList<>();
		expectations.add( CompiledDependency.create( grpId, "refOne", VersionExpression.parse(vrs), null, null, "jar"));
		expectations.add( CompiledDependency.create( grpId, "refTwo", VersionExpression.parse("1.0.2"), null, "classes", "jar"));
		expectations.add( CompiledDependency.create( grpId, "refThree", VersionExpression.parse( "1.0.3"), null, "properties", "zip"));		
		
		CompiledArtifact ca = runResolve(grpId + ":" + artId + "#" + vrs);
		
		Assert.assertTrue("expected group id to be [" + grpId + "] yet it is [" + ca.getGroupId() + "]", grpId.equals(ca.getGroupId()));
		Assert.assertTrue("expected artifact id to be [" + artId + "] yet it is [" + ca.getArtifactId() + "]", artId.equals(ca.getArtifactId()));
		Assert.assertTrue("expected version to be [" + vrs + "] yet it is [" + ca.getVersion().asString() + "]", vrs.equals(ca.getVersion().asString()));
				
		
		
		// validate ..
		List<String> matching = new ArrayList<>();
		List<String> missing = new ArrayList<>();
		List<String> excess = new ArrayList<>();
		
		List<String> expected = expectations.stream().map( d -> ((CompiledDependencyIdentification)d).asString()).collect(Collectors.toList());
		List<String> founds = ca.getDependencies().stream().map( d -> ((CompiledDependencyIdentification)d).asString()).collect(Collectors.toList());
		
		for (String found : founds) {
			if (expected.contains( found)) {
				matching.add( found);
			}
			else {
				excess.add( found);
			}
		}
		
		missing.addAll( expected);
		missing.removeAll( matching);
		
		
		Assert.assertTrue("missing are [" + collate( missing) + "]", missing.size() == 0);
		Assert.assertTrue("excess are [" + collate( excess) + "]", excess.size() == 0);
	}

}
