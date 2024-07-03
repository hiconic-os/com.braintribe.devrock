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
package com.braintribe.devrock.mc.api.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.CommunicationError;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.model.resource.Resource;

/**
 * the return value of the resolvers
 * 
 * @author pit / dirk
 */
public interface ArtifactDataResolution {

	/**
	 * @return - the {@link Resource} that *might* be the result of the resolution 
	 */
	Resource getResource();
	
	default Maybe<InputStream> openStream() {
		Resource resource = getResource();
		if (resource == null)
			return InternalError.create(toString() + " has no resource.").asMaybe();

		try {
			return Maybe.complete(resource.openStream());

		} catch (Exception e) {
			return InternalError.from(e, toString() + " could not open stream on it's resource: " + resource).asMaybe();
		}
	}

	/**
	 * @param supplier - supplies the {@link OutputStream} to write to 
	 * @return- true if it succeeded, false otherwise 
	 */
	boolean tryWriteTo( Supplier<OutputStream> supplier) throws IOException;
	
	default Reason tryWriteToReasoned( Supplier<OutputStream> supplier) {
		try {
			if (tryWriteTo(supplier))
				return null;

			return Reasons.build(CommunicationError.T).text("Unknown problem").toReason();
			
		} catch (Exception e) {
			return Reasons.build(CommunicationError.T).text("Error while transferring resource").cause(InternalError.from(e)).toReason();
		}
	}

	/**
	 * @param out - the {@link OutputStream} to write to
	 * @throws IOException - thrown if it couldn't successfully write the file
	 */
	void writeTo( OutputStream out) throws IOException;
	
	/**
	 * @return - true if the the {@link ArtifactDataResolution} is backed by data (i.e. exists in a remote repository)
	 */
	boolean isBacked();
	
	default Maybe<Boolean> backed() {
		return Maybe.complete(isBacked());
	}
	
	String repositoryId();
}
