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
package com.braintribe.test.multi;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.coding.IdentificationWrapperCodec;
import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.PartType;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.utils.IOTools;

public class WalkResultValidationExpert {
	
	public static boolean listDiscrepancies( Collection<Solution> result, String [] expected) {
		boolean retval = true;
		Map<String, String> resultMap = new HashMap<String, String>();
		Map<String, Solution> nameToSolutionMap = new HashMap<String, Solution>();
		Map<Identification, Solution> identificationToSolutionMap = CodingMap.createHashMapBased( new IdentificationWrapperCodec());
		for (Solution solution : result) {			
			resultMap.put( solution.getGroupId() + ":" + solution.getArtifactId(), VersionProcessor.toString(solution.getVersion()));
			nameToSolutionMap.put( solution.getGroupId() + ":" + solution.getArtifactId() +"#" + VersionProcessor.toString(solution.getVersion()), solution);
			identificationToSolutionMap.put( solution, solution);
		}
		Map<String, String> expectedMap = new HashMap<String, String>();
		for (String str : expected) {
			if (str.startsWith(";"))
				continue;
			int p = str.indexOf('#');
			expectedMap.put( str.substring(0,p), str.substring(p+1));
		}
		Map<String, Boolean> checkedMap = new HashMap<String, Boolean>();
		for (Entry<String, String> entry : resultMap.entrySet()) {
			String expectedVersion = expectedMap.get( entry.getKey());
			if (expectedVersion == null) {
				System.out.println("Unexpected artifact : " + entry.getKey() + "#" + entry.getValue());
				retval = false;
				// list the dependency .. 
				Solution solution = nameToSolutionMap.get( entry.getKey() + "#" + entry.getValue());
				printPath( solution, 1);
				

			}
			else {
				if (!expectedVersion.equalsIgnoreCase( entry.getValue())) {
					System.out.println("Unexpected version : expected for [" + entry.getKey() + "] is [" + expectedVersion +"], retrieved is [" + entry.getValue() + "]");
					retval = false;
				}
				checkedMap.put( entry.getKey() + "#" + expectedVersion, true);
			}
		}
		//
		for (String str : expected) {
			if (str.startsWith(";"))
				continue;
			
			if (!Boolean.TRUE.equals(checkedMap.get(str))) {				
				retval = false;
				Identification identification = Identification.T.create();
				int g = str.indexOf(":");
				identification.setGroupId( str.substring(0, g));
				identification.setArtifactId( str.substring( g+1, str.indexOf('#')));
				Solution discrepancy = identificationToSolutionMap.get( identification);
				if (discrepancy != null) {
					System.out.println("Artifact [" + str +"] expected, but [" + NameParser.buildName(discrepancy) + "] retrieved");
				}
				else {
					System.out.println("Artifact [" + str +"] expected, but not retrieved");	
				}
			}
		}					
		return retval;
	}
	
	public static void printPath( Solution solution, int index) {
		for (Dependency dependency : solution.getRequestors()) {
			Set<Artifact> requesters = dependency.getRequestors();
			String offset = "";
			for (int i = 0; i < index; i++) {
				offset+="\t";
			}
			int i = 0;
			for (Artifact artifact : requesters) {
				i++;			
				System.out.println(offset+ i + ": " + NameParser.buildName(artifact));				
				printPath( (Solution) artifact, index+1);
			}			
		}
	}
	
	public static String [] loadExpectedNamesFromFile( File file) {
		InputStream stream = null;
		try {
			stream = new FileInputStream(file);
			String contents = IOTools.slurp(stream, "UTF-8");
			String [] cnt = contents.split("\r\n");
			return cnt;
		} catch (Exception e) {
			Assert.fail("cannot load file [" + file.getAbsolutePath() + "]");
			return null;
		}
		finally {
			if (stream != null)
				IOTools.closeQuietly(stream);
		}		
	}
	

	public static void testPresence( Collection<Solution> solutions, File repository, PartTuple ... ignore) {
		boolean missing = false;		
		PartTuple javadocTuple = PartTupleProcessor.create(PartType.JAVADOC);
		for (Solution solution : solutions) {
			for (Part part : solution.getParts()) {
				
				if (PartTupleProcessor.equals(javadocTuple, part.getType())) {
					continue;
				}
				String partLocation = part.getLocation();
				if (partLocation != null) {
					File location = new File( partLocation);
					if (!location.exists()) {
						System.out.println("File [" + location.getAbsolutePath() + "] doesn't exist");
						missing = true;
					}
				}
				else {				
					System.out.println("Part [" + NameParser.buildName(part) + "] has no location ");			
					missing = true;
				}
			}
		}
		if (missing) {
			Assert.fail("at least one part is missing");
		}
	}

	
}
