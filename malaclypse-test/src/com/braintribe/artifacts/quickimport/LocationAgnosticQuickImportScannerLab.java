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
package com.braintribe.artifacts.quickimport;

import java.io.File;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.experimental.categories.Category;

import com.braintribe.build.quickscan.agnostic.LocationAgnosticQuickImportScanner;
import com.braintribe.build.quickscan.notification.QuickImportScanPhaseListener;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.testing.category.KnownIssue;
@Category(KnownIssue.class)
public class LocationAgnosticQuickImportScannerLab {
	private static final File groups = new File( "res/quickScan/order");
	private static final File group0 = new File(groups, "com/braintribe/group0");
	private static final File group1 = new File(groups, "com/braintribe/group1");
	private static final File group2 = new File(groups, "com/braintribe/group2");
	private static final File group3 = new File(groups, "com/braintribe/group3");
	private static final File group4 = new File(groups, "com/braintribe/group4");
	private static final File group5 = new File(groups, "com/braintribe/group5");
	
	private static final File challenges = new File("res/quickScan/challenges");
	private static final File variables = new File( challenges, "com/braintribe/test/variables");
	
	
	private static final File root = new File( System.getenv( "BT__ARTIFACTS_HOME"));
	
	public class ScanListener implements QuickImportScanPhaseListener {

		@Override
		public void acknowledgeEnumerationPhase() {
			System.out.println( "enumeration phase");
			
		}

		@Override
		public void acknowledgeScanPhase(int phase, int remaining) {
			System.out.println( "new phase [" + phase + "]: [" + remaining + "] tuples remaining");
			
		}

		@Override
		public void acknowledgeDetected(String file) {
			System.out.println( "detected : " + file);
			
		}

		@Override
		public void acknowledgeScanError(String msg, String file) {
			System.out.println( "error : [" + msg + "] in [" + file + "]");
			
		}

		@Override
		public void acknowledgeResolved(SourceArtifact sourceArtifact) {
			System.out.println( "resolved : " + sourceArtifact.getGroupId() + ":" + sourceArtifact.getArtifactId() + "#" + sourceArtifact.getVersion());			
		}

		@Override
		public void acknowledgeUnresolved(int phases, String file) {
			System.out.println( "remained unresolved after [" + phases + "] phases : [" + file + "]");
		}				
	}

	private SourceRepository generateDefaultSourceRepository() {
		SourceRepository sourceRepository = SourceRepository.T.create();
		sourceRepository.setName( "Local SVN working copy");
		sourceRepository.setRepoUrl("file:/" + System.getenv( "BT__ARTIFACTS_HOME"));

		return sourceRepository;
	}

