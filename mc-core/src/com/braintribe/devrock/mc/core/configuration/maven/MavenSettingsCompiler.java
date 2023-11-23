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
package com.braintribe.devrock.mc.core.configuration.maven;



import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.mc.api.repository.configuration.HasConnectivityTokens;
import com.braintribe.devrock.mc.core.commons.EntityCommons;
import com.braintribe.devrock.mc.core.configuration.StandaloneRepositoryConfigurationLoader;
import com.braintribe.devrock.model.mc.cfg.origination.RepositoryConfigurationResolving;
import com.braintribe.devrock.model.repository.ChecksumPolicy;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repository.RepositoryProbingMethod;
import com.braintribe.devrock.model.repository.RepositoryRestSupport;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.maven.settings.Activation;
import com.braintribe.model.artifact.maven.settings.ActivationFile;
import com.braintribe.model.artifact.maven.settings.ActivationOS;
import com.braintribe.model.artifact.maven.settings.ActivationProperty;
import com.braintribe.model.artifact.maven.settings.Mirror;
import com.braintribe.model.artifact.maven.settings.Profile;
import com.braintribe.model.artifact.maven.settings.Property;
import com.braintribe.model.artifact.maven.settings.Repository;
import com.braintribe.model.artifact.maven.settings.Server;
import com.braintribe.model.artifact.maven.settings.Settings;
import com.braintribe.model.generic.reflection.ConfigurableCloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionRange;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

/**
 * creates a {@link RepositoryConfiguration} from a Maven {@link Settings} assembly
 * @author pit
 *
 */
public class MavenSettingsCompiler implements Supplier<RepositoryConfiguration>, HasConnectivityTokens{	
	private static final String MAVEN_ORG_PROBING_PATH = "org/apache/maven/apache-maven/maven-metadata.xml";
	private static final String MAVEN_ORG_URL = "https://repo1.maven.org/maven2";
	public static final String DEVROCK_REPOSITORY_CONFIGURATION = "DEVROCK_REPOSITORY_CONFIGURATION";
	private static Logger log = Logger.getLogger(MavenSettingsCompiler.class);
	private Supplier<Settings> settingsSupplier;
	private VirtualEnvironment virtualEnvironment = StandardEnvironment.INSTANCE;
	private YamlMarshaller marshaller = new YamlMarshaller();
	private GmDeserializationOptions options = GmDeserializationOptions.defaultOptions.derive().setInferredRootType(com.braintribe.devrock.model.repository.RepositoryConfiguration.T).absentifyMissingProperties(true).build();	
	private LazyInitialized<RepositoryConfiguration> repositoryConfiguration = new LazyInitialized<>(this::initializeRepositoryConfiguration);
	
	@Configurable @Required
	public void setSettingsSupplier(Supplier<Settings> settingsSupplier) {
		this.settingsSupplier = settingsSupplier;
	}
	
