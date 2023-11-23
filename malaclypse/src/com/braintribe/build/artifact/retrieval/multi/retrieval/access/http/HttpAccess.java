// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.retrieval.access.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.braintribe.build.artifact.representations.DebugHelper;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.listener.RepositoryAccessListener;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.common.lcd.Pair;
import com.braintribe.logging.Logger;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.transport.http.HttpClientProvider;
import com.braintribe.transport.ssl.impl.EasySslSocketFactoryProvider;
import com.braintribe.transport.ssl.impl.StrictSslSocketFactoryProvider;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;


public class HttpAccess {
	private static final String LAST_UPDATE = "Last-Update";
	private static Logger log = Logger.getLogger(HttpAccess.class);
	private CloseableHttpClient downLoadClient;
	private CloseableHttpClient upLoadClient;	
	private HttpClientProvider httpClientProvider;
	private static int MAX_RETRIES = 3;
	private boolean lenient = false;
	private static String ENCODING = "ISO-8859-1";
	private Map<String, Pair<String,String>> hashAlgToHeaderKeyAndExtension = new LinkedHashMap<>();
	{
		hashAlgToHeaderKeyAndExtension.put( "MD5", Pair.of("X-Checksum-Md5", "md5"));
		hashAlgToHeaderKeyAndExtension.put( "SHA-1", Pair.of( "X-Checksum-Sha1", "Sha1"));
		hashAlgToHeaderKeyAndExtension.put( "SHA-256", Pair.of( "X-Checksum-Sha256", "Sha256"));
	}
	
	public HttpAccess() {	
	}
	
	public HttpAccess(boolean lenient) {
		this.lenient = lenient;
	}
	
	public void setHttpClientProvider(HttpClientProvider httpClientProvider) {
		this.httpClientProvider = httpClientProvider;
	}
	
	private Object clientProviderInitializationMonitor = new Object();
	
	public HttpClientProvider getHttpClientProvider() {
	
		if (httpClientProvider != null) {
			return httpClientProvider;
		}
		synchronized (clientProviderInitializationMonitor) {
			
			if (httpClientProvider != null) {
				return httpClientProvider;
			}
				
			DefaultHttpClientProvider httpClientProvider = new DefaultHttpClientProvider();
			httpClientProvider.setSocketTimeout(17 * Numbers.MILLISECONDS_PER_SECOND);
			// switch SSL provider depending of leniency setting
			if (lenient) {
				httpClientProvider.setSslSocketFactoryProvider(new EasySslSocketFactoryProvider());
			}
			else {
				httpClientProvider.setSslSocketFactoryProvider(new StrictSslSocketFactoryProvider());
			}
			if (log.isDebugEnabled()) {
				log.debug("instantiated new http client provider " + httpClientProvider.toString());
			}
	
			this.httpClientProvider = httpClientProvider; 
		}
		return httpClientProvider;		
	}
	
	private CloseableHttpClient getDownloadClient() throws HttpRetrievalException{
		if (downLoadClient != null)
			return downLoadClient;
		if (log.isDebugEnabled()) {
			log.debug( "instantiating a download client");
		}
		HttpClientProvider clientProvider = getHttpClientProvider();		
		try {
			downLoadClient = clientProvider.provideHttpClient();
		} catch (Exception e) {
			throw new HttpRetrievalException("cannot retrieve a HttpClient", e);
		}
		return downLoadClient;
	}
	
