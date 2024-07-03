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
package com.braintribe.model.ravenhurst.data;

import java.util.Map;

import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.ravenhurst.Artifact;

public interface RavenhurstSolutionDataContainer extends RavenhurstDataContainer {
	final EntityType<RavenhurstSolutionDataContainer> T = EntityTypes.T(RavenhurstSolutionDataContainer.class);

	void setSolution(Solution solution);
	Solution getSolution();

	void setUrlToSolutionMap(Map<String, Artifact> map);
	Map<String, Artifact> getUrlToSolutionMap();

	void setUrlToMetaDataMap(Map<String, MavenMetaData> metadata);
	Map<String, MavenMetaData> getUrlToMetaDataMap();

	void setMavenMetaDataToRepositoryRoleMap(Map<MavenMetaData, RepositoryRole> map);
	Map<MavenMetaData, RepositoryRole> getMavenMetaDataToRepositoryRoleMap();
}
