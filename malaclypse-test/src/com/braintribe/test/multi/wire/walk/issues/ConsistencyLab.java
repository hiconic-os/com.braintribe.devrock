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
package com.braintribe.test.multi.wire.walk.issues;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.walk.multi.Walker;
import com.braintribe.build.artifacts.mc.wire.classwalk.context.WalkerContext;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;
import com.braintribe.test.multi.wire.AbstractRepoletWalkerWireTest;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.wire.api.context.WireContext;
@Category(KnownIssue.class)
public class ConsistencyLab extends AbstractRepoletWalkerWireTest {

	
	
	
	@Override
	protected File getRoot() {	
		return new File("res/wire/issues/scratch");
	}

	

	private void sequentialTest(Solution terminal, int num) {
		List<String> collectedResults = new ArrayList<>();
		WireContext<ClasspathResolverContract> classpathWalkContext = getClasspathWalkContext( null, null, ResolvingInstant.posthoc);			
	
		try {
			WalkerContext walkerContext = new WalkerContext();
			walkerContext.setSkipOptionals(true);
			
			for (int i = 0; i < num; i++) {
			
				Walker walker = classpathWalkContext.contract().walker( walkerContext);					
				String walkScopeId = UUID.randomUUID().toString();
					
				Collection<Solution> collection = walker.walk( walkScopeId, terminal);
				String result = collection.stream().map( s -> NameParser.buildName(s)).sorted().collect( Collectors.joining("\n"));
				collectedResults.add(result);
			}
			
			for (String suspect : collectedResults) {
				for (String m : collectedResults) {
					if (m == suspect)
						continue;
					
					boolean condition = m.equalsIgnoreCase(suspect);
					Assert.assertTrue("differning results found", condition);
					if (!condition) {
						dumpComparison(m, suspect);
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown");
		} 
	
	}
	
	private void parallelTest(Solution terminal, int num) {
		List<String> collectedResults = new ArrayList<>();
		WireContext<ClasspathResolverContract> classpathWalkContext = getClasspathWalkContext( null, null, ResolvingInstant.posthoc);			
	
		
		ExecutorService es = Executors.newFixedThreadPool(5); 
		try {
			for (int i = 0; i < num; i++) {
							
				Runnable runnable =  new Runnable() {
				
						@Override
						public void run() {			
							System.out.println("<<!!");
							
							WalkerContext walkerContext = new WalkerContext();
							walkerContext.setSkipOptionals(true);
							Walker walker = classpathWalkContext.contract().walker( walkerContext);					
							String walkScopeId = UUID.randomUUID().toString();
								
							Collection<Solution> collection = walker.walk( walkScopeId, terminal);
							String result = collection.stream().map( s -> NameParser.buildName(s)).sorted().collect( Collectors.joining("\n"));
							collectedResults.add(result);
												
							System.out.println(">>");
						}
				};
				
				es.execute(runnable);
			}
			es.awaitTermination(10, TimeUnit.SECONDS);
			
			for (String suspect : collectedResults) {
				for (String m : collectedResults) {
					if (m == suspect)
						continue;
					
					boolean condition = m.equalsIgnoreCase(suspect);
					Assert.assertTrue("differning results found", condition);
					if (!condition) {
						dumpComparison(m, suspect);
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown");
		} 
	
	}
	
		
	private List<String> toList( String in) {
		String [] values = in.split("\n");
		return Arrays.asList( values);
	}
	private void dumpComparison(String m, String suspect) {
		List<String> one = toList(m);
		List<String> two = toList( suspect);
		for (int i = 0; i < one.size(); i++) {
		
			if (!two.contains( one.get(i))) {
				System.out.println("discrepanncy : [" + one.get( i) + "] and [" + two.get( i) + "]"); 
			}
		}
		
	}





	//@Test
	public void sequentialTest() {
		Solution terminal = Solution.T.create();
		terminal.setGroupId( "tribefire.extension.elastic");
		terminal.setArtifactId( "elasticsearch-module");
		terminal.setVersion( VersionProcessor.createFromString("2.0.12"));
		
		sequentialTest( terminal, 10);
			
		
	}
	
	//@Test
	public void parallelTest() {
		Solution terminal = Solution.T.create();
		terminal.setGroupId( "tribefire.extension.elastic");
		terminal.setArtifactId( "elasticsearch-module");
		terminal.setVersion( VersionProcessor.createFromString("2.0.12"));
		
		parallelTest( terminal, 5);
			
		
	}
}
