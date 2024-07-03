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