	private CloseableHttpClient getUploadClient() throws HttpRetrievalException{
		if (upLoadClient != null)
			return upLoadClient;
		if (log.isDebugEnabled()) {
			log.debug( "instantiating a upload client");
		}
		HttpClientProvider clientProvider = getHttpClientProvider();
		try {
			HttpClientBuilder builder = clientProvider.provideClientBuilder();
			// gzip preparation - request 
			builder.addInterceptorLast(new HttpRequestInterceptor() {
	            @Override
				public void process(
	                    final HttpRequest request,
	                    final HttpContext context) throws HttpException, IOException {
	                if (!request.containsHeader("Accept-Encoding")) {
	                    request.addHeader("Accept-Encoding", "gzip");
	                }
	            }
	        });
			// gzip preparation - response
			builder.addInterceptorLast( new HttpResponseInterceptor() {
	            @Override
				public void process(
	                    final HttpResponse response,
	                    final HttpContext context) throws HttpException, IOException {
	                HttpEntity entity = response.getEntity();
	                if (entity != null) {
	                    Header ceheader = entity.getContentEncoding();
	                    if (ceheader != null) {
	                        HeaderElement[] codecs = ceheader.getElements();
	                        for (int i = 0; i < codecs.length; i++) {
	                            if (codecs[i].getName().equalsIgnoreCase("gzip")) {
	                                response.setEntity(
	                                        new GzipDecompressingEntity(response.getEntity()));
	                                return;
	                            }
	                        }
	                    }
	                }
	            }
	        });
			
			
			upLoadClient = builder.build();
		} catch (Exception e) {
			throw new HttpRetrievalException("cannot retrieve a HttpClient", e);
		}
		return upLoadClient;
	}
	
	public void closeContext()  {
		if (downLoadClient != null) {
			if (log.isDebugEnabled()) {
				log.debug("closing http  download client " + downLoadClient.toString());
			}
			try {
				downLoadClient.close();
			} catch (IOException e) {
				if (log.isDebugEnabled()) {					  				 
				  log.debug( "cannot close download client", e);
				}
			}
		}
		if (upLoadClient != null) {
			if (log.isDebugEnabled()) {
				log.debug("closing http uploadClient " + upLoadClient.toString());
			}			
			try {
				upLoadClient.close();
			} catch (IOException e) {
				if (log.isDebugEnabled()) {					  				 
				  log.debug( "cannot close upload client", e);
				}
			}
		}
		downLoadClient = upLoadClient = null;
	}
	
	
	/**
	 * use {@link #acquire(String, Server, RepositoryAccessListener)} or {@link #require(String, Server, RepositoryAccessListener)}
	 */
	@Deprecated
	public String download(String source, Server server, RepositoryAccessListener listener) throws HttpRetrievalException {
		return acquire(source, server, listener);
	}
	
	/**
	 * leniently gets a file's content from an URL 
	 * @param source - the url
	 * @param server - the {@link Server} with the authorization if required 
	 * @param listener - an {@link RepositoryAccessListener}
	 * @return - the content of the file as string OR NULL IF IT AIN'T THERE
	 * @throws HttpRetrievalException - if anything goes catastrophically wrong
	 */
	public String acquire(String source, Server server, RepositoryAccessListener listener) throws HttpRetrievalException {
		return download(source, server, true, listener);
	}
	/**
	 * strictly get a file's content from an URL, so it yells if it ain't there
	 * @param source - the url
	 * @param server - the {@link Server} with the authorization if required 
	 * @param listener - an {@link RepositoryAccessListener}
	 * @return - the content of the file as string 
	 * @throws HttpRetrievalException - if anything goes catastrophically wrong OR IF THE FILE CANNOT BE FOUND
	 */
	public String require(String source, Server server, RepositoryAccessListener listener) throws HttpRetrievalException {
		String result = download(source, server, false, listener);
		if (result == null) {
			throw new HttpRetrievalException("required source [" + source + "] cannot be extracted as it doesn't exist");								
		}
		return result;
	}
	
	/**
	 * require function that returns Pair with the either the Date the server has sent via {@link HttpAccess#LAST_UPDATE} 
	 * or the Date it has been received.
	 * @param source - the URL to read from 
	 * @param server - the {@link Server} with the credentials
	 * @param listener - a {@link RepositoryAccessListener}
	 * @return - a {@link Pair} of received {@link Date} and downloaded {@link String}
	 * @throws HttpRetrievalException
	 */
	public Pair<Date,String> detailedRequire(String source, Server server, RepositoryAccessListener listener) throws HttpRetrievalException {
		Pair<Date,String> result = detailedDownload(source, server, false, listener);
		if (result == null) {
			throw new HttpRetrievalException("required source [" + source + "] cannot be extracted as it doesn't exist");								
		}
		return result;
	}
	
