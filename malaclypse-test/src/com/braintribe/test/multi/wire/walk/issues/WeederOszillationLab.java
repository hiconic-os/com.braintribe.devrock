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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.walk.multi.clash.ClashResolvingException;
import com.braintribe.build.artifact.walk.multi.clash.ConfigurableClashResolver;
import com.braintribe.build.artifact.walk.multi.clash.InitialDependencyPrecedenceSorter;
import com.braintribe.build.artifact.walk.multi.clash.InitialDependencySortByPathIndex;
import com.braintribe.build.artifact.walk.multi.clash.OptimisticWeedingClashResolver;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;
import com.braintribe.test.multi.wire.AbstractRepoletWalkerWireTest;

public class WeederOszillationLab  extends AbstractRepoletWalkerWireTest {
	
	@Override
	protected File getRoot() {	
		return new File("res/wire/issues/weeder-oszillation");
	}

	
	@Test
	public void test() {
		Solution terminal = Solution.T.create();
		terminal.setGroupId( "tribefire.extension.elastic");
		terminal.setArtifactId("elasticsearch-module");
		terminal.setVersion( VersionProcessor.createFromString("2.0.13"));
		int num = 100 ;
		for (int i = 0; i < num; i++) {
			System.out.println("<<");
			test( "tribefire.extension.elastic.elasticsearch-module#2.0.13.dump.txt", terminal);
			System.out.println(">>");
		}
		
	}

	
	private void test(String file, Solution terminal) {
		Set<Dependency> collected = load(file);

		ConfigurableClashResolver bean = new OptimisticWeedingClashResolver();
		bean.setDependencyMerger( null);
		bean.setInitialPrecedenceSorter( new InitialDependencySortByPathIndex());
		bean.setResolvingInstant( ResolvingInstant.posthoc);
		bean.setLeniency( true);
		
		
		try {
			List<Dependency> resolved = bean.resolveDependencyClashes(UUID.randomUUID().toString(), terminal, collected);
			//System.out.println(toString( resolved));
			
			int f = 0;
			for (Dependency d : resolved) {
				if (d.getArtifactId().equalsIgnoreCase("commons-logging")) {
					System.out.println("\t" + NameParser.buildName(d) + "(" + f++ + ")");
				}
			}
			
		} catch (ClashResolvingException e) {
			e.printStackTrace();
			Assert.fail("cannot resolve clashes");
		}
		
	}



	private Set<Dependency> load(String name) {
		File file = new File( getRoot(), name);
		StaxMarshaller marshaller = new StaxMarshaller();
		try (InputStream in = new FileInputStream( file)) {
			return (Set<Dependency>) marshaller.unmarshall(in);
		}
		catch (Exception e) {
			Assert.fail("cannot read file [" + file.getAbsolutePath() + "]");			
		}
		return null;
	}
	
	 
	private String toString( Collection<Dependency> deps) {
		return deps.stream().map( d -> NameParser.buildName(d)).collect( Collectors.joining(";"));
	}

	@Test
	public void testInitialSort() {
		String file = "tribefire.extension.elastic.elasticsearch-module#2.0.13.dump.txt";		
		Set<Dependency> collected = load(file);
		System.out.println( "before : " + toString( collected));		

		int num = 20;
		for (int i = 0; i < num; i++) {
			InitialDependencyPrecedenceSorter sorter = new InitialDependencySortByPathIndex();
			List<Dependency> sortedDependencies = sorter.sortDependencies(collected);
			System.out.println( "after : " + toString( sortedDependencies));
		}
						
	}
}
