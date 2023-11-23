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
package com.braintribe.devrock.mc.core.commons;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.devrock.mc.api.event.EventBroadcaster;
import com.braintribe.devrock.model.mc.core.event.OnPartDownloading;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.CommunicationError;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.IoError;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.StopWatch;

/**
 * helper class that can download a file
 * @author pit / dirk
 *
 */
public class Downloads {
	private static String downloadExtension =".download";
	private static Logger logger = Logger.getLogger(Downloads.class);
	
	/**
	 * transactional download of a single file 
	 * @param file - the local target {@link File}
	 * @param inputStreamProvider - the {@link InputStreamProvider} that delivers the stream
	 * @throws IOException - a {@link FileNotFoundException} or an {@link UncheckedIOException}
	 */
	public static Reason downloadReasoned( File file, Supplier<Maybe<InputStream>> inputStreamProvider) {
		File downloadFile = new File( file.getParentFile(), file.getName() + downloadExtension);
		
		logger.debug("downloading " + file.getName());
		StopWatch watch = new StopWatch(); 
		try {
			int tries = 0;
			int maxTries = 3;
			
			while (true) {
				
				Maybe<InputStream> maybeIn = inputStreamProvider.get();
				
				List<Reason> communicationErrors = null;
				Reason communicationError = null;
				
				if (maybeIn.isSatisfied()) {
					try (InputStream in = maybeIn.get(); OutputStream out = new FileOutputStream(downloadFile);
							) {
						IOTools.transferBytes(in, out, IOTools.BUFFER_SUPPLIER_64K);			
					}
					catch (IOException e) {
						communicationError = Reasons.build(CommunicationError.T).text("Download try " + (tries + 1) + " failed") //
								.cause(InternalError.from(e)).toReason(); 
					}
					catch (Exception e) {
						return InternalError.from(e);
					}
				}
				else {
					Reason whyUnsatisfied = maybeIn.whyUnsatisfied();
					
					if (whyUnsatisfied instanceof NotFound) {
						return whyUnsatisfied;
					}
					else if (whyUnsatisfied instanceof CommunicationError) {
						communicationError = whyUnsatisfied;
					}
					
					return whyUnsatisfied;
				}
				
				if (communicationError != null) {
					if (communicationErrors == null)
						communicationErrors = new ArrayList<>(maxTries);

					communicationErrors.add(communicationError);
					
					if ((tries++) > maxTries) {
						CommunicationError reason = Reasons.build(CommunicationError.T).text("Download failed after retrying with max retries = " + maxTries).toReason();
						reason.getReasons().addAll(communicationErrors);
						return reason;
					}
					
					logger.warn("try " + tries + " of " + maxTries + " to download " + file.getName() + " failed.");
					
					continue;
				}
				
				break;
			}
			
			logger.debug("downloaded " + file.getName() + " in " + watch.getElapsedTime() + "ms");
			
			if (file.exists()) {
				file.delete();
			}
			
			downloadFile.renameTo(file);		
		}
		
		finally {
			if (downloadFile.exists()) {
				downloadFile.delete();
			}			
		}
		
		return null;
	}
	
	/**
	 * transactional download of a single file 
	 * @param file - the local target {@link File}
	 * @param inputStreamProvider - the {@link InputStreamProvider} that delivers the stream
	 * @throws IOException - a {@link FileNotFoundException} or an {@link UncheckedIOException}
	 */
	public static void download( File file, InputStreamProvider inputStreamProvider) throws IOException {
		File downloadFile = new File( file.getParentFile(), file.getName() + downloadExtension);
		
		logger.debug("downloading " + file.getName());
		StopWatch watch = new StopWatch(); 
		try {
			int tries = 0;
			int maxTries = 3;
			
			while (true) {
				
				try (	InputStream in = inputStreamProvider.openInputStream();
						OutputStream out = new FileOutputStream(downloadFile);
					) {
					IOTools.transferBytes(in, out, IOTools.BUFFER_SUPPLIER_64K);			
				}
				catch (NoSuchElementException e) {
					throw e;
				}
				catch (IOException e) {
					if ((tries++) > maxTries)
						throw e;
					
					logger.warn("try " + tries + " of " + maxTries + " to download " + file.getName() + " failed.");
					
					continue;
				}
				
				break;
			}
			
			logger.debug("downloaded " + file.getName() + " in " + watch.getElapsedTime() + "ms");
			
			if (file.exists()) {
				file.delete();
			}
			
			downloadFile.renameTo(file);		
		}
		
		finally {
			if (downloadFile.exists()) {
				downloadFile.delete();
			}			
		}
		
	}
	
