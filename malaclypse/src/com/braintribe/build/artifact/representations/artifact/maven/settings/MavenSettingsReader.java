// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.settings;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpert;
import com.braintribe.build.artifact.representations.artifact.maven.settings.properties.AbstractSettingsPropertyResolver;
import com.braintribe.build.artifact.representations.artifact.maven.settings.properties.SettingsPropertyResolver;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.HasRavenhurstTokens;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.malaclypse.cfg.repository.ContentSettings;
import com.braintribe.model.malaclypse.cfg.repository.CrcFailBehavior;
import com.braintribe.model.malaclypse.cfg.repository.RemoteRepository;
import com.braintribe.model.maven.settings.Activation;
import com.braintribe.model.maven.settings.ActivationProperty;
import com.braintribe.model.maven.settings.Mirror;
import com.braintribe.model.maven.settings.Profile;
import com.braintribe.model.maven.settings.Property;
import com.braintribe.model.maven.settings.Repository;
import com.braintribe.model.maven.settings.RepositoryPolicy;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.model.maven.settings.Settings;

/**
 * a reader/expert for Maven's settings.xml
 * 
 * @author pit
 *
 */
public class MavenSettingsReader extends AbstractSettingsPropertyResolver implements SettingsPropertyResolver, LocalRepositoryLocationProvider, HasRavenhurstTokens {
	private static Logger log = Logger.getLogger(MavenSettingsReader.class);
	
	
	private MavenSettingsPersistenceExpert mavenSettingsLoader;
	private List<MavenProfileActivationExpert> activationExperts;
	private List<LocalRepositoryLocationProvider> localRepositoryRetrievalExperts;
	private Map<String, Profile> idToProfileMap;
	private Settings settings;
	private EnvironmentVariableResolving variableResolvingMode = EnvironmentVariableResolving.strict;
	
	@Configurable @Required
	public void setMavenSettingsLoader(MavenSettingsPersistenceExpert mavenSettingsLoader) {
		this.mavenSettingsLoader = mavenSettingsLoader;
	}
	
	@Configurable @Required
	public void setActivationExpert(List<MavenProfileActivationExpert> activationExperts) {
		this.activationExperts = activationExperts;
	}
	
	@Configurable @Required
	public void setLocalRepositoryRetrievalExperts( List<LocalRepositoryLocationProvider> localRepositoryRetrievalExperts) {
		this.localRepositoryRetrievalExperts = localRepositoryRetrievalExperts;
	}
	
	@Configurable
	public void setVariableResolvingMode(EnvironmentVariableResolving variableResolvingMode) {
		this.variableResolvingMode = variableResolvingMode;
	}
	public Settings getCurrentSettings() throws RepresentationException {
		if (settings == null) {
			settings = mavenSettingsLoader.loadSettings();
			// resolve? 
			expandValues( settings);
			idToProfileMap = extractProfiles();
		}
		return settings;
	}
	
	/**
	 * automatically expand string values in the settings via the virtual environment
	 * currently, only urls are supported, both in mirror.url and repository.url 
	 * @param settings
	 */
	private void expandValues(Settings settings) {
		// path .. 
		String [] paths = new String [] {					
					"servers.username",
					"servers.password",
					"servers.passphrase",
					
					"mirrors.url", 
					"mirrors.mirrorOf",
					
					
					"profiles.repositories.url",
					"profiles.activation.property.name",
					"profiles.activation.property.value",
					
		};
		
		for (String path : paths) {
			expandValues( path, settings);
		}				
		// expand properties
		expandProperties();
	}
	
	

