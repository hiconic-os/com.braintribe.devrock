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
package com.braintribe.devrock.mc.core.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.IOTools;

/**
 * the basic (aka standard) implementation of a {@link ArtifactDataResolution}
 * @author pit
 *
 */
public class BasicArtifactDataResolution implements ArtifactDataResolution {
	private Resource resource;
	private String repositoryId = "unknown";
	
	public BasicArtifactDataResolution() {	
	}
	
	public BasicArtifactDataResolution(Resource resource) {	
		this.resource = resource;
	}
	
	@Configurable
	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}
	
	@Configurable
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	@Override
	public Resource getResource() {	
		return resource;
	}
	
	@Override
	public boolean tryWriteTo(Supplier<OutputStream> supplier) throws IOException{		
		writeTo( supplier.get());				
		return true;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		if (resource == null) {
			throw new IOException("no resource present" );
		}
		try (InputStream in = resource.openStream()) {
			IOTools.transferBytes(in, out, IOTools.BUFFER_SUPPLIER_64K);
		}		
	}

	@Override
	public boolean isBacked() {	
		return true;
	}

	@Override
	public String repositoryId() {	
		return repositoryId;
	}
	
}