	/**
	 * download function that can retry (if not 200 or 404)
	 * @param source
	 * @param server
	 * @param overridingLeniency
	 * @param listener
	 * @return
	 * @throws HttpRetrievalException
	 */
	private String download( String source, Server server, boolean overridingLeniency, RepositoryAccessListener listener) throws HttpRetrievalException {
		int tries = 0;
		do {
			try {		
				Pair<Date,String> _download = _download(source, server, false, listener);
				if (_download != null) {
					return _download.getSecond();
				}
				return null;
				
			} catch (Exception e) {
				log.warn("downloading [" + source + "] produced [" + e.getMessage() + "] in [" + tries + "] try");
				if (++tries >= MAX_RETRIES) {
					break;
				}
			}
		} while (true);
		throw new HttpRetrievalException("Failed to extract contents of [" + source + "] after [" + tries + "] retries");
	}
	
	/**
	 * @param source
	 * @param server
	 * @param overridingLeniency
	 * @param listener
	 * @return
	 * @throws HttpRetrievalException
	 */
	private Pair<Date,String> detailedDownload( String source, Server server, boolean overridingLeniency, RepositoryAccessListener listener) throws HttpRetrievalException {
		int tries = 0;
		do {
			try {		
				return _download(source, server, false, listener);								
			} catch (Exception e) {
				log.warn("downloading [" + source + "] produced [" + e.getMessage() + "] in [" + tries + "] try");
				if (++tries >= MAX_RETRIES) {
					break;
				}
			}
		} while (true);
		throw new HttpRetrievalException("Failed to extract contents of [" + source + "] after [" + tries + "] retries");
	}
	
	/**
	 * @param source
	 * @param server
	 * @param overridingLeniency
	 * @param listener
	 * @return
	 * @throws HttpRetrievalException
	 */
	private Pair<Date, String> _download( String source, Server server, boolean overridingLeniency, RepositoryAccessListener listener) throws HttpRetrievalException {
		CloseableHttpClient httpclient = getDownloadClient();		
		if (log.isDebugEnabled()) {
			log.debug( "start downloading [" + source + "]");
		}
				

		HttpGet httpget = new HttpGet( source);	
		String host = httpget.getURI().getHost();
		
		HttpClientContext context = HttpClientContext.create();
		if (server != null && server.getUsername() != null && server.getPassword() != null) {
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials( new AuthScope( host, AuthScope.ANY_PORT), new UsernamePasswordCredentials( server.getUsername(), server.getPassword()));	
			context.setCredentialsProvider( credentialsProvider);
		}
		
		try {
			long before = System.nanoTime();
			CloseableHttpResponse response = httpclient.execute(httpget, context);			
			long after = System.nanoTime();
		
			Pair<String,String> hashAlgAndValuePair = determineRequiredHashMatch(response);
			MessageDigest messageDigest = hashAlgAndValuePair != null ? createMessageDigest(hashAlgAndValuePair.getFirst()) : null;
			
			
			
			HttpEntity entity = null;
			 try {
				 	Header[] headers = response.getHeaders(LAST_UPDATE);
				 	Date receivedDate = new Date(); 
				 	if (headers != null && headers.length > 0) {
				 		String dateAsString = headers[0].getValue();
				 		receivedDate = DateUtils.parseDate( dateAsString);
				 	}
				 	
			        entity = response.getEntity();
			        int statusCode = response.getStatusLine().getStatusCode();
					if ((statusCode >= 200) && (statusCode < 300)) {
						  if (listener != null) {
							  listener.acknowledgeDownloadSuccess(source, after-before);
						  }
						  if (log.isDebugEnabled()) {					  				 
							  log.debug("download success for [" + source + "] after [" + ((after-before)/1E6) + "] ms");
						  }
						  // extract char encoding from MIME type 
						  String encoding = ENCODING;
						  Header header = entity.getContentEncoding();					
						  if (header != null) {
							  Mimetype mimeType = new Mimetype( header.getValue());
							  encoding = mimeType.getCharset();
						  }
						  						 						  
						  InputStream inputStream = entity.getContent();
						  if (messageDigest != null) {
								inputStream = new DigestInputStream(inputStream, messageDigest);
						  }
						  String retval = pump( inputStream, encoding);
						  checkChecksum(messageDigest, hashAlgAndValuePair, source);
						  return Pair.of(receivedDate, retval);											
					} else {				  
						if (listener != null) {
							listener.acknowledgeDownloadFailure(source, response.getStatusLine().getReasonPhrase());
						}
						if (log.isDebugEnabled()) {
							log.debug("received 404 success for [" + source + "] after [" + ((after-before)/1E6) + "] ms");
						}						
						return null;
					}
			        
			    } finally {			    
			        response.close();
			    }
		} catch (Exception e) {
			if (!overridingLeniency)
				throw new HttpRetrievalException(e);
		} 		
		return null;
	}
	
