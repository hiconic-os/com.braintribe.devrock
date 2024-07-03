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
