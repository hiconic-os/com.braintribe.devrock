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
package com.braintribe.devrock.mc.core.wirings.backend.space;

import java.io.File;
import java.net.URI;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

import org.apache.http.impl.client.CloseableHttpClient;

import com.braintribe.devrock.mc.api.deploy.ArtifactDeployer;
import com.braintribe.devrock.mc.api.repository.RepositoryProbingSupport;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolver;
import com.braintribe.devrock.mc.api.resolver.ChecksumPolicy;
import com.braintribe.devrock.mc.core.commons.ManagedFilesystemLockSupplier;
import com.braintribe.devrock.mc.core.deploy.ArtifactDeployerFactories;
import com.braintribe.devrock.mc.core.deploy.FileSystemRepositoryDeployer;
import com.braintribe.devrock.mc.core.deploy.HttpRepositoryDeployer;
import com.braintribe.devrock.mc.core.deploy.LocalRepositoryDeployer;
import com.braintribe.devrock.mc.core.http.BasicHttpUploader;
import com.braintribe.devrock.mc.core.resolver.ArtifactDataResolverFactories;
import com.braintribe.devrock.mc.core.resolver.ArtifactoryRepositoryArtifactDataResolver;
import com.braintribe.devrock.mc.core.resolver.EmptyRepositoryArtifactDataResolver;
import com.braintribe.devrock.mc.core.resolver.FailingHttpRepositoryProbingSupport;
import com.braintribe.devrock.mc.core.resolver.FilesystemRepositoryArtifactDataResolver;
import com.braintribe.devrock.mc.core.resolver.FilesystemRepositoryProbingSupport;
import com.braintribe.devrock.mc.core.resolver.HttpRepositoryArtifactDataResolver;
import com.braintribe.devrock.mc.core.resolver.HttpRepositoryProbingSupport;
import com.braintribe.devrock.mc.core.resolver.ProbingSupportFactories;
import com.braintribe.devrock.mc.core.resolver.codebase.CodebaseArtifactDataResolver;
import com.braintribe.devrock.mc.core.resolver.workspace.WorkspaceArtifactDataResolver;
import com.braintribe.devrock.mc.core.wirings.backend.contract.ArtifactDataBackendContract;
import com.braintribe.devrock.model.repository.CodebaseRepository;
import com.braintribe.devrock.model.repository.LocalRepository;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.WorkspaceRepository;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.wire.api.annotation.Managed;

/**
 * implementation of the {@link ArtifactDataBackendContract}
 * 
 * @author pit / dirk
 *
 */
@Managed
public class ArtifactDataBackendSpace implements ArtifactDataBackendContract {
	/**
	 * registers {@link RepositoryProbingSupport} producers for the currently known different {@link Repository} types 
	 * @return
	 */
	@Managed
	private ProbingSupportFactories probingSupportFactories() {
		ProbingSupportFactories bean = new ProbingSupportFactories();

		bean.register(MavenHttpRepository.T, this::httpRepositoryProbingSupport);
		bean.register(MavenFileSystemRepository.T, this::filesystemRepositoryProbingSupport);
		bean.register(CodebaseRepository.T, this::codebaseRepositoryProbingSupport);
		bean.register(LocalRepository.T, this::localRepositoryProbingSupport);
		//TODO: what is the probing support of the workspace repository? 

		return bean;
	}
	
	/**
	 * registers {@link ArtifactDataResolver} producers for the currently know different {@link Repository} types
	 * @return
	 */
	@Managed
	private ArtifactDataResolverFactories artifactDataResolverFactories() {
		ArtifactDataResolverFactories bean = new ArtifactDataResolverFactories();

		bean.register(MavenHttpRepository.T, this::httpRepository);
		bean.register(MavenFileSystemRepository.T, this::fileSystemRepository);
		bean.register(CodebaseRepository.T, this::codebaseRepository);
		bean.register(LocalRepository.T, this::localRepository);
		bean.register(WorkspaceRepository.T, this::workspaceRepository);
		
		return bean;
	}

	@Override
	@Managed
	public ArtifactDataResolver emptyRepository(Repository repository) {
		EmptyRepositoryArtifactDataResolver bean = new EmptyRepositoryArtifactDataResolver();
		return bean;
	}

