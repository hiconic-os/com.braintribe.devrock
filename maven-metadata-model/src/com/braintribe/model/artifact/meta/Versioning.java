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
package com.braintribe.model.artifact.meta;

import java.util.Date;
import java.util.List;

import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.generic.GenericEntity;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


/**
 * the {@link Versioning} reflects all available versions of an artifact
 * @author pit
 *
 */
public interface Versioning extends GenericEntity{
		
	final EntityType<Versioning> T = EntityTypes.T(Versioning.class);
	
	String latest = "latest";
	String release = "release";
	String versions = "versions";
	String lastUpdated = "lastUpdated";
	String snapshot = "snapshot";
	String snapshotVersions = "snapshotVersions";
	
	/**
	 * @return - the {@link Version} tagged as latest 
	 */
	Version getLatest();
	void setLatest( Version latest);
	
	/**
	 * @return - the {@link Version} tagged as release (whatever that is(
	 */
	Version getRelease();
	void setRelease( Version release);
	
	/**
	 * @return - the {@link List} of available {@link Version}
	 */
	List<Version> getVersions();
	void setVersions( List<Version> versions);
	
	/**
	 * @return - last updated time stamp
	 */
	Date getLastUpdated();
	void setLastUpdated( Date lastUpdated);
	
	/**
	 * @return - the {@link Snapshot} if any
	 */
	Snapshot getSnapshot();
	void setSnapshot( Snapshot snapshot);

	/**
	 * @return - {@link List} of {@link SnapshotVersion}
	 */
	List<SnapshotVersion> getSnapshotVersions();
	void setSnapshotVersions( List<SnapshotVersion> snapshotVersions);
	
}
