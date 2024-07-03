// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package com.braintribe.devrock.mc.core.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.mc.api.repository.configuration.HasConnectivityTokens;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.changes.RepositoryProbingResult;

/**
 * simple persistence expert that can read/write {@link RepositoryProbingResult} in YAML form
 * @author pit
 *
 */
public class ProbingResultPersistenceExpert implements HasConnectivityTokens {
	private YamlMarshaller marshaller;
	{
		marshaller = new YamlMarshaller();
		marshaller.setWritePooled(true);
	}
	private Function<File, ReadWriteLock> lockSupplier;
	
	@Configurable @Required
	public void setLockSupplier(Function<File, ReadWriteLock> lockSupplier) {
		this.lockSupplier = lockSupplier;
	}
	
	/**
	 * get the correct location and name of the persisted data file
	 * @param repository - the {@link Repository} whose data it is
	 * @param localRepository - the {@link File} representing the 'local repository'
	 * @return - the {@link File} pointing to the respective file for the 'last probing result'
	 */
	public File getFile(Repository repository, File localRepository) {
		File file = new File( localRepository, LAST_PROBING_RESULT + "-" + repository.getName() + ".yaml");
		return file;
	}
	
	/**
	 * write probing result 
	 * @param probingResult - the {@link RepositoryProbingResult} to persist
	 * @param repository - the respective {@link Repository}
	 * @param localRepository - the {@link File} pointing the to 'local repository'
	 */
	public void writeProbingResult( RepositoryProbingResult probingResult, Repository repository, File localRepository) {
		File file = getFile(repository, localRepository);
		// lock					
		ReadWriteLock readWriteLock = lockSupplier.apply(file);		
		Lock writeLock = readWriteLock.writeLock();
		
		writeLock.lock();
		
		try ( OutputStream out = new FileOutputStream( file)) {
			marshaller.marshall(out, probingResult);
		} 
		catch( Exception e) {
			Exceptions.unchecked(e, "cannot write last probing result for repository [" + repository.getName() + "] to [" + file.getAbsolutePath() + "]", IllegalStateException::new);
		}
		finally {
			writeLock.unlock();
		}
	}
	
	/**
	 * load probing result
	 * @param repository - the respective {@link Repository}
	 * @param localRepository - the {@link File} pointing to the 'local repository'
	 * @return - the {@link RepositoryProbingResult} read
	 */
	public RepositoryProbingResult readProbingResult( Repository repository, File localRepository) {
		File file = getFile(repository, localRepository);
		if (!file.exists()) {
			return null;
		}
		// lock					
		ReadWriteLock readWriteLock = lockSupplier.apply(file);		
		Lock writeLock = readWriteLock.writeLock();
		
		writeLock.lock();
		
		try ( InputStream in = new FileInputStream(file)){
			return (RepositoryProbingResult) marshaller.unmarshall(in);
		}
		catch( Exception e) {
			throw Exceptions.unchecked(e, "cannot read last probing result for repository [" + repository.getName() + "] to [" + file.getAbsolutePath() + "]", IllegalStateException::new);
		}
		finally {
			writeLock.unlock();
		}
	}
}
