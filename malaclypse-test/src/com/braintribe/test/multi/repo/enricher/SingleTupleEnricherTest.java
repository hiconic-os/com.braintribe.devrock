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
package com.braintribe.test.multi.repo.enricher;

import java.io.File;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricher;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.SolutionListPresence;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;

public class SingleTupleEnricherTest extends AbstractEnricherRepoLab {
	protected static File settings = new File( "res/enricherLab/contents/settings.listing-lenient.trustworthy.xml");
	protected static String group = "com.braintribe.devrock.test.lenient";
	protected static String artifact="a";
	protected static String version = "1.0";	
	
	@BeforeClass
	public static void before() {
		before( settings);		
	}

	@Test
	public void test() {
		MultiRepositorySolutionEnricher enricher = enricherFactory.get();
		Solution solution = NameParser.parseCondensedSolutionName(group + ":"+ artifact + "#" + version);
		PartTuple tuple = PartTuple.T.create();
		tuple.setType( "jar");
		Part part = enricher.enrich( UUID.randomUUID().toString(), solution, tuple);
		System.out.println(part);
	}

	@Override
	protected void validateStatus(String name, SolutionListPresence presence) {		
	}

	
}
