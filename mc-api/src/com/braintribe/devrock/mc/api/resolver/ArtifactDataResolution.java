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
		return Maybe.complete(getResource().openStream());
	}
	
	/**
	 * @param supplier - supplies the {@link OutputStream} to write to 
	 * @throws IOException 
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
