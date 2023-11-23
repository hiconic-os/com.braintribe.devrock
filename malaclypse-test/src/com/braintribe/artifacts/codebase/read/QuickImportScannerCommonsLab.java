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
