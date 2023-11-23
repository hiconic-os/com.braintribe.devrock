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
package com.braintribe.artifacts.codebase;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.braintribe.build.quickscan.agnostic.LocationAgnosticQuickImportScanner;
import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.artifact.processing.version.VersionMetricTuple;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.utils.template.Template;
import com.braintribe.utils.template.TemplateException;
import com.braintribe.utils.template.model.MergeContext;

public class CodebaseGenerator {
	private static final String ARTIFACT_ID = "artifactId";
	private static final String VERSION = "version";
	private static final String GROUP_ID = "groupId";
	private static final String GROUP_ID_EXP = "groupId.expanded";

	private Template targetTemplate;
	

	private ReentrantReadWriteLock smoodInitializationlock = new ReentrantReadWriteLock();
	boolean reinitializeSmood = false;
	private Smood smood;

	
	@SuppressWarnings("unchecked")
	public void transfer(File sourceCodebaseRoot,  File targetCodebaseRoot, String targetTemplateAsString) {
		try {		
			targetTemplate = Template.parse(targetTemplateAsString);
		} catch (Exception e) {
			throw new IllegalArgumentException("cannot parse passed templates", e);
		}
		
	
		TestUtil.ensure(targetCodebaseRoot);					
	
		try {
			Thread.sleep( 5000);
		} catch (InterruptedException e) {
			;
		}
	
		// find all folders in 
		smood = getSmood( sourceCodebaseRoot);
		EntityQueryBuilder entityQueryBuilder = EntityQueryBuilder.from( SourceArtifact.class);
		EntityQuery entityQuery = entityQueryBuilder.done();
		List<?> entities = smood.queryEntities(entityQuery).getEntities();
			
		for (SourceArtifact sourceArtifact : (List<SourceArtifact>) entities) {
			File artifactSource = new File( sourceCodebaseRoot, sourceArtifact.getPath());			
			String versionAsString = sourceArtifact.getVersion();
			Version version = VersionProcessor.createFromString(versionAsString);
			VersionMetricTuple metrics = VersionProcessor.getVersionMetric(version);
			metrics.revision = null;
			VersionProcessor.setVersionMetric(version, metrics);
			String majorMinorVersion = VersionProcessor.toString(version);
			File artifactTarget = getArtifactHome(targetCodebaseRoot, targetTemplate, sourceArtifact.getGroupId(), sourceArtifact.getArtifactId(),  majorMinorVersion);
			copy( artifactSource, artifactTarget);			
		}
	}
	
	
	
	

	private void copy(File artifactSource, File artifactTarget) {
		//System.out.println("copying [" + artifactSource + "] -> [" + artifactTarget + "]");
		System.out.print(".");
		if (!artifactTarget.exists()) {
			artifactTarget.mkdirs();
		}
		File [] files = artifactSource.listFiles();
		for (File file : files) {
			File target = new File( artifactTarget, file.getName());
			if (file.isDirectory()) {
				copy( file, target);
			}
			else {				
				try {
					Files.copy( file.toPath(), target.toPath());
				} catch (IOException e) {
					try {
						System.err.println("[" + e + "] thrown, waiting");
						Thread.sleep(5000);
						try {
							Files.copy( file.toPath(), target.toPath());
						} catch (IOException e1) {
							throw new IllegalStateException("cannot copy [" + file.getAbsolutePath() + "] to [" + target.getAbsolutePath() + "]", e);

						}
					} catch (InterruptedException e1) {
						throw new IllegalStateException("interrupted", e);
					}
					
				}
			}
		}
	}





	private MergeContext buildMergeContext(String groupId, String artifactId, String version) {
		MergeContext mergeContext = new MergeContext();
		mergeContext.setVariableProvider(name -> {
			switch (name) {
				case GROUP_ID:
					return groupId;
				case GROUP_ID_EXP:
					return groupId.replace('.', File.separatorChar);
				case ARTIFACT_ID:
					return artifactId;
				case VERSION:
					return version;
				default:
					throw new IllegalStateException("Template is in an unexpected state");
			}
		});
		return mergeContext;
	}

	private File getArtifactHome(File codebaseRoot, Template template, String groupId, String artifactId, String version) {
		MergeContext mergeContext = buildMergeContext(groupId, artifactId, version);
		
		try {
			String normalizedEvaluatedPath = template.merge(mergeContext);
			String path = normalizedEvaluatedPath.replace('/', File.separatorChar);
			
			File file = new File(codebaseRoot, path);				
			return file;
			
		} catch (TemplateException e) {
			throw new IllegalStateException("Error while evaluating template", e);
		}
	} 
	
	public Smood getSmood(File codebaseRoot) {
		Lock lock = smoodInitializationlock.writeLock();				
		if (smood == null || reinitializeSmood) {
			try {
				lock.lock();		
				smood = new Smood( EmptyReadWriteLock.INSTANCE);
				smood.initialize( getScannedSourceArtifacts( codebaseRoot));
				reinitializeSmood = false;
			}
			finally {
				lock.unlock();
			}				
		}			
		return smood;		
	}

	private List<SourceArtifact> getScannedSourceArtifacts(File codebaseRoot) throws IllegalStateException{
		List<SourceArtifact> result = new ArrayList<SourceArtifact>();
		LocationAgnosticQuickImportScanner scanner = new LocationAgnosticQuickImportScanner();
		long before = System.nanoTime();
		SourceRepository sourceRepository = SourceRepository.T.create();
		try {
			sourceRepository.setRepoUrl( codebaseRoot.toURI().toURL().toString());
		} catch (MalformedURLException e) {
			throw new IllegalStateException( e);
		}
		result.addAll(getScannedSourceArtifacts(scanner, sourceRepository));
		
		long after = System.nanoTime();
		System.out.println("scanned [" + codebaseRoot.getAbsolutePath() + "] repositories with combined ["+ result.size() + "] artifacts in [" + (after-before)/1E6 + "] ms");
		return result;
	}
	
	private List<SourceArtifact> getScannedSourceArtifacts( LocationAgnosticQuickImportScanner scanner, SourceRepository sourceRepository) {
		scanner.setSourceRepository(sourceRepository);
		String repoUrlAsString = sourceRepository.getRepoUrl();
		URL repoUrl;
		try {
			repoUrl = new URL( repoUrlAsString);
		} catch (MalformedURLException e) {
			throw new IllegalStateException("url is invalid", e);
		}
		long before = System.nanoTime();
		List<SourceArtifact> result = scanner.scanLocalWorkingCopy( repoUrl.getFile());
		long after = System.nanoTime();
		System.out.println("scanned [" + result.size() + "] artifacts in [" + (after-before)/1E6 + "] ms");
		return result;		
	}
	
	public static void main(String [] args) {
		CodebaseGenerator codeBaseGenerator = new CodebaseGenerator();
		File contents = new File( "res/grouping");
		//codeBaseGenerator.transfer( new File(contents, "grouping.flattened"), new File( contents, "standard.flattened"), "${groupId}/${artifactId}/${version}");
		codeBaseGenerator.transfer( new File(contents, "grouping.flattened"), new File( contents, "standardX.expanded"), "${groupId.expanded}/${artifactId}/${version}");
	}
}
