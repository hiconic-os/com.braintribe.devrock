package com.braintribe.build.artifact.retrieval.multi.retrieval.access;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.logging.Logger;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;

/**
 * just some common helper routines for downloads.. 
 * @author pit
 *
 */
public class DownloadHelper {
	private static Logger log = Logger.getLogger(DownloadHelper.class);
	public static final String SUFFIX_TRANSACTION =".download";
	private static final int uuidLen = UUID.randomUUID().toString().length();
	private static final int suffixLen = SUFFIX_TRANSACTION.length();
	
	public static String deriveDownloadFileName( File target) {
		String transactionalTarget = target.getAbsolutePath() + "." + UUID.randomUUID().toString() + SUFFIX_TRANSACTION;
		return transactionalTarget;
	}
	
	public static String deriveActualFileName( File source) {
		String name = source.getName();
		if (name.endsWith( SUFFIX_TRANSACTION)) {
			String targetName = name.substring(0, name.length() - (uuidLen+suffixLen+1));			
			return source.getParent() + File.separator + targetName;
		}
		return source.getAbsolutePath();
	}
	
	public static boolean isDownloadFile( File file) {
		return file.getName().endsWith(SUFFIX_TRANSACTION);
	}
	public static void ensureContentsOfActualFile(LockFactory lockFactory, File downloadedFile, File actualFile) {
		Path actualFilePath = actualFile.toPath();
		try {
			Files.createDirectories( actualFile.getParentFile().toPath());
		} catch (IOException e2) {
			throw new IllegalStateException("cannot create directories required for [" + actualFile.getAbsolutePath() + "]", e2);
		}
		
		Lock lock = lockFactory.getLockInstance(actualFile).writeLock();
		
		try {
			lock.lock();
			
			if (actualFile.exists()) {
				String msg ="race condition : a download request was issued for this enricher instance, yet another one has downloaded [" + actualFile.getAbsolutePath() + "]";
				log.debug(msg);
				
				if (downloadedFile.exists()) {
					if (!downloadedFile.delete()) {
						log.debug("cannot delete [" + downloadedFile.getAbsolutePath() + "]");
					}
				}
				return;
			}
			// file is not there, must move it (rename hope
			Files.move(downloadedFile.toPath(), actualFilePath);
						
			
		} catch (IOException e) {
			throw new IllegalStateException("cannot move [" + downloadedFile.getAbsolutePath() + "] to [" + actualFile.getAbsolutePath() + "]", e);
		}
		finally {			
			lock.unlock();			
		}
		
		
	}
	public static void ensureContentsOfActualFileWithPump(File downloadedFile, File actualFile) {
		Path path = actualFile.toPath();
		try {
			Files.createDirectories( actualFile.getParentFile().toPath());
		} catch (IOException e2) {
			throw new IllegalStateException("cannot create directories required for [" + actualFile.getAbsolutePath() + "]", e2);
		}
		try {
			Files.createFile( path);					
		} catch (FileAlreadyExistsException f) {
			
			String msg ="race condition : a download request was issued for this enricher instance, yet another one has downloaded [" + actualFile.getAbsolutePath() + "]";
			log.debug(msg);
			
			if (downloadedFile.exists()) {
				if (!downloadedFile.delete()) {
					log.debug("cannot delete [" + downloadedFile.getAbsolutePath() + "]");
				}
			}
			return;
		} catch (IOException e1) {
			throw new IllegalStateException("failed while leniently grapping [" + actualFile.getAbsolutePath() + "]", e1);
		}
		if (downloadedFile.exists() == false) {
			throw new IllegalStateException("source file [" + downloadedFile.getAbsolutePath() + "] doesn't exist");
		}
		
		// get an MD5 of the downloaded file 
		
		if (log.isDebugEnabled()) {
			log.debug( "pumping [" + downloadedFile.length() + "] bytes from [" + downloadedFile.getAbsolutePath() + "] to [" + actualFile.getAbsolutePath() + "]");
		}
		
		// get a digest here
		MessageDigest downloadDigest = null;
		try (
				InputStream in = new FileInputStream(downloadedFile);
				DigestInputStream dig = new DigestInputStream(in, MessageDigest.getInstance("MD5"));
				OutputStream out = new FileOutputStream(actualFile);
			) {						
			IOTools.pump(dig, out, IOTools.SIZE_64K);			
			downloadDigest = dig.getMessageDigest();			
			
		} catch (Exception e) {
			throw new IllegalStateException("failed to transfer contents from [" + downloadedFile.getAbsolutePath() + "] to [" + actualFile.getAbsolutePath() + "]", e);
		}
		
		if (log.isDebugEnabled()) {
			log.debug( "expected size [" + downloadedFile.length() + "] bytes, pumped are [" + actualFile.length() + "]");
		}
		if (downloadedFile.length() != actualFile.length()) {
			String msg = "Size mismatch on [" + actualFile.getAbsolutePath() + "]: size of temp downloaded file [" + downloadedFile.length() + "] bytes, pumped are [" + actualFile.length() + "]";
			log.error( msg);
			throw new IllegalStateException( msg);
		}
		

		String downloadFileHash = StringTools.toHex( downloadDigest.digest());
		// check md5
		try {
			byte[] bytes = Files.readAllBytes( actualFile.toPath());			
			String actualFileHash = StringTools.toHex( MessageDigest.getInstance("MD5").digest(bytes));
			
			if (log.isDebugEnabled()) {
				log.debug( "expected hash [" + downloadFileHash + "] bytes, pumped result hash [" + actualFileHash + "]");
			}
			
			if (!downloadFileHash.equalsIgnoreCase( actualFileHash)) {
				String msg = "hash mismatch after copy of [" + downloadedFile.getAbsolutePath() + "] (" + downloadFileHash + ") to [" + actualFile.getAbsolutePath() + "](" + actualFileHash +")";
				log.error(msg);
				throw new IllegalStateException( msg);
			}
			
			
		} catch (Exception e) {
			String msg = "Cannot verify MD5 on [" + actualFile.getAbsolutePath() + "] to compare to [" + downloadedFile.getAbsolutePath() + "], should've been [" + downloadFileHash + "]";			
			log.error( msg);
			throw new IllegalStateException( msg);
		}
		
		if (!downloadedFile.delete()) {
			log.debug("cannot delete [" + downloadedFile.getAbsolutePath() + "]");
		}
	}
}
