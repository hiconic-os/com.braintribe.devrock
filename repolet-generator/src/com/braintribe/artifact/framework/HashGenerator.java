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
package com.braintribe.artifact.framework;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;

/**
 * generator to create hashes for the repolet (see com.braintribe.devrock:repolet#V.com.braintribe.devock)
 * 
 * @author pit
 *
 */
public class HashGenerator {

	private static final String EXT_SHA256 = ".sha256";
	private static final String EXT_SHA1 = ".sha1";
	private static final String EXT_MD5 = ".md5";
	private static final String EXT_BLK = ".blk";

	/**
	 * recursively creates hashes for all files in the directory 
	 * @param directory - the starting point {@link File}
	 * @param bulk - true if a 'bulk' file should be made, false if standard hash files 
	 * @throws IOException
	 */
	public static void createHashes( File directory, boolean bulk) throws IOException {
		File [] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				createHashes(file, bulk);
			}
			else {
				createHash( file, bulk);
			}
		}
	}
	
	/**
	 * creates the hashes for a single file 
	 * @param file - the {@link File} to hash
	 * @param bulk - true if a 'bulk' file should be made, false if standard hash files
	 * @throws IOException
	 */
	public static void createHash( File file, boolean bulk) throws IOException {
		String path = file.getAbsolutePath();
		if (
				path.endsWith( "maven-metadata.xml") ||
				path.endsWith( EXT_BLK) || path.endsWith( EXT_MD5) || path.endsWith( EXT_SHA1) || path.endsWith( EXT_SHA256)
			) {
			System.out.println("skip [" + path);
			return;
		}
		try ( 
				InputStream in = new FileInputStream(file);
				DigestInputStream md5_stream = new DigestInputStream(in, MessageDigest.getInstance( "MD5"));
				DigestInputStream sha1_stream = new DigestInputStream(md5_stream, MessageDigest.getInstance( "SHA-1"));
				DigestInputStream sha256_stream = new DigestInputStream(sha1_stream, MessageDigest.getInstance( "SHA-256"));
				OutputStream out = new ByteArrayOutputStream();
			) {
			IOTools.transferBytes(sha256_stream, out, IOTools.BUFFER_SUPPLIER_64K);
			
			String md5 = StringTools.toHex(md5_stream.getMessageDigest().digest());
			String sha1 = StringTools.toHex(sha1_stream.getMessageDigest().digest());
			String sha256 = StringTools.toHex(sha256_stream.getMessageDigest().digest());
			
			if (bulk) {
				// create bulk file, format <HASH TYPE>:<HASH VALUE>[\n<HASH TYPE>:<HASH VALUE>..]
				File bulkFile = new File( path + EXT_BLK);
				try (Writer writer = new OutputStreamWriter( new FileOutputStream( bulkFile), "US-ASCII")) {
					writer.write( "X-Checksum-Md5:" + md5 + "\n");
					writer.write( "X-Checksum-Sha1:" + sha1 + "\n");
					writer.write( "X-Checksum-Sha256:" + sha256 + "\n");
				}
			}
			else {
				// sing
				IOTools.spit(new File( path + EXT_MD5), md5, "US-ASCII", false);
				IOTools.spit(new File( path + EXT_SHA1), sha1, "US-ASCII", false);
				IOTools.spit(new File( path + EXT_SHA256), sha256, "US-ASCII", false);
			}						
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * call with a list of file names or directories to create hashes..
	 * expression with a prefix of '!' are processed as bulk hashes..
	 * @param args
	 */
	public static void main(String[] args) {
		for (String arg : args) {
			boolean bulk = false;
			if (arg.startsWith( "!"))  {
				bulk = true;
				arg = arg.substring(1);
			}
			
			File file = new File( arg);
			if (!file.exists()) {
				System.out.println("no such file [" + file.getAbsolutePath() + "]");
				continue;
			}
			if (file.isDirectory()) {
				try {
					createHashes(file, bulk);
				} catch (IOException e) {
					System.err.println("cannot create bulk hashes for [" + file.getAbsolutePath() + "] as " + e.getMessage());
				}
			}
			else {
				try {
					createHash(file, bulk);
				} catch (IOException e) {
					System.err.println("cannot create hash files for [" + file.getAbsolutePath() + "] as " + e.getMessage());
				}
			}
		}
	}
}
