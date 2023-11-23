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
