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
package com.braintribe.marshaller.maven.metadata.test;

import java.io.File;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.marshaller.maven.metadata.MavenMetaDataMarshaller;
import com.braintribe.model.artifact.meta.MavenMetaData;

public class MavenMetaDataTest {

	private File contents = new File("res/parse");
	private File input = new File( contents, "input");
	private File output = new File( contents, "output");
	
	private MavenMetaDataMarshaller marshaller = new MavenMetaDataMarshaller();
	
	private void runRoundtripTest( String name) {
		File in = new File( input, name);
		
		File out = new File( output, name);
		output.mkdirs();
		
		
		MavenMetaData metaData;
		try {
			metaData = marshaller.unmarshall(in);
		} catch (Exception e) {
			Assert.fail("cannot unmarshall metadata [" + e + "]");
			return;
		}
		
		try {
			marshaller.marshall(out, metaData);
		} catch (XMLStreamException e) {
			Assert.fail("cannot marshall metadata [" + e + "]");
			return;
		}
	}
	
	private void runReadTest( String name, int threshold, int repeat) {
		File in = new File( input, name);
		long sum = 0;
		for (int i=0; i < repeat; i++) {
			try {
				long before = System.nanoTime();
				marshaller.unmarshall(in);
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
		System.out.println( "reading [" + name + "] averaged to " + (average / 1E6));
	}
	
	@Test
	public void versionedArtifactTest() {
		runRoundtripTest( "maven-metadata.1.xml");
	}
	@Test
	public void versionedSnapshotTest() {
		runRoundtripTest( "maven-metadata.2.xml");
	}
	@Test
	public void unversionedArtifactTest() {
		runRoundtripTest( "parent.maven-metadata.1.xml");
	}
	
	@Test
	public void unversionedReleaseArtifactTest() {
		runRoundtripTest( "parent.maven-metadata.2.xml");
	}
	
	@Test
	public void unversionedArtifactPerformanceTest() {
		runReadTest( "parent.maven-metadata.1.xml", 100, 1100);
	}
	
	@Test
	public void modelledVersionMavendata() {
		runRoundtripTest( "maven-metadata.versioned.xml");
	}
	
	@Test
	public void commentedVersionMavendata() {
		runRoundtripTest( "commented.maven-metadata.versioned.xml");
	}
	
	
}
