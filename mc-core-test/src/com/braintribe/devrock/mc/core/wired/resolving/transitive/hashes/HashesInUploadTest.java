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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.hashes;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.wire.api.util.Lists;

/**
 * tests whether all three hash files are sent to the repolet, and that they match what is sent via the headers
 * 
 * NOTE: hash files are sent without headers and without hash files, as it would be project into infinity
 * 
 * @author pit
 *
 */
public class HashesInUploadTest extends AbstractHashTest {
	
	private static final List<String> knownHashExtensions = Lists.list( "md5", "sha1", "sha256");
	
	private static final Map<String, String> digestToExtensionTransposingMap = new HashMap<>();
	
	{
		digestToExtensionTransposingMap.put("MD5", "md5");
		digestToExtensionTransposingMap.put("SHA-1", "sha1");
		digestToExtensionTransposingMap.put("SHA-256", "sha256");
	}

	@Test
	public void runUploadHashTest() {
		File directory = new File( uploadSource, "a-1.0.2");
		Artifact artifactA = generateArtifact("com.braintribe.devrock.test:a#1.0.2", directory);
		
		int numParts = directory.list().length;
		
		numParts *= 4; // hashes
		
		numParts += 4; // second level maven metadata 
		
		numParts += 4; // first level maven metadata
		
		numParts ++; // publish.complete
		
		
		runUpload(artifactA);
		
		int size = updateDataNotified.size();
		Assert.assertTrue("expected [" + numParts + "] part entries in the upload notification, yet there are [" + size + "]", size == numParts);
		
		for (Map.Entry<String, UploadData> entry : updateDataNotified.entrySet()) {
			UploadData uploadData = entry.getValue();
			
			String target = uploadData.target;
			
			
			int p = target.lastIndexOf( '.');
			String extension = target.substring( p+1);
			
			if (extension.equals("publish-complete"))
				continue;
			
			if (knownHashExtensions.contains( extension)) {
				continue;
			}
			
			Map<String, String> headerHashes = uploadData.headerHashes;
			Map<String,String> fileHashes = uploadData.fileHashes;
						
			
			// 
			for (Map.Entry<String, String> headerEntry : headerHashes.entrySet()) {
				String headerHash = headerEntry.getValue();
				String hashExtension = digestToExtensionTransposingMap.get( headerEntry.getKey());
				String fileHash = fileHashes.get( hashExtension);
				Assert.assertTrue("no file hash value for [" + headerEntry.getKey() + "]", fileHash != null);
				Assert.assertTrue("expected for [" + headerEntry.getKey() + "] : [" + headerHash + "], but found [" + fileHash + "]", headerHash.equals(fileHash));				
			}
		}
		
		
	}
}