	/**
	 * actual test runner
	 * @param scanRoot - the {@link File} that points to the directory to enumerate
	 * @return - a {@link List} of {@link SourceArtifact} that were found
	 */
	public List<SourceArtifact> testEnhancedScan(SourceRepository sourceRepository, File scanRoot) {
		// 
		List<SourceArtifact> sourceArtifacts;
		LocationAgnosticQuickImportScanner scanner = new LocationAgnosticQuickImportScanner();		
		scanner.setSourceRepository( sourceRepository);
		//scanner.addListener( new ScanListener());

		try {			
			sourceArtifacts = scanner.scanLocalWorkingCopy( scanRoot.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception [" + e + "] thrown");
			return null;
		}
		
		Assert.assertTrue("no results found", sourceArtifacts.size() > 0);
		/*
		for (SourceArtifact src : sourceArtifacts) {
			System.out.println( src.getGroupId() + ":" + src.getArtifactId() + "#" + src.getVersion() + "->" + scanner.getPomFile( src).getAbsolutePath() + "," + scanner.getProjectFile(src).getAbsolutePath());
		}
		*/
		return sourceArtifacts;
		 	
	}
	
	/**
	 * verify if all expected artifacts were found 
	 * @param sourceArtifacts - the result of the scan, a {@link List} of {@link SourceArtifact}
	 * @param condensedNames - an {@link Array} of {@link String} with the expected condensed names 
	 */
	private boolean verifyScanResult( SourceRepository sourceRepository, List<SourceArtifact> sourceArtifacts, Set<String> condensedNames) {
		
		List<String> result = sourceArtifacts.stream().map( p -> p.getGroupId() + ":" + p.getArtifactId() + "#" + p.getVersion()).collect( Collectors.toList());
		
		Set<String> found = new HashSet<String>();
		Set<String> notFound = new HashSet<String>();
		
		for (String name : condensedNames) {
			if (result.contains(name)) {
				found.add( name);
			}
			else {
				notFound.add( name);
			}
		}
		boolean retval = true;
		if (notFound.size() > 0) {
			System.out.println("not found:");
			for (String nf : notFound) {
				System.out.println( "\t" + nf);
			}
			retval = false;
		}
		if (result.size() > found.size()) {
			System.out.println("not expected :");
			for (String name : result) {
				if (!found.contains( name)) {
					System.out.println("\t" + name);
				}
			}
			retval = false;
		}
		Assert.assertTrue( "some names not found", notFound.size() == 0);
		Assert.assertTrue( "more found than expected", result.size() == found.size());
		
		return retval;
		
	}
	
	/**
	 * verifies that the source artifacts were linked to the appropriate project file 
	 * <br/>does this be reading the project's name in the .project file, so make sure the names are correct
	 * @param sourceArtifacts - the result of the scan, a {@link List} of {@link SourceArtifact}
	 */
	private boolean verifyScanAssociation( SourceRepository sourceRepository, List<SourceArtifact> sourceArtifacts, Map<String, File> expectedProjectFiles) {		
		// check association with .project file. 
		Map<String, File> result = new HashMap<String, File>();
		
		sourceArtifacts.forEach( s -> {
			String value = s.getPath();
			try {
				URL url = new URL( sourceRepository.getRepoUrl() + "/" + value);
				File file = new File( url.getFile());
				result.put( s.getGroupId() + ":" + s.getArtifactId() + "#" + s.getVersion(), file);
			} catch (MalformedURLException e) {				
				e.printStackTrace();
			}
		});
		boolean retval = true;
		
		for (Entry<String, File> entry : result.entrySet()) {
			File expected = expectedProjectFiles.get(entry.getKey());
			if (expected == null) {
				Assert.fail( "no file found for key [" + entry.getKey() + "]");
				retval = false;
				continue;
			}
			if (!entry.getValue().getAbsolutePath().equalsIgnoreCase( expected.getAbsolutePath())) {
				Assert.fail("[" + expected.getAbsolutePath() + "] expected, yet found [" + entry.getValue().getAbsolutePath() +"]");
				retval = false;
			}
		
		}
		
		return retval;
		
		
	}
	
	//@Test
	public void rootTest() {			
		testEnhancedScan( generateDefaultSourceRepository(), root);
	}
	
	//@Test
	public void fullTest() {			
		testEnhancedScan( generateDefaultSourceRepository(), groups);
	}
	
	//@Test
	public void testGroup0() {
		SourceRepository sourceRepository = generateDefaultSourceRepository();
		List<SourceArtifact> result = testEnhancedScan( sourceRepository, group0);

		Map<String, File> expected = new HashMap<>();
		expected.put("com.braintribe.group0:Model#1.0.1-PC", new File(group0, "1.0/Model"));
		expected.put("com.braintribe.group0:Parent#1.0.1-PC", new File(group0, "1.0/Parent"));
		expected.put("com.braintribe.group0:Processor#1.0.1-PC", new File(group0, "1.0/Processor"));
		verifyScanResult(sourceRepository, result, expected.keySet());
		verifyScanAssociation(sourceRepository, result, expected);
	}
	//@Test
	public void testGroup1() {		
		SourceRepository sourceRepository = generateDefaultSourceRepository();
		List<SourceArtifact> result = testEnhancedScan( sourceRepository, group1);
		Map<String, File> expected = new HashMap<>();
		expected.put("com.braintribe.group1:Model#1.0.1-PC", new File(group1, "1.0/Model"));
		expected.put("com.braintribe.group1:Parent#1.0.1-PC", new File(group1, "1.0/Parent"));
		expected.put("com.braintribe.group1:Processor#1.0.1-PC", new File(group1, "1.0/Processor"));
		verifyScanResult(sourceRepository, result, expected.keySet());
		verifyScanAssociation(sourceRepository, result, expected);
	}
	//@Test
	public void testGroup2() {			
		SourceRepository sourceRepository = generateDefaultSourceRepository();
		List<SourceArtifact> result = testEnhancedScan( sourceRepository, group2);
		Map<String, File> expected = new HashMap<>();
		expected.put("com.braintribe.group2:Model#1.0.1-PC", new File(group2, "1.0/Model"));
		expected.put("com.braintribe.group2:Parent#1.0.1-PC", new File(group2, "1.0/Parent"));
		expected.put("com.braintribe.group2:Processor#1.0.1-PC", new File(group2, "1.0/Processor"));
		verifyScanResult(sourceRepository, result, expected.keySet());
		verifyScanAssociation(sourceRepository, result, expected);
	}
	//@Test
	public void testGroup3() {			
		SourceRepository sourceRepository = generateDefaultSourceRepository();
		List<SourceArtifact> result = testEnhancedScan( sourceRepository, group3);
		Map<String, File> expected = new HashMap<>();
		expected.put("com.braintribe.group3:Model#1.0.1-PC", new File(group3, "1.0/Model"));
		expected.put("com.braintribe.group3:GrandParent#1.0.1-PC", new File(group3, "1.0/GrandParent"));
		expected.put("com.braintribe.group3:Parent#1.0.1-PC", new File(group3, "1.0/Parent"));
		expected.put("com.braintribe.group3:Processor#1.0.1-PC", new File(group3, "1.0/Processor"));
		verifyScanResult(sourceRepository, result, expected.keySet());
		verifyScanAssociation(sourceRepository, result, expected);
	}
	//@Test
	public void testGroup4() {			
		SourceRepository sourceRepository = generateDefaultSourceRepository();
		List<SourceArtifact> result = testEnhancedScan( sourceRepository, group4);
		Map<String, File> expected = new HashMap<>();
		expected.put("com.braintribe.group4:Model#1.0.1-PC", new File(group4, "1.0/Model"));
		expected.put("com.braintribe.group4:Parent#1.0.1-PC", new File(group4, "1.0/Parent"));
		expected.put("com.braintribe.group4:Processor#1.0.1-PC", new File(group4, "1.0/Processor"));
		expected.put("com.braintribe.group4:Model#1.1.1-PC", new File(group4, "1.1/Model"));
		expected.put("com.braintribe.group4:Parent#1.1.1-PC", new File(group4, "1.1/Parent"));
		expected.put("com.braintribe.group4:Processor#1.1.1-PC", new File(group4, "1.1/Processor"));
		verifyScanResult(sourceRepository, result, expected.keySet());
		verifyScanAssociation(sourceRepository, result, expected);				
	}
	//@Test
	public void testGroup5() {			
		SourceRepository sourceRepository = generateDefaultSourceRepository();
		List<SourceArtifact> result = testEnhancedScan( sourceRepository, group5);		
		Map<String, File> expected = new HashMap<>();
		expected.put("com.braintribe.group5.model:Model#1.0.1-PC", new File(group5, "1.0/Model"));
		expected.put("com.braintribe.group5:Parent#1.0.1-PC", new File(group5, "1.0/Parent"));
		expected.put("com.braintribe.group5.processing:Processor#1.0.1-PC", new File(group5, "1.0/Processor"));
		verifyScanResult( sourceRepository, result, expected.keySet());
		verifyScanAssociation( sourceRepository, result, expected);
	}


	//@Test
	public void runChallenge1() {
		SourceRepository sourceRepository = generateDefaultSourceRepository();
		List<SourceArtifact> result = testEnhancedScan( sourceRepository, variables);		
		Map<String, File> expected = new HashMap<>();
		expected.put("com.braintribe.test.variables:Model#1.0.1-PC", new File(variables, "1.0/Model"));
		expected.put("com.braintribe.test.variables:Parent#1.0.1-PC", new File(variables, "1.0/Parent"));
		expected.put("com.braintribe.test.variables:Processor#1.0.1-PC", new File(variables, "1.0/Processor"));
		verifyScanResult( sourceRepository, result, expected.keySet());
		verifyScanAssociation( sourceRepository, result, expected);
	}
	

}
