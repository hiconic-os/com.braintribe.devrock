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
