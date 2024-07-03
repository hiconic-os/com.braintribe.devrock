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

/**
 * a reflection of Maven's policy when it comes to CRC checks<br/>
 * NOTE: WE RUN A STRICT CRC POLICY DURING DOWNLOAD DISREGARDING THIS.<br/>
 * Rationale: it doesn't make sense to disregard the fact that a file we just downloaded
 * isn't complete (or rather: wrong)
 * 
 * @author pit / dirk
 *
 */
public enum ChecksumPolicy {
	ignore,warn,fail
}