	public static Reason downloadNotifying(File file, Supplier<Maybe<InputStream>> inputStreamProvider, BiConsumer<Integer, Integer> listener) throws IOException {
		File downloadFile = new File( file.getParentFile(), file.getName() + downloadExtension);
		
		logger.debug("downloading " + file.getName());
		StopWatch watch = new StopWatch(); 
		try {
			int tries = 0;
			int maxTries = 3;
			byte buffer[] = IOTools.BUFFER_SUPPLIER_64K.get();
			while (true) {
				
				Maybe<InputStream> maybeIn = inputStreamProvider.get();
				
				List<Reason> communicationErrors = null;
				Reason communicationError = null;
				
				if (maybeIn.isSatisfied()) {
					try (InputStream in = maybeIn.get(); OutputStream out = new FileOutputStream(downloadFile);
							) {
						
						int bytes = 0;
						long start = System.currentTimeMillis();
						int totalDataAmount = 0;
						int dataAmount = 0;
						
						while ((bytes = in.read(buffer)) != -1) {
							out.write(buffer, 0, bytes);
							dataAmount += bytes;
							totalDataAmount += bytes;
							
							long end = System.currentTimeMillis();
							
							long delta = end - start;
							
							if (delta >= 100) {
								listener.accept(dataAmount, totalDataAmount);
								start = System.currentTimeMillis();
								dataAmount = 0;
							}
						}
						
						if (dataAmount > 0) {
							listener.accept(dataAmount, totalDataAmount);
						}
					}
					catch (ReasonException e) {
						communicationError = e.getReason();
					}
					catch (IOException e) {
						communicationError = Reasons.build(IoError.T).text("Download try " + (tries + 1) + " failed") //
								.cause(InternalError.from(e)).toReason(); 
					}
					catch (Exception e) {
						return InternalError.from(e);
					}
				}
				else {
					Reason whyUnsatisfied = maybeIn.whyUnsatisfied();
					
					if (whyUnsatisfied instanceof NotFound) {
						return whyUnsatisfied;
					}
					else if (whyUnsatisfied instanceof CommunicationError) {
						communicationError = whyUnsatisfied;
					}
					
					return whyUnsatisfied;
				}
				
				if (communicationError != null) {
					if (communicationErrors == null)
						communicationErrors = new ArrayList<>(maxTries);

					communicationErrors.add(communicationError);
					
					if ((tries++) > maxTries) {
						IoError reason = Reasons.build(IoError.T).text("Download failed after retrying with max retries = " + maxTries).toReason();
						reason.getReasons().addAll(communicationErrors);
						return reason;
					}
					
					logger.warn("try " + tries + " of " + maxTries + " to download " + file.getName() + " failed.");
					
					continue;
				}
				
				break;
			}
			
			logger.debug("downloaded " + file.getName() + " in " + watch.getElapsedTime() + "ms");
			
			if (file.exists()) {
				file.delete();
			}
			
			downloadFile.renameTo(file);		
		}
		
		finally {
			if (downloadFile.exists()) {
				downloadFile.delete();
			}			
		}
		
		return null;
	}
	
	/**
	 * locking download 
	 * @param file - the local target {@link File}
	 * @param inputStreamProvider - the {@link InputStreamProvider} that delivers the stream
	 * @throws IOException - a {@link FileNotFoundException} or an {@link UncheckedIOException}
	 */
	public static void download( File file, InputStreamProvider inputStreamProvider, Function<File, ReadWriteLock> lockSupplier) throws IOException {
		ReadWriteLock lock = lockSupplier.apply( file);
		
		Lock readLock = lock.readLock();
				
		readLock.lock();
		
		try {
			download(file, inputStreamProvider);
		}
		finally {
			readLock.unlock();
		}
	}
	
	/**
	 * locking download 
	 * @param file - the local target {@link File}
	 * @param inputStreamProvider - the {@link InputStreamProvider} that delivers the stream
	 * @throws IOException - a {@link FileNotFoundException} or an {@link UncheckedIOException}
	 */
	public static Reason downloadNotifying(EventBroadcaster broadcaster, CompiledPartIdentification part, String repositoryOrigin, File file, Supplier<Maybe<InputStream>> inputStreamProvider, Function<File, ReadWriteLock> lockSupplier) throws IOException {
		ReadWriteLock lock = lockSupplier.apply( file);
		
		Lock readLock = lock.readLock();
		
		readLock.lock();
		
		try {
			return downloadNotifying(file, inputStreamProvider, (dataAmount, totalDataAmount) -> {
				OnPartDownloading event = OnPartDownloading.T.create();
				event.setRepositoryOrigin(repositoryOrigin);
				event.setPart(part);
				event.setDataAmount(dataAmount);
				event.setTotalDataAmount(totalDataAmount);
				broadcaster.sendEvent(event);
			});
		}
		finally {
			readLock.unlock();
		}
	}
	
}
