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
package com.braintribe.devrock.mc.core.repository.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.api.commons.PartIdentifications;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ChecksumPolicy;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.repository.HttpClientFactory;
import com.braintribe.devrock.mc.core.resolver.HttpRepositoryArtifactDataResolver;
import com.braintribe.devrock.repolet.common.RepoletCommons;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;


/**
 * requires the following archive structure <br/>
 * 
 * com.braintribe.devrock.test.mc-ng-hashes:terminal#1.0 (no hashes) <br/>
 * com.braintribe.devrock.test.mc-ng-hashes:a#1.0.0 (bulk hashes) <br/>
 * com.braintribe.devrock.test.mc-ng-hashes:b#1.0.0 (single file hashes) <br/>
 * 
 * currently inactive test due to the following issues
 * - the validator cannot validate the file correctly as it doesn't know what was 
 * sent by the repolet
 * - the repolet always tries to send the correct hashes unless told (now)
 * - what the test should aim for is to make sure the *download* process detects discrepancies
 * between the sent file and the sent hashes 
 * 
 * @author pit
 *
 */
@Category(KnownIssue.class)
public class HttpRepositoryArtifactDataResolverTest extends AbstractRepoletBasedTest {
	private static final String GRP ="com.braintribe.devrock.test.mc-ng-hashes";	
	
	private File hashes = new File( input, "hashes");
	private File scratch = new File( input, "scratch");	
	
	{
		launcher = Launcher.build()
							.repolet()
								.name( "archive")
								.filesystem()
									.filesystem(hashes)
									.useExternalHashes(true)
								.close()
							.close()
							.done();						
	}
	
	public static Map<String, Pair<String,String>> hashAlgToHeaderKeyAndExtension = new LinkedHashMap<>();
	static {
		hashAlgToHeaderKeyAndExtension.put( "MD5", Pair.of("X-Checksum-Md5", "md5"));
		hashAlgToHeaderKeyAndExtension.put( "SHA-1", Pair.of( "X-Checksum-Sha1", "Sha1"));
		hashAlgToHeaderKeyAndExtension.put( "SHA-256", Pair.of( "X-Checksum-Sha256", "Sha256"));
	}
		
	@Before
	public void before() {
		runBefore();
		TestUtils.ensure(scratch);
	}
	
	@After
	public void after() {
		runAfter();
	}
	
	private String getRoot() {
		return launchedRepolets.get( "archive");				
	}
	
	
	private String buildUrl( String root, CompiledArtifactIdentification ci, PartIdentification part) {
		return ArtifactAddressBuilder.build()
			.root(root)
			.compiledArtifact(ci)
			.part(part)
			.toPath().toSlashPath();				
	}
	
	private HttpRepositoryArtifactDataResolver resolver(String root, String user, String pwd) {
		HttpRepositoryArtifactDataResolver resolver = new HttpRepositoryArtifactDataResolver();
		resolver.setRoot( root);				
		resolver.setUserName(user);
		resolver.setPassword(pwd);
		resolver.setHttpClient(HttpClientFactory.get());
		return resolver;
	}
	
	private File resolve(ResolvingContext context) {
		HttpRepositoryArtifactDataResolver resolver = resolver(context.getRoot(), context.getUserName(), context.getPassword());
		resolver.setChecksumPolicy( context.getChecksumPolicy());
		
		Maybe<ArtifactDataResolution> resolved = resolver.resolvePart(context.getCi(), context.getPart());	
		ArtifactDataResolution resolution = resolved.get();
		assertTrue("no resolution returned", resolution != null);
		
		File file = new File( scratch, resolved.get().getResource().getName());
		try (OutputStream out = new FileOutputStream( file)) {			
			resolution.writeTo(out);		
		} catch (IOException e) {
			fail("cannot download file [" + buildUrl( context.getRoot(),context.getCi(), context.getPart()) + "]");
		}						
		return file;
	}
	
	/**
	 * @param bulkFile - the bulk file to read the hashes from 
	 * @return - a {@link Map} of hash-header-name and hash-value
	 * @throws IOException - if it can't be read
	 */
	protected Map<String, String> readBulkHashes(File bulkFile) throws IOException {
		Map<String,String> result = new HashMap<>();
		String bulkContents = IOTools.slurp(bulkFile, "US-ASCII");
		String [] hashes = bulkContents.split( "\n");
		for (String hash : hashes) {
			int pC = hash.indexOf(':');
			result.put( hash.substring(0, pC), hash.substring( pC+1).trim());			
		}		
		return result;
	}
	
