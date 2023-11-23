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
package com.braintribe.marshaller.maven.metadata.test;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;

import com.braintribe.marshaller.artifact.maven.metadata.DeclaredMavenMetaDataMarshaller;
import com.braintribe.model.artifact.maven.meta.MavenMetaData;
import com.braintribe.model.artifact.maven.meta.Snapshot;
import com.braintribe.model.artifact.maven.meta.SnapshotVersion;
import com.braintribe.model.artifact.maven.meta.Versioning;
import com.braintribe.model.version.Version;

/**
 * tests for the declared-maven-metadata-marshaller.. <br/>
 * files are read from disk, and written back out. Each value read is compared to the expected value
 * of such a file.<br/>
 * speed test on read. 
 * 
 * @author pit
 *
 */
public class MavenMetaDataTest {
	protected static DateTimeFormatter timeFormat = DateTimeFormat.forPattern("yyyyMMddHHmmss");
	protected static DateTimeFormatter altTimeFormat = DateTimeFormat.forPattern("yyyyMMdd.HHmmss");

	private File contents = new File("res/parse");
	private File input = new File( contents, "input");
	private File output = new File( contents, "output");
	
	private DeclaredMavenMetaDataMarshaller marshaller = new DeclaredMavenMetaDataMarshaller();
	
	/**
	 * runs a roundtrip test : read, validate, write 
	 * @param name - the name of the file 
	 * @param expected - a {@link MavenMetaData} instance as expected (or null for no validation)
	 */
	private void runRoundtripTest( String name, MavenMetaData expected) {
		File in = new File( input, name);
		
		File out = new File( output, name);
		output.mkdirs();
		
		
		MavenMetaData metaData;
		try (InputStream instream = new FileInputStream(in)){
			metaData = (MavenMetaData) marshaller.unmarshall(instream);
		} catch (Exception e) {
			Assert.fail("cannot unmarshall metadata [" + e + "]");
			return;
		}
		
		// validate read 
		if (expected != null) {
			MavenMetadataValidator.validate(metaData, expected);
		}
		
		try (OutputStream outstream = new FileOutputStream(out)){
			marshaller.marshall(outstream, metaData);
		} catch (Exception e) {
			Assert.fail("cannot marshall metadata [" + e + "]");
			return;
		}
		
		// validate write ? 
	}
	
	/**
	 * performance speed test 
	 * @param name - the name of the file
	 * @param threshold - the grace period (to let Java's JIT to optimize)
	 * @param repeat - number of repeats to get the average 
	 */
	private void runReadTest( String name, int threshold, int repeat) {
		File in = new File( input, name);
		long sum = 0;
		for (int i=0; i < repeat; i++) {
			try (InputStream instream = new FileInputStream(in)){
				long before = System.nanoTime();
				marshaller.unmarshall(instream);
				long after = System.nanoTime();
				if (i > threshold) {
					sum += (after-before);
				}
			} catch (Exception e) {
				Assert.fail("cannot unmarshall metadata [" + e + "]");
				return;
			}
		}
		float average = sum / (float) (repeat-threshold);
		System.out.println( "reading [" + name + "] averaged to " + (average / 1E6) + " ms");
	}
	
	/*
	 * actual tests below  
	 */
	
	@Test
	public void versionedArtifactTest() {
		MavenMetaData md = MavenMetaData.T.create();
		md.setGroupId("com.braintribe");
		md.setArtifactId( "FilterApi");
		md.setVersion( Version.parse("1.1"));

		runRoundtripTest( "maven-metadata.1.xml", md);
						
	}
	@Test
	public void versionedSnapshotTest() {
		MavenMetaData md = MavenMetaData.T.create();
		md.setGroupId("com.braintribe.model");
		md.setArtifactId( "WorkbenchModel");
		md.setVersion( Version.parse("1.0-SNAPSHOT"));
		Versioning vers = Versioning.T.create();
		vers.setLastUpdated( "20161220105848");
		
		Snapshot snapshot = Snapshot.T.create();
		snapshot.setTimestamp( "20161220.105848");
		snapshot.setBuildNumber(901454234);
		vers.setSnapshot(snapshot);
		
		SnapshotVersion sv1 = SnapshotVersion.T.create();
		sv1.setExtension("jar");
		sv1.setValue("1.0-20161220.105848-901454234");
		sv1.setUpdated( "20161220105848");
				
		vers.getSnapshotVersions().add(sv1);
		
		md.setVersioning(vers);
		
		
		runRoundtripTest( "maven-metadata.2.xml", md);
	}
	@Test
	public void unversionedArtifactTest() {
		MavenMetaData md = MavenMetaData.T.create();
		md.setGroupId("com.braintribe");
		md.setArtifactId( "FilterApi");
		Versioning vers = Versioning.T.create();
		vers.setLatest( Version.parse( "1.1"));
		vers.setRelease( Version.parse( "1.1"));		
		md.setVersioning(vers);
		
		vers.getVersions().add( Version.parse( "1.0"));
		vers.getVersions().add( Version.parse( "1.1"));
		vers.setLastUpdated( "20151119113048");
		
	
		
		runRoundtripTest( "parent.maven-metadata.1.xml", md);
	}
	
	@Test
	public void unversionedReleaseArtifactTest() {
		MavenMetaData md = MavenMetaData.T.create();
		md.setGroupId("org.springframework");
		md.setArtifactId( "spring-context");
		Versioning vers = Versioning.T.create();
		vers.setLatest( Version.parse( "4.1.6.RELEASE"));
		vers.setRelease( Version.parse( "4.1.6.RELEASE"));		
		md.setVersioning(vers);
		
		vers.getVersions().add( Version.parse( "2.5.4"));
		vers.getVersions().add( Version.parse( "2.5.5"));
		vers.getVersions().add( Version.parse( "2.5.6"));
		
		vers.getVersions().add( Version.parse( "3.0.3.RELEASE"));
		vers.getVersions().add( Version.parse( "3.0.5.RELEASE"));
		vers.getVersions().add( Version.parse( "3.0.7.RELEASE"));
		
		vers.getVersions().add( Version.parse( "3.2.0.RELEASE"));
		vers.getVersions().add( Version.parse( "3.2.4.RELEASE"));
		vers.getVersions().add( Version.parse( "3.2.12.RELEASE"));
		
		vers.getVersions().add( Version.parse( "4.1.6.RELEASE"));
		
		vers.setLastUpdated( "20151013044115");
		runRoundtripTest( "parent.maven-metadata.2.xml", md);
	}
	
	@Test
	public void unversionedArtifactPerformanceTest() {
		runReadTest( "parent.maven-metadata.1.xml", 100, 1100);
	}
	
	@Test
	public void modelledVersionMavendata() {
		MavenMetaData md = MavenMetaData.T.create();
		md.setGroupId("com.braintribe.model");
		md.setArtifactId( "ResourceModel");
		md.setVersion(Version.parse("3.1.0.7"));
		Versioning vers = Versioning.T.create();
		vers.setLatest( Version.parse( "3.1.0.7"));
		vers.setRelease( Version.parse( "3.1.0.7"));		
		md.setVersioning(vers);
		
		vers.getVersions().add( Version.parse( "3.1"));
		vers.getVersions().add( Version.parse( "3.1.0.7"));
		vers.setLastUpdated( "20180320215539");
		
		runRoundtripTest( "maven-metadata.versioned.xml", md);
	}
	
	@Test
	public void runSnaphotReadTest() {
		runReadTest("remote-snapshot-maven-metadata.xml", 1, 1);
	}
	
	
}
