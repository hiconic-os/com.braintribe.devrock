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
package com.braintribe.devrock.mc.core.configuration;

import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.codec.string.DateCodec;
import com.braintribe.devrock.mc.api.repository.configuration.ArtifactChangesSynchronization;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolver;
import com.braintribe.devrock.mc.core.commons.FileCommons;
import com.braintribe.devrock.mc.core.repository.index.ArtifactIndex;
import com.braintribe.devrock.mc.core.resolver.BasicDependencyResolver;
import com.braintribe.devrock.model.mc.reason.InvalidRepositoryConfiguration;
import com.braintribe.devrock.model.mc.reason.UnresolvedDependencyVersion;
import com.braintribe.devrock.model.repository.ChangesIndexType;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.filters.AllMatchingArtifactFilter;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.CommunicationError;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.IoError;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.changes.ArtifactChanges;
import com.braintribe.model.artifact.changes.ArtifactIndexLevel;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.version.Version;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.paths.UniversalPath;

/**
 * https://artifactory.example.com/Ravenhurst/rest/devrock/changes/?timestamp=2018-06-23T14%3A59%3A40.898%2B0200
 * https://artifactory.example.com/Ravenhurst/rest/devrock/changes/
 * @author pit
 *
 */
public class BasicArtifactChangesSynchronization implements ArtifactChangesSynchronization {
	private static Logger log = Logger.getLogger(BasicArtifactChangesSynchronization.class);	
	private static final int MAX_THREAD = 5;
	private static final String RAVENHURST_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	private Function<File, ReadWriteLock> lockSupplier;
	private CloseableHttpClient httpClient;
	private YamlMarshaller marshaller;
	private Function<MavenHttpRepository, ArtifactDataResolver> artifactDataResolverFactory;
	{
		marshaller = new YamlMarshaller();
		marshaller.setWritePooled(true);
	}
	private BasicGroupFilterPersistenceExpert groupFilterExpert;
	
