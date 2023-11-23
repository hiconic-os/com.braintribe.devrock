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
package com.braintribe.artifacts.test.maven.metadata;

import java.io.File;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.name.NameParserException;
import com.braintribe.build.artifact.representations.artifact.maven.metadata.MavenMetaDataCodec;
import com.braintribe.build.artifact.representations.artifact.maven.metadata.MavenMetaDataProcessor;
import com.braintribe.codec.CodecException;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

@Category(KnownIssue.class)
public class MetaDataLab {
	private static final String input = "res/maven/metadata/input";
	private static final String output = "res/maven/metadata/output";

	@BeforeClass
	public static void prepare() {
		TestUtil.ensure( output);
	}
	
	private void readWriteLab(File in, File out) {
		String inName = in.getAbsolutePath();
		Document inDocument;
		try {
			inDocument = DomParser.load().from( in);
		} catch (DomParserException e1) {
			Assert.fail("cannot load [" + inName + "]");
			return;
		}
		MavenMetaDataCodec codec = new MavenMetaDataCodec();
		MavenMetaData metadata;
		try {
			metadata = codec.decode(inDocument);
		} catch (CodecException e) {
			Assert.fail("cannot decode [" + inName + "]");
			return;
		}
		
		Document outDocument;
		try {
			outDocument = codec.encode( metadata);
		} catch (CodecException e) {
			Assert.fail("cannot re-encode [" + inName + "]");
			return;
		}
		try {
			DomParser.write().from(outDocument).to( out);
		} catch (DomParserException e) {
			Assert.fail("cannot write [" + out.getAbsolutePath() + "]");
			return;
		}			
	}
	
	private void processorAddSolutionTest( File in, String solutionAsString, File out) {
		MavenMetaDataCodec codec = new MavenMetaDataCodec();
		MavenMetaData metaData = null;
		if (in != null) {
			try {
				Document inDocument = DomParser.load().from(in);
				
				metaData = codec.decode(inDocument);
			} catch (DomParserException e) {
				Assert.fail("cannot load [" + in.getAbsolutePath() + "]");
				return;
			} catch (CodecException e) {
				Assert.fail("cannot decode [" + in.getAbsolutePath() + "]");
				return;
			}
		}
		Solution solution;
		try {
			Artifact artifact = NameParser.parseCondensedArtifactName(solutionAsString);
			solution = Solution.T.create();
			ArtifactProcessor.transferIdentification(solution, artifact);
		} catch (NameParserException e) {
			Assert.fail("cannot create solution from [" + solutionAsString + "]");
			return;
		}
		
		try {
			metaData = MavenMetaDataProcessor.addSolution(metaData, solution);
		} catch (CodecException e) {
			Assert.fail("cannot add solution [" + solutionAsString + "] to MavenMetaData");
			return;
		}
		
		try {
			Document outDocument = codec.encode(metaData);
			DomParser.write().from(outDocument).to(out);
		} catch (CodecException e) {
			Assert.fail("cannot encode MavenMetaData to document");
			return;
		} catch (DomParserException e) {
			Assert.fail("cannot write metadata to  [" + out.getAbsolutePath() + "]");
			return;
		}						
	}
	
	private void processorCreateSolutionTest( String solutionAsString, File out) {
		
		Solution solution;
		try {
			Artifact artifact = NameParser.parseCondensedArtifactName(solutionAsString);
			solution = Solution.T.create();
			ArtifactProcessor.transferIdentification(solution, artifact);
		} catch (NameParserException e) {
			Assert.fail("cannot create solution from [" + solutionAsString + "]");
			return;
		}
		
		MavenMetaData metaData = null;
		try {
			metaData = MavenMetaDataProcessor.createMetaData( solution);
		} catch (CodecException e) {
			Assert.fail("cannot add solution [" + solutionAsString + "] to MavenMetaData");
			return;
		}
		
		try {
			MavenMetaDataCodec codec = new MavenMetaDataCodec();
			Document outDocument = codec.encode(metaData);
			DomParser.write().from(outDocument).to(out);
		} catch (CodecException e) {
			Assert.fail("cannot encode MavenMetaData to document");
			return;
		} catch (DomParserException e) {
			Assert.fail("cannot write metadata to  [" + out.getAbsolutePath() + "]");
			return;
		}				
		
	}
	
	//@Test
	public void runReadWrites() {
		readWriteLab( new File( input, "parent.maven-metadata.1.xml"), new File( output, "parent.maven-metadata.1.xml"));
		readWriteLab( new File( input, "parent.maven-metadata.2.xml"), new File( output, "parent.maven-metadata.2.xml"));
		
		readWriteLab( new File( input, "maven-metadata.1.xml"), new File( output, "maven-metadata.1.xml"));
		readWriteLab( new File( input, "maven-metadata.2.xml"), new File( output, "maven-metadata.2.xml"));
	}

	@Test
	public void runProcessorCreateSolutionTests() {
		String solution1 ="com.braintribe:TestSolution#5.0";
		String solution2 ="com.braintribe:TestSolution#5.0.1";
		processorCreateSolutionTest(solution1, new File(output, "maven-metadata.f.1.xml"));
		processorCreateSolutionTest(solution2, new File(output, "maven-metadata.f.2.xml"));
		
	}
	
	@Test
	public void runProcessorAddSolutionTests() {
		String solution0 ="com.braintribe:TestSolution#5.0";
		String solution1 ="com.braintribe:TestSolution#5.1.0";
		String solution2 ="com.braintribe:TestSolution#5.1.1";
		processorAddSolutionTest( null, solution0, new File( output, "parent.maven-metadata.f.0.xml"));		
		processorAddSolutionTest( new File( output, "parent.maven-metadata.f.0.xml"), solution1, new File( output, "parent.maven-metadata.f.1.xml"));
		processorAddSolutionTest( new File( output, "parent.maven-metadata.f.1.xml"), solution1, new File( output, "parent.maven-metadata.f.1b.xml"));
		processorAddSolutionTest( new File( output, "parent.maven-metadata.f.1.xml"), solution2, new File( output, "parent.maven-metadata.f.2.xml"));		
	}

}
