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
package com.braintribe.test.multi.ravenhurstLab;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.artifacts.test.maven.framework.FakeLocalRepositoryProvider;
import com.braintribe.artifacts.test.maven.framework.FakeMavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.FilesystemBasedPersistenceExpertForRavenhurstBundle;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.RavenhurstPersistenceExpertForMainDataContainer;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.FilesystemSemaphoreLockFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionImpl;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.http.HttpAccess;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.http.HttpRetrievalException;
import com.braintribe.build.artifact.test.repolet.LauncherShell;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.build.artifact.test.repolet.RavenhurstRepolet;
import com.braintribe.build.artifact.test.repolet.Repolet;
import com.braintribe.model.malaclypse.cfg.repository.RemoteRepository;
import com.braintribe.model.maven.settings.Mirror;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.model.ravenhurst.data.RavenhurstMainDataContainer;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.test.multi.AbstractWalkLab;
import com.braintribe.testing.category.KnownIssue;

/**
 * tests how MC (actually {@link RepositoryReflectionImpl} handles the ravenhurst accesses and its answers -
 * especially whether they are reachable or not. 
 * 
 * @author pit
 *
 */
@Category(KnownIssue.class)
public class RavenhurstLab extends AbstractWalkLab {
	private static LauncherShell launcherShell;
	private static File contents = new File( "res/ravenhurstLab/contents");
	protected static File localRepository = new File (contents, "repo");
	protected static int port = -1;
	protected static List<Repolet> launchedRepolets;
	protected static String [] repositories = new String [] {"braintribe.A", "braintribe.B", "braintribe.C", "braintribe.D"};
	protected static Map<String, Mirror> mirrorMap  = new HashMap<>();
	protected static HttpAccess httpAccess;
	long t = 10*1000;
	
	@BeforeClass
	public static void runBeforeClass() {
		before( new File( contents, "settings.user.xml"));
	}

	protected static void before( File settings) {
		settingsPersistenceExpert = new FakeMavenSettingsPersistenceExpertImpl( settings);
		localRepositoryLocationProvider = new FakeLocalRepositoryProvider( localRepository);
		localRepository.mkdirs();
		port = runBefore();
		
		// clean local repository
		TestUtil.ensure(localRepository);			
		TestUtil.ensure( new File( localRepository, "updateInfo"));
			
		// fire them up 
		launchRepolets( port);
		
		httpAccess = new HttpAccess(false);
		
		for (String rep : repositories) {
			mirrorMap.put( rep, getMirrorOfRepository(rep));
		}
	}

	private static void launchRepolets( int port) {
		
		launcherShell = new LauncherShell(port);		
		launchedRepolets = launcherShell.launch( new String [] {"archiveA;archiveB;archiveC;archiveD"}, RepoType.ravenhurst);				
	}
	
	@AfterClass
	public static void after() {
		runAfter();
		launcherShell.shutdown();
	}
	
	private static Mirror getMirrorOfRepository( String repository) {
		List<RemoteRepository> activeRepositories = mavenSettingsReader.getActiveRemoteRepositories();
		for (RemoteRepository repo : activeRepositories) {
			if (repository.equalsIgnoreCase( repo.getName())) {
				String url = repo.getUrl();
				Mirror mirror = mavenSettingsReader.getMirror(repository, url);
				return mirror;				
			}
		}				
		return null;
	}
	
	
	
	private RavenhurstMainDataContainer getMainContainer() {
		RavenhurstMainDataContainer mainContainer;
		RavenhurstPersistenceExpertForMainDataContainer pe = new RavenhurstPersistenceExpertForMainDataContainer();
		pe.setLocalRepositoryLocationProvider(mavenSettingsReader);
		pe.setLockFactory(repositoryRegistry);
		try {
			mainContainer = pe.decode();
			return mainContainer;
		} catch (RavenhurstException e) {
			Assert.fail( "cannot read rh main container");
			return null;
		}		
	}
		
	private boolean toggleRepolet( String repositoryId, boolean activate) {
		Mirror mirror = mirrorMap.get(repositoryId);
		String url = mirror.getUrl();		
		Server server = mavenSettingsReader.getServerById( mirror.getId());
		String source = activate ? url + RavenhurstRepolet.MARKER_RESUME : url + RavenhurstRepolet.MARKER_STOP;
		try {
			httpAccess.require(source, server, null);
			return true;
		} catch (HttpRetrievalException e) {
			Assert.fail("cannot toggle repolet [" + repositoryId + "] as " + e.getLocalizedMessage());			
		}
		return false;
	}
	
	private Map<String, Date> getRepositoryMainDates() {
		Map<String,Date> result = new HashMap<>();
		RavenhurstMainDataContainer container = getMainContainer();		
		for (String repo : repositories) {
			Mirror mirror = mirrorMap.get(repo);
			Date date = container.getUrlToLastAccessMap().get( mirror.getUrl());
			result.put( repo, date);
		}
		return result;
	}
	
