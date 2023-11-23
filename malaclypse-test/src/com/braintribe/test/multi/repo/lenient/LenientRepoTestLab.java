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
package com.braintribe.test.multi.repo.lenient;

import java.io.File;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

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
public class LenientRepoTestLab extends AbstractLenientRepoLab {
		
	
	protected static File settings = new File( "res/lenientRepo/contents/settings.xml");
	protected static String group = "com.braintribe.devrock.test.lenient";
	protected static String version = "1.0";
	
	@BeforeClass
	public static void before() {
		before( settings);
	}


	@Override
	protected void testPresence(Collection<Solution> solutions, File repository) {
		super.testPresence(solutions, repository);
	}	
	
	
	public void testNonLenientYetTrustworthy() {
		String[] expectedNames = new String [] {						
				group + ":" + "a" + "#" + version, // no packaging -> jar
				group + ":" + "b" + "#" + version, // jar packaging
		};
		
		// first run 
		runTest( null, group + ":" + "lenient-test-terminal-one" + "#" + version, expectedNames, ScopeKind.compile, WalkKind.classpath, false);
		
		// second run 
		runTest( null, group + ":" + "lenient-test-terminal-one" + "#" + version, expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	}
	
		
}
