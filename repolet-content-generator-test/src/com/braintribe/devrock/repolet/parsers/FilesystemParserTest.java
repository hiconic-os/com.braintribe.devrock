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
package com.braintribe.devrock.repolet.parsers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.junit.Test;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.parser.FilesystemContentParser;

public class FilesystemParserTest {
	private YamlMarshaller marshaller = new YamlMarshaller();

	private void run(File in, File out) {
		FilesystemContentParser parser = new FilesystemContentParser();		
		RepoletContent content = parser.parse( in);
		
		if (content != null) {
			try (OutputStream outStream = new FileOutputStream( out )) {
				marshaller.marshall(outStream, content);
			} catch (Exception e) {				
				throw new IllegalStateException("cannot write content to [" + out.getAbsolutePath() + "]", e);
			} 
			
		}
		else {
			System.out.println("no content found for [" + in.getAbsolutePath() + "]");
		}

	}
	@Test
	public void extract_wiredResolving( ) {
		File in = new File("F:/works/COREDR-10/com.braintribe.devrock/mc-core-test/res/wired/resolving/input/remoteRepoA");
		File out = new File( in.getParentFile(), "remoteRepoA.definition.yaml");
		run(in, out);
		
		in = new File("F:/works/COREDR-10/com.braintribe.devrock/mc-core-test/res/wired/resolving/input/remoteRepoB");
		out = new File( in.getParentFile(), "remoteRepoB.definition.yaml");
		run(in, out);
		
	}
	
	@Test
	public void extract_wiredBase( ) {
		
		//"F:\works\COREDR-10\com.braintribe.devrock\mc-core-test\res\wired\transitive\base\input\commonBranches"
		
		File in = new File("F:/works/COREDR-10/com.braintribe.devrock/mc-core-test/res/wired/transitive/base/input/commonBranches");
		File out = new File( in.getParentFile(), "commonBranches.definition.yaml");
		run(in, out);
		
		in = new File("F:/works/COREDR-10/com.braintribe.devrock/mc-core-test/res/wired/transitive/base/input/commonBranchesWithExclusions");
		out = new File( in.getParentFile(), "commonBranchesWithExclusions.definition.yaml");
		run(in, out);
		
		in = new File("F:/works/COREDR-10/com.braintribe.devrock/mc-core-test/res/wired/transitive/base/input/redirectionTree");
		out = new File( in.getParentFile(), "redirectionTree.definition.yaml");
		run(in, out);
		
		in = new File("F:/works/COREDR-10/com.braintribe.devrock/mc-core-test/res/wired/transitive/base/input/simpleImportingReferenceTree");
		out = new File( in.getParentFile(), "simpleImportingReferenceTree.definition.yaml");
		run(in, out);
		
		in = new File("F:/works/COREDR-10/com.braintribe.devrock/mc-core-test/res/wired/transitive/base/input/simpleReferenceTree");
		out = new File( in.getParentFile(), "simpleReferenceTree.definition.yaml");
		run(in, out);
		
		in = new File("F:/works/COREDR-10/com.braintribe.devrock/mc-core-test/res/wired/transitive/base/input/simpleTree");
		out = new File( in.getParentFile(), "simpleTree.definition.yaml");
		run(in, out);
	}
}
