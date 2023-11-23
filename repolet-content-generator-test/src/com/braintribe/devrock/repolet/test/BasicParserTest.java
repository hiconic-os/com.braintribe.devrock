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
package com.braintribe.devrock.repolet.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.parser.RepoletContentParser;
import com.braintribe.model.resource.Resource;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.wire.api.util.Lists;


@Category(KnownIssue.class)
public class BasicParserTest {
	private File contents = new File( "res/simple");
	private File input = new File( contents, "input");
	//private File output = new File( contents, "output");

	
	private void test(File file) {
		try (InputStream in = new FileInputStream(file)) {
			RepoletContent content = RepoletContentParser.INSTANCE.parse(in);		
			System.out.println(content);
		}
		catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception [" + e.getMessage() + "] thrown");
		}
	}
	
	@Test
	public void testSimple() {
		test( new File( input, "simple.txt"));
	}
	
	@Test
	public void testPartParsing() {
		// 
		List<String> expressions = Lists.list( "cl:ty;cn", "ty;cn", "cl:;cn", ":;cn", "cl:ty;@fl");	
		for (String arg : expressions) {
			Pair<String,Resource> result = RepoletContentParser.INSTANCE.processPart( arg);
			if (result.second == null) {
				System.out.println( arg + " -> " + result.first);
			}
			else {
				System.out.println( arg + " -> " + result.first + " @" + result.second.getClass().getName());
			}
		}
	}

}