	/**
	 * acquire a file from an URL 
	 * @param targetFile - the {@link File} to write to
	 * @param source - the {@link URL} as a string
	 * @param server - the {@link Server}
	 * @param listener - an {@link RepositoryAccessListener}
	 * @return - the {@link File}, null IF IT AIN'T THERE
	 * @throws HttpRetrievalException - - if anything goes catastrophically wrong 
	 */
	public File acquire( File targetFile, String source, Server server, RepositoryAccessListener listener) throws HttpRetrievalException {
		return download(targetFile, source, server, listener);
	}
	/**
	 * get a file from an URL, and yell if it ain't there 
	 * @param targetFile - the {@link File} to write to
	 * @param source - the {@link URL} as a string
	 * @param server - the {@link Server}
	 * @param listener - an {@link RepositoryAccessListener}
	 * @return - the {@link File}
	 * @throws HttpRetrievalException - if anything goes catastrophically wrong OR IF THE FILE CANNOT BE FOUND
	 */
	public File require( File targetFile, String source, Server server, RepositoryAccessListener listener) throws HttpRetrievalException {
		File result = download(targetFile, source, server, listener);
		if (result == null) {
			//throw new HttpRetrievalException("required [" + source + "] cannot be found");
			log.warn("required [" + source + "] cannot be found");
		}
		return result;
		
	}
	private File download( File targetFile, String source, Server server, RepositoryAccessListener listener) throws HttpRetrievalException {
		int tries = 0;
		do {
			try {				
				return _download(targetFile, source, server, listener);
			} catch (Exception e) {
				if (++tries >= MAX_RETRIES) {
					break;
				}
			}
		} while (true);
		throw new IllegalStateException("Failed to download [" + source + "] to [" + targetFile.getAbsolutePath() + "] after [" + tries + "] retries");
	}
	
