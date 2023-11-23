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
