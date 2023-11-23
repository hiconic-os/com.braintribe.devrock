package com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;

/**
 * basic implementation 
 * @author pit
 *
 */
public class BasicRavenhurstRepositoryIndexPersistenceExpert implements RavenhurstRepositoryIndexPersistenceExpert {

	private LockFactory lockFactory;

	@Override @Configurable @Required
	public void setLockFactory(LockFactory factory) {
		this.lockFactory = factory;		
	}

	@Override
	public Set<String> decode(File file) {
		Set<String> result = new HashSet<>();
		if (file.exists() == false)
			return result;
		
		Lock lock = lockFactory.getLockInstance(file).readLock();		
		lock.lock();
		
		try {
			// supports multi OS EOL formats
			List<String> lines = FileTools.readLines(file, "UTF-8");
			if (lines != null && lines.size() > 0) {
				for (String line : lines) {
					result.add( line.trim());
				}
			}
		} catch (RuntimeException e) {
			throw new IllegalStateException( "cannot read index file [" + file.getAbsolutePath() + "]", e);			
		}
		finally {
			lock.unlock();
		}
		
		return result;
	}

	@Override
	public void encode(File file, Collection<String> index) {
		update(file, index, false);		
	}

	@Override
	public void update(File file, Collection<String> index) {
		update(file, index, true);
	}
	
	/**
	 * @param file
	 * @param index
	 * @param append
	 */
	public void update(File file, Collection<String> index, boolean append) {
		if (index == null || index.size() == 0)
			return;
		
		StringBuilder sb = new StringBuilder();
		for (String s : index) {
			if (sb.length() > 0)
				sb.append( System.lineSeparator());
			sb.append( s);
		}
		String dump;
		if (file.exists()) {
			dump = System.lineSeparator() + sb.toString();
		}
		else {
			dump = sb.toString();
		}
		
		try {
			IOTools.spit(file, dump, "UTF-8", append);
		} catch (IOException e) {
			throw new IllegalStateException( "cannot write/append to index file [" + file.getAbsolutePath() + "]", e);
		}		
	}
	
}
