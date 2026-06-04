package com.braintribe.devrock.mc.core.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.ProtocolException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.net.ssl.SSLException;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.model.mc.reason.UnknownRepositoryHost;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.CommunicationError;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.gm.model.security.reason.AuthenticationFailure;
import com.braintribe.gm.model.security.reason.Forbidden;
import com.braintribe.logging.Logger;
import com.braintribe.utils.IOTools;

/**
 * common base for {@link HttpRepositoryArtifactDataResolver} and {@link HttpRepositoryProbingSupport}
 * 
 * @author pit / dirk
 *
 */
public class HttpRepositoryBase {
	private static final List<RetryStatusCodeRule> RETRY_STATUS_CODE_RULES = List.of(
		new RetryStatusCodeRule(Pattern.compile("://maven\\.pkg\\.github\\.com[:/]"), Set.of(404), RetryCascade.of(0, 10))
	);

	private static final List<RetryExceptionRule> RETRY_EXCEPTION_RULES = List.of(
		new RetryExceptionRule(Pattern.compile(".*"), IOException.class, RetryCascade.of(0, 100, 200))
	);

	private static final List<Class<? extends IOException>> NON_RETRYABLE_EXCEPTION_TYPES = List.of(
		UnknownHostException.class,
		NoRouteToHostException.class,
		MalformedURLException.class,
		ProtocolException.class,
		UnknownServiceException.class,
		SSLException.class
	);
	
	private final Logger logger = Logger.getLogger(HttpRepositoryBase.class);
	protected String root;
	protected String userName;
	protected String password;
	protected CloseableHttpClient httpClient;
	protected String repositoryId = "unknown";

	@Configurable
	@Required
	public void setRoot(String root) {
		this.root = root;
	}
	@Configurable
	public void setUserName(String userName) {
		this.userName = userName;
	}
	@Configurable
	public void setPassword(String password) {
		this.password = password;
	}
	@Configurable
	@Required
	public void setHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	@Configurable
	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	protected CloseableHttpResponse getResponse(String url, boolean headOnly) throws IOException {
		return getResponse(headOnly ? new HttpHead(url) : new HttpGet(url));
	}

	protected CloseableHttpResponse getResponse(HttpRequestBase requestBase) throws IOException {
		if (userName != null && password != null) {
			String auth = userName + ":" + password;
			String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
			requestBase.setHeader(new BasicHeader("Authorization", "Basic " + encodedAuth));
		}

		long start = System.nanoTime();
		try {
			CloseableHttpResponse response = httpClient.execute(requestBase, HttpClientContext.create());
			logDuration(start, requestBase, "took");
			return response;

		} catch (Exception e) {
			logDuration(start, requestBase, "failed after");
			throw e;
		}
	}

	private void logDuration(long start, HttpRequestBase requestBase, String what) {
		if (!logger.isDebugEnabled())
			return;

		try {
			logger.debug("Request " + what + ": " + (System.nanoTime() - start) / 1_000_000 + " ms. URL: " + requestBase.getURI().toURL().toString());
		} catch (Exception e) {
			// ignored
		}
	}

	protected static <T> Maybe<T> statusProblemMaybe(HttpUriRequest request, CloseableHttpResponse response) {
		return statusProblemMaybe(request.getURI().toString(), response);

	}

	protected static <T> Maybe<T> statusProblemMaybe(String url, CloseableHttpResponse response) {
		int statusCode = response.getStatusLine().getStatusCode();

		switch (statusCode) {
			case 404:
				return Reasons.build(NotFound.T).text("Resource not found  at url " + url).toMaybe();
			case 403:
				return Reasons.build(Forbidden.T).text("Forbidden access to resource at url " + url).toMaybe();
			case 401:
				return Reasons.build(AuthenticationFailure.T).text("Unauthenticated access to resource at url " + url).toMaybe();
			default:
				return Reasons.build(CommunicationError.T).text("Error [" + response.getStatusLine() + "] accessing resource at url " + url)
						.toMaybe();
		}
	}

	protected Maybe<String> readText(String url, String encoding) throws IOException {
		var maybe = openInputStream(url);

		if (maybe.isUnsatisfied())
			return maybe.whyUnsatisfied().asMaybe();

		try (InputStream in = maybe.get()) {
			return Maybe.complete(IOTools.slurp(in, encoding));
		}
	}

	protected Maybe<InputStream> openInputStream(String url) throws IOException {
		var responseMaybe = getResponseReasoned(url);

		if (responseMaybe.isUnsatisfied()) {
			return responseMaybe.whyUnsatisfied().asMaybe();
		}

		var response = responseMaybe.get();

		if (logger.isDebugEnabled()) {
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			String phrase = statusLine.getReasonPhrase();
			phrase = phrase != null ? " (" + phrase + ")" : "";
			logger.debug("received status " + statusCode + phrase + " from url " + url);
		}

		HttpEntity entity = response.getEntity();

		return Maybe.complete(entity.getContent());
	}

