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
package com.braintribe.test.multi.wire.walk.performance;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.walk.multi.Walker;
import com.braintribe.build.artifacts.mc.wire.classwalk.context.WalkerContext;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.test.multi.wire.AbstractWalkerWireTest;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.wire.api.context.WireContext;


@Category(KnownIssue.class)
public class TestOnMalalclypse extends AbstractWalkerWireTest {
	private File contents = new File("res/performance");
	private File settings = new File( contents, "settings.xml");
	private File repository = new File( contents, "repository");
	
	private Dependency terminalDependency;
	
	{
		terminalDependency = Dependency.T.create();
		terminalDependency.setGroupId( "com.braintribe.devrock");
		terminalDependency.setArtifactId( "malaclypse");
		terminalDependency.setVersionRange( VersionRangeProcessor.createFromString("[1.0,1.1)"));
	}
	
	@Before
	public void before() {
		TestUtil.ensure(repository);
	}

	@Test
	public void singleRunTest() {

		try {
			WireContext<ClasspathResolverContract> classpathWalkContext = getClasspathWalkContext( settings, repository, ResolvingInstant.posthoc);			
			performWalk(classpathWalkContext, "single");			
		} catch (Exception e) {
			e.printStackTrace();			
		} 	
	}

	private void performWalk(WireContext<ClasspathResolverContract> classpathWalkContext, String tag) {
		WalkerContext walkerContext = new WalkerContext();		
		Walker walker = classpathWalkContext.contract().walker( walkerContext);			
		String walkScopeId = UUID.randomUUID().toString();
		
		Solution terminal = classpathWalkContext.contract().dependencyResolver().resolveSingleTopDependency(walkScopeId, terminalDependency);

		long before = System.nanoTime();
		Collection<Solution> collection = walker.walk( walkScopeId, terminal);
		long after = System.nanoTime();
		
		long retval = after - before;			
		System.out.println( tag  + " resolution took [" + retval / 1_000_000_000D + "s]");
	}
	
	@Test
	public void cachedRunTest() {
		try {
			WireContext<ClasspathResolverContract> classpathWalkContext = getClasspathWalkContext( settings, repository, ResolvingInstant.posthoc);			
			performWalk(classpathWalkContext, "empty");
			
			classpathWalkContext = getClasspathWalkContext( settings, repository, ResolvingInstant.posthoc);			
			performWalk(classpathWalkContext, "filesystem cache");
			
			performWalk(classpathWalkContext, "full cache");
			
		} catch (Exception e) {
			e.printStackTrace();			
		} 	
		
	}

}
