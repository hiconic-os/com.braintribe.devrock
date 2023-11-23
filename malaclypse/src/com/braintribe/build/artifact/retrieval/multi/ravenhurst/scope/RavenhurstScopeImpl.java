// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.maven.settings.OverrideableVirtualEnvironment;
import com.braintribe.build.artifact.retrieval.multi.HasConnectivityTokens;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.registry.RavenhurstPersistenceRegistry;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.registry.RavenhurstPersistenceRegistryImpl;
import com.braintribe.build.artifact.retrieval.multi.repository.filtering.ArtifactFilterExpertSupplier;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.codec.string.DateCodec;
import com.braintribe.devrock.mc.core.filters.AllMatchingArtifactFilterExpert;
import com.braintribe.devrock.mc.core.filters.ArtifactFilterExpert;
import com.braintribe.devrock.mc.core.filters.ArtifactFilters;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.devrock.model.repositoryview.resolution.RepositoryViewResolution;
import com.braintribe.logging.Logger;
import com.braintribe.model.malaclypse.cfg.repository.ContentSettings;
import com.braintribe.model.malaclypse.cfg.repository.CrcFailBehavior;
import com.braintribe.model.malaclypse.cfg.repository.RemoteRepository;
import com.braintribe.model.maven.settings.Mirror;
import com.braintribe.model.maven.settings.Profile;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.model.ravenhurst.data.RavenhurstMainDataContainer;
import com.braintribe.model.ravenhurst.interrogation.CrcValidationLevelForBundle;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstRequest;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.ve.api.VirtualEnvironment;

