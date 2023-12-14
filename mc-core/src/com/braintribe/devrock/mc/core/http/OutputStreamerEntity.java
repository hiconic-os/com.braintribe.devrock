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
package com.braintribe.devrock.mc.core.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.entity.AbstractHttpEntity;

import com.braintribe.model.generic.session.OutputStreamer;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipes;

public class OutputStreamerEntity extends AbstractHttpEntity {

	private OutputStreamer outputStreamer;
	private boolean buildMd5;
	private String md5;
	private MessageDigest digest;
	
	public OutputStreamerEntity(OutputStreamer outputStreamer) {
		this(outputStreamer, false);
	}
	
	public OutputStreamerEntity(OutputStreamer outputStreamer, boolean buildMd5) {
		super();
		this.outputStreamer = outputStreamer;
		this.buildMd5 = buildMd5;
	}

	@Override
	public boolean isRepeatable() {
		return true;
	}

	@Override
	public long getContentLength() {
		return -1;
	}

	@Override
	public InputStream getContent() throws IOException, UnsupportedOperationException {
		StreamPipe pipe = StreamPipes.fileBackedFactory().newPipe("http-output");
		
		try (OutputStream out = wrapWithDigestIfRequired(pipe.openOutputStream())) {
			writeTo(out);
		}
		
		assignDigestIfRequired();
		
		return pipe.openInputStream();
	}

	private void assignDigestIfRequired() {
		if (digest != null) {
			md5 = StringTools.toHex(digest.digest());
		}
	}
	
	private OutputStream wrapWithDigestIfRequired(OutputStream out) {
		if (buildMd5) {
			try {
				digest = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				throw new UnsupportedOperationException(e);
			}
			out = new DigestOutputStream(out, digest);
		}
		return out;
	}

	@Override
	public void writeTo(OutputStream outStream) throws IOException {
		OutputStream out = wrapWithDigestIfRequired(outStream);
		outputStreamer.writeTo(out);
		out.flush();
		assignDigestIfRequired();
	}

	@Override
	public boolean isStreaming() {
		return false;
	}
	
	public String getMd5() {
		return md5;
	}
}
