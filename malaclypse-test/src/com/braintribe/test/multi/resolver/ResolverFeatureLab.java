// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
