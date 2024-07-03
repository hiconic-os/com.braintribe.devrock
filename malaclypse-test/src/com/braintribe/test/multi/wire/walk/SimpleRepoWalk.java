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
package com.braintribe.test.multi.wire.walk;

import java.util.Collection;
import java.util.UUID;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.walk.multi.Walker;
import com.braintribe.build.artifacts.mc.wire.classwalk.context.WalkerContext;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;
import com.braintribe.test.multi.wire.AbstractWalkerWireTest;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.wire.api.context.WireContext;
@Category(KnownIssue.class)
public class SimpleRepoWalk extends AbstractWalkerWireTest {
	

	/**
	 * 
	 */
	@Test
	public void testStandard() {
	
		try {
			WireContext<ClasspathResolverContract> classpathWalkContext = getClasspathWalkContext( null, null, ResolvingInstant.posthoc);
			
			WalkerContext walkerContext = new WalkerContext();
			
			Walker walker = classpathWalkContext.contract().walker( walkerContext);
			
			String walkScopeId = UUID.randomUUID().toString();
			
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.devrock");
			terminal.setArtifactId( "malaclypse");
			terminal.setVersion( VersionProcessor.createFromString("1.0.53"));
			
			Collection<Solution> collection = walker.walk( walkScopeId, terminal);
			System.out.println("found [" + collection.size() + "] dependencies");
			collection.stream().forEach( s -> System.out.println( NameParser.buildName(s)));
		} catch (Exception e) {

			e.printStackTrace();
		} 
	}
	
	@Test
	public void testAdHoc() {
	
		try {
			WireContext<ClasspathResolverContract> classpathWalkContext = getClasspathWalkContext( null, null, ResolvingInstant.adhoc);			
			
			WalkerContext walkerContext = new WalkerContext();
			
			Walker walker = classpathWalkContext.contract().walker( walkerContext);
			
			String walkScopeId = UUID.randomUUID().toString();
			
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.devrock");
			terminal.setArtifactId( "malaclypse");
			terminal.setVersion( VersionProcessor.createFromString("1.0.53"));
			
			Collection<Solution> collection = walker.walk( walkScopeId, terminal);
			System.out.println("found [" + collection.size() + "] dependencies");
		} catch (Exception e) {

			e.printStackTrace();
		} 
	}


}
