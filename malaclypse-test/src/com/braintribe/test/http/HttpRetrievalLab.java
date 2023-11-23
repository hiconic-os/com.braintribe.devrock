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
package com.braintribe.test.http;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.build.artifact.retrieval.multi.retrieval.access.http.HttpRetrievalExpert;
import com.braintribe.utils.IOTools;

public class HttpRetrievalLab {
	private static File contents = new File("res/retrieval/href");

	private void test( File file) {	
		try {
			String content = IOTools.slurp(file, "UTF-8");
			List<String> extractHrefs = HttpRetrievalExpert.extractHrefs(content);
			for (String href : extractHrefs) {
				System.out.println( href);
			}
		} catch (IOException e) {
			Assert.fail( "Exception [" + e.getMessage() + "] thrown");
		}
	}
	
	private void testParse( File file) {	
		try {
			String content = IOTools.slurp(file, "UTF-8");
			List<String> extractHrefs = HttpRetrievalExpert.parseFilenamesFromHtml(content, "<source>");
			for (String href : extractHrefs) {
				System.out.println( href);
			}
		} catch (IOException e) {
			Assert.fail( "Exception [" + e.getMessage() + "] thrown");
		}
	}
	
	//@Test
	public void test() {
		test( new File ( contents, "view-source_central.maven.org_maven2_com_google_guava_guava_21.0_.html"));
		testParse( new File ( contents, "view-source_central.maven.org_maven2_com_google_guava_guava_21.0_.html"));
	}

}