	private File _download( File targetFile, String source, Server server, RepositoryAccessListener listener) throws HttpRetrievalException {
		if (log.isDebugEnabled()) {
			log.debug( "starting download [" + source + "] to [" + targetFile.getAbsolutePath() + "]");
		}
		CloseableHttpClient httpclient = getDownloadClient();		
				
		HttpGet httpget = new HttpGet( source);	
		String host = httpget.getURI().getHost();
		
		HttpClientContext context = HttpClientContext.create();
		if (server != null && server.getUsername() != null && server.getPassword() != null) {
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials( new AuthScope( host, AuthScope.ANY_PORT), new UsernamePasswordCredentials( server.getUsername(), server.getPassword()));	
			context.setCredentialsProvider( credentialsProvider);
		}
		
		
		long before = System.nanoTime();
		CloseableHttpResponse response;
		try {
			response = httpclient.execute(httpget, context);
		} catch (Exception e) {
			  String msg = "cannot download [" + source + "]";
			  log.error(msg, e);
			  throw new IllegalStateException(msg, e);
		} 
		Pair<String, String> hashAlgAndValuePair;
		try {
			hashAlgAndValuePair = determineRequiredHashMatch(response);
		} catch (IOException e) {
			String msg = "cannot extract hashes from header [" + source + "]";
			log.error(msg, e);
			throw new IllegalStateException(msg, e);
		}
		MessageDigest messageDigest = hashAlgAndValuePair != null ? createMessageDigest(hashAlgAndValuePair.getFirst()) : null;
		
		if (log.isDebugEnabled()) {
			if (hashAlgAndValuePair != null) {
				log.debug( "first hash in header for [" + source + "-> " + hashAlgAndValuePair.getFirst() + ":" + hashAlgAndValuePair.getSecond());
			}
			else {
				log.debug( "no hashes in header received for [" + source + "]");
			}
		}
		
		HttpEntity entity = null;
		try {
	        entity = response.getEntity();
	        int statusCode = response.getStatusLine().getStatusCode();
			if (
					(statusCode >= 200) && 
					(statusCode < 300)
				) {						  						  
				InputStream instream;
				try {
					instream = entity.getContent();
				} catch (Exception e) {
					String msg = "cannot retrieve input stream from [" + source + "]";
					log.error(msg, e);
					throw new IllegalStateException(msg, e);
				} 
				if (messageDigest != null) {
					instream = new DigestInputStream(instream, messageDigest);
				}
				  						  
				String parentFile = targetFile.getParent();
				if (parentFile != null) {
					File targetDir = new File(  parentFile);
					if (targetDir.exists() == false)
						targetDir.mkdirs();
				}													  
				
				try {
					pump( instream, targetFile);
				} catch (IOException e) {
					throw new IllegalStateException( "cannot pump content of [" + source + "] to [" +  targetFile.getAbsolutePath() + "]", e);
				}
				  
				long after = System.nanoTime();
				if (listener != null) {
					listener.acknowledgeDownloadSuccess(source, after - before);
				}				  
				if (log.isDebugEnabled()) {					  				 
					log.debug("download success for ["+  source + "] after [" + ((after-before)/1E6) + "] ms");
				}
				checkChecksum(messageDigest, hashAlgAndValuePair, source);
				return targetFile;
									
			} else {			
				  long after = System.nanoTime();
				  if (statusCode == 404) {
					  if (listener != null) {
						  listener.acknowledgeDownloadFailure(source, response.getStatusLine().getReasonPhrase());
					  }
				  }
				  else {					  
					  if (listener != null) {
						  listener.acknowledgeDownloadFailure(source, response.getStatusLine().getReasonPhrase());
					  }
					  String msg = "cannot download [" + source + "]. Reason :" + response.getStatusLine().getReasonPhrase();
					  
					  if (DebugHelper.isDebugging()) {
							msg += " authentication info : server [" + server.getId() + "] with [" + server.getUsername() + ":" + server.getPassword() + "]";
						}
						else {
							msg += " authentication info : server [" + server.getId() + "]";
						}
					  
					  log.warn( msg);
				  }				
				  if (log.isDebugEnabled()) {
					  log.debug("received 404 for [" + source + "] after [" + ((after-before)/1E6) + "] ms");
				  }
				  return null;
			}
	        
	    } finally {			    
	        try {
				response.close();
			} catch (IOException e) {
				throw new IllegalStateException( "cannot close url stream while downloading [" + source + "]", e);
			}
	    }				
	}
	
	private void pump( InputStream in, File outfile) throws IOException {
		try (FileOutputStream out = new FileOutputStream(outfile);) {
			pump( in, out);
		}
	}
	
	private long pump( InputStream in, OutputStream out) throws IOException {		
		return IOTools.pump(in, out, IOTools.SIZE_64K);		
	}
	
