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
package com.braintribe.devrock.model.mc.reason;

import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("checksum mismatch on ${url}: expected ${expectedHash}, found ${foundHash}")
public interface MismatchingHash extends McReason, NotFound {
	
	EntityType<MismatchingHash> T = EntityTypes.T(MismatchingHash.class);
	
	String url = "url";
	String expectedHash = "expectedHash";
	String foundHash = "foundHash";
	String hashAlgorithm = "hashAlgorithm";
	
	String getUrl();
	void setUrl(String value);
	
	String getExpectedHash();
	void setExpectedHash(String value);
	
	String getFoundHash();
	void setFoundHash(String value);

	String getHashAlgorithm();
	void setHashAlgorithm(String value);



}
