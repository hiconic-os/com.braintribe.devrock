import java.io.File;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.message.BasicHeader;

import com.braintribe.devrock.mc.core.commons.Downloads;
import com.braintribe.devrock.mc.core.download.PartDownloadInputStream;
import com.braintribe.gm.model.reason.Maybe;

public class GitHubDownloadSpeedLab {
	public static void main(String[] args) {

		SocketConfig socketConfig = SocketConfig.custom() //
				.setSoTimeout(15_0000) //
				.build();

		RequestConfig requestConfig = RequestConfig.custom() //
				.setConnectTimeout(10_000) //
				.setSocketTimeout(15_0000) //
				.build();

		PoolingHttpClientConnectionManager cxMgr = new PoolingHttpClientConnectionManager();
		cxMgr.setMaxTotal(1000);
		cxMgr.setDefaultMaxPerRoute(200);
		cxMgr.setDefaultSocketConfig(socketConfig);
		cxMgr.setValidateAfterInactivity(5_000);
		cxMgr.closeIdleConnections(1, TimeUnit.MINUTES);

		CloseableHttpClient httpClient = HttpClients.custom() //
				.setRoutePlanner(new SystemDefaultRoutePlanner(null)) //
				.setDefaultSocketConfig(socketConfig) //
				.setDefaultRequestConfig(requestConfig) //
				.setConnectionManager(cxMgr) //
				.build();



		File file1 = new File("C:\\devrock-sdk\\repo\\tribefire\\extension\\setup\\hiconic-sdk\\2.1.77\\test-maven-metadata.xml");
		File file2 = new File("C:\\devrock-sdk\\repo\\tribefire\\extension\\setup\\hiconic-sdk\\2.1.77\\test.zip");
		
		ExecutorService threadPool = Executors.newFixedThreadPool(5);
		
		try {
			
			List<Future<?>> futures = new ArrayList<>();
			
			futures.add(threadPool.submit(() -> {
				consume(httpClient, file1, "https://maven.pkg.github.com/hiconic-os/maven-repo-dev/tribefire/extension/setup/hiconic-sdk/maven-metadata.xml");
			}));
			
			futures.add(threadPool.submit(() -> {
				consume(httpClient, file2, "https://maven.pkg.github.com/hiconic-os/maven-repo-dev/tribefire/extension/setup/hiconic-sdk/2.1.77/hiconic-sdk-2.1.77.zip");
			}));
			
			for (Future<?> future: futures) {
				future.get();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void consume(CloseableHttpClient client, File file, String url) {
		HttpGet get = new HttpGet(url);
		
		String auth = "ignored:" + System.getenv("GITHUB_READ_PACKAGES_TOKEN");
		String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
		get.setHeader(new BasicHeader("Authorization", "Basic " + encodedAuth));

		try {
			long start = System.currentTimeMillis();
	
			InputStream in = client.execute(get, HttpClientContext.create()).getEntity().getContent();
			
			MessageDigest digest = MessageDigest.getInstance("MD5");
			
			PartDownloadInputStream pin = new PartDownloadInputStream(in, "test", null, null, null, url, digest, null);
			
			Downloads.downloadReasoned(file, () -> Maybe.complete(pin));
			
			long end = System.currentTimeMillis();
			System.out.println((end- start) + "ms");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