public class RavenhurstScopeImpl
		implements RavenhurstScope, RepositoryConfigurationExposure, HasRavenhurstTokens, HasConnectivityTokens, ArtifactFilterExpertSupplier {
	private static Logger log = Logger.getLogger(RavenhurstScopeImpl.class);
	private MavenSettingsReader reader;
	private LockFactory lockFactory;
	private File localRepositoryDirectory;
	private List<RavenhurstBundle> bundles;
	private Set<String> inhibitedProfileIds;
	private Set<String> inhibitedRepositoryIds;
	private Set<String> inhibitedRepositoryUrls;
	private RavenhurstPersistenceRegistry persistenceRegistry;
	private VirtualEnvironment virtualEnvironment;
	private YamlMarshaller yamlMarshaller = new YamlMarshaller();
	private GmDeserializationOptions options = GmDeserializationOptions.deriveDefaults().setInferredRootType(com.braintribe.devrock.model.repository.RepositoryConfiguration.T).absentifyMissingProperties(true).build();
	private LazyInitialized<Map<String, ArtifactFilterExpert>> repositoryToFilter = new LazyInitialized<>( this::loadFilters);
	private RepositoryViewResolution repositoryViewResolution;

	@Configurable
	@Required
	@Override
	public void setReader(MavenSettingsReader reader) {
		this.reader = reader;
		persistenceRegistry.setLocalRepositoryLocationProvider(reader);
	}
	@Configurable
	@Required
	public void setLockFactory(LockFactory lockFactory) {
		this.lockFactory = lockFactory;
		persistenceRegistry.setLockFactory(this.lockFactory);
	}

	@Override
	public void setInhibitedProfilesIds(Set<String> profileIds) {
		this.inhibitedProfileIds = profileIds;
	}
	@Override
	public void setInhibitedRepositoryIds(Set<String> repositoryIds) {
		this.inhibitedRepositoryIds = repositoryIds;
	}
	@Override
	public void setInhibitedRepositoryUrls(Set<String> repositoryUrls) {
		this.inhibitedRepositoryUrls = repositoryUrls;
	}

	@Configurable
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}

	@Override
	public RavenhurstPersistenceRegistry getPersistenceRegistry() {
		return persistenceRegistry;
	}

	public RavenhurstScopeImpl() {
		persistenceRegistry = new RavenhurstPersistenceRegistryImpl();
	}

	private VirtualEnvironment getVirtualEnvironment() {
		if (virtualEnvironment == null) {
			virtualEnvironment = new OverrideableVirtualEnvironment();
		}
		return virtualEnvironment;
	}

	@Override
	public void updateTimestamp(String url, Date date) throws RavenhurstException {
		persistenceRegistry.getRavenhurstMainDataContainer().getUrlToLastAccessMap().put(url, date);
	}

	@Override
	public RepositoryViewResolution getRepositoryViewResolution() {
		// ensure filters ARE loaded by triggering lazy instantiation
		repositoryToFilter.get();
		return repositoryViewResolution;
	}

	@Override
	public Date getUpdateTimestamp(String url) throws RavenhurstException {
		return persistenceRegistry.getRavenhurstMainDataContainer().getUrlToLastAccessMap().get(url);
	}

	@Override
	public void persistData() throws RavenhurstException {
		persistenceRegistry.persistRavenhustMainDataContainer();

	}

	@Override
	public void clear() {
		persistenceRegistry.clear();
		bundles = null;
	}

	private Object localRepositoryMonitor = new Object();

	@Override
	public File getLocalRepository() throws RavenhurstException {
		if (localRepositoryDirectory != null) {
			return localRepositoryDirectory;
		}
		synchronized (localRepositoryMonitor) {
			if (localRepositoryDirectory != null) {
				return localRepositoryDirectory;
			}
			try {
				localRepositoryDirectory = new File(reader.getLocalRepository(null));
			} catch (RepresentationException e) {
				String msg = "cannot retrieve current local repository directory";
				log.error(msg, e);
				throw new RavenhurstException(msg, e);
			}

			return localRepositoryDirectory;
		}
	}

	private Object ravenhurstBundleMonitor = new Object();
	private RepositoryConfiguration repositoryConfiguration;

	@Override
	public List<RavenhurstBundle> getRavenhurstBundles() throws RavenhurstException {
		if (bundles != null) {
			return bundles;
		}
		synchronized (ravenhurstBundleMonitor) {
			if (bundles != null) {
				return bundles;
			}
			bundles = generateBundles(getLocalRepository());
			return bundles;
		}

	}

	@Override
	public RavenhurstBundle getRavenhurstBundleByName(String name) throws RavenhurstException {
		List<RavenhurstBundle> rbundles = getRavenhurstBundles();
		for (RavenhurstBundle bundle : rbundles) {
			if (bundle.getRepositoryId().equalsIgnoreCase(name))
				return bundle;
		}
		return null;
	}
	@Override
	public RavenhurstBundle getRavenhurstBundleByUrl(String url) throws RavenhurstException {
		List<RavenhurstBundle> rbundles = getRavenhurstBundles();
		for (RavenhurstBundle bundle : rbundles) {
			if (bundle.getRepositoryUrl().equalsIgnoreCase(url))
				return bundle;
		}
		return null;
	}

	private List<RavenhurstBundle> generateBundles(File localRepository) throws RavenhurstException {
		RavenhurstMainDataContainer mainData = persistenceRegistry.getRavenhurstMainDataContainer();
		List<RavenhurstBundle> bundles = generateRequests(mainData.getUrlToLastAccessMap());
		return bundles;
	}
	/**
	 * generates requests for all repositories of all currently active profiles
	 * 
	 * @return - a {@link List} of {@link RavenhurstBundle}
	 * @throws RavenhurstException
	 *             -
	 */
	private List<RavenhurstBundle> generateRequests(Map<String, Date> urlToLastAccessDateMap) throws RavenhurstException {

		List<RavenhurstBundle> bundles = new ArrayList<RavenhurstBundle>();
		List<Profile> profiles;
		try {
			profiles = reader.getActiveProfiles();
		} catch (RepresentationException e) {
			String msg = "cannot retrieve currently active profiles";
			throw new RavenhurstException(msg, e);
		}

		for (Profile profile : profiles) {
			String profileId = profile.getId();
			if (inhibitedProfileIds != null && inhibitedProfileIds.contains(profileId)) {
				if (log.isDebugEnabled()) {
					log.debug("profile [" + profileId + "] is marked as inhibited, skipping");
				}
				continue;
			}
			List<RavenhurstBundle> contextsToAdd = generateRequests(profileId, urlToLastAccessDateMap);
			bundles.addAll(contextsToAdd);
		}

		return bundles;
	}

	/**
	 * generate requests for all repositories of a certain profile
	 * 
	 * @throws RavenhurstException
	 *             -
	 */
	private List<RavenhurstBundle> generateRequests(String profileId, Map<String, Date> urlToLastAccessDateMap) throws RavenhurstException {
		String mcMode = getVirtualEnvironment().getEnv(MC_CONNECTIVITY_MODE);
		boolean offline = false;
		if (mcMode != null && mcMode.equalsIgnoreCase(MODE_OFFLINE)) {
			offline = true;
			log.warn("MC mode [" + MC_CONNECTIVITY_MODE + "] is set to [" + MODE_OFFLINE + "]. Marking all repositories as inaccessible");
		}

		List<RavenhurstBundle> bundles = new ArrayList<RavenhurstBundle>();
		Set<String> processedUrl = new HashSet<String>();
		List<RemoteRepository> remoteRepositoriesOfProfile;
		try {
			remoteRepositoriesOfProfile = reader.getRemoteRepositoriesOfProfile(profileId);
		} catch (RepresentationException e) {
			String msg = "cannot read remote profiles of profile [" + profileId + "]";
			log.error(msg, e);
			throw new RavenhurstException(msg, e);
		}

		for (RemoteRepository remoteRepository : remoteRepositoriesOfProfile) {
			String repositoryId = remoteRepository.getName();
			if (inhibitedRepositoryIds != null && inhibitedRepositoryIds.contains(repositoryId)) {
				if (log.isDebugEnabled()) {
					log.debug("repository [" + repositoryId + "] of profile [" + profileId + "] is marked as inhibited, skipping");
				}
				continue;
			}
			// get the url - default is the same of the repository
			String repositoryRavenhurstUrl = getRavenhurstParameter(profileId, repositoryId, reader, RAVENHURST_URL, null);

			String repositoryUrl = remoteRepository.getUrl();
			if (inhibitedRepositoryUrls != null && inhibitedRepositoryUrls.contains(repositoryUrl)) {
				if (log.isDebugEnabled()) {
					log.debug("url [" + repositoryUrl + "] belonging to repository [" + repositoryId + "] of profile [" + profileId
							+ "] is marked as inhibited, skipping");
				}
				continue;
			}

			// duplicate check?
			if (processedUrl.contains(repositoryUrl)) {
				continue;
			} else {
				processedUrl.add(repositoryUrl);
			}
			String protocol = null;
			if (repositoryRavenhurstUrl == null) {
				try {
					URL url = new URL(repositoryUrl);
					// check port, if port's -1, then default port's meant
					int port = url.getPort();
					if (port != -1) {
						repositoryRavenhurstUrl = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + url.getPath();
					} else {
						repositoryRavenhurstUrl = url.getProtocol() + "://" + url.getHost() + url.getPath();
					}
					protocol = url.getProtocol();

				} catch (MalformedURLException e) {
					String msg = "url [" + repositoryUrl + "] of repository [" + repositoryId + "] of profile [" + profileId + "] is not valid";
					log.error(msg, e);
					continue;
				}
			}
			// context (i.e. add on to the url)
			String context = getRavenhurstParameter(profileId, repositoryId, reader, RAVENHURST_CONTEXT, RAVENHURST_DEFAULT_CONTEXT);
			String function = getRavenhurstParameter(profileId, repositoryId, reader, RAVENHURST_FUNCTION, RAVENHURST_DEFAULT_FUNCTION);
			String parameter = getRavenhurstParameter(profileId, repositoryId, reader, RAVENHURST_PARAMETER, RAVENHURST_DEFAULT_PARAMETER);
			String format = getRavenhurstParameter(profileId, repositoryId, reader, RAVENHURST_FORMAT, RAVENHURST_DEFAULT_FORMAT);

			// determine the interrogation access key : protocol is now either file:// or http://

			String interrogationClientKey = getRavenhurstParameter(profileId, repositoryId, reader, RAVENHURST_INTERROGATION_CLIENT, protocol);

			String repositoryAccessClientKey = getRavenhurstParameter(profileId, repositoryId, reader, REPOSITORY_ACCESS_CLIENT, protocol);

			String indexRequest;
			if (repositoryRavenhurstUrl.endsWith("/")) {
				indexRequest = repositoryRavenhurstUrl + context + "/" + function;
			} else {
				indexRequest = repositoryRavenhurstUrl + "/" + context + "/" + function;
			}

			String fullRequest;
			// find last time we accessed this repository
			Date date;
			if (urlToLastAccessDateMap != null) {
				date = urlToLastAccessDateMap.get(repositoryUrl);
				if (date != null) {
					// anything found? use it
					String timestamp = generateTimestamp(date, format);
					if (repositoryRavenhurstUrl.endsWith("/")) {
						fullRequest = repositoryRavenhurstUrl + context + "/" + function + "/?" + parameter + "=" + timestamp;
					} else {
						fullRequest = repositoryRavenhurstUrl + "/" + context + "/" + function + "/?" + parameter + "=" + timestamp;
					}
				} else {
					// nothing? don't send the time-stamp as this is a first time access to the repository..
					fullRequest = indexRequest;
				}

				// set interrogation date to now.
				Date now = new Date();
				urlToLastAccessDateMap.put(repositoryUrl, now);
			} else {
				// this is a test run ..
				date = new Date();
				String timestamp = generateTimestamp(date, format);
				fullRequest = repositoryRavenhurstUrl + "/" + context + "/" + function + "/?" + parameter + "=" + timestamp;
			}
			//
			// find server
			//
			Server server = null;
			Mirror mirror = null;
			try {
				mirror = reader.getMirror(repositoryId, repositoryUrl);
			} catch (RepresentationException e) {
				String msg = "cannot retrieve mirror of repository [" + repositoryId + "]";
				log.error(msg, e);
				continue;
			}
			if (mirror != null) {
				try {
					String mirrorId = mirror.getId();
					server = reader.getServerById(mirrorId);
				} catch (RepresentationException e) {
					String msg = "remote repository [" + repositoryId + "] of profile [" + profileId + "]'s mirror has no configured server";
					log.error(msg, e);
					continue;
				}
			} else {
				try {
					server = reader.getServerById(repositoryId);
				} catch (RepresentationException e) {
					String msg = "remote repository [" + repositoryId + "] of profile [" + profileId + "] has no configured server";
					log.error(msg, e);
					continue;
				}
			}
			if (date == null) {
				date = new Date();
			}

			// if mirror is set, we must override the repository url with the one from the server
			if (mirror != null) {
				repositoryUrl = mirror.getUrl();
			}

			// filter expression
			String indexPropertyName = INDEX_DECLARATION_PROPERTY_PREFIX + "-" + repositoryId;
			String indexPropertyValue = getPropertyOfRepository(reader, profileId, indexPropertyName);

			//
			// determine
			//
			RavenhurstBundle bundle = RavenhurstBundle.T.create();
			bundle.setProfileId(profileId);
			bundle.setDate(date); // date is actually the last interrogation and NOT the time-stamp of the response.
			bundle.setRepositoryId(repositoryId);
			bundle.setRepositoryUrl(repositoryUrl);
			bundle.setIndexFilterExpression(indexPropertyValue);

			RavenhurstRequest request = RavenhurstRequest.T.create();
			request.setServer(server);
			bundle.setRavenhurstRequest(request);
			request.setIndexUrl(indexRequest);
			request.setUrl(fullRequest);

			bundle.setRavenhurstClientKey(interrogationClientKey);

			bundle.setRepositoryClientKey(repositoryAccessClientKey);

			// settings for release
			ContentSettings settings = remoteRepository.getReleaseSettings();

			CrcFailBehavior crcFailBehaviorForRelease = settings.getCrcFailBehavior();
			switch (crcFailBehaviorForRelease) {
				case fail:
					bundle.setFailOnCrcMismatchForRelease(true);
					bundle.setCrcValidationLevelForRelease(CrcValidationLevelForBundle.fail);
					break;
				case warn:
					bundle.setFailOnCrcMismatchForRelease(false);
					bundle.setCrcValidationLevelForRelease(CrcValidationLevelForBundle.warn);
					break;
				default:
				case ignore:
					bundle.setFailOnCrcMismatchForRelease(false);
					bundle.setCrcValidationLevelForRelease(CrcValidationLevelForBundle.ignore);
					break;
			}

			boolean enabledForRelease = settings.getEnabled();
			bundle.setRelevantForRelease(enabledForRelease);
			// if not enabled, set update policy to never (rather than default daily)
			if (enabledForRelease) {
				bundle.setUpdateIntervalForRelease(settings.getUpdateInterval());
			} else {
				bundle.setUpdateIntervalForRelease(-1);
			}

			settings = remoteRepository.getSnapshotSettings();
			CrcFailBehavior crcFailBehaviorForSnapshots = settings.getCrcFailBehavior();
			switch (crcFailBehaviorForSnapshots) {
				case fail:
					bundle.setFailOnCrcMismatchForSnapshot(true);
					bundle.setCrcValidationLevelForSnapshot(CrcValidationLevelForBundle.fail);
					break;
				case warn:
					bundle.setFailOnCrcMismatchForSnapshot(false);
					bundle.setCrcValidationLevelForSnapshot(CrcValidationLevelForBundle.warn);
					break;
				default:
				case ignore:
					bundle.setFailOnCrcMismatchForSnapshot(false);
					bundle.setCrcValidationLevelForSnapshot(CrcValidationLevelForBundle.ignore);
					break;
			}
			boolean enabledForSnapshot = settings.getEnabled();
			bundle.setRelevantForSnapshot(enabledForSnapshot);
			// if not enabled, set update policy to never (rather than default daily)
			if (enabledForSnapshot) {
				bundle.setUpdateIntervalForSnapshot(settings.getUpdateInterval());
			} else {
				bundle.setUpdateIntervalForSnapshot(-1);
			}

			// mark repository as dynamic & automatically as trustworthy
			try {
				boolean isDynamic = reader.isDynamicRepository(profileId, repositoryId, repositoryUrl);
				bundle.setDynamicRepository(isDynamic);
				if (isDynamic) {
					bundle.setTrustworthyRepository(true);
					bundle.setListingLenient(true);
				}
				if (isDynamic) {
					// dynamics are "never" in Maven settings
					bundle.setUpdateIntervalForRelease(-1);
					bundle.setUpdateIntervalForSnapshot(-1);
				}
			} catch (RepresentationException e) {
				bundle.setDynamicRepository(false);
			}
			// not marked as dynamic, test if it's marked as trustworthy
			if (!bundle.getDynamicRepository()) {
				try {
					boolean isTrustworthy = reader.isTrustworthyRepository(profileId, repositoryId, repositoryUrl);
					bundle.setTrustworthyRepository(isTrustworthy);
				} catch (RepresentationException e) {
					bundle.setTrustworthyRepository(false);
				}
				try {
					boolean isListingLenient = reader.isListingLenientRepository(profileId, repositoryId, repositoryUrl);
					bundle.setListingLenient(isListingLenient);
				} catch (RepresentationException e) {
					bundle.setTrustworthyRepository(false);
				}
			}

			// marked as weakly certified - use lenient ssl treatement
			try {
				boolean isWeakCertified = reader.isWeaklyCertifiedRepository(profileId, repositoryId, repositoryUrl);
				bundle.setWeaklyCertified(isWeakCertified);
			} catch (RepresentationException e) {
				bundle.setTrustworthyRepository(false);
			}

			// check for offline mode
			if (offline) {
				bundle.setInaccessible(true);
			}

			bundles.add(bundle);
		}
		return bundles;
	}

	/**
	 * generate a timestamp that can be sent via a REST call
	 * 
	 * @param date
	 *            - the {@link Date} to convert into a timestamp
	 * @param format
	 *            - the format to use for the {@link SimpleDateFormat}
	 * @return - the formatted time stamp
	 */
	private String generateTimestamp(Date date, String format) {
		String timestamp = new DateCodec(format).encode(date);
		String formattedTimestamp = null;
		try {
			formattedTimestamp = URLEncoder.encode(timestamp, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			//
			;
		}
		return formattedTimestamp;
	}
	/**
	 * access a property of a profile
	 * 
	 * @param reader
	 *            - the {@link MavenSettingsReader}
	 * @param profileName
	 *            - the name (or id) of the profile
	 * @param propertyName
	 *            - the name of the property to look for
	 * @return - the property's value
	 */
	private String getPropertyOfRepository(MavenSettingsReader reader, String profileName, String propertyName) {
		try {
			Map<String, String> properties = reader.getProperties(profileName, propertyName);
			if (properties == null || properties.size() == 0)
				return null;
			return properties.get(propertyName);
		} catch (RepresentationException e) {
			return null;
		}
	}

	/**
	 * returns a property with the following logic : <br/>
	 * if a localized parameter's found, it is used, format: &lt;name of property&gt;-&lt;repository id&gt; <br/>
	 * if a generalized parameter's found, it is used, format : &lt;name of property&gt;<br/>
	 * otherwise the global value is used
	 * 
	 * @param profileName
	 *            - the name of the profile
	 * @param repoName
	 *            - the name of the repository
	 * @param reader
	 *            - the {@link MavenSettingsReader} to access the profile
	 * @param key
	 *            - the name of the property (common name)
	 * @param defaultValue
	 *            - the default value
	 * @return - the determined value
	 */
	private String getRavenhurstParameter(String profileName, String repoName, MavenSettingsReader reader, String key, String defaultValue) {
		String result = defaultValue;
		// scan for dedicated value
		String dedicatedKey = key + "-" + repoName;
		result = getPropertyOfRepository(reader, profileName, dedicatedKey);
		if (result != null)
			return result;
		// scan for common value
		result = getPropertyOfRepository(reader, profileName, key);
		if (result != null)
			return result;
		// use default
		return defaultValue;
	}

	/**
	 * @return - a {@link Map} of repository id to {@link ArtifactFilterExpert}
	 */
	private Map<String, ArtifactFilterExpert> loadFilters() {
		String filterLocation = virtualEnvironment.getEnv(DEVROCK_REPOSITORY_CONFIGURATION);
		if (filterLocation == null) {
			return new HashMap<>();
		}
		File filterFile = new File(filterLocation);
		if (!filterFile.exists()) {
			throw new IllegalStateException(
					"artifact-filter file [" + filterLocation + "] pointed to by [" + DEVROCK_REPOSITORY_CONFIGURATION + "] doesn't exist");
		}
		try (InputStream in = new BufferedInputStream(new FileInputStream(filterFile))) {
			repositoryConfiguration = (RepositoryConfiguration) yamlMarshaller.unmarshall(in, options);
		} catch (Exception e) {
			throw new IllegalStateException("cannot unmarshal injected artifact-filter file [" + filterLocation + "] pointed to by ["
					+ DEVROCK_REPOSITORY_CONFIGURATION + "] ", e);
		}

		Map<String, ArtifactFilterExpert> map = new HashMap<>();
		List<Repository> repositories = repositoryConfiguration.getRepositories();
		if (repositories == null) {
			return map;
		}
		for (Repository repository : repositories) {
			ArtifactFilter filter = repository.getArtifactFilter();
			if (filter != null) {
				ArtifactFilterExpert expert = ArtifactFilters.forDenotation(filter);
				map.put(repository.getName(), expert);
			}
		}

		File repositoryViewResolutionFile = new File(filterFile.getParentFile(), "repository-view-resolution.yaml");

		if (repositoryViewResolutionFile.exists()) {
			GmDeserializationOptions deserializationOptions = GmDeserializationOptions.defaultOptions.derive()
					.setInferredRootType(RepositoryViewResolution.T).set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic).build();
			try (InputStream in = new BufferedInputStream(new FileInputStream(repositoryViewResolutionFile))) {
				repositoryViewResolution = (RepositoryViewResolution) yamlMarshaller.unmarshall(in, deserializationOptions);
			} catch (Exception e) {
				throw new IllegalStateException("cannot unmarshal injected repository-view-resolution file [" + repositoryViewResolutionFile
						+ "] pointed to by [" + DEVROCK_REPOSITORY_CONFIGURATION + "] ", e);
			}
		}

		return map;
	}

	@Override
	public RepositoryConfiguration exposeRepositoryConfiguration() {
		// ensure filters ARE loaded by triggering lazy instantiation
		repositoryToFilter.get();
		return repositoryConfiguration;
	}

	@Override
	public ArtifactFilterExpert artifactFilter(String repositoryId) {
		ArtifactFilterExpert associatedExpert = repositoryToFilter.get().get(repositoryId);
		if (associatedExpert == null) {
			return AllMatchingArtifactFilterExpert.instance;
		}
		return associatedExpert;
	}

	public static void main(String[] args) {
		Date date = new Date();
		System.out.println(new RavenhurstScopeImpl().generateTimestamp(date, RAVENHURST_DEFAULT_FORMAT));
	}
}
