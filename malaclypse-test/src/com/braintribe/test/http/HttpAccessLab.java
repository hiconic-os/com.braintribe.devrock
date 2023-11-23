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
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.braintribe.build.artifact.retrieval.multi.retrieval.access.http.HttpAccess;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.http.HttpRetrievalException;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.utils.FileTools;

public class HttpAccessLab {
	private static final String root="http://archiva.braintribe.com/repository/standalone";
	private static final File contents = new File("res/accesslab/content/batch");
	private static final String user = "builder";
	private static final String pwd = "operating2005";
	private static HttpAccess httpAccess;
	
	@BeforeClass
	public static void runBefore() {
		 httpAccess = new HttpAccess();
	}
	
	@AfterClass
	public static void runAfter() {
		httpAccess.closeContext();
	}
		
	private void download( String source) {
		Server server = Server.T.create();
		server.setUsername(user);
		server.setPassword(pwd);
		
		try {
			String value = httpAccess.acquire(source, server, null);
			System.out.println( value);
		} catch (HttpRetrievalException e) {
			Assert.fail("cannot download [" + source + "] as " + e.getMessage());
		}
	}
	
	private void download( File target, String source) {
		Server server = Server.T.create();
		server.setUsername(user);
		server.setPassword(pwd);
		
		try {
			File file = httpAccess.require(target, source, server, null);
			System.out.println( file.getAbsolutePath());
		} catch (HttpRetrievalException e) {
			Assert.fail("cannot download [" + source + "] as " + e.getMessage());
		}
	}
	
	private boolean upload( String target, File source) {
		Server server = Server.T.create();
		server.setUsername(user);
		server.setPassword(pwd);
		Map<File, String> map = new HashMap<File, String>();
		map.put( source, target);
		Map<File, Integer> result;
		try {
			result = httpAccess.upload(server, map, false, null);
		} catch (HttpRetrievalException e) {
			Assert.fail("cannot upload [" + source + "] to as [" + target + "] as "+ e.getMessage());
			return false;
		}
		
		Integer code = result.get( source);
		if (code < 200 || code >= 300) {
			Assert.fail("unexpected result code : [" + code + "]");
			return false;
		}				
		return true;
	}
	
	private boolean delete( String source) {
		Server server = Server.T.create();
		server.setUsername(user);
		server.setPassword(pwd);		
		try {
			return httpAccess.delete(source, server, null);			
		} catch (HttpRetrievalException e) {
			Assert.fail("cannot delete [" + source + "] as "+ e.getMessage());
			return false;
		}
	}
	

	public void roundtripTest() {
		Server server = Server.T.create();
		server.setUsername(user);
		server.setPassword(pwd);
		Map<File, String> sourceToTargetMap = new HashMap<File, String>();
		File [] files = new File(contents, "upload").listFiles( new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".zip"))
					return true;
				return false;
			}
		});
		for (File file : files) {
			sourceToTargetMap.put( file, root +"/test/uploads/batch/" + file.getName());
		}
		Map<File, Integer> result;
		try {
			result = httpAccess.upload(server, sourceToTargetMap, false, null);
		} catch (HttpRetrievalException e) {
			Assert.fail("cannot upload batch as "+ e.getMessage());
			return;
		}
		for (Entry<File, Integer> entry : result.entrySet()) {
			if (entry.getValue() < 200 || entry.getValue() >= 300) {
				Assert.fail( "upload error [" + entry.getKey() + "] to [" + sourceToTargetMap.get(entry.getKey()) + "], code [" + entry.getValue() + "]" );
			}
		}
		
		// download again
		for (Entry<File, String> entry : sourceToTargetMap.entrySet()) {
			String uploadSource = entry.getKey().getAbsolutePath();
			String downloadTarget = contents.getParent() + "/download" + "/" + uploadSource.substring( uploadSource.lastIndexOf( File.separatorChar)); 
			String downloadSource = entry.getValue();
			try {
				httpAccess.require( new File(downloadTarget), downloadSource, server, null);
			} catch (HttpRetrievalException e) {
				Assert.fail("cannot download [" + downloadSource + "] to [" + downloadTarget + "] as " + e.getMessage());
			}
			try {
				httpAccess.delete(downloadSource, server, null);
			} catch (HttpRetrievalException e) {
				Assert.fail("cannot delete [" + downloadSource + "] as " + e.getMessage());
			}
			
			// compare
			if (!FileTools.isFileContentEqual(new File( uploadSource), new File( downloadTarget))) {
				Assert.fail("files [" + uploadSource + "] and [" + downloadTarget + "] don't match");
			}
		}
		
		try {
			if (!httpAccess.delete(root +"/test/uploads/batch/", server, null)) {
				Assert.fail("cannot delete test directory");
			}
		} catch (HttpRetrievalException e) {
			Assert.fail("cannot delete upload directory as " + e.getMessage());
		}
		
	}
	
	public void testDownloadText() {
		String source = root + "/com/braintribe/model/RootModel/2.0/RootModel-2.0.pom";
		download( source);
		download( new File("res/accesslab/content/RootModel-2.0.pom"), source);
	}
	
	public void testDownloadBinary() {
		String source = root + "/com/braintribe/build/artifacts/Malaclypse/2.0/Malaclypse-2.0.jar";
		download( source);
		download( new File( "res/accesslab/content/Malaclypse-2.0.jar"), source);
	}

	
	public void testUploadBinary() {
		
		String source = "res/accesslab/content/Malaclypse-2.0.jar";
		String target = root + "/test/uploads/upload-1.0.jar";
		upload( target, new File(source));
	}
	
	
	public void testDelete() {
		String target = root + "/test/uploads/upload-1.0.jar";
		delete( target);
	}

	
	public void testDownload404() {
		Server server = Server.T.create();
		server.setUsername(user);
		server.setPassword(pwd);
		
		File target = new File( contents, "test");
		String source = root + "/bla/mavenmetadata.xml";
		try {
			File file = httpAccess.require(target, source, server, null);
			System.out.println( file.getAbsolutePath());
		} catch (HttpRetrievalException e) {
			Assert.fail("cannot download [" + source + "] as " + e.getMessage());
		}
	}

}