	private void expandValues(String path, GenericEntity entity) {			
		if (entity == null)
			return;
		try {
			int delimiter = path.indexOf( '.');
			if (delimiter < 0) {
				com.braintribe.model.generic.reflection.Property property = entity.entityType().getProperty(path);
				switch (property.getType().getTypeCode()) {
					case stringType : {
						String rawValue = property.getDirect(entity);
						if (rawValue != null) {
							String processedValue = expandValue(rawValue);					
							if (processedValue != null) {
								property.setDirect(entity, processedValue);
							}
							else {
								log.error( "cannot expand expression [" + rawValue + "] in path [" + path + "]");
							}
						}
					}			
					default:
						break;		
				}
			}
			else {
				String step = path.substring(0, delimiter);
				String remainder = path.substring( delimiter+1);
				com.braintribe.model.generic.reflection.Property property = entity.entityType().getProperty( step);
				switch ( property.getType().getTypeCode()) {
					case entityType:
							expandValues(remainder, property.getDirect(entity));
						break;
					case listType:
					case setType:
						Collection<?> obk = (Collection<?>) property.getDirect(entity);
						if (obk == null)
							return;
						for (Object obj : obk) {
							if (obj instanceof GenericEntity) {
								expandValues(remainder, (GenericEntity) obj);
							}
						}					
						break;
					default:
						break;
				}
			}
		} catch (GenericModelException e) {
			throw new IllegalStateException("cannot expand values in settings", e);
		}
		
	}
	
	private void expandProperties() {
		List<Profile> profiles = getAllProfiles();
		if (profiles == null) {
			throw new IllegalStateException("the settings.xml must contain at least one profile");
		}
		for (Profile profile : profiles) {
			List<Property> properties = profile.getProperties();
			for (Property property : properties) {
				if (property.getValue() == null) {
					String rvalue = property.getRawValue();
					String value = expandValue(rvalue);
					if (value == null) {
						log.error( "cannot expand expression [" + rvalue + "] of property [" + property.getName() + "]");
					}
					property.setValue( value);
				}
			}
		}
	}

	/**
	 * returns the profiles that are currently active, i.e. either directly declared, or via the {@link MavenProfileActivationExpert}s
	 * @return - a {@link List} with the currently active {@link Profile}
	 * @throws RepresentationException - arrgh
	 */
	public List<Profile> getActiveProfiles() throws RepresentationException{
		List<Profile> result = new ArrayList<Profile>();
		List<String> activateProfileIds = new ArrayList<String>();
		// check what profiles are directly declared to be active in the settings.xml itself 
		List<Profile> activeProfiles = getCurrentSettings().getActiveProfiles();
		if (activeProfiles.size() > 0) {				
			activateProfileIds = activeProfiles.stream().map( p -> (String) p.getId()).collect( Collectors.toList());			
			for (String id : activateProfileIds) {
				Profile profile = idToProfileMap.get( id); 
				result.add( profile);
			}
		}
		
		// now check whether others are to be dynamically activated via the activation experts 
		List<Profile> profiles = getCurrentSettings().getProfiles();
		for (Profile profile : profiles) {
			String id = profile.getId();
			if (activateProfileIds.contains(id)) {
				continue;
			}
			Activation activation = profile.getActivation();
			for (MavenProfileActivationExpert expert : activationExperts) {
				Boolean retval = expert.isActive(profile, activation);
				// if the expert returns a non-null Boolean value, it has decided, otherwise continue 
				if (retval != null) {
					if (retval) {
						result.add(profile);						
					}	
					break;
				}				
			}
		}				
		return result;
	}
	
