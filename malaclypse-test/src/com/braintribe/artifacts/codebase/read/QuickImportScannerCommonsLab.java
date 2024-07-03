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
package com.braintribe.artifacts.codebase.read;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.braintribe.build.quickscan.commons.QuickImportScannerCommons;
import com.braintribe.model.panther.SourceRepository;

public class QuickImportScannerCommonsLab {
	private static File contents = new File("res/quickImportScannerLab");
	
	
	private void test(URL url, File ... files) {
		SourceRepository sourceRepository = SourceRepository.T.create();
		sourceRepository.setRepoUrl( url.toString());
		
		for (File file : files) {
			String path = QuickImportScannerCommons.derivePath(file, sourceRepository);
			System.out.println(path);
		}
	}

	//@Test
	public void test() {
 
		try {
			URL url = contents.toURI().toURL();
			List<File> files = new ArrayList<File>();
			files.add( new File( contents, "com/braintribe/model/test/pom.xml"));
			test( url, files.toArray( new File[0]));
		} catch (MalformedURLException e) {
			
		}
	}
	
}