	@Configurable @Required
	public void setHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}
	
	@Configurable @Required
	public void setLockSupplier(Function<File, ReadWriteLock> lockSupplier) {
		this.lockSupplier = lockSupplier;
	}
	
	@Configurable @Required
	public void setGroupFilterExpert(BasicGroupFilterPersistenceExpert groupFilterExpert) {
		this.groupFilterExpert = groupFilterExpert;
	}

	@Configurable @Required
	public void setArtifactDataResolverFactory(
			Function<MavenHttpRepository, ArtifactDataResolver> artifactDataResolverFactory) {
		this.artifactDataResolverFactory = artifactDataResolverFactory;
	}
	
	/**
	 * generate a timestamp that can be sent via a REST call 
	 * @param date - the {@link Date} to convert into a timestamp
	 * @return - the formatted time stamp 
	 */
	private String generateTimestamp( Date date) {
		String timestamp = new DateCodec(RAVENHURST_DATE_FORMAT).encode(date);
		String formattedTimestamp=null;
		try {
			formattedTimestamp = URLEncoder.encode( timestamp, "UTF-8");
		} catch (UnsupportedEncodingException e) {			
			throw new IllegalStateException("format [" + RAVENHURST_DATE_FORMAT + "] is not valid", e);
		}		
		return formattedTimestamp;
	}
	private CloseableHttpResponse getResponse( String url) throws IOException {
		HttpRequestBase requestBase = new HttpGet( url);
		HttpClientContext context = HttpClientContext.create();
		CloseableHttpResponse response = httpClient.execute( requestBase, context);
		return response;
	}
	
	private Maybe<List<VersionedArtifactIdentification>> getRavenhurstResponse( String url){
		CloseableHttpResponse response = null;
		try {
			response = getResponse(url);
		} catch (IOException e1) {
			return Maybe.incomplete(Collections.emptyList(), Reasons.build(CommunicationError.T).text("cannot read RH response from [" + url + "] as [" + e1.getMessage() + "]").toReason());
		}		
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode >= 200 && statusCode < 300) {
			HttpEntity entity = response.getEntity();
			List<VersionedArtifactIdentification> result = new LinkedList<>();
			try ( BufferedReader reader = new BufferedReader( new InputStreamReader(entity.getContent(), "UTF-8"))) {				
				String line;
				while ((line = reader.readLine()) != null) 	{			
					try {
						result.add( VersionedArtifactIdentification.parse( line.trim()));
					} catch (Exception e) {						
						log.error("invalid entry [" + line.trim() + "] detected in RH response of [" + url + "]");
					}
				}
				return Maybe.complete( result);
			} catch (Exception e) {								
				return Maybe.incomplete(Collections.emptyList(), Reasons.build(CommunicationError.T).text("error processing RH response from [" + url + "], as " + e.getMessage()).toReason());				
			} 
		}		
		return Maybe.incomplete(Collections.emptyList(), Reasons.build(CommunicationError.T).text("status code RH response from [" + url + "] was [" + statusCode + "]").toReason());
	}
	
	
	
	@Override
	public Maybe<List<VersionedArtifactIdentification>> queryContents(Repository repository) {
		if (!(repository instanceof MavenHttpRepository))
			return Maybe.empty( Reasons.build( InvalidRepositoryConfiguration.T).text("repository is not a HTTP backed repository : " + repository.getName() + "]").toReason());
		
		MavenHttpRepository mavenHttpRepository = (MavenHttpRepository) repository;
	
		String url = mavenHttpRepository.getChangesUrl();
		Maybe<List<VersionedArtifactIdentification>> touchedArtifactsMaybe = getRavenhurstResponse(url);
		
		return touchedArtifactsMaybe;		 		
	}

	@Override
	public List<VersionedArtifactIdentification> queryChanges(File localRepo, Repository repository) {
		if (!(repository instanceof MavenHttpRepository))
			return Collections.emptyList();
		
		MavenHttpRepository mavenHttpRepository = (MavenHttpRepository) repository;
		// read last access date
		File lastAccessFile = new File( localRepo, "last-changes-access-" + mavenHttpRepository.getName() + ".yaml");
		ReadWriteLock lock = lockSupplier.apply(lastAccessFile);
		Lock writeLock = lock.writeLock();		
		try {
			ArtifactChanges changes = null;
			if (lastAccessFile.exists()) {
				String content = FileTools.read(lastAccessFile).asString();
				requireNonNull(content, "lastAccessFile content is null");

				try (StringReader sr = new StringReader( content)) {
					changes = (ArtifactChanges) marshaller.unmarshall(sr);					
				}
				catch (Exception e) {
					throw new IllegalStateException("cannot read from file [" + lastAccessFile.getAbsolutePath() + "], as this is its content :\n" + content, e);
				}
				if (changes == null) {
					throw new IllegalStateException("file [" + lastAccessFile.getAbsolutePath() + "] wasn't properly unmarshalled, contents is [" + content + "]");		
				}
			}
			if (changes == null) {
				changes = ArtifactChanges.T.create();
				changes.setRepositoryUrl( mavenHttpRepository.getUrl()); // required by mc-legacy
			}
			Date date = changes.getLastSynchronization();
			Date lastAccess = new Date(); // just get the timestamp before the RH call
			
			
			String basicUrl = mavenHttpRepository.getChangesUrl();
			
			final Maybe<List<VersionedArtifactIdentification>> touchedArtifactsMaybe;
			
			ChangesIndexType changesIndexType = mavenHttpRepository.getChangesIndexType();
			
			if (changesIndexType == null)
				throw new IllegalArgumentException("Repository [" + mavenHttpRepository.getName() + "] is missing changedIndexType");
			
			switch (changesIndexType) {
				case incremental:
					if (basicUrl == null) { 
						return null;
					}
					touchedArtifactsMaybe = retrieveChangesIncrementally(basicUrl, date);
					break;
				case total:
					touchedArtifactsMaybe = retrieveChangesTotally(changes, mavenHttpRepository);
					break;
				default:
					throw new UnsupportedOperationException("Repository [" + mavenHttpRepository.getName() + "] has unsupported changes index type: " + mavenHttpRepository.getChangesIndexType());
			}
			
			// only process (and store date) if a correct message was retrieved 
			if (touchedArtifactsMaybe.isSatisfied()) {
		
				List<VersionedArtifactIdentification> touchedArtifacts = touchedArtifactsMaybe.get();
				
				groupFilterExpert.appendGroupFilterings(localRepo, mavenHttpRepository, touchedArtifacts.stream().map( vai -> vai.getGroupId()).distinct().collect(Collectors.toList()));
															
				// write last-access file (if all above is successful) 
				changes.setLastSynchronization( lastAccess);
				try (OutputStream out = new FileOutputStream(lastAccessFile)) {
					marshaller.marshall( out, changes);
					return touchedArtifacts;			
				}
				catch (IOException e) {
					throw new UncheckedIOException("cannot write to file [" + lastAccessFile.getAbsolutePath() + "]", e);
				}
			}		
		}
		finally {
			writeLock.unlock();
		}		
		return Collections.emptyList();
		
	}
	
	private Maybe<List<VersionedArtifactIdentification>> retrieveChangesTotally(ArtifactChanges artifactChanges, MavenHttpRepository mavenHttpRepository) {
		ArtifactDataResolver artifactDataResolver = artifactDataResolverFactory.apply(mavenHttpRepository);
		
		BasicDependencyResolver dependencyResolver = new BasicDependencyResolver(artifactDataResolver);
		
		CompiledDependencyIdentification cdi = CompiledDependencyIdentification.create("meta", "artifact-index", "[1,)");
		
		Maybe<CompiledArtifactIdentification> caiMaybe = dependencyResolver.resolveDependency(cdi);
		
		if (caiMaybe.isUnsatisfied()) {
			if (caiMaybe.isUnsatisfiedBy(UnresolvedDependencyVersion.T)) {
				return Maybe.complete(Collections.emptyList());
			}
			return caiMaybe.whyUnsatisfied().asMaybe();
		}
		
		CompiledArtifactIdentification cai = caiMaybe.get();
		
		if (isHigherVersion(artifactChanges, cai)) {
			Maybe<ArtifactDataResolution> dataResMaybe = artifactDataResolver.resolvePart(cai, PartIdentification.create("gz"));

			if (dataResMaybe.isUnsatisfied()) {
				return dataResMaybe.whyUnsatisfied().asMaybe();
			}
			
			Maybe<InputStream> inMaybe = dataResMaybe.get().openStream();
			
			if (inMaybe.isUnsatisfied()) {
				return inMaybe.whyUnsatisfied().asMaybe();
			}
			
			try (InputStream in = new GZIPInputStream(inMaybe.get())) {
				ArtifactIndexLevel artifactIndexLevel = artifactChanges.getArtifactIndexLevel();
				ArtifactIndex index = ArtifactIndex.read(in, false, artifactIndexLevel != null? artifactIndexLevel.getSequenceNumber(): -1);
				
				List<VersionedArtifactIdentification> changedArtifacts = index.getArtifacts().stream().map(VersionedArtifactIdentification::parse).collect(Collectors.toList());
				
				if (artifactIndexLevel == null) {
					artifactIndexLevel = ArtifactIndexLevel.T.create();
					artifactChanges.setArtifactIndexLevel(artifactIndexLevel);
				}
				
				artifactIndexLevel.setSequenceNumber(index.getLastSequenceNumber());
				artifactIndexLevel.setVersion(cai.getVersion().asString());
				
				return Maybe.complete(changedArtifacts);
			}
			catch (IOException e) {
				return Reasons.build(IoError.T).text("Error while reading " + cai.asString() + " from " + mavenHttpRepository.getUrl()).cause(InternalError.from(e)).toMaybe();
			}
		}
		
		
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isHigherVersion(ArtifactChanges artifactChanges,
			CompiledArtifactIdentification compiledArtifactIdentification) {
		ArtifactIndexLevel artifactIndexLevel = artifactChanges.getArtifactIndexLevel();
		
		if (artifactIndexLevel == null)
			return true;
		
		Version version = Version.parse(artifactIndexLevel.getVersion());
		if (compiledArtifactIdentification.getVersion().compareTo(version) > 0)
			return true;
		
		return false;
	}

	private Maybe<List<VersionedArtifactIdentification>> retrieveChangesIncrementally(String basicUrl, Date date) {
		String url;
		if (date != null) {
			url = basicUrl + "/?timestamp=" + generateTimestamp(date);
		}
		else {
			url = basicUrl;
		}
		return getRavenhurstResponse(url);
	}

	/**
	 * helper class to delete the files to be purged
	 * @author pit / dirk
	 *
	 */
	private class Marker {
		private final ExecutorService es = Executors.newFixedThreadPool(MAX_THREAD);
		
		private void mark( File fileToMark) {
			es.submit( () -> purgeFile( fileToMark));
		}
		
		private void purgeFile( File file) {
			// no file to mark as outdated 
			if (!file.exists()) {
				return;
			}			
			// build marker file 
			File markerFile = FileCommons.markerFile(file);
	
			// somebody already marked the file 
			if (markerFile.exists()) {
				return;
			}
		
			// create marker file 
			Lock dropFileWriteLock = lockSupplier.apply(markerFile).writeLock();
			dropFileWriteLock.lock();			
			try {				
				markerFile.createNewFile();				
			}
			catch (IOException e) {
				throw new UncheckedIOException("cannot create marker filer [" + markerFile.getAbsolutePath() + "]", e);
			}
			finally {			
				dropFileWriteLock.unlock();
			}
		}
		
		public void awaitTermination() {
			es.shutdown();
			try {
				boolean allprocessed = es.awaitTermination(1, TimeUnit.MINUTES);
				if (!allprocessed) {
					log.error("couldn't wait for termination of the thread-pool");
				}
			} catch (InterruptedException e) {
				log.error("interuption while waiting for purge termination");
			}
		}
		
	}
	

	@Override
	public void purge(File localRepo, Map<Repository, List<VersionedArtifactIdentification>> vaisMap) {
		ArtifactChangesTreeNode node = new ArtifactChangesTreeNode("root");
		node.setNodeType( ArtifactChangesNodeType.normalFolder);
		node.setPath( localRepo);
		// determine what files need to be purged on what level 		
		for (Map.Entry<Repository, List<VersionedArtifactIdentification>> entry : vaisMap.entrySet()) {
			String repositoryId = entry.getKey().getName();
			List<VersionedArtifactIdentification> vais = entry.getValue();
			for( VersionedArtifactIdentification vai : vais) {				
				List<String> elements = UniversalPath.empty().push( vai.getGroupId(), ".").push( vai.getArtifactId()).push( vai.getVersion()).stream().collect( Collectors.toList());
				node.addPath(elements, repositoryId);				
			}
		}
		// actually drop the files
		Marker dropper = new Marker();
		node.drop( dropper::mark);
		dropper.awaitTermination();
	}

	@Override
	public ArtifactFilter getFilterForRepository( File localRepository, Repository repository) {
		ArtifactFilter groupFilter = groupFilterExpert.getGroupFilter(localRepository, repository);
		if (groupFilter != null) {
			return groupFilter;
		}		
		return AllMatchingArtifactFilter.T.create();
	}
	
}
