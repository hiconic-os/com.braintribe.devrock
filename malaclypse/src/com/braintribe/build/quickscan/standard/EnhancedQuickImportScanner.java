// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.quickscan.standard;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsExpertFactory;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.build.artifact.retrieval.multi.cache.CacheFactoryImpl;
import com.braintribe.build.quickscan.QuickImportScanException;
import com.braintribe.build.quickscan.QuickImportScanner;
import com.braintribe.build.quickscan.commons.QuickImportScannerCommons;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.panther.SourceArtifact;

/**
 * scan the local file system - handles parent poms, yet makes assumptions about the structure of the working copy,
 * i.e. that the working copy is structured identical to the local repository (group/artifact/version) 
 * 
 * @author Pit
 *
 */
public class EnhancedQuickImportScanner extends AbstractQuickImportScanner implements QuickImportScanner, LocalRepositoryLocationProvider {

	private static Logger log = Logger.getLogger(EnhancedQuickImportScanner.class);	

	private ArtifactPomReader reader;
	
	public EnhancedQuickImportScanner() { 
		
		MavenSettingsExpertFactory mavenSettingsExpertFactory = new MavenSettingsExpertFactory();
		MavenSettingsReader settingsReader = mavenSettingsExpertFactory.getMavenSettingsReader();
		
		ScannerDependencyResolverFactory dependencyResolverFactory = new ScannerDependencyResolverFactory();
		PomExpertFactory pomExpertFactory = new PomExpertFactory();
		pomExpertFactory.setIdentifyArtifactOnly(true);
		pomExpertFactory.setEnforceParentResolving(false);
		pomExpertFactory.setDetectParentLoops(true);
		
		dependencyResolverFactory.setLocalRepositoryLocationProvider( settingsReader);
		dependencyResolverFactory.setWorkingCopyLocationProvider( this);
		dependencyResolverFactory.setPomExpertFactory( pomExpertFactory);
		
		pomExpertFactory.setSettingsReader( settingsReader);
		pomExpertFactory.setDependencyResolverFactory( dependencyResolverFactory);
		pomExpertFactory.setCacheFactory( new CacheFactoryImpl());
	
		reader = pomExpertFactory.getReader();
		
	}
		
	/**
	 * scan the working copy for artifacts 
	 * @param workingCopy - the working copy path 
	 * @return - a {@link List} of {@link SourceArtifact} found 
	 */
	@Override
	public List<SourceArtifact> scanLocalWorkingCopy(String startLocation) {
		
		File root = new File( startLocation);
		
		String walkScopeId = UUID.randomUUID().toString();
		
		long before = System.nanoTime();
		List<SourceArtifact> tuples = listFiles( walkScopeId, root);
		long after = System.nanoTime();
		if (log.isDebugEnabled()) {
			log.debug( "Scanning took [" + ((after - before)/1E6) + "] ms for [" + tuples.size() + "] projects");
		}
		return tuples;
	}
	
	private List< SourceArtifact> listFiles( String walkScopeId, File directory) {
		List<SourceArtifact> result = new ArrayList<SourceArtifact>();
		if (scanAbortSignaller != null && scanAbortSignaller.abortScan()) {
			return result;
		}
		if (directory.isDirectory() == false)
			return result;
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
						SourceArtifact sourceArtifact = extractSourceArtifact(walkScopeId, reader, file);
						result.add( sourceArtifact);
						acknowledgeScanned(sourceArtifact);
					} catch (QuickImportScanException e) {
						acknowlegeScanError( file.getAbsolutePath());
					}
					return result;
				}
			}			
		}
		for (File dir : directories) {
			result.addAll( listFiles( walkScopeId, dir));
		}

		return result;
	}
	
	private SourceArtifact extractSourceArtifact(String walkScopeId, ArtifactPomReader reader, File pom) throws QuickImportScanException {
		// 
		try {
			Solution artifact = reader.readPom( walkScopeId, pom);		
			SourceArtifact sourceArtifact = SourceArtifact.T.create();
			sourceArtifact.setGroupId( artifact.getGroupId());
			sourceArtifact.setArtifactId( artifact.getArtifactId());
			sourceArtifact.setVersion( VersionProcessor.toString( artifact.getVersion()));						
			sourceArtifact.setPath( QuickImportScannerCommons.derivePath(pom.getParentFile(), sourceRepository));
			sourceArtifact.setRepository(sourceRepository);
			return sourceArtifact;
		} catch (PomReaderException e) {
			String msg="cannot read pom";			
			throw new QuickImportScanException(msg, e);
		}	
	}

	@Override
	public String getLocalRepository(String expression) throws RepresentationException {
		try {
			return new URL(sourceRepository.getRepoUrl()).getFile();
		} catch (MalformedURLException e) {
			throw new RepresentationException(e);
		}
	}
	
	

}
