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
