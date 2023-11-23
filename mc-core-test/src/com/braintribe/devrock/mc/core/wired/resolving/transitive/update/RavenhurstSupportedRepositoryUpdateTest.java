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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.update;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.core.commons.FileCommons;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.utils.paths.UniversalPath;

/**
 * 
 * test several issues concerning repositories with differing update logics.
 * 
 * updating RH capable repositories should only react on markers, yet not on outdated files
 * non-updating RH capable repositories should neither react on markers, nor on outdated files
 * 
 * dumb updating repositories should not react on markers, yet on outdated files 
 * dumb non-updating repositories should neither react on markers, nor on outdated files
 * 
 * mc-core knows whether the repo is RH capable from the probing of the URL of the repo. Whether it's 
 * updating or not comes from the configuration. 
 *  
 *  4 repos : rh-updateable, rh-non-updateable, dumb updateable, dumb non-updateable
 *  2 cfg modes: cfg via maven settings.xml, via repository configuration
 *  2 run modes: simulating passedtime, simulation RH notification
 *  
 *  also tests the Maven settings compiler in declaring repos 'updateable' via update-policy detection,
 *  the repository configuration loader/enricher to handle the model's defaults correctly.
 *    
 * @author pit
 *
 */
public class RavenhurstSupportedRepositoryUpdateTest extends AbstractMavenMetadataUpdateTest {
	private static final String REMOTE_MAVEN_METADATA_XML = "maven-metadata.xml";
	private static final String LOCAL_MAVEN_METADATA_XML_RH = "maven-metadata-" + ARCHIVE_RH_UPDATE + ".xml";
	private static final String LOCAL_MAVEN_METADATA_XML_RH_NEVER = "maven-metadata-" + ARCHIVE_RH_NONUPDATE + ".xml";
	private static final String LOCAL_MAVEN_METADATA_XML_DUMB = "maven-metadata-" + ARCHIVE_DB_UPDATE + ".xml";
	private static final String LOCAL_MAVEN_METADATA_XML_DUMB_NEVER = "maven-metadata-" + ARCHIVE_DB_NONUPDATE + ".xml";
	private static final String TERMINAL = "com.braintribe.devrock.test:t#[1.0, 1.1)";

	enum Tweak {duration, marker}
	enum Cfg {maven, yaml}

	@Override
	protected RepoletContent archiveInput() {
		return archiveInput( "archive.definition.yaml");
	}
	
	private void pushBackIntoThePast( File localMavenMetadataFile) {
		long lastModified = localMavenMetadataFile.lastModified();
		
		long modifiedInThePast = lastModified - (2*24*60*60*1000); // two days back in time
		
		localMavenMetadataFile.setLastModified(modifiedInThePast);		
	}

	/**
	 * settings.xml based cfg, tests update logic via timestamp of files
	 */
	@Test
	public void runRepoUpdateWithMavenCfgWithTimestampBasedUpdateTest() {
		run( Cfg.maven, Tweak.duration);
	}
	/**
	 * settings.xml based cfg, tests update logic via RH markers
	 */
	@Test
	public void runRepoUpdateWithMavenCfgWithMarkerBasedUpdateTest() {
		run( Cfg.maven, Tweak.marker);
	}

	/**
	 * repository-configuration.yaml based cfg, tests update logic via timestamp of files
	 */
	@Test
	public void runRepoUpdateWithYamlCfgWithTimestampBasedUpdateTest() {
		run( Cfg.yaml, Tweak.duration);
	}
	/**
	 * repository-configuration.yaml based cfg, tests update logic via RH markers
	 */
	@Test
	public void runRepoUpdateWithYamlCfgWithMarkerBasedUpdateTest() {
		run( Cfg.yaml, Tweak.marker);
	}
	

	
	