	@Override
	@Managed
	public CodebaseArtifactDataResolver codebaseRepository(CodebaseRepository repository) {
		CodebaseArtifactDataResolver bean = new CodebaseArtifactDataResolver(repository.normalizedRootPath().toFile(), repository.getTemplate());
		bean.setRepositoryId( repository.getName());
		bean.setArchetypesExcludes(repository.getArchetypesExcludes());
		bean.setArchetypesIncludes(repository.getArchetypesIncludes());
		return bean;
	}
	
	@Override
	@Managed
	public WorkspaceArtifactDataResolver workspaceRepository(WorkspaceRepository repository) {
		WorkspaceArtifactDataResolver bean = new WorkspaceArtifactDataResolver();
		bean.setRepositoryId( repository.getName());	
		bean.setArtifacts( repository.getArtifacts());
		return bean;
	}
	
	
	@Override
	@Managed
	public ArtifactDataResolver fileSystemRepository(MavenFileSystemRepository repository) {
		FilesystemRepositoryArtifactDataResolver bean = new FilesystemRepositoryArtifactDataResolver();
		bean.setRepositoryId(repository.getName());
		bean.setRoot(repository.normalizedRootPath().toFile());			
		return bean;
	}
	
	@Override
	@Managed
	public CloseableHttpClient httpClient() {
		DefaultHttpClientProvider bean = new DefaultHttpClientProvider();
		bean.setSocketTimeout(5_000);
		
		try {
			return bean.provideHttpClient();
		} catch (Exception e) {
			throw new IllegalStateException("cannot setup http client", e);
		}				
	}
	
	@Override
	public ArtifactDataResolver httpRepository(MavenHttpRepository repository) {
		switch (repository.getRestSupport()) {
			case artifactory:
				return artifactoryRepository( repository);
			default:
				return standardHttpRepository(repository);
		}
	}
	
	@Override
	public BasicHttpUploader httpUploader() {
		BasicHttpUploader bean = new BasicHttpUploader();
		bean.setHttpClient(httpClient());
		return bean;
	}
	
	private ChecksumPolicy transpose( com.braintribe.devrock.model.repository.ChecksumPolicy policy) {
		return ChecksumPolicy.valueOf( policy.name());
	}
	
	@Override
	@Managed
	public ArtifactDataResolver standardHttpRepository(MavenHttpRepository repository) {
		HttpRepositoryArtifactDataResolver bean = new HttpRepositoryArtifactDataResolver();
		bean.setRepositoryId( repository.getName());
		bean.setRoot( repository.getUrl());
		bean.setHttpClient(httpClient());
		bean.setPassword( repository.getPassword());
		bean.setUserName( repository.getUser());
		
		com.braintribe.devrock.model.repository.ChecksumPolicy checkSumPolicy = repository.getCheckSumPolicy();
		if (checkSumPolicy != null) {
			bean.setChecksumPolicy( transpose(checkSumPolicy));
		}
		return bean;
	}
	
	@Override
	@Managed
	public ArtifactDataResolver artifactoryRepository(MavenHttpRepository repository) {
		ArtifactoryRepositoryArtifactDataResolver bean = new ArtifactoryRepositoryArtifactDataResolver();
		bean.setRepositoryId( repository.getName());
		bean.setRoot( repository.getUrl());
		bean.setHttpClient(httpClient());
		bean.setPassword( repository.getPassword());
		bean.setUserName( repository.getUser());
		return bean;
	}
	
	/**
	 * @param repository - the {@link LocalRepository}
	 * @return - a 'empty' {@link ArtifactDataResolver} as it's not backed by a 'real' repository
	 */
	public ArtifactDataResolver localRepository(LocalRepository repository) {
		return EmptyRepositoryArtifactDataResolver.instance;
	}

	@Override
	public ArtifactDataResolver repository(Repository repository) {	
		if (repository.getOffline()) {
			return emptyRepository(repository);
		}
		
		return artifactDataResolverFactories().get(repository).apply(repository);
	}
	
	/**
	 * @param repository - the {@link Repository} (a filesystem based repo) 
	 * @return - the respective {@link FilesystemRepositoryProbingSupport}
	 */
	@Managed
	private FilesystemRepositoryProbingSupport filesystemRepositoryProbingSupport(MavenFileSystemRepository repository) {
		FilesystemRepositoryProbingSupport bean = new FilesystemRepositoryProbingSupport();
		bean.setRepositoryId( repository.getName());
		bean.setRoot(repository.normalizedRootPath().toFile());		
		return bean;
	}
	