	/**
	 * checks if a profile is active (via retrieval of all currently active profiles) 
	 * @param id - the id of the {@link Profile}
	 * @return - true if active, false otherwise 
	 * @throws RepresentationException - arrgh
	 */
	public boolean isProfileActive( String id) throws RepresentationException {
		List<Profile> profiles = getActiveProfiles();
		for (Profile profile : profiles) {
			String profileId = profile.getId();
			if (profileId.equalsIgnoreCase(id)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * checks if a profile is active 
	 * @param profile - the {@link Profile} to check 
	 * @return - true if active, false otherwise 
	 * @throws RepresentationException - arrgh
	 */
	public boolean isProfileActive( Profile profile) throws RepresentationException {
		List<Profile> profiles = getActiveProfiles();
		return profiles.contains(profile);		
	}
		
	/**
	 * gets all {@link Profile} in the current settings 
	 * @return - a {@link List} of {@link Profile}
	 * @throws RepresentationException - arrgh
	 */
	public List<Profile> getAllProfiles() throws RepresentationException {
		return getCurrentSettings().getProfiles();		
	}		
		
	/**
	 * checks if a repository has a dynamic update policy (ravenhurst support) 
	 * @param profile - the {@link Profile} that contains the repo
	 * @param repository - the {@link Repository}
	 * @return - true if dynamic, false otherwise 
	 * @throws RepresentationException - arrgh
	 */
	public boolean isDynamicRepository( Profile profile, Repository repository) throws RepresentationException{
		for (Profile suspect : getCurrentSettings().getProfiles()) {
			if (suspect != profile)
				continue;			
			String repositoryId = repository.getId();
			return isDynamicRepository(profile, repositoryId, repository.getUrl());			 			
		}
		return false;
	}
	
	/**
	 * checks if a repository is mentioned in an expression stored in a property <br/>
	 * analyzes the profiles properties section for the following values:
	 * <ul>
	 * 	<li>a comma delimited list of repository ids or/and regular expressions</li>
	 * 	<li> a single * (which is expanded to ".*") or a single id or a single regular expression</li>
	 * </ul>
	 * @param profile - the {@link Profile} that contains the repository 
	 * @param repoId - the id of the {@link Repository}
	 * @return - true if if it's mentioned in the expresion  
	 */
	private boolean isNotedInAPropertyExpression( Profile profile, String repoId, String urlAsString, String propertyKey){
		String dynamicsProperty;
		try {
			String profileId = profile.getId();
			dynamicsProperty = getProperty( profileId, propertyKey);
		} catch (RepresentationException e) {
			return false;
		}
		if (dynamicsProperty == null || dynamicsProperty.length() == 0)
			return false;
		
		boolean external;
		try {
			external = isExternalUrl(urlAsString);
		} catch (Exception e) {
			throw new IllegalArgumentException( "[" + urlAsString + "] is not a valid url for repository [" + repoId + "] of profile [" + profile.getId() + "]", e);
		}
		return isRepositoryNotedInPropertyValue(repoId, dynamicsProperty, external);
	}

	public static boolean isExternalUrl(String urlAsString) {
		boolean external = true;
		try {
			URL url = new URL( urlAsString);
			String protocol = url.getProtocol();
			if (protocol.equalsIgnoreCase("file")) {
				external = false;
			}
			String host = url.getHost(); 
			if (host.startsWith( "localhost")) {
				external = false;
			}
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException( e);
		}
		return external;
	}

	public static boolean isRepositoryNotedInPropertyValue(String key, String expression, boolean external) {
		String [] dynamics = expression.split( ",");
		// sort so that all negating are in front
		
		List<String> recombined = moveNegatorsToFrontInList(dynamics);
		
		for (String dynamic : recombined) {
			dynamic = dynamic.trim();
			boolean reverse = false;
			if (dynamic.startsWith( "!")) {
				dynamic = dynamic.substring(1);
				reverse = true;
			}			
			if (dynamic.startsWith( "external:")) {
				if (!external) {
					continue;
				}
				dynamic = dynamic.substring( "external:".length());
			}
		
			dynamic = dynamic.replaceAll("\\*", ".*");
			if (key.matches( dynamic))
				if (!reverse)
					return true;
				else
					return false;
		}
		return false;
	}

	private static List<String> moveNegatorsToFrontInList(String[] expressions) {
		List<String> tpList = new ArrayList<String>( Arrays.asList( expressions));
		List<String> negator = new ArrayList<String>();
		Iterator<String> iterator = tpList.iterator();
		while (iterator.hasNext())  {
			String s = iterator.next();
			if (s.startsWith( "!")) {
				iterator.remove();
				negator.add( s);
			}
		}
		List<String> recombined = new ArrayList<String>();
		recombined.addAll(negator);
		recombined.addAll(tpList);
		return recombined;
	}
	
	/**
	 * tests if it's marked as a dynamic repo, aka Ravenhurst capable, <br/>
	 * checks propery {@link #DYN_UPDATE_REPOS}
	 * @param profile - the {@link Profile}
	 * @param repoId - the id of the repo 
	 * @return - true if marked as dynamic
	 */
	private boolean isDynamicRepository( Profile profile, String repoId, String url) {
		return isNotedInAPropertyExpression(profile, repoId, url, DYN_UPDATE_REPOS);
	}
	
	/**
	 * tests if it's marked as being trustworthy, i.e. its metadata can be trusted<br/>
	 * checks {@link #TRUSTWORTHY_REPOS}
	 * @param profile - the {@link Profile}
	 * @param repoId - the id of the repo
	 * @return - true if marked as trustworthy
	 */
	private boolean isTrustworthyRepository( Profile profile, String repoId, String url) {		
		return isNotedInAPropertyExpression(profile, repoId, url, TRUSTWORTHY_REPOS);
	}
	
	private boolean isWeaklyCertifiedRepository( Profile profile, String repoId, String url) {		
		return isNotedInAPropertyExpression(profile, repoId, url, WEAK_CERTIFIED_REPOS);
	}	
	
	private boolean isListingLenientRepository( Profile profile, String repoId, String url) {		
		return isNotedInAPropertyExpression(profile, repoId, url, LISTING_LENIENT_REPOS);
	}	
	
	/**
	 * checks if a repository has a dynamic update policy (ravenhurst support)
	 * @param profileId - the id of the {@link Profile}
	 * @param repositoryId - the id of the {@link Repository}
	 * @return -. true if dynamic, false otherwise
	 * @throws RepresentationException - arrgh
	 */
	public boolean isDynamicRepository( String profileId, String repositoryId, String url) throws RepresentationException{		
		for (Profile suspect : getCurrentSettings().getProfiles()) {
			String suspectId = suspect.getId();
			if (!suspectId.equalsIgnoreCase( profileId))
				continue;
			return isDynamicRepository(suspect, repositoryId, url);
		}
		return false;
	}
	
	/**
	 * checks if a repository is marked as trustworthy, i.e. its metadata can be trusted (and treated as cache information)
	 * @param profileId - the id of the {@link Profile}
	 * @param repositoryId - the id of the {@link Repository}
	 * @return -. true if trustworthy, false otherwise
	 * @throws RepresentationException - arrgh
	 */
	public boolean isTrustworthyRepository( String profileId, String repositoryId, String url) throws RepresentationException{		
		for (Profile suspect : getCurrentSettings().getProfiles()) {
			String suspectId = suspect.getId();
			if (!suspectId.equalsIgnoreCase( profileId))
				continue;
			return isTrustworthyRepository(suspect, repositoryId, url);
		}
		return false;
	}
	
	/**
	 * checks if a repository is marked as trustworthy, i.e. its metadata can be trusted (and treated as cache information)
	 * @param profileId - the id of the {@link Profile}
	 * @param repositoryId - the id of the {@link Repository}
	 * @return -. true if trustworthy, false otherwise
	 * @throws RepresentationException - arrgh
	 */
	public boolean isWeaklyCertifiedRepository( String profileId, String repositoryId, String url) throws RepresentationException{	
		for (Profile suspect : getCurrentSettings().getProfiles()) {
			String suspectId = suspect.getId();
			if (!suspectId.equalsIgnoreCase( profileId))
				continue;
			return isWeaklyCertifiedRepository(suspect, repositoryId, url);
		}
		return false;
	}
	public boolean isListingLenientRepository( String profileId, String repositoryId, String url) throws RepresentationException{	
		for (Profile suspect : getCurrentSettings().getProfiles()) {
			String suspectId = suspect.getId();
			if (!suspectId.equalsIgnoreCase( profileId))
				continue;
			return isListingLenientRepository(suspect, repositoryId, url);
		}
		return false;
	}
	
	
	 
	/**
	 * returns a list of {@link RemoteRepository} that reflect the active remote repository,
	 * URL, User and Password 
	 * @return - {@link List} of {@link RemoteRepository} that are active 
	 * @throws RepresentationException - arrgh
	 */
	public List<RemoteRepository> getActiveRemoteRepositories() throws RepresentationException {
		List<Profile> activeProfiles = getActiveProfiles();		
		return extractRemoteRepositories(activeProfiles);
	}
	
	/**
	 * returns all declared remote repositories 
	 * @return - {@link List} of {@link RemoteRepository} that are declared in the current settings 
	 * @throws RepresentationException - arrgh
	 */
	public List<RemoteRepository> getAllRemoteRepositories() throws RepresentationException {
		List<Profile> profiles = getCurrentSettings().getProfiles();		
		return extractRemoteRepositories(profiles);
	}
	
	/**
	 * return all remote repositories of a certain profile, as identified by its id
	 * @param profileId - the id of the profile 
	 * @return - a {@link List} of {@link RemoteRepository}
	 * @throws RepresentationException - arrgh
	 */
	public List<RemoteRepository> getRemoteRepositoriesOfProfile( String profileId) throws RepresentationException {
		List<RemoteRepository> result = new ArrayList<RemoteRepository>();
		Profile profile = getProfileById(profileId);
		if (profile == null) {
			return result;
		}
					
		for (Repository repository : profile.getRepositories()) {
			String url = repository.getUrl();
			String repositoryId = repository.getId();			
			Server server = null;

			Mirror mirror = getMirror( repositoryId, url);
			
			if (mirror == null) {
				if (log.isDebugEnabled()) {
					log.debug("no mirror defined for repository [" + url + "] of profile [" + profileId + "]");
				}
				server = getServerById( repositoryId);
			}
			else {
				String mirrorId = mirror.getId();
				// override url with the one from the mirror
				url = mirror.getUrl();
				server = getServerById( mirrorId);
				if (server == null) {
					log.warn("no server defined for mirror [id:" + mirror.getId() + ", url:" + url + "] of profile [" + profileId + "]");
					continue;
				}
			}
			if (server == null) {
				log.debug("no server defined for repository [" + url + "] of profile [" + profileId + "], repository access must be without authentication");
			}
			
			RemoteRepository remoteRepository = RemoteRepository.T.create();			
			remoteRepository.setName( repositoryId);			
			remoteRepository.setProfileName( profileId);
			remoteRepository.setUrl(url);
			remoteRepository.setUser( server != null ? server.getUsername() : null);
			remoteRepository.setPassword( server != null ? server.getPassword() : null);
			
			RepositoryPolicy releasePolicy = repository.getReleases();			
			ContentSettings settings = extractContentSettingsFromUpdatePolicy(releasePolicy);
			remoteRepository.setReleaseSettings(settings);
			
			RepositoryPolicy snapshotPolicy = repository.getSnapshots();
			settings = extractContentSettingsFromUpdatePolicy(snapshotPolicy);
			remoteRepository.setSnapshotSettings(settings);
			
			result.add(remoteRepository);
		}
		return result;
		
	}

	private ContentSettings extractContentSettingsFromUpdatePolicy(RepositoryPolicy repositoryPolicy) {
		ContentSettings settings = ContentSettings.T.create();
		if (repositoryPolicy == null) {
			settings.setUpdateInterval( UPDATE_DAILY_IN_MS); // daily
			settings.setCrcFailBehavior( CrcFailBehavior.warn);
			settings.setEnabled( true);
		}
		else {
			String updateInterval = repositoryPolicy.getUpdatePolicy();
			if (updateInterval == null || updateInterval.length() == 0) {
				settings.setUpdateInterval( UPDATE_DAILY_IN_MS);
			}
			else {
				updateInterval = updateInterval.trim();
				if (updateInterval.equalsIgnoreCase( UPDATE_DAILY)) {
					settings.setUpdateInterval( UPDATE_DAILY_IN_MS); 
				}				
				else if (updateInterval.equalsIgnoreCase( UPDATE_ALWAYS)) { // always
					settings.setUpdateInterval(0);
				}
				else if (updateInterval.equalsIgnoreCase( UPDATE_NEVER)) { // never 
					settings.setUpdateInterval(-1);
				}
				else {
					int p = updateInterval.indexOf( UPDATE_INTERVAL); // set in minutes
					if (p < 0) {
						settings.setUpdateInterval(-1);
					}
					else {
						String minutes = updateInterval.substring( UPDATE_INTERVAL.length());
						settings.setUpdateInterval( Integer.parseInt(minutes) * 60 * 1000);
					}
				}					
			}
			String checksumPolicy = repositoryPolicy.getChecksumPolicy();
			if (checksumPolicy == null || checksumPolicy.equalsIgnoreCase( "ignore")) {
				settings.setCrcFailBehavior( CrcFailBehavior.ignore);
			}
			else if (checksumPolicy.equalsIgnoreCase( "warn")) {
				settings.setCrcFailBehavior( CrcFailBehavior.warn);
			}
			else {
				settings.setCrcFailBehavior( CrcFailBehavior.fail);
			}
		}
		settings.setEnabled(repositoryPolicy != null && repositoryPolicy.getEnabled() != null ? Boolean.TRUE.equals( repositoryPolicy.getEnabled()) : true);
		return settings;
	}

	private List<RemoteRepository> extractRemoteRepositories(List<Profile> activeProfiles) throws RepresentationException {
		List<RemoteRepository> result = new ArrayList<RemoteRepository>();
		for (Profile profile : activeProfiles) {			
			String profileId = profile.getId();

			for (Repository repository : profile.getRepositories()) {				
				// 
				String repoUrl = repository.getUrl();
				String repositoryId = repository.getId();
				Server server = null;
				// might have a mirror 
				Mirror mirror = getMirror( repositoryId, repository.getUrl());
				if (mirror == null) {  // use the repository to find the server
					server = getServerById( repositoryId);
				}
				else { // use the mirror to find the server
					String mirrorId = mirror.getId();
					server = getServerById( mirrorId);
					// override the url with the one from the mirror
					repoUrl = mirror.getUrl();
				
				}
				if (server == null) {
					String msg = "cannot correlate server neither by mirror or repository for ["+ repositoryId + "]'s id";
					log.warn( msg);				
				}
				RemoteRepository remoteRepository = RemoteRepository.T.create();
				remoteRepository.setUrl(repoUrl);
				remoteRepository.setProfileName( profileId);
				remoteRepository.setUser( server != null ? server.getUsername() : null);
				remoteRepository.setPassword( server != null ? server.getPassword() : null);
				remoteRepository.setName( repositoryId);
				result.add( remoteRepository);
			}
		}
		return result;
	}
	
	
	
	private Map<String, Profile> extractProfiles() throws RepresentationException {
		Map<String, Profile> result = new HashMap<String, Profile>();			
		for (Profile profile : getCurrentSettings().getProfiles()) {
			String profileId = profile.getId();
			result.put( profileId, profile);
		}		
		return result;
	}
	
	/**
	 * gets a server with the given id 
	 * @param id - server id 
	 * @return - the {@link Server} found with the id 
	 * @throws RepresentationException - arrgh
	 */
	public Server getServerById( String id) throws RepresentationException{	
		for (Server server : getCurrentSettings().getServers()) {
			String serverId = server.getId();
			if (serverId.equalsIgnoreCase(id))
				return server;
		}			
		return null;		
	}
	
	/**
	 * gets the mirror with the given id 
	 * @param id - the id of the mirror
	 * @return - the {@link Mirror} with the id
	 * @throws RepresentationException - arrgh
	 */
	public Mirror getMirrorById( String id) throws RepresentationException{
		
		for (Mirror mirror : getCurrentSettings().getMirrors()) {				
			String mirrorId = mirror.getId();
			if (mirrorId.equalsIgnoreCase( id))
				return mirror;
		}		
		return null;
	}
	
	/**
	 * gets a mirror that mirrors the repository passed  
	 * @param repositoryId - the id of the repository 
	 * @param url - the url of the repo
	 * @return - the {@link Mirror} matching it or null if none's found 
	 * @throws RepresentationException - arrgh
	 */
	public Mirror getMirror( String repositoryId, String url) throws RepresentationException{		
		for (Mirror mirror : getCurrentSettings().getMirrors()) {
			String mirrorOf = mirror.getMirrorOf();
			boolean external = isExternalUrl(url);
			if (isRepositoryNotedInPropertyValue(repositoryId, mirrorOf, external)) {
				return mirror;
			}											
		}
		return null;
	}
	
	/**
	 * gets the local repository - if it cannot be set (via settings), it defaults to 
	 * whatever Java's system property "user.home" returns;
	 * @param expression - an expression to be passed to the expert, mostly null 
	 * @return  - the path to the local repository 
	 * @throws RepresentationException - arrgh
	 */
	@Override
	public String getLocalRepository(String expression) throws RepresentationException {
		expression = getCurrentSettings().getLocalRepository();
		if (localRepositoryRetrievalExperts != null) {
			for (LocalRepositoryLocationProvider expert : localRepositoryRetrievalExperts) {
				String value = expert.getLocalRepository(expression);
				if (value != null) {
					return expandValue(value); // expand the expression AFTER it has been delivered
				}
			}
		}
		String value = expandValue(expression);
		if (value == null) {
			//value = virtualEnvironment.getProperty("user.home") + "/.m2/repository";
			throw new IllegalStateException( "cannot resolve local repositry expression [" + expression + "]");
		}
		return value;
	}
	
	/**
	 * gets the raw value as set for the local repository 
	 * @return - the value for the local repository as set in the settings (no variable expansion, no override)
	 * @throws RepresentationException - arrgh
	 */
	public String getLocalRepositoryExpression() throws RepresentationException {
		return getCurrentSettings().getLocalRepository();
	}
	
	@Override
	public String expandValue(String line) {
		if (line == null)
			return null;
		
		String expression = line;
		while (requiresEvaluation( expression)) {			
			String variable = extract( expression);
			String [] tokens = split( variable);
			String value = null;
			if (tokens[0].equalsIgnoreCase( "settings")) {
				value = resolveValue( tokens[1]);
			}		
			else if (tokens[0].equalsIgnoreCase( "env")) {
				// override here .. 
				switch ( variableResolvingMode) {
					case combined:
				
						value = getSystemProperty(variable);
						if (value == null) {				
							value = getEnvironmentProperty( tokens[1]);
						}				
						break;
					default : 
						value = getEnvironmentProperty( tokens[1]);
						break;					
						
				}
			}
			else { 				
				
					value = getSystemProperty(variable);
					if (value == null) {				
						value = resolveProperty(variable);
					}				
			}
			if (value != null) {
				expression = replace(variable, value, expression);
			}
			else 
				return null;
		}
		return expression;
	}
		

	@Override
	public String resolveValue(String expression) {	
		try {
			return resolveValue(getCurrentSettings(), expression);
		} catch (RepresentationException e) {
			return null;
		}
	}

	@Override
	public String resolveProperty(String property) {
		try {
			Map<String, Map<String, String>> properties = getPropertiesOfActiveProfiles();
			for (Entry<String, Map<String, String>> entry : properties.entrySet()) {
				Map<String, String> map = entry.getValue();
				String value = map.get(property);
				if (value != null)
					return value;
			}
		} catch (RepresentationException e) {
			if (log.isDebugEnabled()) {
				log.error( "cannot resolve [" + property + "]", e);
			}
		}		
		return null;
	}
		
	/**
	 * returns a {@link Map} of <profile id>,<variable value> of any profiles that are switched with the given variable 
	 * @param variableName - the {@link String} with the name of the variable 
	 * @return - a {@link Map}
	 * @throws RepresentationException - arrgh
	 */
	public Map<String,String> getVariableValuesPerProfileIdForEnvironmentVariable( String variableName) throws RepresentationException {
		Map<String, String> result = new HashMap<String, String>();				
		for (Profile profile : getCurrentSettings().getProfiles()) {
			Activation activation = profile.getActivation();
			if (activation == null)
				continue;
			ActivationProperty property = activation.getProperty();
			if (property == null)
				continue;
			if (property.getName().equalsIgnoreCase(variableName)) {
				String profileId = profile.getId();
				result.put( profileId, property.getValue());
			}
		}
		return result;
	}
	
	/**
	 * return the value certain property of a certain profile <br/>
	 * if the property's value is resolved, it is returned. Otherwise, it's raw value is returned
	 * @param profileName - the name (id) of the profile 
	 * @param propertyName - the name of the property 
	 * @return - the value of the property or null 
	 * @throws RepresentationException - arrgh
	 */
	public String getProperty(String profileName, String propertyName) throws RepresentationException {
		Profile profile = getProfileById( profileName);
		if (profile == null) {
			return null;
		}		
		for (Property property : profile.getProperties()) {
			if (property.getName().equalsIgnoreCase(propertyName)) {
				String value = property.getValue();
				if (value != null)
					return value;
				return property.getRawValue();
			}
		}		
		return null;				
	}
	
	/**
	 * extract a certain number of specified properties of a profile  
	 * @param profileName - the name of the profile
	 * @param names - an array of property names
	 * @return a {@link Map} of name to value strings 
	 * @throws RepresentationException - arrgh
	 */
	public Map<String, String> getProperties( String profileName, String ...names) throws RepresentationException {
		Profile profile = getProfileById( profileName);
		if (profile == null) {
			return null;
		}
		Map<String, String> result = new HashMap<String, String>();
		
		for (Property property : profile.getProperties()) {
			String suspect=property.getName();
			for (String name : names) {
				if (suspect.equalsIgnoreCase( name)) {
					String value = property.getValue();
					if (value == null)
						value = property.getRawValue();
					result.put(suspect, value);
				}
			}
		}	
		return result;
	}
	
	/**
	 * returns a map of all properties of a certain profile <br/>
	 * if the property has a resolved values, it is used.
	 * Otherwise, the raw value (as found in the settings.xml) is used.
	 * @param profileName - the name of the profile
	 * @return - {@link Map} of property name to property value
	 * @throws RepresentationException - arrgh
	 */
	public Map<String, String> getProperties( String profileName) throws RepresentationException {		
		
		Map<String, String> result = new HashMap<String, String>();
		Profile profile = getProfileById( profileName);
		if (profile == null)
			return result;	
		
		for (Property property : profile.getProperties()) {
			String value = property.getValue();
			if (value == null) {
				value = property.getRawValue();
			}
			result.put( property.getName(), value);
		}
		return result;		
	}
	
	/**
	 * returns a map with all properties of all active profiles
	 * @return - a {@link Map} of profile name to {@link Map} of property name to property value 
	 * @throws RepresentationException - arrgh
	 */
	public Map<String, Map<String, String>> getPropertiesOfActiveProfiles() throws RepresentationException {
		List<Profile> profiles = getActiveProfiles();
		if (profiles == null)
			return null;
		
		Map<String, Map<String, String>> result = new HashMap<String, Map<String,String>>();
		for (Profile profile : profiles) {		
			String profileName = profile.getId();
			Map<String, String> properties = getProperties( profileName);
			result.put(profileName, properties);
		}
		return result;
	}
	
	/**
	 * returns a profile (if any) with the given id 
	 * @param id - the id of the profile 
	 * @return - the {@link Profile} with the id or null 
	 * @throws RepresentationException - if no profiles can be retrieved
	 */
	public Profile getProfileById( String id) throws RepresentationException {
		for (Profile profile : getCurrentSettings().getProfiles()) {
			String profileId = profile.getId();
			if (profileId.equalsIgnoreCase( id)) {
				return profile;
			}
		}
		return null;
	}
	
	public static void main( String [] args) {
		String expression = args[0];
		for (int i=1; i < args.length; i++) {
			String e = args[i];
			String [] t = e.split(",");
			String key = t[0];
			boolean external = Boolean.parseBoolean( t[1]);
			if (MavenSettingsReader.isRepositoryNotedInPropertyValue( key, expression, external)) {
				System.out.println("[" + expression + "] contains [" + key + "]");
			}
			else {
				System.out.println("[" + expression + "] does not contain [" + key + "]");
			}
		}
	}
}
