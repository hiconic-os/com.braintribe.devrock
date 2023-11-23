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
package com.braintribe.devrock.model.repository;

import java.net.URL;

import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface MavenHttpRepository extends MavenRepository, HasCredentials {

	final EntityType<MavenHttpRepository> T = EntityTypes.T(MavenHttpRepository.class);

	String url = "url";	
	String weaklyCertified = "weaklyCertified";
	String probingMethod = "probingMethod";
	String probingPath = "probingPath";
	String checksumPolicy = "checksumPolicy";
	
	/**
	 * some repositories have bad https relevant certificates. Ours do not.<br/>
	 * true if the access to the url of the repositories needs be to lenient (insecure), false if the access to the url
	 * of the repositories can be strict (secure),
	 *
	 * @return - whether the URL is weakly certified
	 */
	boolean getIsWeaklyCertified();
	void setIsWeaklyCertified(boolean weaklyCertified);

	/**
	 * @return - the {@link String} representation of the {@link URL} of the {@link MavenRepository}. May contain
	 *         variables like ${env.*}
	 */
	@Mandatory
	String getUrl();
	void setUrl(String url);

	
	/**
	 * @return - the {@link RepositoryProbingMethod} to be used
	 */
	RepositoryProbingMethod getProbingMethod();
	void setProbingMethod(RepositoryProbingMethod probingMethod);

	/**
	 * @return - an suffix to the base url used for probing
	 */
	String getProbingPath();
	void setProbingPath(String probingPath);

	/**
	 * @return - the {@link ChecksumPolicy} set (may be null, defaulting to 'ignore')
	 */
	ChecksumPolicy getCheckSumPolicy();
	void setCheckSumPolicy(ChecksumPolicy checkSumPolicy);
}
