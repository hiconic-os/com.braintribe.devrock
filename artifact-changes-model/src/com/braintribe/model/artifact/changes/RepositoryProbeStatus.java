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
package com.braintribe.model.artifact.changes;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

/**
 * the different states a repository probe can result in
 * 
 * @author pit / dirk
 */
public enum RepositoryProbeStatus implements EnumBase<RepositoryProbeStatus> {
    available,
    unauthorized,
    unauthenticated,
    erroneous,
    unavailable,
    unprobed;

    public static final EnumType<RepositoryProbeStatus> T = EnumTypes.T(RepositoryProbeStatus.class);

    @Override
    public EnumType<RepositoryProbeStatus> type() {
        return T;
    }

}