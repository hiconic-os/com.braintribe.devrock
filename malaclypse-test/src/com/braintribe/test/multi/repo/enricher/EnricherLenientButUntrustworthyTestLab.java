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
import java.util.Collection;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.SolutionListPresence;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.WalkKind;


/**
 *
 * 
 * 
 *
 * @author pit
 *
 */
public class EnricherLenientButUntrustworthyTestLab extends AbstractEnricherRepoLab {
		
	
	protected static File settings = new File( "res/enricherLab/contents/settings.listing-lenient.untrustworthy.xml");
	protected static String group = "com.braintribe.devrock.test.lenient";
	protected static String version = "1.0";
	protected static String [] knownMissing = new String [] { "javadoc.zip", ".jdar", "javadoc.jar", "classes.jar"};	
	protected static String [] knownPresent = new String [] { "sources.jar", "javadoc.jar", "javadoc.zip", ".jdar", ".jar", ".pom", ".md5", ".sha1", ".xml"};
	
	@BeforeClass
	public static void before() {
		before( settings);
	}


	@Override
	protected void testPresence(Collection<Solution> solutions, File repository) {
		super.testPresence(solutions, repository);
	}	
	
	@Test
	public void testLenientAndTrustworthy() {
		String[] expectedNames = new String [] {						
				group + ":" + "a" + "#" + version, // no packaging -> jar
				group + ":" + "b" + "#" + version, // jar packaging
		};
		
		// first run 
		runTest( null, group + ":" + "lenient-test-terminal-one" + "#" + version, expectedNames, ScopeKind.compile, WalkKind.classpath, false);
		
		// second run 
		runTest( null, group + ":" + "lenient-test-terminal-one" + "#" + version, expectedNames, ScopeKind.compile, WalkKind.classpath, false);
		
		validateIndices("braintribe.Base", expectedNames[0], knownPresent);
		validateIndices("braintribe.Base", expectedNames[1], knownPresent);
	}
	
	protected void validateStatus( String name, SolutionListPresence presence) {
		if (presence == SolutionListPresence.missing) {
			boolean accepted = false;
			for (String s : knownMissing) {
				if (name.endsWith(s)) {			
					accepted = true;
					break;
				}
			}
			if (accepted == false) {				
				Assert.fail( "file [" + name + "] has status [" + presence + "]");
			}
		}	
	}
		
}