	private String pump( InputStream in, String encoding) throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			pump( in, out);
			return out.toString( encoding);
		}		
	}
	public boolean delete( String target, Server server, RepositoryAccessListener listener) throws HttpRetrievalException {
		CloseableHttpClient httpClient = getDownloadClient();
		HttpDelete httpDelete = new HttpDelete( target);		
		String host = httpDelete.getURI().getHost();	
		
		HttpClientContext context = HttpClientContext.create();
		if (server != null && server.getUsername() != null && server.getPassword() != null) {
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials( new AuthScope( host, AuthScope.ANY_PORT), new UsernamePasswordCredentials( server.getUsername(), server.getPassword()));	
			context.setCredentialsProvider( credentialsProvider);
		}
		
		try {
			long before = System.nanoTime();
			CloseableHttpResponse response = httpClient.execute(httpDelete, context);
			long after = System.nanoTime();
			int statusCode = response.getStatusLine().getStatusCode();
			response.close();
			if ((statusCode >= 200) && (statusCode < 300)) {			
				if (listener != null) {				
					listener.acknowledgeDeleteSuccess(target, (after-before) / 1000);
				}
				return true;
			}
			if (statusCode != 404) {
				String msg = "cannot delete [" + target + "] as statuscode's [" + statusCode + "]";
				if (DebugHelper.isDebugging()) {
					msg += " authentication info : server [" + server.getId() + "] with [" + server.getUsername() + ":" + server.getPassword() + "]";
				}
				else {
					msg += " authentication info : server [" + server.getId() + "]";
				}
				log.warn( msg);
				if (listener != null) {
					listener.acknowledgeDeleteFailure(target, response.getStatusLine().getReasonPhrase());
				}
				return false;
			}
			else {
				// 404 means, no such resource, ergo deleting is fine
				return true;
			}
		} catch (Exception e) {
			if (!lenient)
				throw new HttpRetrievalException( e);
		} 		
		return false;
	}
	
	public Map<File, Integer> upload( Server server, Map<File, String> sourceToTargetMap, boolean overwrite, RepositoryAccessListener listener) throws HttpRetrievalException {
		Map<File, Integer> result = new HashMap<File, Integer>( sourceToTargetMap.size());
		CloseableHttpClient httpclient = getUploadClient();
		
		HttpPut httpSpearheadPut = new HttpPut( sourceToTargetMap.values().toArray(new String[0])[0]);
		String host = httpSpearheadPut.getURI().getHost();
		
		HttpClientContext context = HttpClientContext.create();
		if (server != null && server.getUsername() != null && server.getPassword() != null) {
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials( new AuthScope( host, AuthScope.ANY_PORT), new UsernamePasswordCredentials( server.getUsername(), server.getPassword()));	
			context.setCredentialsProvider( credentialsProvider);
			
		}
						
		provokeAuthentication( httpclient, context, sourceToTargetMap.values().toArray(new String[0])[0]);
		
		
		// actual upload 
		for (Entry<File, String> entry : sourceToTargetMap.entrySet()) {
			
			File sourceFile = entry.getKey();
			Map<String, String> hashes = generateHash( sourceFile, Arrays.asList("sha1", "MD5", "SHA-256"));
			
			boolean targetExists = false;
			Pair<String, String> hashAlgAndValuePairOfExistingFile = null;
		
			// test if it's there already.. 
			HttpHead headRequest =  new HttpHead( entry.getValue());
			try {
				HttpResponse headResponse = httpclient.execute( headRequest, context);
				int headStatusCode = headResponse.getStatusLine().getStatusCode();
				if (headStatusCode == 200) {
					targetExists = true;
					try {
						hashAlgAndValuePairOfExistingFile = determineRequiredHashMatch(headResponse);					
					} catch (IOException e) {
						String msg = "cannot extract hashes from header of existing [" + entry.getValue() + "]";
						log.error(msg, e);				
					}
				}
				
			} catch (Exception e) {
				log.warn("cannot determine if target [" + entry.getValue() + "] exists. Assuming not to exist");
			}	
			
			// if overwrite, try to delete it first
			if (targetExists) {				
				if (overwrite) {
					boolean requiresOverwrite = true;
					if (hashAlgAndValuePairOfExistingFile != null) {
						String sourceHash = hashes.get( hashAlgAndValuePairOfExistingFile.first);
						if (sourceHash != null) {
							requiresOverwrite = sourceHash.compareTo( hashAlgAndValuePairOfExistingFile.second) != 0;
						}
					}
					if (!requiresOverwrite) {
						// hashes match, so even if we need to overwrite, the files are the same
						continue;
					}					
					deleteTarget(httpclient, context, entry);
				}
				else {
					// target exists, no overwrite 
					continue;
				}
			}			
			
			
			HttpPut filePut = new HttpPut( entry.getValue());
						
			FileEntity fileEntity = new FileEntity( sourceFile);
			filePut.setHeader("X-Checksum-Sha1", hashes.get("sha1"));
			filePut.setHeader("X-Checksum-MD5", hashes.get("md5"));
			filePut.setHeader("X-Checksum-SHA256", hashes.get("SHA-256"));
			
			filePut.setEntity( fileEntity);			
			long before = System.nanoTime();
			int statusCode = 200;

			// prepare looping on fail 
			boolean done = false;
			int tries = 0;
			do {
				StatusLine httpStatusLine = put( httpclient, filePut, context);
				long after = System.nanoTime();
				statusCode = httpStatusLine.getStatusCode();
				if (statusCode >= 200 && statusCode < 300) {
					done = true;
					if (listener != null) {
						listener.acknowledgeUploadSuccess(entry.getKey().getAbsolutePath(), entry.getValue(), after - before);
					} 
					else {
						if (log.isDebugEnabled())
							log.debug("Successfully uploaded [" + entry.getKey() + "] to [" + entry.getValue() +"]");	
					}
				} else {
					String msg = "Upload of [" + entry.getKey() + "] to [" + entry.getValue() +"] failed, code [" + statusCode +"], reason [" + httpStatusLine.getReasonPhrase();
					if (DebugHelper.isDebugging()) {
						msg += " ]authentication info : server [" + server.getId() + "] with [" + server.getUsername() + ":" + server.getPassword() + "]";
					}
					else {
						msg += " ] authentication info : server [" + server.getId() + "]";
					}
					log.warn(msg);
					if (listener != null) {
						listener.acknowledgeUploadFailure(entry.getKey().getAbsolutePath(), entry.getValue(), httpStatusLine.getReasonPhrase());
					} 
				}
				// if failed, only try max retries before giving up
				if (!done) {
					done = ++tries > MAX_RETRIES;
				}
			} while (!done);
			
			result.put( entry.getKey(), statusCode);
			
		}
		return result;			
	}

	private HttpEntity deleteTarget(CloseableHttpClient httpclient, HttpClientContext context, Entry<File, String> entry) {
		HttpEntity entity = null;				
		try {
			HttpDelete httpDelete = new HttpDelete( entry.getValue());
			HttpResponse response = httpclient.execute( httpDelete, context);
			int statusCode = response.getStatusLine().getStatusCode();
			entity = response.getEntity();
			if (statusCode == 404) {
				if (log.isDebugEnabled()) {
					log.debug("target [" + entry.getValue() + "] doesn't exist");
				}
			}
			else if ((statusCode >= 200) && (statusCode < 300)) {
				if (log.isDebugEnabled()){
					log.debug("target [" + entry.getValue() + "] successfully deleted");
				}
			}
			else {
				log.warn( "cannot delete [" + entry.getValue() + "] as statuscode's [" + statusCode + "]");		
			}											
			
		} catch (Exception e) {
			log.warn( "cannot delete [" + entry.getValue() + "]", e);
		}
		finally {
			try {
				if (entity != null)
					EntityUtils.consume( entity);
			} catch (IOException e) {
				String msg = "can't consume http entity as " + e;
				log.error(msg, e);						
			}		
		}
		return entity;
	}
	
	private void provokeAuthentication(CloseableHttpClient httpClient, HttpClientContext context, String target) throws HttpRetrievalException {
		try {
			HttpHead httpSpearHeadDelete = new HttpHead(target );
			httpClient.execute( httpSpearHeadDelete, context);
		} catch (Exception e) {
			throw new HttpRetrievalException(e);
		} 
		
	}

	private Map<String, String> generateHash(File sourceFile, List<String> types) {
		Map<String, String> result = new HashMap<>();
		List<MessageDigest> digests = types.stream().map( t -> {
			try {
				return MessageDigest.getInstance( t);
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("no digest found for [" + t + "]");
			}
		}).collect( Collectors.toList());
	
		byte bytes [] = new byte[65536];
		try (FileInputStream in = new FileInputStream( sourceFile)) {
			int size = 0;
			while ((size = in.read(bytes)) != -1) {
				for (MessageDigest digest : digests)  {
					digest.update(bytes, 0, size);
				}				
			}
			for (int i = 0; i < types.size(); i++)  {
				MessageDigest digest = digests.get( i);
				byte [] digested = digest.digest();
				result.put( types.get(i), StringTools.toHex(digested));
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException("can't read [" + sourceFile.getAbsolutePath() + "]", e);
		}
		return result;
	}

	private static StatusLine put(CloseableHttpClient client, HttpEntityEnclosingRequestBase request, HttpContext httpContext) throws HttpRetrievalException {
		try {
			CloseableHttpResponse httpResponse = client.execute( request, httpContext);
			StatusLine httpStatusLine = httpResponse.getStatusLine();									
			httpResponse.close();													
			return httpStatusLine;
		} catch (Exception e) {
			String msg =" cannot execute request";
			log.error( msg, e);
			throw new HttpRetrievalException(msg, e);
		}
	}

	/**
	 * @param response - the {@link HttpResponse} as returned by the server 
	 * @return - a {@link Pair} consting of the hash type and hash value
	 * @throws IOException
	 */
	private Pair<String,String> determineRequiredHashMatch(HttpResponse response) throws IOException {
		// only check if relevant
		// search for the hashes in the headers, take the first one matching
	
		for (Entry<String, Pair<String,String>> entry : hashAlgToHeaderKeyAndExtension.entrySet()) {
			String first = entry.getValue().first();
			Header header = response.getFirstHeader( first);
			if (header != null) {
				return Pair.of( entry.getKey(), header.getValue());
			}
		}
				
		
		return null;					
	}
	/**
	 * @param hashAlg - name of hashing algo
	 * @return - a matching {@link MessageDigest}
	 */
	private MessageDigest createMessageDigest(String hashAlg) {
		try {
			return MessageDigest.getInstance( hashAlg);
		} catch (NoSuchAlgorithmException e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	private void checkChecksum(MessageDigest messageDigest, Pair<String,String> hashAlgAndValuePair, String url) {						
		if (messageDigest != null) {
			String hash = StringTools.toHex( messageDigest.digest());
			if (!hash.equalsIgnoreCase( hashAlgAndValuePair.getSecond())) {
				String msg = "checksum [" + hashAlgAndValuePair.first() + "] mismatch for [" + url + "], expected [" + hashAlgAndValuePair.getSecond() + "], found [" + hash + "]";
				log.error( msg);
				throw new IllegalStateException(msg);														
			}
			else {
				if (log.isDebugEnabled()) {
					String msg = "checksum [" + hashAlgAndValuePair.first() + "] match for [" + url + "], expected [" + hashAlgAndValuePair.getSecond() + "], found [" + hash + "]";
					log.debug(msg);
				}
			}
		}
		
	}
	
}
