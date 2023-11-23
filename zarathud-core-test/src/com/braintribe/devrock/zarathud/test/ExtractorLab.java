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
package com.braintribe.devrock.zarathud.test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.walk.multi.Walker;
import com.braintribe.build.artifacts.mc.wire.classwalk.context.WalkerContext;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.devrock.zarathud.extracter.ZarathudExtractor;
import com.braintribe.devrock.zarathud.output.ArtifactWriter;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;
import com.braintribe.model.zarathud.data.AbstractEntity;
import com.braintribe.model.zarathud.data.Artifact;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.wire.api.context.WireContext;

@Category(KnownIssue.class)
public class ExtractorLab extends AbstractExtractorLab {
	
	ArtifactWriter dumper = new ArtifactWriter();

	protected void test(Solution solution, Predicate<AbstractEntity> filter) {
		String walkScopeId = UUID.randomUUID().toString();

		WireContext<ClasspathResolverContract> wireContext = getClasspathWalkContext(null, null, ResolvingInstant.adhoc);
	
		WalkerContext context = new WalkerContext();
		context.setAbortOnUnresolvedDependency( true);
		
		Walker walker = wireContext.contract().walker(context);
		Collection<Solution> result = walker.walk( walkScopeId, solution);
		
		String name = NameParser.buildName(solution);
		if (result == null || result.size() == 0) {
			System.out.println("no dependencies found for [" + name + "]");			
		}
		
		// we do jars, no classes folder, so the jar of the terminal must go into the solution list
		Part jarPart = wireContext.contract().enricher().enrich(walkScopeId, solution, PartTupleProcessor.createJarPartTuple());
		if (jarPart == null) {
			Assert.fail("cannot find jar for [" + name + "]");
			return;
		}		
		solution.getParts().add(jarPart);
		
		
		// we do need the pom to get the declared dependencies
		//Part pomPart = wireContext.contract().enricher().enrich(walkScopeId, solution, PartTupleProcessor.createPomPartTuple());
		//wireContext.contract().pomReader().readPom(walkScopeId,  pomPart.getLocation());
		
		List<Dependency> dependencies = solution.getDependencies();
				
		ZarathudExtractor extractor = new ZarathudExtractor();
		
		result.add(solution);
		extractor.setClasspath(result);
		extractor.setDeclared(dependencies);
	
		Artifact terminalArtifact = toArtifact( solution);
		Artifact analysedArtifact = extractor.analyseJar( terminalArtifact);
		
		
	
		try {
			StringWriter writer = new StringWriter();
			dumper.dump(writer, analysedArtifact, filter);
			System.out.println( writer.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	@Test
	public void testRootModel() {
		test( NameParser.parseCondensedSolutionName( "com.braintribe.gm:root-model#1.0.10"), null);
	}
	

	@Test
	public void testTimeModel() {
		test( NameParser.parseCondensedSolutionName( "com.braintribe.gm:time-model#1.0.11"), null);
	}
	
	@Test
	public void testZOne() {
		//Predicate<AbstractEntity> filter = (s) -> true;
		test( NameParser.parseCondensedSolutionName( "com.braintribe.devrock.test.zarathud:z-one#1.0.1-pc"), null);
	}

	@Test
	public void testZTwo() {
		//Predicate<AbstractEntity> filter = (s) -> true;
		test( NameParser.parseCondensedSolutionName( "com.braintribe.devrock.test.zarathud:z-two#1.0.1-pc"), null);
	}
	
	@Test
	public void testZThree() {
		//Predicate<AbstractEntity> filter = (s) -> true;
		test( NameParser.parseCondensedSolutionName( "com.braintribe.devrock.test.zarathud:z-three#1.0.1-pc"), null);
	}
	@Test
	public void testZFour() {
		//Predicate<AbstractEntity> filter = (s) -> true;
		test( NameParser.parseCondensedSolutionName( "com.braintribe.devrock.test.zarathud:z-four#1.0.1-pc"), null);
	}
	
	@Test
	public void testZFive() {
		//Predicate<AbstractEntity> filter = (s) -> true;
		test( NameParser.parseCondensedSolutionName( "com.braintribe.devrock.test.zarathud:z-five#1.0.1-pc"), null);
	}
	
	@Test
	public void testMc() {
		Predicate<AbstractEntity> filter = (s) -> true;
		test( NameParser.parseCondensedSolutionName( "com.braintribe.devrock:malaclypse#1.0.55"), filter);
	}

	 
	
	@Test
	public void testModel() {
		//Predicate<AbstractEntity> filter = (s) -> true;
		test( NameParser.parseCondensedSolutionName( "com.braintribe.devrock:repository-configuration-model#1.0.14-pc"), null);
	}
	
	

}
