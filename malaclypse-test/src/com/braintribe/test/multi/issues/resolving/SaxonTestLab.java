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
package com.braintribe.test.multi.issues.resolving;

import java.io.File;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.WalkKind;
import com.braintribe.testing.category.KnownIssue;


/**
 
 * 
 * @author pit
 *
 */
@Category(KnownIssue.class)
public class SaxonTestLab extends AbstractResolvingIssueLab {
		
	private static String userHome = System.getProperty( "user.home");
	protected static File settings = new File( userHome + "/.m2/settings.xml");
	@BeforeClass
	public static void before() {
		before( settings);
	}


	@Override
	protected void testPresence(Collection<Solution> solutions, File repository) {
		super.testPresence(solutions, repository);
	}	
	
	@Test
	public void testSaxon() {
		String[] expectedNames = new String [] {					
				
		};
		
		Collection<Solution> result = runTest( "net.sf.saxon:Saxon-HE#9.8.0-8", null, ScopeKind.compile, WalkKind.classpath, false);
		
		result.stream().forEach( s -> System.out.println( NameParser.buildName(s)));
	}
	
	
	

	
}
