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
package com.braintribe.artifacts.test.maven.metadata;

import java.io.File;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

import com.braintribe.build.artifact.representations.artifact.maven.metadata.MavenMetaDataCodec;
import com.braintribe.marshaller.maven.metadata.MavenMetaDataMarshaller;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.builder.LoadDocumentContext;
import com.braintribe.utils.xml.parser.builder.WriteDocumentContext;

@Category(KnownIssue.class)
public class MetadataCodecVsMarshallerTest {

	private static final String MAVEN_METADATA_XML = "parent.maven-metadata.1.xml";
	private static File contents = new File("res/maven/metadata");
	private static File input = new File( contents, "input");
	private static File output = new File (contents, "output");
	
	private MavenMetaDataMarshaller marshaller = new MavenMetaDataMarshaller();
	private MavenMetaDataCodec codec = new MavenMetaDataCodec();
	
	@BeforeClass 
	public static void beforeClass() {
		TestUtil.ensure(output);
	}
	
	private void runMarshallerReadTest( String name, int threshold, int repeat) {
		File in = new File( input, name);
		long sum = 0;
		for (int i=0; i < repeat; i++) {
			try {
				long before = System.nanoTime();
				marshaller.unmarshall(in);
				long after = System.nanoTime();
				if (i >= threshold) {
					sum += (after-before);
				}
			} catch (Exception e) {
				Assert.fail("cannot unmarshall metadata [" + e + "]");
				return;
			}
		}
		float average = sum / (float) (repeat-threshold);
		System.out.println( "reading [" + name + "] via marshaller averaged to " + (average / 1E6) + " in " + (repeat-threshold) + " runs");
	}
	
	private void runCodecReadTest( String name, int threshold, int repeat) {
		File in = new File( input, name);
		String fileName = in.getAbsolutePath();
		LoadDocumentContext loadDocumentContext = DomParser.load();
		long sum = 0;
		for (int i=0; i < repeat; i++) {
			try {
				long before = System.nanoTime();
				Document document = loadDocumentContext.fromFilename( fileName);
				codec.decode(document);
				long after = System.nanoTime();
				if (i >= threshold) {
					sum += (after-before);
				}
			} catch (Exception e) {
				Assert.fail("cannot unmarshall metadata [" + e + "]");
				return;
			}
		}
		float average = sum / (float) (repeat-threshold);
		System.out.println( "reading [" + name + "] via codec averaged to " + (average / 1E6) + " in " + (repeat-threshold) + " runs");
	}
	
	private void runMarshallerWriteTest( File in, MavenMetaData metadata, int threshold, int repeat) {		
		long sum = 0;
		for (int i=0; i < repeat; i++) {
			try {
				long before = System.nanoTime();
				marshaller.marshall(in, metadata);
				long after = System.nanoTime();
				if (i >= threshold) {
					sum += (after-before);
				}
			} catch (Exception e) {
				Assert.fail("cannot unmarshall metadata [" + e + "]");
				return;
			}
		}
		float average = (float) sum / (float) (repeat-threshold);
		System.out.println( "writing [" + in.getAbsolutePath() + "] via marshaller averaged to " + (average / 1E6) + " in " + (repeat-threshold) + " runs");
	}
	
	private void runCodecWriteTest( File out, MavenMetaData metadata, int threshold, int repeat) {
	
		WriteDocumentContext writeDocumentContext = DomParser.write();
		long sum = 0;
		for (int i=0; i < repeat; i++) {
			try {
				long before = System.nanoTime();
				Document document = codec.encode( metadata);
				writeDocumentContext.from( document).to( out);
				long after = System.nanoTime();
				if (i >= threshold) {
					sum += (after-before);
				}
			} catch (Exception e) {
				Assert.fail("cannot unmarshall metadata [" + e + "]");
				return;
			}
		}
		float average = (float) sum / (float) (repeat-threshold);
		System.out.println( "writing [" + out.getAbsolutePath() + "] via codec averaged to " + (average / 1E6) + " in " + (repeat-threshold) + " runs");
	}
	
	
	
	private void runReadComparisonUnversionedArtifact(int threshold, int repeat) {
		String name =MAVEN_METADATA_XML;
		runMarshallerReadTest(name, threshold, repeat);
		runCodecReadTest(name, threshold, repeat);
	}
	
	//@Test
	public void averageRead() {
		int threshold = 100;
		int repeat = 1100;
		runReadComparisonUnversionedArtifact(threshold, repeat);
	}
	
	//@Test
	public void singleRead() {
		int threshold = 0;
		int repeat = 1;
		runReadComparisonUnversionedArtifact(threshold, repeat);
	}
	
	private void runWriteComparisonUnversionedArtifact(int threshold, int repeat) {
		MavenMetaData metadata;
		try {
			metadata = marshaller.unmarshall( new File( input, MAVEN_METADATA_XML));
		} catch (XMLStreamException e) {
			Assert.fail("cannot read file "+ e);
			return;
		}
		
		runMarshallerWriteTest( new File(output, MAVEN_METADATA_XML), metadata, threshold, repeat);
		runCodecWriteTest(new File( output, MAVEN_METADATA_XML), metadata, threshold, repeat);
	}
	

	//@Test
	public void averageWrite() {
		int threshold = 100;
		int repeat = 1100;
		runWriteComparisonUnversionedArtifact(threshold, repeat);
	}
	
	//@Test
	public void singleWrite() {
		int threshold = 0;
		int repeat = 1;
		runWriteComparisonUnversionedArtifact(threshold, repeat);
	}
}
