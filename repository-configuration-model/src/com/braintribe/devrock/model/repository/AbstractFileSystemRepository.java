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
package com.braintribe.devrock.model.repository;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Abstract
public interface AbstractFileSystemRepository extends Repository {

	final EntityType<AbstractFileSystemRepository> T = EntityTypes.T(AbstractFileSystemRepository.class);

	String rootPath = "rootPath";
	/**
	 * @return - the filesystem root 
	 */
	@Mandatory
	String getRootPath();
	void setRootPath(String rootPath);
	
	default Path normalizedRootPath() {
		String rootPath = getRootPath();
		if (rootPath == null)
			return null;
		
		return Paths.get(rootPath).normalize();
	}
}
