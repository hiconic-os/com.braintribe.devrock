package com.braintribe.devrock.mc.core.download;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.function.Consumer;

import com.braintribe.devrock.mc.api.event.EventBroadcaster;
import com.braintribe.devrock.mc.api.event.EventBroadcasterAttribute;
import com.braintribe.devrock.model.mc.core.event.OnPartDownloaded;
import com.braintribe.devrock.model.mc.core.event.OnPartDownloading;
import com.braintribe.devrock.model.mc.core.event.PartDownloadEvent;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.model.version.Version;
import com.braintribe.utils.collection.impl.AttributeContexts;

public class PartDownloadInputStream extends InputStream {
	private InputStream in;
	private final EventBroadcaster eventBroadcaster;
	private int dataAmount = 0;
	private int totalDataAmount = 0;
	private String repositoryOrigin;
	private ArtifactIdentification artifact;
	private Version version;
	private PartIdentification part;
	private long start;
	private MessageDigest digest;
	private Consumer<MessageDigest> digestConsumer;
	private String path;
	
	public PartDownloadInputStream(InputStream in, String repositoryOrigin, ArtifactIdentification artifact, Version version, PartIdentification part, String path, MessageDigest digest, Consumer<MessageDigest> digestConsumer) {
		super();
		this.in = in;
		this.repositoryOrigin = repositoryOrigin;
		this.artifact = artifact;
		this.version = version;
		this.part = part;
		this.path = path;
		this.digest = digest;
		this.digestConsumer = digestConsumer;
		this.eventBroadcaster = AttributeContexts.peek().findAttribute(EventBroadcasterAttribute.class).orElse(EventBroadcaster.empty);

		notifyStart();
	}
	
	private <T extends PartDownloadEvent> T createEvent(EntityType<T> type) {
		var event = type.create();
		event.setArtifact(artifact);
		event.setVersion(version);
		event.setPart(part);
		event.setPath(path);
		event.setRepositoryOrigin(repositoryOrigin);
		return event;
	}
	
	private void notifyStart() {
		start = System.currentTimeMillis();
		
		OnPartDownloading event = createEvent(OnPartDownloading.T);
		eventBroadcaster.sendEvent(event);
	}

	private void notify(int bytesRead) {
		this.dataAmount = bytesRead;
		this.totalDataAmount += bytesRead;
		OnPartDownloading event = createEvent(OnPartDownloading.T);
		event.setDataAmount(dataAmount);
		event.setTotalDataAmount(totalDataAmount);
		eventBroadcaster.sendEvent(event);
	}
	
	private void notifyEnd() {
		long end = System.currentTimeMillis();
		
		TimeSpan elapsedTime = TimeSpan.create((end-start)/1000.0, TimeUnit.second);
		
		OnPartDownloaded event = createEvent(OnPartDownloaded.T);
		event.setElapsedTime(elapsedTime);
		event.setDownloadSize(totalDataAmount);
		
		eventBroadcaster.sendEvent(event);
		
		if (digest != null && digestConsumer != null) {
			digestConsumer.accept(digest);
		}
	}
	
	@Override
	public int read() throws IOException {
		int res = in.read();
		
		if (res != -1) {
			if (digest != null)
				digest.update((byte)res);

			notify(1);
		}
		else
			notifyEnd();
		
		return res;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int res = in.read(b, off, len);

		if (res != -1) {
			if (digest != null)
				digest.update(b, off, res);
			
			notify(res);
		}
		else
			notifyEnd();
		
		return res;
	}
	
	@Override
	public void close() throws IOException {
		in.close();
	}
}
