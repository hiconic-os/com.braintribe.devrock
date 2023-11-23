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
package com.braintribe.artifacts.test.maven.pom;

import java.io.File;
import java.util.UUID;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader.CodecStyle;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.model.artifact.Solution;
import com.braintribe.testing.category.KnownIssue;

@Category(KnownIssue.class)
public class PomPerformanceLab extends AbstractPomReaderLab {
	private static final String MALACLYPSE_POM = "com/braintribe/devrock/malaclypse/1.0/malaclypse-3.0.pom";
	private static final String TRIBEFIRE_SERVICES_DEPS_POM = "com/braintribe/product/tribefire/TribefireServicesDeps/2.0/TribefireServicesDeps-2.0.pom";
	private static String home = System.getenv( "M2_REPO");
	private static File homeDir = new File( home);
	
	@BeforeClass
	public static void runBefore  () {
		runbefore();		
	}
	
	private Solution read( File testPomFile, CodecStyle codecStyle, int num, int threshold) {
		ArtifactPomReader reader = pomExpertFactory.getReader();
		reader.setCodecStyle(codecStyle);
		Solution solution = null;
		long sum = 0;
		
		try {
			for (int i = 0; i < num; i++) {
				long before = System.nanoTime();
				solution = reader.readPom( UUID.randomUUID().toString(), testPomFile);
				long after = System.nanoTime();
				
				long dif = after - before;
				if (num >= threshold) {
					sum = sum + dif;
				}				
			}
		} catch (PomReaderException e) {
			Assert.fail("exception thrown :" + e.getMessage());
			return null;
		}
		long runs = (num-threshold);
		double time = ((double) sum) / (runs == 0 ? 1 : runs);
		System.out.println( codecStyle.toString() + ": took [" + time / 1E6 + "] ms averaged over [" + (num-threshold) + "] reads for [" + testPomFile.getName() + "]");
		return solution;
	}
	
	//@Test
	public void averageStaxMC() {		
		File pom = new File( homeDir, MALACLYPSE_POM);
		read( pom, CodecStyle.stax, 1100, 100);
	}
	//@Test
	public void averageSaxMC() {		
		File pom = new File( homeDir, MALACLYPSE_POM);
		read( pom, CodecStyle.sax, 1100, 100);
	}
	//@Test
	public void averageStagedMC() {		
		File pom = new File( homeDir, MALACLYPSE_POM);
		read( pom, CodecStyle.staged, 1100, 100);
	}

	//@Test
	public void averageMarshalledMC() {		
		File pom = new File( homeDir, MALACLYPSE_POM);
		read( pom, CodecStyle.marshall, 1100, 100);
	}
	
	
	//@Test
	public void singleStaxMC() {		
		File pom = new File( homeDir, MALACLYPSE_POM);
		read( pom, CodecStyle.stax, 1, 1);
	}
	
	//@Test
	public void singleStagedMC() {		
		File pom = new File( homeDir, MALACLYPSE_POM);
		read( pom, CodecStyle.staged, 1, 1);
	}
	
	//@Test
	public void singleMarshalledMC() {		
		File pom = new File( homeDir, MALACLYPSE_POM);
		read( pom, CodecStyle.marshall, 1, 1);
	}
	
	/* TF */
	
	//@Test
	public void averageSaxTF() {		
		File pom = new File( homeDir, TRIBEFIRE_SERVICES_DEPS_POM);
		read( pom, CodecStyle.sax, 1100, 100);
	}
	
	//@Test
	public void averageStaxTF() {		
		File pom = new File( homeDir, TRIBEFIRE_SERVICES_DEPS_POM);
		read( pom, CodecStyle.stax, 1100, 100);
	}
	
	//@Test
	public void averageStagedTF() {		
		File pom = new File( homeDir, TRIBEFIRE_SERVICES_DEPS_POM);
		read( pom, CodecStyle.staged, 1100, 100);
	}
	
	//@Test
	public void averageMarshalledTF() {		
		File pom = new File( homeDir, TRIBEFIRE_SERVICES_DEPS_POM);
		read( pom, CodecStyle.marshall, 1100, 100);
	}
	
	//@Test
	public void singleSaxTF() {		
		File pom = new File( homeDir, TRIBEFIRE_SERVICES_DEPS_POM);
		read( pom, CodecStyle.sax, 1, 1);
	}
	
	
	//@Test
	public void singleStaxTF() {		
		File pom = new File( homeDir, TRIBEFIRE_SERVICES_DEPS_POM);
		read( pom, CodecStyle.stax, 1, 1);
	}
	
	//@Test
	public void singleStagedTF() {		
		File pom = new File( homeDir, TRIBEFIRE_SERVICES_DEPS_POM);
		read( pom, CodecStyle.staged, 1, 1);
	}
	//@Test
	public void xsingleMarshallTF() {		
		File pom = new File( homeDir, TRIBEFIRE_SERVICES_DEPS_POM);
		read( pom, CodecStyle.marshall, 1, 1);
	}
	

}
