// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.settings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpert;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpertImpl;
import com.braintribe.cfg.Configurable;
import com.braintribe.model.malaclypse.cfg.repository.RemoteRepository;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;


/**
 * a factory that creates instances of the {@link MavenSettingsReader} <br/>
 * the following experts can be either injected or replaced
 *   
 * <ul>
 * <li>{@link MavenProfileActivationExpert} - to overload standard profile activation (default uses maven standard)</li>
 * <li>{@link LocalRepositoryLocationProvider} - to overload local repository (default comes from settings)</li>
 * <li>{@link MavenSettingsPersistenceExpert} - to overload the origin of the settings.xml (default comes from maven installation)</li>
 * </ul>
 * 
 * @author pit
 *
 */
public class MavenSettingsExpertFactory {
	private MavenSettingsPersistenceExpert settingsPeristenceExpert;
	private MavenSettingsReader settingsReader;
	private MavenProfileActivationExpert injectedActivationExpert;
	private MavenProfileActivationExpert defaultActivationExpert = new MavenProfileActivationExpertImpl();
	private LocalRepositoryLocationProvider injectedRepositoryRetrievalExpert;
	private VirtualEnvironment virtualEnvironment = StandardEnvironment.INSTANCE; 
	
	@Configurable
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}
	
	/**
	 * add an additional {@link MavenProfileActivationExpert}, for use by AC for instance <br/>
	 * allows to override profile activation 
	 * @param expert -  the {@link MavenProfileActivationExpert} to inject at first place 
	 */
	@Configurable
	public void setMavenProfileActivationExpert( MavenProfileActivationExpert expert) {
		injectedActivationExpert = expert;
		settingsReader = null; // next time, generate an new settings reader
	}
	
	/**
	 * add an additional {@link LocalRepositoryLocationProvider}, for use by AC for instance <br/>
	 * allows to override the local repository as defined in the settings.xml 
	 * @param injectedRepositoryRetrievalExpert - the {@link LocalRepositoryLocationProvider} 
	 */
	@Configurable 
	public void setInjectedRepositoryRetrievalExpert(LocalRepositoryLocationProvider injectedRepositoryRetrievalExpert) {
		this.injectedRepositoryRetrievalExpert = injectedRepositoryRetrievalExpert;
		settingsReader = null;
	}
	
	/**
	 * enables the replacement of the standard {@link MavenSettingsPersistenceExpert}, so that other settings files
	 * can be loaded (test purposes for instance)  
	 * @param settingsPeristenceExpert - {@link MavenSettingsPersistenceExpert}
	 */
	@Configurable
	public void setSettingsPeristenceExpert(MavenSettingsPersistenceExpert settingsPeristenceExpert) {
		this.settingsPeristenceExpert = settingsPeristenceExpert;
	}
	
	/**
	 * grants access to the persistence layer 
	 * @return - the {@link MavenSettingsPersistenceExpertImpl} used
	 */
	public MavenSettingsPersistenceExpert getMavenSettingsPersistenceExpert() {
		if (settingsPeristenceExpert == null) {
			MavenSettingsPersistenceExpertImpl mavenSettingsPersistenceExpertImpl = new MavenSettingsPersistenceExpertImpl();
			mavenSettingsPersistenceExpertImpl.setVirtualEnvironment(virtualEnvironment);
			settingsPeristenceExpert = 	mavenSettingsPersistenceExpertImpl;
		}
		return settingsPeristenceExpert;
	}
	/**
	 * grants access to the {@link MavenSettingsReader}, cached, unless a new injected {@link MavenProfileActivationExpert} has been set 
	 * @return - a configured {@link MavenSettingsReader}
	 */
	public MavenSettingsReader getMavenSettingsReader() {
		if (settingsReader == null) {
			settingsReader = new MavenSettingsReader();
			settingsReader.setMavenSettingsLoader( getMavenSettingsPersistenceExpert());
			settingsReader.setVirtualEnvironment(virtualEnvironment);
			
			List<MavenProfileActivationExpert> activationExperts = new ArrayList<MavenProfileActivationExpert>();
			if (injectedActivationExpert != null) {
				injectedActivationExpert.setPropertyResolver(settingsReader);
				activationExperts.add( injectedActivationExpert);
			}
			defaultActivationExpert.setPropertyResolver(settingsReader);
			activationExperts.add( defaultActivationExpert);
			settingsReader.setActivationExpert(activationExperts);
			
			if (injectedRepositoryRetrievalExpert != null) {
				List<LocalRepositoryLocationProvider> localRepositoryRetrievalExperts = new ArrayList<LocalRepositoryLocationProvider>();
				localRepositoryRetrievalExperts.add( injectedRepositoryRetrievalExpert);
				settingsReader.setLocalRepositoryRetrievalExperts(localRepositoryRetrievalExperts); 			
			}			
		}
		return settingsReader;
	}


	/**
	 * returns the current active local repository path (cannot be influenced by profile properties) 
	 * @return - a {@link File} that points to the local repository 
	 * @throws RuntimeException - arrgh
	 */
	public File provideLocalRepositoryPath() throws RuntimeException {
		try {
			return new File(getMavenSettingsReader().getLocalRepository(null));
		} catch (RepresentationException e) {
			String msg="cannot retrieve local repository from settings";
			throw new RuntimeException(msg, e);
		}
	}
	/**
	 * returns all currently active remote repositories 
	 * @return - a {@link List} of {@link RemoteRepository}
	 * @throws RuntimeException - arrgh
	 */
	public List<RemoteRepository> provideActiveRemoteRepository() throws RuntimeException {
		try {
			return getMavenSettingsReader().getActiveRemoteRepositories();
		} catch (RepresentationException e) {
			String msg="cannot retrieve remote repositories from settings";
			throw new RuntimeException(msg, e);
		}
	}
}
