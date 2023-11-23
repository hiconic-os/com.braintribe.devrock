package com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.bias;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.Lock;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.RepositoryPersistenceException;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.utils.IOTools;


public class ArtifactBiasPersitenceExpert {
	public static final String BIAS_FILENAME =".pc_bias";
	private MavenSettingsReader reader;
	private LockFactory lockFactory;
	private LocalRepositoryLocationProvider localRepositoryLocationProvider;
	
	@Configurable @Required
	public void setLockFactory(LockFactory lockFactory) {
		this.lockFactory = lockFactory;
	}
	
	@Configurable @Required
	public void setLocalRepositoryLocationProvider(LocalRepositoryLocationProvider localRepositoryLocationProvider) {
		this.localRepositoryLocationProvider = localRepositoryLocationProvider;
	}
	
	public File getPersistenceContainerLocation() throws RepositoryPersistenceException {
		String localRepository;
		try {
			localRepository = localRepositoryLocationProvider.getLocalRepository(null);
		} catch (RepresentationException e) {
			String msg ="cannot find publishing container bias file [" + BIAS_FILENAME +"] as local repository cannot be found";
			throw new RepositoryPersistenceException( msg, e);
		}
		File file = new File( localRepository);
		return file;	
	}
	
	public File getPersistenceContainerFile() throws RepositoryPersistenceException {
		return new File( getPersistenceContainerLocation(), BIAS_FILENAME);
	}
	
	public List<ArtifactBias> decode() throws RepositoryPersistenceException {
		List<ArtifactBias> result = new ArrayList<>();
		File file = getPersistenceContainerFile();

		if (file.exists() == false) {
			return result;
		}
		Lock semaphore = lockFactory.getLockInstance( file).readLock();

		try {
			semaphore.lock();
			String contents = IOTools.slurp( file, "UTF-8");
			String [] lines = contents.split("\n");
			for (String line : lines) {
				line = line.trim();
				if (line.length() == 0)
					continue;
				
				ArtifactBias bias = new ArtifactBias(line);
				
			
				result.add( bias);
			}
			result.sort( new Comparator<ArtifactBias>() {

				@Override
				public int compare(ArtifactBias o1, ArtifactBias o2) {					
					return o1.getIdentification().getGroupId().compareToIgnoreCase( o2.getIdentification().getGroupId());
				}				
			});
			return result;
		} catch (IOException e) {
			String msg ="cannot read publishing container bias file [" + BIAS_FILENAME +"]";
			throw new RepositoryPersistenceException( msg, e);
		}
		finally {
			semaphore.unlock();
		}
		
	}
	
	public void encode( List<ArtifactBias> biased) throws RepositoryPersistenceException {
		File file = getPersistenceContainerFile();
		Lock semaphore = lockFactory.getLockInstance( file).writeLock();
		try {
			semaphore.lock();
			
			StringBuilder builder = new StringBuilder(); 
			for (ArtifactBias bias : biased) {
				if (builder.length() > 0) {
					builder.append("\n");
				}
				builder.append( bias.toString());				
			}
			IOTools.spit(file, builder.toString(), "UTF-8", false);
		} catch (IOException e) {
			String msg ="cannot read publishing container bias file [" + BIAS_FILENAME +"]";
			throw new RepositoryPersistenceException( msg, e);
		}
		finally {
			semaphore.unlock();
		}
	}
	
}
