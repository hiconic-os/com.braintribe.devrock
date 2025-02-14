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
package com.braintribe.devrock.mc.core.commons;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.devrock.model.mc.reason.UnknownRepositoryHost;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.IoError;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.resource.Resource;
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
	
	private static Maybe<InputStream> openStreamReasoned(Resource resource) {
		try {
			return Maybe.complete(resource.openStream());
		}
		catch (ReasonException e) {
			return e.getReason().asMaybe();
		}
	}
	
	public static Reason downloadReasoned(File file, Resource resource) {
		return downloadReasoned(file, () -> openStreamReasoned(resource));
	}
	
	public static Reason downloadReasoned(File file, Supplier<Maybe<InputStream>> inputStreamProvider)  {
		try {
			Files.createDirectories(file.getParentFile().toPath());
		}
		catch (Exception e) {
			return Reasons.build(IoError.T) //
					.text("Could not ensure directory for downloading: " + file.getAbsolutePath()) //
					.cause(InternalError.from(e)) //
					.toReason();
		}
		
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
					catch (ReasonException e) {
						communicationError = e.getReason();
						logger.debug("Error in download try " + (tries + 1) + " to " + file.getAbsolutePath() + " failed: " + communicationError.stringify());
					}
					catch (IOException e) {
						logger.debug("IOException in download try " + (tries + 1) + " to " + file.getAbsolutePath() + " failed.", e);
						communicationError = Reasons.build(IoError.T).text("Download try " + (tries + 1) + " failed") //
								.cause(InternalError.from(e)).toReason(); 
					}
					catch (Exception e) {
						logger.debug("Exception in download try " + (tries + 1) + " to " + file.getAbsolutePath() + " failed.", e);
						return InternalError.from(e);
					}
				}
				else {
					Reason whyUnsatisfied = maybeIn.whyUnsatisfied();
					
					if (whyUnsatisfied instanceof NotFound) {
						logger.debug("Source for download to " + file.getAbsolutePath() + " not found.");
						return whyUnsatisfied;
					}
					
					if (whyUnsatisfied instanceof UnknownRepositoryHost) 
						return whyUnsatisfied;

					communicationError = whyUnsatisfied;
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
			
			try {
				Files.move(downloadFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			catch (Exception e) {
				return Reasons.build(IoError.T) //
					.text("Could not rename download file : " + downloadFile.getAbsolutePath() + " to " + file.getAbsolutePath()) //
					.cause(InternalError.from(e)) //
					.toReason();
			}
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
	public static Reason downloadLocked(CompiledPartIdentification part, String repositoryOrigin, File file, Supplier<Maybe<InputStream>> inputStreamProvider, Function<File, ReadWriteLock> lockSupplier) {
		ReadWriteLock lock = lockSupplier.apply( file);
		
		Lock readLock = lock.readLock();
		
		readLock.lock();
		
		try {
			return downloadReasoned(file, inputStreamProvider);
		}
		finally {
			readLock.unlock();
		}
	}
	
}