	protected Maybe<CloseableHttpResponse> getResponseReasoned(String url) {
		try {
			var response = getResponse(url);
			var statusLine = response.getStatusLine();
			var statusCode = statusLine.getStatusCode();

			if (statusCode >= 200 && statusCode < 300)
				return Maybe.complete(response);

			return statusProblemMaybe(url, response);
		} catch (UnknownHostException e) {
			logger.debug("Unknown host: " + url);
			return Reasons.build(UnknownRepositoryHost.T).text("Unknown host: " + url) //
					.toMaybe();
		} catch (Exception e) {
			String tracebackId = UUID.randomUUID().toString();
			String msg = "Could not open input stream for: " + url + " (tracebackId=" + tracebackId + ")";
			logger.error(msg, e);

			return Reasons.build(CommunicationError.T).text(msg).toMaybe();
		}
	}

	protected CloseableHttpResponse getResponse(String url) throws IOException {
		return getResponseWithRetry(new HttpGet(url));
	}

	protected CloseableHttpResponse getResponseWithRetry(HttpGet get) throws IOException {
		return getResponseWithRetry((HttpRequestBase) get);
	}

	private CloseableHttpResponse getResponseWithRetry(HttpRequestBase requestBase) throws IOException {
		String url = requestBase.getURI().toString();
		int attempt = 1;

		while (true) {
			try {
				CloseableHttpResponse response = getResponse(requestBase);
				int statusCode = response.getStatusLine().getStatusCode();
				RetryCascade retryCascade = retryCascadeForStatusCode(url, statusCode);

				if (retryCascade == null || !retryCascade.hasDelayForAttempt(attempt))
					return response;

				closeQuietly(response);

				long delayMs = retryCascade.delayForAttempt(attempt);
				logger.warn("received retryable HTTP status " + statusCode + " on try " + attempt + " of " + retryCascade.totalTries() + " for: " + url
						+ "; retrying after " + delayMs + " ms");

				sleepBeforeRetry(delayMs);
				attempt++;
			} catch (IOException e) {
				RetryCascade retryCascade = retryCascadeForException(url, e);
				if (retryCascade == null || !retryCascade.hasDelayForAttempt(attempt))
					throw e;

				long delayMs = retryCascade.delayForAttempt(attempt);
				logger.warn("failed try " + attempt + " of " + retryCascade.totalTries() + " to open a http request to: " + url
						+ "; retrying after " + delayMs + " ms", e);

				sleepBeforeRetry(delayMs);
				attempt++;
			}
		}
	}

	private static RetryCascade retryCascadeForStatusCode(String url, int statusCode) {
		for (RetryStatusCodeRule rule: RETRY_STATUS_CODE_RULES) {
			if (rule.matches(url, statusCode))
				return rule.retryCascade;
		}

		return null;
	}

	private static RetryCascade retryCascadeForException(String url, IOException exception) {
		if (isNonRetryableException(exception))
			return null;

		for (RetryExceptionRule rule: RETRY_EXCEPTION_RULES) {
			if (rule.matches(url, exception))
				return rule.retryCascade;
		}

		return null;
	}

	private static boolean isNonRetryableException(IOException exception) {
		for (Class<? extends IOException> nonRetryableExceptionType: NON_RETRYABLE_EXCEPTION_TYPES) {
			if (nonRetryableExceptionType.isInstance(exception))
				return true;
		}

		return false;
	}

	private static void sleepBeforeRetry(long delayMs) throws IOException {
		if (delayMs <= 0)
			return;

		try {
			Thread.sleep(delayMs);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Interrupted while waiting before retry", e);
		}
	}

	private void closeQuietly(CloseableHttpResponse response) {
		if (response == null)
			return;

		try {
			response.close();
		} catch (IOException e) {
			logger.warn("failed to close HTTP response before retry", e);
		}
	}

	private static class RetryStatusCodeRule {
		private final Pattern urlPattern;
		private final Set<Integer> statusCodes;
		private final RetryCascade retryCascade;

		private RetryStatusCodeRule(Pattern urlPattern, Set<Integer> statusCodes, RetryCascade retryCascade) {
			this.urlPattern = urlPattern;
			this.statusCodes = statusCodes;
			this.retryCascade = retryCascade;
		}

		private boolean matches(String url, int statusCode) {
			return statusCodes.contains(statusCode) && urlPattern.matcher(url).find();
		}
	}

	private static class RetryExceptionRule {
		private final Pattern urlPattern;
		private final Class<? extends IOException> exceptionType;
		private final RetryCascade retryCascade;

		private RetryExceptionRule(Pattern urlPattern, Class<? extends IOException> exceptionType, RetryCascade retryCascade) {
			this.urlPattern = urlPattern;
			this.exceptionType = exceptionType;
			this.retryCascade = retryCascade;
		}

		private boolean matches(String url, IOException exception) {
			return exceptionType.isInstance(exception) && urlPattern.matcher(url).find();
		}
	}

	private static class RetryCascade {
		private final long[] retryDelayMs;

		private RetryCascade(long[] retryDelayMs) {
			this.retryDelayMs = retryDelayMs;
		}

		private static RetryCascade of(long... retryDelayMs) {
			return new RetryCascade(Arrays.copyOf(retryDelayMs, retryDelayMs.length));
		}

		private boolean hasDelayForAttempt(int attempt) {
			return attempt > 0 && attempt <= retryDelayMs.length;
		}

		private long delayForAttempt(int attempt) {
			return retryDelayMs[attempt - 1];
		}

		private int totalTries() {
			return retryDelayMs.length + 1;
		}
	}
}
