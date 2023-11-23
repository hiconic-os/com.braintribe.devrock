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
