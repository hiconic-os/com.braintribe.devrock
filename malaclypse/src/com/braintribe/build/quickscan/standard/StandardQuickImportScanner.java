// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.quickscan.standard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.braintribe.build.artifact.representations.artifact.pom.CheapPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.build.quickscan.QuickImportScanner;
import com.braintribe.build.quickscan.commons.QuickImportScannerCommons;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.panther.SourceArtifact;

/**
 * scan the local file system - standard procedure, cannot handle parent poms, i.e. all poms must have the artifact declaration
 * defined directly, no parents, no properties  
 * 
 * @author Pit
 *
 */
public class StandardQuickImportScanner extends AbstractQuickImportScanner implements QuickImportScanner {

	private static Logger log = Logger.getLogger(StandardQuickImportScanner.class);
	

	/**
	 * scan the working copy for artifacts 
	 * @param workingCopy - the working copy path 
	 * @return - a {@link List} of {@link SourceArtifact} found 
	 */
	@Override
	public List<SourceArtifact> scanLocalWorkingCopy(String workingCopy) {
		File root = new File( workingCopy);
		
		long before = System.nanoTime();
		List<SourceArtifact> tuples = listFiles( root);
		long after = System.nanoTime();
		if (log.isDebugEnabled()) {
			log.debug( "Scanning took [" + ((after - before)/1E6) + "] ms for [" + tuples.size() + "] projects");
		}				
		return tuples;
	}
	
	private List< SourceArtifact> listFiles( File directory) {
		List<SourceArtifact> result = new ArrayList<SourceArtifact>();
		if (scanAbortSignaller != null && scanAbortSignaller.abortScan()) {
			return result;
		}
		List<File> directories = new ArrayList<File>();
		if (log.isDebugEnabled()) {
			log.debug("found [" + directory.getAbsolutePath() + "]");
		}
		for (File file : directory.listFiles()) {
		
			if (file.getName().startsWith(".svn"))
				continue;						
			
			if (file.isDirectory()) {			
				directories.add( file);						
			} else {
				if (file.getName().equalsIgnoreCase("pom.xml")) {
					try {
						SourceArtifact sourceArtifact = extractSourceArtifact(file);
						result.add( sourceArtifact);
						acknowledgeScanned(sourceArtifact);
					} catch (PomReaderException e) {
						acknowlegeScanError( file.getAbsolutePath());
					}
					return result;
				}
			}			
		}
		for (File dir : directories) {
			result.addAll( listFiles( dir));
		}

		return result;
	}
	
	private SourceArtifact extractSourceArtifact( File pom) throws PomReaderException {
		Artifact artifact = Artifact.T.create();
		artifact = CheapPomReader.identifyPom(pom);
		SourceArtifact sourceArtifact = SourceArtifact.T.create();
		sourceArtifact.setGroupId( artifact.getGroupId());
		sourceArtifact.setArtifactId( artifact.getArtifactId());
		sourceArtifact.setVersion( VersionProcessor.toString( artifact.getVersion()));		
		sourceArtifact.setPath( QuickImportScannerCommons.derivePath(pom.getParentFile(), sourceRepository));
		sourceArtifact.setRepository(sourceRepository);
		return sourceArtifact;
	}
	
}
