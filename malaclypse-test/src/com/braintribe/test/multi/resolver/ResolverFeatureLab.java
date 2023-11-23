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
package com.braintribe.test.multi.resolver;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

public class ResolverFeatureLab extends AbstractResolverLab {
	private static final String GRP_ID = "com.braintribe.devrock";
	private static final String ART_ID = "malaclypse";
	private static final String TERMINAL = GRP_ID + ":" + ART_ID + "#[1.0,1.1)";
	
	
	@BeforeClass
	public static void before() {
		before( new File( contents, "settings.xml"));
	}

	@Test
	public void testTopLevel() {
		String [] expected = new String [] {
			GRP_ID + ":" + ART_ID + "#1.0.45",
		};
		runTest( TERMINAL, expected, true);
	}
	

	@Test
	public void testMatching() {
		String [] expected = new String [] {
				GRP_ID + ":" + ART_ID + "#1.0.38",
				GRP_ID + ":" + ART_ID + "#1.0.39-pc",
				GRP_ID + ":" + ART_ID + "#1.0.40",
				GRP_ID + ":" + ART_ID + "#1.0.45-pc",
				GRP_ID + ":" + ART_ID + "#1.0.45",
			};
			runTest( TERMINAL, expected, false);
	}
	


}
