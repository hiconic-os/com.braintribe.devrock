// ============================================================================
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

import java.io.File;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mc.api.repository.RepositoryProbingSupport;
import com.braintribe.devrock.model.mc.reason.configuration.HasRepository;
import com.braintribe.devrock.model.mc.reason.configuration.RepositoryUnavailable;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.essential.FilesystemError;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.changes.RepositoryProbeStatus;
import com.braintribe.model.artifact.changes.RepositoryProbingResult;

public class FilesystemRepositoryProbingSupport implements RepositoryProbingSupport {
	
	private File root;
	private String repositoryId;
	
	@Configurable @Required
	public void setRoot(File root) {
		this.root = root;
	}

	@Configurable @Required
	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	@Override
	public RepositoryProbingResult probe() {
		if (root.isDirectory())
			return RepositoryProbingResult.create(RepositoryProbeStatus.available, null, null, null);
		else
			return RepositoryProbingResult.create(RepositoryProbeStatus.unavailable, error(), null, null);
	}

	private Reason error() {
		return TemplateReasons.build(RepositoryUnavailable.T) //
				.assign(HasRepository::setRepository, repositoryId) //
				.cause(FilesystemError.create("Directory [" + root + "] " + (root.exists() ? "is not a directory." : "does not exist."))) //
				.toReason();
	}

	@Override
	public String repositoryId() {
		return repositoryId;
	}

}