	/**
	 * @param repository - the {@link CodebaseRepository} (a special file system repo backed by sources)
	 * @return - the respective {@link FilesystemRepositoryProbingSupport}
	 */
	@Managed
	private FilesystemRepositoryProbingSupport codebaseRepositoryProbingSupport(CodebaseRepository repository) {
		FilesystemRepositoryProbingSupport bean = new FilesystemRepositoryProbingSupport();
		bean.setRepositoryId( repository.getName());
		bean.setRoot(repository.normalizedRootPath().toFile());		
		return bean;
	}
	
	/**
	 * @param repository - the {@link Repository} (a filesystem based repo)
	 * @return - the respective {@link FileRepositoryProbingSupport}
	 */
	
	@Managed
	private FilesystemRepositoryProbingSupport localRepositoryProbingSupport(LocalRepository repository) {
		FilesystemRepositoryProbingSupport bean = new FilesystemRepositoryProbingSupport();
		bean.setRepositoryId( repository.getName());
		bean.setRoot(repository.normalizedRootPath().toFile());		
		return bean;
	}
	
	/**
	 * @param repository - a {@link MavenHttpRepository} 
	 * @return - the appropriate {@link HttpRepositoryProbingSupport}
	 */
	@Managed
	private RepositoryProbingSupport httpRepositoryProbingSupport(MavenHttpRepository repository) {
		
		// probing path may be overriden  
		
		String probingPath = repository.getProbingPath();
		
		if (probingPath != null) {
			URI uri;
			try {
				uri = URI.create(probingPath);
				
				if (!uri.isAbsolute())
					probingPath = repository.getUrl() + probingPath;
				
			} catch (IllegalArgumentException e) {
				Reason failure = Reasons.build(InvalidArgument.T).text("The probing path of repository " + repository.getName() + " is an invalid URI").toReason();
				return new FailingHttpRepositoryProbingSupport(repository.getName(), failure);
			}
			
		}
		else {
			probingPath = repository.getUrl();
		}

		HttpRepositoryProbingSupport bean = new HttpRepositoryProbingSupport();
		bean.setRepositoryId( repository.getName());
		bean.setRoot( probingPath);
		// probing method may be overriden
		bean.setProbingMethod( repository.getProbingMethod());
		
		bean.setHttpClient(httpClient());
		bean.setPassword( repository.getPassword());
		bean.setUserName( repository.getUser());
		return bean;
	}

	
	@Override
	public RepositoryProbingSupport probingSupport(Repository repository) {
		return probingSupportFactories().get(repository).apply(repository);
	}
	
	@Override
	public ArtifactDeployer artifactDeployer(Repository repository) {
		return artifactDeployerFactories().get(repository).apply(repository);
	}

	/**
	 * registers {@link ArtifactDataResolver} producers for the currently know different {@link Repository} types
	 * @return
	 */
	@Managed
	private ArtifactDeployerFactories artifactDeployerFactories() {
		ArtifactDeployerFactories bean = new ArtifactDeployerFactories();

		bean.register(MavenHttpRepository.T, this::httpDeployer);
		bean.register(MavenFileSystemRepository.T, this::fileSystemDeployer);
		bean.register(LocalRepository.T, this::localDeployer);
		
		return bean;
	}
	
	@Managed
	private HttpRepositoryDeployer httpDeployer(MavenHttpRepository repository) {
		HttpRepositoryDeployer bean = new HttpRepositoryDeployer();
		bean.setRepository(repository);
		bean.setHttpClient(httpClient());
		return bean;
	}
	
	@Managed
	private FileSystemRepositoryDeployer fileSystemDeployer(MavenFileSystemRepository repository) {
		FileSystemRepositoryDeployer bean = new FileSystemRepositoryDeployer();
		bean.setRepository(repository);
		return bean;
	}
	
	@Managed
	private LocalRepositoryDeployer localDeployer(LocalRepository repository) {
		LocalRepositoryDeployer bean = new LocalRepositoryDeployer();
		bean.setRepository(repository);
		return bean;
	}
	

	@Override
	@Managed
	public Function<File, ReadWriteLock> lockSupplier() {
		ManagedFilesystemLockSupplier bean = new ManagedFilesystemLockSupplier();
		
		return bean;
	}

	

}
