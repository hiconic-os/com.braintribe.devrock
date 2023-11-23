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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepoletContentGenerator;
import com.braintribe.devrock.repolet.generators.TestUtils;
import com.braintribe.devrock.repolet.parser.RepoletContentParser;
import com.braintribe.testing.category.KnownIssue;

@Category(KnownIssue.class)
public class ProcessingInstructionsTest {

	private File contents = new File( "res/processing");
	private File input = new File( contents, "input");
	private File output = new File( contents, "output");
	
	@Before 
	public void before() {
		TestUtils.ensure(output);
	}

	
	private void test(File file) {
		try (InputStream in = new FileInputStream(file)) {
			RepoletContent content = RepoletContentParser.INSTANCE.parse(in);
			RepoletContentGenerator.INSTANCE.generate(output, content);
			
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

}
