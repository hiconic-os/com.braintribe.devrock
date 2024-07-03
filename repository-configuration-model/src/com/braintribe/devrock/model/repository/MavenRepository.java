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

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * a dumbed- down/simplified version of a repository, containing only the minimal requirements, but still allowing all
 * sensible configuration possibilities
 *
 * @author pit
 *
 */
// this type is intentionally not abstract, since repository views may use it to hold (partial) repository data
public interface MavenRepository extends Repository {

	final EntityType<MavenRepository> T = EntityTypes.T(MavenRepository.class);



}
