// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.json.genericmodel.GenericModelJsonStringCodec;
import com.braintribe.logging.Logger;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;
import com.braintribe.utils.IOTools;

/**
 * persistence expert for ravenhurst interrogation information - {@link RavenhurstBundle} 
 * @author pit
 *
 */
public class CommonFileRavenhurstPersistenceExpertForRavenhurstBundle implements RavenhurstPersistenceExpertForRavenhurstBundle {
	private static final String MARKER_TERMINATOR = System.lineSeparator() + "|#|TERMINATOR|#|" + System.lineSeparator();
	private static Logger log = Logger.getLogger(CommonFileRavenhurstPersistenceExpertForRavenhurstBundle.class);
	private GenericModelJsonStringCodec<RavenhurstBundle> codec = new GenericModelJsonStringCodec<RavenhurstBundle>();
	private LockFactory lockFactory;
	
	@Configurable @Required
	public void setLockFactory(LockFactory lockFactory) {
		this.lockFactory = lockFactory;
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.RavenhustPersistenceExpertForRavenhurstBundle#decodePerStringSearch(java.io.File)
	 */
	@Override
	public List<RavenhurstBundle> decodePerStringSearch( File file) throws RavenhurstException {
		Lock semaphore = lockFactory.getLockInstance(file).readLock();
		try {
			semaphore.lock();
			
			String contents = IOTools.slurp(file, "UTF-8");
			
			// split file 
			List<String> values = new ArrayList<String>();			
			do {
				int p = contents.indexOf(MARKER_TERMINATOR);
				String value;
				if (p < 0) {
					if (contents.length() == 0) {
						break;
					}
					else {
						p = 0;
						value = contents;
					}
				}
				else {
					value = contents.substring(0, p);
				
				}
				values.add( value);
				if (p == 0)
					break;
				contents = contents.substring( p + MARKER_TERMINATOR.length());
				
			} while (true);

			// decode bundles 
			List<RavenhurstBundle> bundles = new ArrayList<RavenhurstBundle>( values.size());				
			for (String value : values) {
				if (value.length() > 0) {
					RavenhurstBundle bundle =  codec.decode( value);
					bundles.add(bundle);
				}
			}
			return bundles;
		} catch (IOException e) {
			String msg ="cannot read file [" + file.getAbsolutePath() + "]";
			log.error( msg, e);
			throw new RavenhurstException(msg, e);
		} catch (CodecException e) {
			String msg ="cannot decode contents of file [" + file.getAbsolutePath() + "]";
			log.error( msg, e);
			throw new RavenhurstException(msg, e);
		}
		finally {
			semaphore.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.RavenhustPersistenceExpertForRavenhurstBundle#decode(java.io.File)
	 */
	@Override
	public List<RavenhurstBundle> bulkDecode( File file) throws RavenhurstException {
		Lock semaphore = lockFactory.getLockInstance(file).readLock();
		try {
			semaphore.lock();
			
			String contents = IOTools.slurp(file, "UTF-8");
			String values [] = contents.split( "\\|#\\|TERMINATOR\\|#\\|");
								
			// decode bundles 
			int length = values.length;
			int i=0;
			List<RavenhurstBundle> bundles = new ArrayList<RavenhurstBundle>( length);				
			
				for (; i < length; i++) {				
					String value = values[i];
					if (value.trim().length() > 0) {
						try {
							RavenhurstBundle bundle =  codec.decode( value);
							bundles.add(bundle);
						} catch (CodecException e) {
							String msg ="decode error in bundle [" + i + "] within [" + file.getAbsolutePath() + "], skipping bundle";
							log.warn( msg, e);
						}
					}
				}
			return bundles;
		} catch (IOException e) {
			String msg ="cannot read file [" + file.getAbsolutePath() + "]";
			log.error( msg, e);
			throw new RavenhurstException(msg, e);
		} 
		finally {
			semaphore.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.RavenhustPersistenceExpertForRavenhurstBundle#encode(java.io.File, java.util.List)
	 */
	@Override
	public void encode( File file, List<RavenhurstBundle> bundles) throws RavenhurstException {
		StringBuilder buffer = new StringBuilder();
		for (RavenhurstBundle bundle : bundles) {
			if (buffer.length() > 0) {
				buffer.append( MARKER_TERMINATOR);
			}
			try {
				String contents = codec.encode(bundle);
				if (contents != null && contents.trim().length() > 0) {
					buffer.append( contents);
				}
			} catch (CodecException e) {
				String msg ="cannot encode ravenhurst bundle, skipping";
				log.error( msg, e);
			}			
		}
		
		Lock semaphore = lockFactory.getLockInstance(file).writeLock();
		try {
			semaphore.lock();
			IOTools.spit(file, buffer.toString(), "UTF-8", false);
		} catch (IOException e) {
			String msg ="cannot write ravenhurst bundles to [" + file.getAbsolutePath() + "]";
			log.error(msg, e);
			throw new RavenhurstException(msg, e);
		}
		finally {
			semaphore.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.RavenhustPersistenceExpertForRavenhurstBundle#encode(java.io.File, com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle)
	 */
	@Override
	public void encode( File file, RavenhurstBundle bundle) throws RavenhurstException {
		Lock semaphore = lockFactory.getLockInstance(file).writeLock();
		try {
			String contents = codec.encode(bundle);
			if (file.exists()) {
				contents = MARKER_TERMINATOR + contents;
			}
			semaphore.lock();
			IOTools.spit(file, contents, "UTF-8", true);
		} catch (CodecException e) {
			String msg ="cannot encode contents of bundle to [" + file.getAbsolutePath() + "]";
			log.error( msg, e);
			throw new RavenhurstException(msg, e);
		} catch (IOException e) {
			String msg ="cannot append to file [" + file.getAbsolutePath() + "]";
			log.error( msg, e);
			throw new RavenhurstException(msg, e);
		}
		finally {
			semaphore.unlock();
		}
	}

	
	
	@Override
	public RavenhurstBundle decode(File file) throws RavenhurstException {
		throw new UnsupportedOperationException( "not supported by this type of expert");
	}

	public static void main( String [] args) {
		RavenhurstPersistenceExpertForRavenhurstBundle bundleExpert = new CommonFileRavenhurstPersistenceExpertForRavenhurstBundle();
		for (String arg : args) {
			File file = new File( arg);
			try {		
				List<RavenhurstBundle> bundles = bundleExpert.bulkDecode(file);
				System.out.println("Sucessfully read [" + bundles.size() + "] bundles");
				for (RavenhurstBundle bundle : bundles) {
					System.out.println("Date : " + bundle.getDate());
				}
			} catch (RavenhurstException e) {			
				e.printStackTrace();
			}
		}
	}
}