	protected Map<String, String> createBulkHashes(File file) throws IOException {		
		Map<String,MessageDigest> digests = new HashMap<>();
		InputStream stream = new FileInputStream( file);
		try {
			for (Entry<String, Pair<String,String>> entry : RepoletCommons.hashAlgToHeaderKeyAndExtension.entrySet()) {
				try {
					MessageDigest digest = MessageDigest.getInstance( entry.getKey());
					DigestInputStream digestStream = new DigestInputStream(stream, digest);
					stream = digestStream;
					digests.put( entry.getValue().first, digest);
				} catch (NoSuchAlgorithmException e) {
					throw Exceptions.unchecked(e, "cannot produce hashes for file [" + file.getAbsolutePath() + "]", IllegalStateException::new);
				}		
			}
			IOTools.consume(stream);
			
			Map<String,String> hashes = new HashMap<>();
			for (Map.Entry<String, MessageDigest> entry : digests.entrySet()) {
				hashes.put( entry.getKey(), StringTools.toHex( entry.getValue().digest()));
			}		
			return hashes;
		}
		finally {
			IOTools.closeQuietly(stream);
		}
		
	}
	
	 
	/**
	 * checks whether the expected file has been downloaded
	 * @param resolved - the {@link File} retrieved
	 * @param ci - the {@link CompiledArtifactIdentification}
	 * @param pom - the {@link PartIdentification}
	 */
	private void validate(File resolved, ResolvingContext context) {
		assertTrue("no file downloaded", resolved != null && resolved.exists());
		String expectedPath = scratch.getAbsolutePath() + File.separator + ArtifactAddressBuilder.build().compiledArtifact(context.getCi()).part( context.getPart()).getFileName();
		String foundPath = resolved.getAbsolutePath();
		assertTrue("expected [" + expectedPath + "], found [" + foundPath + "]",  foundPath.equals(expectedPath));	
		
		/*
		// currently deactivated as the validator cannot know what was sent by the repolet (no access to headers)
		if (context.getChecksumPolicy() != ChecksumPolicy.ignore) {
			String org = ArtifactAddressBuilder.build().root( hashes.getAbsolutePath()).compiledArtifact(context.getCi()).part( context.getPart()).toPath().toFilePath();
			File blk = new File(org + ".blk");
			File md5 = new File(org + ".md5");
			String md5Hash = null;
			try {
				// extract 'remote' md5 hash, aka read it from repolet's folder 
				if (blk.exists()) {
						Map<String,String> bulkHashes = readBulkHashes(blk);
						md5Hash = bulkHashes.get( "X-Checksum-Md5");
										
				}
				else if (md5.exists()) {
					md5Hash = IOTools.slurp(md5, "US-ASCII");
				}
				else {
					fail("crc check is set to [" + context.getChecksumPolicy().toString() + "], but cannot find declaring hash");
					return;
				}
				
				MessageDigest md = MessageDigest.getInstance("MD5");
				try (InputStream in = new DigestInputStream( new FileInputStream( resolved), md)) {
					IOTools.consume(in);
				}
					
				// extract md5 hash from 'downloaded' file 				
				byte[] digest = md.digest();
				String foundMd5Hash = StringTools.toHex( digest);
				
				
				// compare the hashes 
				assertTrue("hashes do not match", md5Hash.equals(foundMd5Hash));
			
			} catch (Exception e) {
				Assert.fail("exception thrown while checking hashes:" + e);
			}
			
		}
		*/
	}

	/**
	 * ignore CRC, just download
	 */
	@Test
	public void testIgnoreCrc() {
		CompiledArtifactIdentification ci = CompiledArtifactIdentification.parse( GRP + ":terminal#1.0");
		PartIdentification pom = PartIdentifications.pom;
		ResolvingContext rc = new ResolvingContext( getRoot(), ci, pom, ChecksumPolicy.ignore);
		File resolved = resolve(rc);
				
		// validate
		validate( resolved, rc);
	}


	/**
	 * check CRC, use hashes in headers
	 */
	@Test
	public void testValidateCrcPerHeader() {
		CompiledArtifactIdentification ci = CompiledArtifactIdentification.parse( GRP + ":a#1.0.0");
		PartIdentification pom = PartIdentifications.pom;		
		ResolvingContext rc = new ResolvingContext( getRoot(), ci, pom, ChecksumPolicy.warn);
		File resolved = resolve(rc);
		// validate
		validate( resolved, rc);
	}
	/**
	 * check CRC, download (additional) hash file
	 */
	@Test	
	public void testValidateCrcPerFile() {	
		CompiledArtifactIdentification ci = CompiledArtifactIdentification.parse( GRP + ":b#1.0.0");
		PartIdentification pom = PartIdentifications.pom;				
		ResolvingContext rc = new ResolvingContext( getRoot(), ci, pom, ChecksumPolicy.warn);
		File resolved = resolve(rc);
		// validate
		validate( resolved, rc);
	}
	
	
	 
	
	//@Test
	@Category(KnownIssue.class)
	public void testValidateCrcPerFileForReal() {	
		String url = "https://artifactory.example.com/artifactory/core-dev";
		CompiledArtifactIdentification ci = CompiledArtifactIdentification.parse( "com.braintribe.devrock:malaclypse#1.0.80");
		PartIdentification pom = PartIdentifications.pom;		
		ResolvingContext rc = new ResolvingContext( url, ci, pom, ChecksumPolicy.warn, "diusername", "disissicret");
		rc.setIgnoreHashHeader(true);
		File resolved = resolve(rc);
		assertTrue("no file downloaded", resolved != null && resolved.exists());
	}
}