	@Configurable
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}
	
	/**
	 * @param property - the property to check for (only either environment or system property)
	 * @return - the found value or null if nothing found 
	 */
	private String lookupActivationProperty(String property) {
		if (property.startsWith("env.")) {		
			return virtualEnvironment.getEnv(property.substring( 4));
		}
		else {
			return virtualEnvironment.getProperty(property);
		}
	}
	
	/**
	 * @param profile - the {@link Profile} to check for activation
	 * @return - true if the {@link Profile}'s active
	 */
	private boolean isActive( Profile profile){
		Activation activation = profile.getActivation();
		if (activation == null)
			return false;
		
		boolean result = true;
		// active by default
		if (Boolean.TRUE.equals(activation.getActiveByDefault()))
			return true;
		
		// jdk test 
		String jdk = activation.getJdk();
				
		if (jdk != null && jdk.length() > 0) {
			VersionRange jdkRange = VersionRange.parse( jdk);
			String suspect = lookupActivationProperty("java.specification.version");
			if (suspect == null)
				return false;
			Version jdkVersion = Version.parse( suspect);
			return jdkRange.matches( jdkVersion);
		}
		
		// os 
		ActivationOS os = activation.getOs();
		if (os != null) {
			// actually, we do not properly support the os.family as it's not a direct java system property
			// so we're redirecting it to the osname, but we're only checking whether the found os-name starts
			// with the given family name. So 'Windows' will match 'Windows XP' and 'Windows 10'
			if (os.getFamily() !=  null && os.getFamily().length() > 0) {
				String osFamily = lookupActivationProperty( "os.name");			
				if (osFamily == null || !osFamily.startsWith(os.getFamily()))
					return false;
			}
			// direct match on name 
			if (os.getName() !=  null && os.getName().length() > 0) {
				String osName = lookupActivationProperty( "os.name");
				
				if (osName == null || !os.getName().equalsIgnoreCase(osName))
					return false;
			}
			// direct match on arch
			if (os.getArch() !=  null && os.getArch().length() > 0) {
				String osArch = lookupActivationProperty( "os.arch");				
				if (osArch == null || !os.getArch().equalsIgnoreCase(osArch))
					return false;
			}
			// range support on version 
			if (os.getVersion() !=  null && os.getVersion().length() > 0) {
				VersionRange osRange = VersionRange.parse( os.getVersion());
				String suspect = lookupActivationProperty( "os.version");
				if (suspect == null) 
					return false;
				Version osVersion = Version.parse( suspect);
				if (!osRange.matches( osVersion))
					return false;
			}
			
			
			
		}
		// property 
		ActivationProperty activationProperty = activation.getProperty();
		if (activationProperty != null) {
			String testValue = activationProperty.getValue();
			String key = activationProperty.getName();
			if (key.startsWith( "!")) {				
				key = key.substring(1);
				if (lookupActivationProperty( key) != null)
					return false;
				else {
					if (testValue == null) {
						return true;
					}
				}
			}
		
			

			String value = lookupActivationProperty( key);
			
			// 
			if (value == null) { // no value, not set, negative existence is tested above -> ok				
				return false;							
			}
			else {
				if (testValue == null) { // value exists, no test value -> ok
					return true;
				}
				// ! as the first character means negation.. 
				if (testValue.startsWith("!")) {
					return !testValue.substring(1).equalsIgnoreCase(value);
				}
				else {
					return testValue.equalsIgnoreCase(value);
				}
			}
		}
		
		// files
		ActivationFile activationFile = activation.getFile();
		if (activationFile != null) {
			// existing
			String existing = activationFile.getExists();
			if (existing != null) {
				// check if overridden (might have been a symbolic value)				
				File file = new File(existing);
				if (file.exists() == false)
					return false;
			}
			// missing
			String missing = activationFile.getMissing();
			if (missing != null) {
				// check if overridden (might have been a symbolic value)			
				File file = new File( missing);
				if (file.exists())
					return false;
			}
		}
		return result;
	}	
	
	/**
	 * @param effectiveSettings - the settings 
	 * @return - a {@link List} of active {@link Profile}
	 */
	private List<Profile> determineActiveProfiles(Settings effectiveSettings) {
		
		List<Profile> declaredProfiles = effectiveSettings.getProfiles(); 
		List<Profile> activeProfilesPerDeclaration = effectiveSettings.getActiveProfiles();
		
		List<Profile> activeProfiles = new ArrayList<>();
		for (Profile suspect : declaredProfiles) {
			if (activeProfilesPerDeclaration.contains(suspect) || isActive( suspect)) {
				activeProfiles.add( suspect);
			}			
		}
		return activeProfiles;
	}

	
	/**
	 * @param declaredSettings - the {@link Settings} as read by the marshaller
	 * @return - the completely resolved {@link Settings}
	 */
	private Settings compileEffectiveSettings(Settings declaredSettings) {
		
		// add the direct reference-able properties 
		Map<String, String> settingsProperties = new HashMap<>();
		settingsProperties.put( "settings.localRepository", declaredSettings.getLocalRepository());
		settingsProperties.put( "settings.offline", declaredSettings.getOffline() != null ? Boolean.toString( declaredSettings.getOffline()) : "false");
		settingsProperties.put( "settings.interactiveMode", declaredSettings.getInteractiveMode() != null ? Boolean.toString( declaredSettings.getInteractiveMode()) : "false");
		settingsProperties.put( "settings.usePluginRegistry", declaredSettings.getUsePluginRegistry() != null ? Boolean.toString( declaredSettings.getUsePluginRegistry()) : "false");
		
		// extract ak 
		Map<Profile, Map<String, String>> resolvedPropertiesMapOfProfile = new HashMap<>();
		for (Profile profile : declaredSettings.getProfiles()) {
			List<Property> properties = profile.getProperties();
			if (properties != null) {
				Map<String,String> effectivePropertiesMap = new HashMap<>();
				for (Property property : properties) {			
					effectivePropertiesMap.put( property.getName(), property.getValue());
				}
				resolvedPropertiesMapOfProfile.put( profile, effectivePropertiesMap);		
			}
			else {
				resolvedPropertiesMapOfProfile.put( profile, new HashMap<>());
			}
		}
		// compile profiles with their own properties only (plus the settings.* ones) 
		Map<Profile, Profile> precompiledProfilesMap = new HashMap<>();
		for (Profile profile : declaredSettings.getProfiles()) {
			Map<String, String> effectiveProfileProperties = resolvedPropertiesMapOfProfile.get(profile);
			
			effectiveProfileProperties.putAll(settingsProperties);
			
			ConceptualizedCloner conceptualizedClonerForProfile = new ConceptualizedCloner();
			conceptualizedClonerForProfile.setEffectiveProperties( effectiveProfileProperties);
			conceptualizedClonerForProfile.setVirtualEnvironment(virtualEnvironment);
			Profile resolvedProfile = profile.clone( ConfigurableCloningContext.build().withClonedValuePostProcesor( conceptualizedClonerForProfile::clonedValuePostProcessor).done());
			precompiledProfilesMap.put( profile, resolvedProfile);			
		}		
		// replace the declared profiles with the compiled profiles
		declaredSettings.setProfiles(new ArrayList<>(precompiledProfilesMap.values()));
		List<Profile> activePrecompiledProfiles = new ArrayList<>();
		List<Profile> activeProfiles = declaredSettings.getActiveProfiles();
		if (activeProfiles != null) {
			for (Profile active : activeProfiles) {
				activePrecompiledProfiles.add( precompiledProfilesMap.get(active));
			}
		}
		declaredSettings.setActiveProfiles(activePrecompiledProfiles);
		
		// compile the full settings (profiles won't need to be compiled again)
		ConceptualizedCloner conceptualizedClonerForSettings = new ConceptualizedCloner();
		conceptualizedClonerForSettings.setEffectiveProperties(settingsProperties);
		conceptualizedClonerForSettings.setVirtualEnvironment(virtualEnvironment);
		
		return declaredSettings.clone(ConfigurableCloningContext.build().withClonedValuePostProcesor( conceptualizedClonerForSettings::clonedValuePostProcessor).done());		
	}
	
	/**
	 * gets a mirror that mirrors the repository passed  
	 * @param repositoryId - the id of the repository 
	 * @param url - the url of the repo
	 * @return - the {@link Mirror} matching it or null if none's found 
	 * @throws RepresentationException - arrgh
	 */
	private Mirror getMirror( Settings effectiveSettings, String repositoryId, String url){		
		List<Mirror> mirrors = effectiveSettings.getMirrors();
		if (mirrors == null) { 
			return null;
		}
		for (Mirror mirror : mirrors) {
			String mirrorOf = mirror.getMirrorOf();
			boolean external = Transposer.isExternalUri(url);
			if (Transposer.isRepositoryNotedInPropertyValue(repositoryId, mirrorOf, external)) {
				return mirror;
			}											
		}
		return null;
	}
	
	/**
	 * gets a server with the given id 
	 * @param id - server id 
	 * @return - the {@link Server} found with the id 
	 * @throws RepresentationException - arrgh
	 */
	private Server getServerById( Settings effectiveSettings, String id){	
		List<Server> servers = effectiveSettings.getServers();
		if (servers == null)
			return null;
		for (Server server : servers) {
			String serverId = server.getId();
			if (serverId.equalsIgnoreCase(id))
				return server;
		}			
		return null;		
	}
	
	/**
	 * returns the value of a property named {@code <tag>-<repositoryId>} in the profile 
	 * @param profile - the {@link Profile} to look in
	 * @param repositoryId - the id of the repository we want the property-value of  (may be null)
	 * @param tag - the prefix of the variable
	 * @return - a {@link String} with the variable's value or null if not found 
	 */
	private String getProfilePropertyValue( Profile profile, String repositoryId, String tag) {		
		String variable = repositoryId != null ? tag + "-" + repositoryId : tag;
		List<Property> properties = profile.getProperties();
		if (properties == null) {
			return null;
		}
		Property property = properties.stream().filter( p -> p.getName().equals( variable)).findFirst().orElse(null);
		if (property == null)
			return null;
		return property.getValue();
	}
	
	/**
	 * @param effectiveSettings - the already completely resolved {@link Settings}
	 * @return - a consumable {@link RepositoryConfiguration} 
	 */
	private RepositoryConfiguration compileRepositoryConfiguration(Settings effectiveSettings) {
		
		RepositoryConfiguration repositoryConfiguration = RepositoryConfiguration.T.create();
		// must create it explicitly if created with absence information
		repositoryConfiguration.setRepositories( new ArrayList<>());
		String localRepository = effectiveSettings.getLocalRepository();
		if (localRepository != null) {
			repositoryConfiguration.setLocalRepositoryPath( localRepository);
		}
				
		boolean offlinePerEnvironmentOverride = false;
		String env = virtualEnvironment.getEnv(MC_CONNECTIVITY_MODE);
		if (env != null && env.compareTo( MODE_OFFLINE) == 0) {
			offlinePerEnvironmentOverride = true;
		}
		
		// set offline mode from the maven settings.xml 
		Boolean offlinePerSettings = effectiveSettings.getOffline();
		
		if (Boolean.TRUE.equals(offlinePerSettings) || offlinePerEnvironmentOverride) {
			repositoryConfiguration.setOffline( true);
		}
		
		RepositoryConfiguration injectedRepositoryConfiguration = null;
		boolean foundCentralRepository = false;
		List<Profile> profiles = determineActiveProfiles(effectiveSettings);		
	
		String targetRepository = null;
		
		for (Profile profile : profiles) {
			String targetRepositoryCandidate = getProfilePropertyValue(profile, null, "target-repository");
			if (targetRepositoryCandidate != null)
				targetRepository = targetRepositoryCandidate;
			// 
			String mcConfig = getProfilePropertyValue(profile, null, "mc-config");
			if (mcConfig != null) {
				try (Reader reader = new StringReader(mcConfig.trim())) {
					injectedRepositoryConfiguration = (RepositoryConfiguration) marshaller.unmarshall(reader, options);				
				}
				catch (Exception e) {
					throw new IllegalStateException("cannot parse [mc-config] property of [" + profile.getId() + "]", e);
				}
			}
			
			for (Repository repository : profile.getRepositories()) {
				
				// detect a declaration of a repository tagged 'central' 
				String repositoryId = repository.getId();
				if (repositoryId.equalsIgnoreCase("central")) {
					foundCentralRepository = true;
				}
				
				/* TODO : no artifact regexp filter in the current settings.xml?
				// artifact regex filter
				ArtifactRegexFilter artifactFilter = null;
				String artifactFilterExpression = getProfilePropertyValue(profile, repositoryId, "artifact-filter");
				if (artifactFilterExpression != null) {
					artifactFilter = ArtifactRegexFilter.T.create();
					artifactFilter.setPattern(artifactFilterExpression);
				}
				*/			
				// repository rest support
				RepositoryRestSupport restSupport = null;
				String restSupportExpression = getProfilePropertyValue(profile, repositoryId, "rest-support");
				if (restSupportExpression != null) {
					restSupport = RepositoryRestSupport.valueOf(restSupportExpression);
				}
				// changes url 
				String changesUrl = getProfilePropertyValue(profile, repositoryId, "changes-url");	
				
				
				// must instrument the traditional repository from settings.xml with its URL so it can be transposed
				ensureUrlOnSettingsRepository(effectiveSettings, repository);
				
				// transposing splits a repository in to one or two (if release and/or snapshot)
				List<com.braintribe.devrock.model.repository.Repository> configuredRepositories = Transposer.transpose(repository);
				
				for (com.braintribe.devrock.model.repository.Repository configuredRepository : configuredRepositories) { 								
					// assign property driven members
					// TODO : replace with matching filter 
					//configuredRepository.setArtifactFilter(artifactFilter);
					//configuredRepository.setArtifactPartFilter(artifactPartFilter);
					configuredRepository.setChangesUrl(changesUrl);
					if (restSupport != null) {
						configuredRepository.setRestSupport(restSupport);
					}
			
					repositoryConfiguration.getRepositories().add( configuredRepository);
				}
								
			} // loop over repository in profile
		
			// merge injected repository configuration via the profile's YAML property 
			if (injectedRepositoryConfiguration != null) {
				mergeRepositoryConfigurations(repositoryConfiguration, injectedRepositoryConfiguration);
			}
		} // profile
		
		// load external configuration
		RepositoryConfiguration externalRepositoryConfiguration = null;
		String externalConfigurationFilePath = virtualEnvironment.getEnv( DEVROCK_REPOSITORY_CONFIGURATION);
		if (externalConfigurationFilePath != null) {
			File externalConfigurationFile = new File( externalConfigurationFilePath);
			if (externalConfigurationFile.exists()) {
			
				StandaloneRepositoryConfigurationLoader srcl = new StandaloneRepositoryConfigurationLoader();
				srcl.setVirtualEnvironment(virtualEnvironment);
				srcl.setAbsentify(true);
				Maybe<RepositoryConfiguration> loadRepositoryConfigurationMaybe = srcl.loadRepositoryConfiguration(externalConfigurationFile);
				if (!loadRepositoryConfigurationMaybe.isSatisfied()) {
					throw new IllegalStateException("cannot load [" + externalConfigurationFilePath + "] as " + loadRepositoryConfigurationMaybe.whyUnsatisfied().stringify());
				}
				else {
					externalRepositoryConfiguration = loadRepositoryConfigurationMaybe.get();
				}							
			}
			else {
				throw new IllegalStateException("cannot find external configuration file [" + externalConfigurationFilePath + "]");
			}		
		}
		// merge external configuration into the one from the settings.xml (and YAML property)
		if (externalRepositoryConfiguration != null) {
			mergeRepositoryConfigurations(repositoryConfiguration, externalRepositoryConfiguration);
		}
		 
		// if loaded via the standard maven cascade, we might need to inject the maven-central repository
		if (!foundCentralRepository && effectiveSettings.getStandardMavenCascadeResolved()) {
			
			MavenHttpRepository centralRelease = MavenHttpRepository.T.create();
			centralRelease.setName( "central");
			centralRelease.setUrl(MAVEN_ORG_URL);
			centralRelease.setCheckSumPolicy(ChecksumPolicy.ignore);
			repositoryConfiguration.getRepositories().add( centralRelease);
			
			MavenHttpRepository centralSnapshot = MavenHttpRepository.T.create();
			centralSnapshot.setName( "central");
			centralSnapshot.setUrl(MAVEN_ORG_URL);			
			centralSnapshot.setCheckSumPolicy(ChecksumPolicy.ignore);
			centralSnapshot.setSnapshotRepo(true);			
			repositoryConfiguration.getRepositories().add( centralSnapshot);
		}
		// now apply mirror/server logic 
		for (com.braintribe.devrock.model.repository.Repository configuredRepository : repositoryConfiguration.getRepositories()) {
			if (!(configuredRepository instanceof MavenHttpRepository))
				continue;
			
			MavenHttpRepository httpRepository = (MavenHttpRepository)configuredRepository;
			
			// determine server, via mirror 
			Mirror mirror = getMirror(effectiveSettings, httpRepository.getName(), httpRepository.getUrl());
			Server server;
			if (mirror != null) {
				server = getServerById(effectiveSettings, mirror.getId());
				httpRepository.setUrl( mirror.getUrl());
			}
			else {
				server = getServerById(effectiveSettings, configuredRepository.getName());
			}
			if (server != null) {							
				// assign authentication
				httpRepository.setUser( server.getUsername());
				httpRepository.setPassword( server.getPassword());					
			}
			else {
				log.warn("cannot find a matching server for repository [" + configuredRepository.getName() + "]. Only unauthenticated access possible");
			}
						 															
		}
		
		// finally, see if there's still a reference to the maven url, and if so, modify the probing-method & -path
		for (com.braintribe.devrock.model.repository.Repository configuredRepository : repositoryConfiguration.getRepositories()) {
			if (!(configuredRepository instanceof MavenHttpRepository))
				continue;
			
			MavenHttpRepository httpRepository = (MavenHttpRepository)configuredRepository;

			String url = httpRepository.getUrl();
			if (url == null) {
				log.warn("[" + configuredRepository.getName() + "] has no assigned URL. Will be set to offline");
				configuredRepository.setOffline(true);
			}
			else {
				if (url.equals( MAVEN_ORG_URL)) {
					// TODO is there an easier way?
					EntityCommons.setIfNotAbsent(configuredRepository, MavenHttpRepository.probingMethod,  RepositoryProbingMethod.get);				
					EntityCommons.setIfNotAbsent(configuredRepository, MavenHttpRepository.probingPath,  MAVEN_ORG_PROBING_PATH);									
				}
				
				// if settings is offline in itself, mark *all* repositories as offline
				if (repositoryConfiguration.getOffline()) {
					configuredRepository.setOffline(true);
				}
			}
		}
		
		// transfer default upload repository
		if (targetRepository != null) {
			String searchedName = targetRepository;
			Optional<com.braintribe.devrock.model.repository.Repository> repoOptional = repositoryConfiguration.getRepositories().stream().filter(r -> searchedName.equals(r.getName())).findFirst();
			
			if (repoOptional.isPresent()) {
				repositoryConfiguration.setUploadRepository(repoOptional.get());
			}
		}
		
		// TODO : add plausibility test? i.e. local repository IS set, offline IS set
		Date now = new Date();
		RepositoryConfigurationResolving origination = TemplateReasons.build(RepositoryConfigurationResolving.T) //		
				.assign( RepositoryConfigurationResolving::setTimestamp, now) //
				.assign( RepositoryConfigurationResolving::setAgent, "maven settings compiler") //
				.cause( effectiveSettings.getOrigination()).toReason();
		repositoryConfiguration.setOrigination(origination);
		return repositoryConfiguration;
	}

	/**
	 * merges two {@link RepositoryConfiguration} instances
	 * @param existingRepositoryConfiguration - the one establish previously
	 * @param injectedRepositoryConfiguration - the one coming from outside
	 */
	private void mergeRepositoryConfigurations(RepositoryConfiguration existingRepositoryConfiguration, RepositoryConfiguration injectedRepositoryConfiguration) {
		com.braintribe.model.generic.reflection.Property repositoriesProperty = RepositoryConfiguration.T.getProperty(RepositoryConfiguration.repositories);
		EntityCommons.mergeIfNotAbsentInSource(injectedRepositoryConfiguration, existingRepositoryConfiguration, p -> p == repositoriesProperty);
		
		// repository definitions in YAML 
		for (com.braintribe.devrock.model.repository.Repository injectedRepository : injectedRepositoryConfiguration.getRepositories()) {
			// name and snapshot are the two identifiers 
			String iName = injectedRepository.getName();
			
			List<com.braintribe.devrock.model.repository.Repository>  configuredRepositoriesToPatch = new ArrayList<>();
			
			if (EntityCommons.isAbsent(injectedRepository, com.braintribe.devrock.model.repository.Repository.snapshotRepo)) {
				// absent property -> repos are only identified by name  -> perhaps two 
				List<com.braintribe.devrock.model.repository.Repository> toPatch = existingRepositoryConfiguration.getRepositories().stream().filter( r -> {
					return r.getName().equals( iName);
				}).collect( Collectors.toList());					
				configuredRepositoriesToPatch.addAll(toPatch);
			}
			else {
				// snapshot's set -> repo is identified by name & snapshot -> only one
				boolean iSnapshot = injectedRepository.getSnapshotRepo();
				com.braintribe.devrock.model.repository.Repository toPatch = existingRepositoryConfiguration.getRepositories().stream().filter( r -> {
					return r.getName().equals( iName) && r.getSnapshotRepo() == iSnapshot;
				}).findFirst().orElse(null);
				if (toPatch != null) {
					configuredRepositoriesToPatch.add(toPatch);
				}
			}
			// nothing to patch -> just add 
			if (configuredRepositoriesToPatch.size() == 0) {
				EntityType<? extends com.braintribe.devrock.model.repository.Repository> entityType = injectedRepository.entityType();
				com.braintribe.devrock.model.repository.Repository manifestedRepository = entityType.create();
				configuredRepositoriesToPatch.add(manifestedRepository);
				existingRepositoryConfiguration.getRepositories().add(manifestedRepository);
			}
			
			// merge 
			for (com.braintribe.devrock.model.repository.Repository toPatch : configuredRepositoriesToPatch) {				
				EntityCommons.mergeIfNotAbsentInSource( injectedRepository, toPatch);
			}					
		}
	}
	
	/**
	 * @param effectiveSettings - the {@link Settings} that are active now
	 * @param configuredRepository - the {@link Repository} (as declared in maven)
	 */
	private void ensureUrlOnSettingsRepository( Settings effectiveSettings, Repository configuredRepository) {
		Mirror mirror = getMirror(effectiveSettings, configuredRepository.getId(), configuredRepository.getUrl());
		if (mirror != null) {		
			configuredRepository.setUrl( mirror.getUrl());
		}		
	}
	

	/**
	 * lazy initializer for the repository configuration to enable caching
	 * @return
	 */
	private RepositoryConfiguration initializeRepositoryConfiguration() {
		Settings effectiveSettings = compileEffectiveSettings( settingsSupplier.get());
		return compileRepositoryConfiguration( effectiveSettings);
		
	}
	
	@Override
	public RepositoryConfiguration get() { 		
		return repositoryConfiguration.get();		
	}

	
}
