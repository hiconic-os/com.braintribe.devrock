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
package com.braintribe.devrock.mc.core.configuration;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryConfigurationLocation;
import com.braintribe.gm.model.reason.Reason;

public class RepositoryConfigurationLocationImpl implements RepositoryConfigurationLocation {

	
	private Reason origination;
	private File file;
	private Map<String, String> properties = Collections.emptyMap();

	public RepositoryConfigurationLocationImpl(File file, Reason origination) {
		super();
		this.origination = origination;
		this.file = file;
	}

	@Override
	public File getFile() {
		return file;
	}

	@Override
	public Reason getOrigination() {
		return origination;
	}

	@Configurable
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	@Override
	public Map<String, String> getProperties() {
		return properties;
	}

}
