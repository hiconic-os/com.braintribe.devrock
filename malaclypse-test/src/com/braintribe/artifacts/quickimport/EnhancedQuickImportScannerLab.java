// ============================================================================
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
package com.braintribe.artifacts.quickimport;

import java.util.List;

import org.junit.Assert;
import org.junit.experimental.categories.Category;

import com.braintribe.build.quickscan.notification.QuickImportScanNotificationListener;
import com.braintribe.build.quickscan.standard.EnhancedQuickImportScanner;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.testing.category.KnownIssue;
@Category(KnownIssue.class)
public class EnhancedQuickImportScannerLab {
	
	public class ScanListener implements QuickImportScanNotificationListener {

		@Override
		public void acknowledgeScanned(SourceArtifact tuple) {		
			System.out.println( "success \t" + tuple.getPath());
		}

		@Override
		public void acknowlegeScanError(String file) {
			System.out.println( "error \t" + file);
			
		}
		
	}


	public void testEnhancedScan(SourceRepository sourceRepository, String scanRoot) {
		// 
		List<SourceArtifact> sourceArtifacts;
		try {
			EnhancedQuickImportScanner scanner = new EnhancedQuickImportScanner();
			scanner.setSourceRepository(sourceRepository);
			//scanner.addListener( new ScanListener());
			sourceArtifacts = scanner.scanLocalWorkingCopy( scanRoot);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception [" + e + "] thrown");
			return;
		}
		
		Assert.assertTrue("no results found", sourceArtifacts.size() > 0);
	}
	
	private SourceRepository generateDefaultSourceRepository() {
		SourceRepository sourceRepository = SourceRepository.T.create();
		sourceRepository.setName( "Local SVN working copy");
		sourceRepository.setRepoUrl("file:/" + System.getenv( "BT__ARTIFACTS_HOME"));		
		return sourceRepository;
	}
	
	//@Test
	public void fullTest() {		
	
		String scanRoot = System.getenv("BT__ARTIFACTS_HOME");
		testEnhancedScan( generateDefaultSourceRepository(), scanRoot);
	}
	
	//@Test
	public void parentTest() {			
		String scanRoot = System.getenv("BT__ARTIFACTS_HOME") + "/com/braintribe/test/dependencies/resolvingPerParentTest";
		testEnhancedScan( generateDefaultSourceRepository(), scanRoot);
	}
	
	//@Test
	public void parentLoopTest() {		
		
		String scanRoot = System.getenv("BT__ARTIFACTS_HOME") + "/com/braintribe/test/dependencies/parentLoopTest";
		testEnhancedScan( generateDefaultSourceRepository(), scanRoot);
	}
	

}