	private Map<String, List<RavenhurstBundle>> getRepositoryBundles() {
		Map<String,List<RavenhurstBundle>> result = new HashMap<>();
		FilesystemBasedPersistenceExpertForRavenhurstBundle expert = new FilesystemBasedPersistenceExpertForRavenhurstBundle();
		expert.setLockFactory( new FilesystemSemaphoreLockFactory());
		for (String repo : repositories) {
			File location = new File( localRepository, "updateInfo/" + repo );
			File [] files = location.listFiles();
			List<RavenhurstBundle> bundles = new ArrayList<>();
			result.put(repo, bundles);
			for (File file : files) {
				if (file.getName().endsWith(".index"))
					continue;
				RavenhurstBundle decoded;
				try {
					decoded = expert.decode(file);
					bundles.add(decoded);
				} catch (RavenhurstException e) {
					Assert.fail("cannot read rh file [" + file.getAbsolutePath() + "]");
				}				
			}
		}
		return result;
	}
	
	@Test
	public void runTest() {
		
		// manipulate hysteresis
		repositoryRegistry.setHysteresis(0);
		repositoryRegistry.setPreemptiveConnectionTestMode(false);
		
		// run purge 
		repositoryRegistry.purgeOutdatedMetadata();
		
		wait(1000);
				 		
		// check main date
		Map<String,Date> mainDatesAfterSuccess = getRepositoryMainDates();
		Map<String, List<RavenhurstBundle>> bundlesAfterSuccess = getRepositoryBundles();
		for (String repository : repositories) {
			Date mainDate = mainDatesAfterSuccess.get(repository);
			List<RavenhurstBundle> bundles = bundlesAfterSuccess.get(repository);
			
			Assert.assertTrue("["+ repository + "] : expected [1] bundle, found ["+ bundles.size() + "]", bundles.size() == 1);
			RavenhurstBundle bundle = bundles.get(0);
			
			Assert.assertTrue("["+ repository + "] : request failed", bundle.getRavenhurstResponse().getIsFaulty() == false);
			
			Date bundleDate = bundle.getDate();
			
			// might be an issue to compare that on the qa server... so.
			Assert.assertTrue("["+ repository + "] : main timestamps do not match", Math.abs(bundleDate.getTime() - mainDate.getTime()) < t);
		}
		
		
		
		
		// switch off some repos
		toggleRepolet( "braintribe.A", false);
		toggleRepolet( "braintribe.B", false);
		toggleRepolet( "braintribe.C", false);
		toggleRepolet( "braintribe.D", false);
		
		//  run purge
		repositoryRegistry.purgeOutdatedMetadata();
		wait(1000);
		
		// check again
		Map<String,Date> mainDatesAfterStopped = getRepositoryMainDates();
		Map<String, List<RavenhurstBundle>> bundlesAfterStopped = getRepositoryBundles();
		for (String repository : repositories) {
			Date mainDate = mainDatesAfterStopped.get(repository);
			List<RavenhurstBundle> bundles = bundlesAfterStopped.get(repository);
			
			Assert.assertTrue("["+ repository + "] after stopped : expected [2] bundles, found ["+ bundles.size() + "]", bundles.size() == 2);
			RavenhurstBundle bundle = bundles.get(1);
			
			Assert.assertTrue("["+ repository + "] after stopped : request didn't fail", bundle.getRavenhurstResponse().getIsFaulty() == true);
			Assert.assertTrue("["+ repository + "] after stopped : no error message stored", bundle.getRavenhurstResponse().getErrorMsg() != null);
			
			
			Assert.assertTrue (mainDate.getTime() == mainDatesAfterSuccess.get( repository).getTime());
		}
	
		
		// switch them on again 
		toggleRepolet( "braintribe.A", true);
		toggleRepolet( "braintribe.B", true);
		toggleRepolet( "braintribe.C", true);
		toggleRepolet( "braintribe.D", true);
		
		//  run purge
		repositoryRegistry.purgeOutdatedMetadata();
		wait(1000);
		
		// check third time
		Map<String,Date> mainDatesAfterResumed = getRepositoryMainDates();
		Map<String, List<RavenhurstBundle>> bundlesAfterResumed = getRepositoryBundles();
		for (String repository : repositories) {
			Date mainDate = mainDatesAfterResumed.get(repository);
			List<RavenhurstBundle> bundles = bundlesAfterResumed.get(repository);
			
			Assert.assertTrue("["+ repository + "] : expected [3] bundle, found ["+ bundles.size() + "]", bundles.size() == 3);
			
			RavenhurstBundle bundle = bundles.get(2);
			
			Assert.assertTrue("["+ repository + "] after resumed : request failed", bundle.getRavenhurstResponse().getIsFaulty() == false);
			
			Date bundleDate = bundle.getDate();			
			Assert.assertTrue("["+ repository + "] after resumed : main timestamps do not match", Math.abs( bundleDate.getTime() - mainDate.getTime()) < t);
		}
		
		
		
	}
	

	private void wait(int ms) {
		try {
			Thread.sleep( ms);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
