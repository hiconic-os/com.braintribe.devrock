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
package com.braintribe.test.multi.repo.enricher.queued;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.retrieval.multi.enriching.queued.QueueingMultiRepositorySolutionEnricher;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.wire.api.context.WireContext;

@Category(KnownIssue.class)
public class StandardQueuedEnricherLab extends AbstractQueuedEnricherLab {
	private static final String VRS = "1.0.1-pc";
	private static final String GRP = "com.braintribe.devrock.test.ape";
	private File settings = new File( contents, "settings.xml");
	private Map<String, RepoType> map;	
	private WireContext<ClasspathResolverContract> context;
	private QueueingMultiRepositorySolutionEnricher enricher;
	
	@Before
	public void before() {
		map = new HashMap<>();		
		map.put( "archive," + new File( contents, "archive.zip").getAbsolutePath(), RepoType.singleZip);
		fireup( map);
		
		context = acquireClasspathWalkContext(settings);
		enricher = acquireEnricher(context);
	}
	
	@Test
	public void basicTest() {
		// feed enricher
		List<Solution> acquiredSolutions = acquireSolutions( GRP + ":" + "ape-commons" + "#" + VRS);
		PartTuple[] acquiredPartTuples = acquirePartTuples( "pom");
		
		acquiredSolutions.stream().forEach( s -> {
			enricher.enrich(s, acquiredPartTuples);
		});
		
		// wait for finish
		enricher.finalizeEnrichment();
		
		// validate result
		validate( acquiredSolutions, Arrays.asList( acquiredPartTuples));
	}
	
	@Test
	public void multiTest() {
		// feed enricher
	
		PartTuple[] acquiredPartTuples = acquirePartTuples( "pom", "jar");
		List<Solution> acquiredSolutions = acquireSolutions( 
					GRP + ":" + "ape-commons" + "#" + VRS,
					GRP + ":" + "ape-terminal" + "#" + VRS
				);
		acquiredSolutions.stream().forEach( s -> {
			enricher.enrich(s, acquiredPartTuples);
		});
		
		// wait for finish
		enricher.finalizeEnrichment();
		
		// validate result
		validate( acquiredSolutions, Arrays.asList( acquiredPartTuples));
	}
	
	@Test
	public void midLoadTest() {
		// feed enricher
		Map<Solution, PartTuple[]> feed = acquireEnrichmentInput( 
				GRP + ":" + "ape-commons" + "#" + VRS +";pom,jar",
				GRP + ":" + "ape-terminal" + "#" + VRS+";pom,jar",
				GRP + ":" + "a" + "#" + VRS+";pom,jar",
				GRP + ":" + "b" + "#" + VRS+";pom,jar",
				GRP + ":" + "c" + "#" + VRS+";pom,jar",
				GRP + ":" + "parent" + "#" + VRS+";pom"
				);
		
		for (Entry<Solution, PartTuple[]> entry : feed.entrySet()) {		
			enricher.enrich(entry.getKey(), entry.getValue());
		}
		
		// wait for finish
		enricher.finalizeEnrichment();
		
		// validate result
		validate( feed);
	}

}
