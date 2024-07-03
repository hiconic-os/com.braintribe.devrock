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
package com.braintribe.devrock.mc.api.download;

import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.processing.async.api.Promise;

/**
 * a {@link PartDownloadScope} can be used to download files in a fair manner 
 * @author pit / dirk
 *
 */
public interface PartDownloadScope {
	/**
	 * @param identification - the {@link CompiledArtifactIdentification} that identifies the artifact 
	 * @param partIdentification - the {@link PartIdentification} that identifies the part
	 * @return - a {@link Promise} with the download job - which is automatically queued 
	 */
	Promise<Maybe<ArtifactDataResolution>> download(CompiledArtifactIdentification identification, PartIdentification partIdentification);
}
