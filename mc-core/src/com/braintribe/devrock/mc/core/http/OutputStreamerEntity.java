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

import org.apache.http.entity.AbstractHttpEntity;

import com.braintribe.model.generic.session.OutputStreamer;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipes;

public class OutputStreamerEntity extends AbstractHttpEntity {

	private OutputStreamer outputStreamer;
	
	public OutputStreamerEntity(OutputStreamer outputStreamer) {
		super();
		this.outputStreamer = outputStreamer;
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
		
		try (OutputStream out = pipe.openOutputStream()) {
			writeTo(out);
		}
		
		return pipe.openInputStream();
	}

	@Override
	public void writeTo(OutputStream outStream) throws IOException {
		outputStreamer.writeTo(outStream);
	}

	@Override
	public boolean isStreaming() {
		return false;
	}
}
