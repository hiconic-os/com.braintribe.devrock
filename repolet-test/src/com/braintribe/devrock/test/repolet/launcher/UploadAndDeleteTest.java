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
package com.braintribe.devrock.test.repolet.launcher;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.repolet.AbstractRepolet;
import com.braintribe.devrock.repolet.launcher.builder.api.LauncherCfgBuilderContext;
import com.braintribe.devrock.test.repolet.launcher.utils.TestUtils;

public class UploadAndDeleteTest extends AbstractLauncherTest {
	private File initial = new File( getRoot(), "initial");
	private File uploads = new File( initial, "uploads");
	private File target = new File( getRoot(), "uploads");
	
	{
		launcher = LauncherCfgBuilderContext.build()
				.repolet()
					.name("archiveA")							
					.filesystem()
						.filesystem( new File( initial, "remoteRepoA"))
					.close()
					.uploadFilesystem()
						.filesystem( new File( target, "remoteRepoA"))
					.close()
				.close()
				.repolet()
					.name("archiveB")					
					.filesystem()
						.filesystem( new File( initial, "remoteRepoB"))
					.close()
					.uploadFilesystem()
						.filesystem( new File( target, "remoteRepoB"))
					.close()
				.close()
			.done();				
	}
	
	@Override
	protected File getRoot() {		
		return new File( res, "upload.folder");
	}
	
	
	@Override
	protected void runBeforeBefore() {
		TestUtils.ensure(target);	
	}



	private void test(File fileToUpload) {	
		Path path = fileToUpload.toPath();
		Path uploadPath = uploads.toPath();
		Path relPath = uploadPath.relativize(path);
		String relPathAsString = relPath.toString().replace("\\", "/");		
				
		Map<String, String> launchedRepolets = launcher.getLaunchedRepolets();
		System.out.println();
		
		CloseableHttpClient client = client();
		HttpPut httpPut = new HttpPut( launchedRepolets.get( "archiveA") + "/" + relPathAsString);
		FileEntity fileEntity = new FileEntity( fileToUpload);
				
		Map<String, String> hashes = AbstractRepolet.generateHash( fileToUpload, Arrays.asList("sha1", "md5", "SHA-256"));		
		httpPut.setHeader("X-Checksum-Sha1", hashes.get("sha1"));
		httpPut.setHeader("X-Checksum-MD5", hashes.get("md5"));
		httpPut.setHeader("X-Checksum-SHA256", hashes.get("SHA-256"));
		
		httpPut.setEntity( fileEntity);
		int code = -1;
		try {
			CloseableHttpResponse response = client.execute(httpPut);
			code = response.getStatusLine().getStatusCode();
			response.close();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception [" + e.getMessage() + "] thrown");
			return;
		} 
		Assert.assertTrue("unexpected return code [" + code + "] returned", code == 200);		
	}
	@Test
	public void test() {
		test( new File( uploads, "com/braintribe/devrock/test/artifact/1.0/artifact-1.0.pom"));
	}
	
	

}
