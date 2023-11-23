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
import java.util.Collection;
import java.util.UUID;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.walk.multi.Walker;
import com.braintribe.build.artifacts.mc.wire.classwalk.context.WalkerContext;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;
import com.braintribe.test.multi.wire.AbstractRepoletWalkerWireTest;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.wire.api.context.WireContext;
@Category(KnownIssue.class)
public class GwtUserClassfierClash extends AbstractRepoletWalkerWireTest {

	
	
	@Override
	protected File getRoot() {	
		return new File("res/wire/issues/scratch");
	}


	public void test(Solution terminal) {
	
		try {
			WireContext<ClasspathResolverContract> classpathWalkContext = getClasspathWalkContext( null, null, ResolvingInstant.adhoc);			
			
			WalkerContext walkerContext = new WalkerContext();
			
			Walker walker = classpathWalkContext.contract().walker( walkerContext);
			
			String walkScopeId = UUID.randomUUID().toString();
			
			
			Collection<Solution> collection = walker.walk( walkScopeId, terminal);
			System.out.println("found [" + collection.size() + "] dependencies");
			for (Solution s : collection) {
				System.out.println( NameParser.buildName(s));
				for (Part part : s.getParts()) {
					System.out.println( "\t" + part.getLocation());
				}
			}
		} catch (Exception e) {

			e.printStackTrace();
		} 
	}
	
		
	@Test
	public void test() {
		Solution terminal = Solution.T.create();
		terminal.setGroupId( "com.google.gwt");
		terminal.setArtifactId( "gwt-user");
		terminal.setVersion( VersionProcessor.createFromString("2.8.0"));
		
		test( terminal);
	}
}