	private boolean hasDownloads( String repolet) {
		List<String> downloadsOfRepo = downloads.get(repolet);
		return (downloadsOfRepo != null && downloadsOfRepo.stream().filter( s -> s.endsWith( REMOTE_MAVEN_METADATA_XML)).findFirst().orElse( null) != null);					
	}
	
		
	private void run( Cfg cfg, Tweak tweak ) {
				
		// run first resolution
		switch (cfg) {
			case maven:
				runWithMavenBasedCfg(TERMINAL, standardResolutionContext);
				break;
			case yaml:
				runWithRepoCfgBasedCfg(TERMINAL, standardResolutionContext);	
				break;
			default:
				throw new UnsupportedOperationException("no support for " + cfg.name());
		}
		
		// check downloads -> expect maven-metadata.xml
		Assert.assertTrue("initial run: expected maven-metadata.xml to be downloaded at this time for repolet with RH support, but it wasn't", hasDownloads( ARCHIVE_RH_UPDATE));				
		Assert.assertTrue("initial run: expected maven-metadata.xml to be downloaded at this time for repolet with RH support (non-updateable), but it wasn't", hasDownloads( ARCHIVE_RH_NONUPDATE));		
		Assert.assertTrue("initial run: expected maven-metadata.xml to be downloaded at this time for repolet without RH support (dumb updateable), but it wasn't", hasDownloads( ARCHIVE_DB_UPDATE));
		Assert.assertTrue("initial run: expected maven-metadata.xml to be downloaded at this time for repolet without RH support (dumb non-updateable), but it wasn't", hasDownloads(ARCHIVE_DB_NONUPDATE));

		switch( tweak) {
			case duration:
				pushback();
				break;
			case marker: 
				mark();
				break;
			default: {
				throw new UnsupportedOperationException("no support for " + tweak.name());
			}
		}
		

		downloads.clear();
		
		// run resolution again
		switch (cfg) {
		case maven:
			runWithMavenBasedCfg(TERMINAL, standardResolutionContext);
			break;
		case yaml:
			runWithRepoCfgBasedCfg(TERMINAL, standardResolutionContext);	
			break;
		default:
			throw new UnsupportedOperationException("no support for " + cfg.name());			
	}


		boolean validStateForRhUpdateable;
		boolean validateStateForRhNonUpdateable;
		boolean validStateForDumbUpdateable;
		boolean validStateForDumbNonUpdateable;
		
		switch (tweak)  {
			case duration : {
				validStateForRhUpdateable = !hasDownloads( ARCHIVE_RH_UPDATE);
				validateStateForRhNonUpdateable = !hasDownloads(ARCHIVE_RH_NONUPDATE);
				validStateForDumbUpdateable = hasDownloads( ARCHIVE_DB_UPDATE);
				validStateForDumbNonUpdateable = !hasDownloads(ARCHIVE_DB_NONUPDATE);
				break;
			}
			case marker:
				validStateForRhUpdateable = hasDownloads( ARCHIVE_RH_UPDATE);
				validateStateForRhNonUpdateable = !hasDownloads(ARCHIVE_RH_NONUPDATE);
				validStateForDumbUpdateable = !hasDownloads( ARCHIVE_DB_UPDATE);
				validStateForDumbNonUpdateable = !hasDownloads(ARCHIVE_DB_NONUPDATE);
				break;
			default: {
				throw new UnsupportedOperationException("no support for " + tweak.name());
			}
		}

		
		// check downloads -> do not expect maven-metadata-xml 
		Assert.assertTrue("update run: expected maven-metadata.xml not to be downloaded at this time for repolet with RH support, but it was", validStateForRhUpdateable);		
		Assert.assertTrue("update run: expected maven-metadata.xml not to be downloaded at this time for repolet with RH support (but not updateable), but it was", validateStateForRhNonUpdateable);
				
		Assert.assertTrue("update run: expected maven-metadata.xml to be downloaded at this time for repolet without RH support (dumb updateable), but it wasn't", validStateForDumbUpdateable);
		Assert.assertTrue("update run: expected maven-metadata.xml to be downloaded at this time for repolet without RH support (dumb non-updateable), but it wasn't", validStateForDumbNonUpdateable);
			
	}
	
	/**
	 * create markers fro the files
	 */
	private void mark() {
		mark( LOCAL_MAVEN_METADATA_XML_RH);
		mark( LOCAL_MAVEN_METADATA_XML_RH_NEVER);
		mark( LOCAL_MAVEN_METADATA_XML_DUMB);
		mark( LOCAL_MAVEN_METADATA_XML_DUMB_NEVER);				
	}
	 
	/**
	 * push back the files in time
	 */
	private void pushback() {
		pushback( LOCAL_MAVEN_METADATA_XML_RH);
		pushback( LOCAL_MAVEN_METADATA_XML_RH_NEVER);
		pushback( LOCAL_MAVEN_METADATA_XML_DUMB);
		pushback( LOCAL_MAVEN_METADATA_XML_DUMB_NEVER);				
	}
	
	private void mark( String key) {
		File localMavenMetadataFileForRh = UniversalPath.from(repo).push( "com.braintribe.devrock.test.t",  ".").push(key).toFile();		
		if (!localMavenMetadataFileForRh.exists()) {
			Assert.fail("cannot find expected downloaded metadata file [" + localMavenMetadataFileForRh.getAbsolutePath() + "]");
			return;
		}
		File markerFile = FileCommons.markerFile(localMavenMetadataFileForRh);
		try {
			boolean result = markerFile.createNewFile();
			if (!result) {
				Assert.fail("cannot mark metadata file [" + localMavenMetadataFileForRh.getAbsolutePath() + "] as target file exists");	
			}
		} catch (IOException e) {
			Assert.fail("cannot mark metadata file [" + localMavenMetadataFileForRh.getAbsolutePath() + "]");
		}
	}
	
	
	private void pushback( String key) {
		File localMavenMetadataFileForRh = UniversalPath.from(repo).push( "com.braintribe.devrock.test.t",  ".").push(key).toFile();		
		if (!localMavenMetadataFileForRh.exists()) {
			Assert.fail("cannot find expected downloaded metadata file [" + localMavenMetadataFileForRh.getAbsolutePath() + "]");
			return;
		}
		pushBackIntoThePast(localMavenMetadataFileForRh);
		
	}
}
