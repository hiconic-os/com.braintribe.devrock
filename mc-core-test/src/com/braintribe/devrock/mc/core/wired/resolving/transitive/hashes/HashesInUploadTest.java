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
		
		
		runUpload(artifactA);
		
		int size = updateDataNotified.size();
		Assert.assertTrue("expected [" + numParts + "] part entries in the upload notification, yet there are [" + size + "]", size == numParts);
		
		for (Map.Entry<String, UploadData> entry : updateDataNotified.entrySet()) {
			UploadData uploadData = entry.getValue();
			
			String target = uploadData.target;
			int p = target.lastIndexOf( '.');
			String extension = target.substring( p+1);
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
