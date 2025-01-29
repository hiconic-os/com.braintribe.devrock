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

import java.net.URL;

import com.braintribe.model.generic.annotation.Initializer;
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
	 * @return - the {@link ChecksumPolicy} set (may be null, defaulting to 'fail')
	 */
	@Initializer("fail")
	ChecksumPolicy getCheckSumPolicy();
	void setCheckSumPolicy(ChecksumPolicy checkSumPolicy);
}
