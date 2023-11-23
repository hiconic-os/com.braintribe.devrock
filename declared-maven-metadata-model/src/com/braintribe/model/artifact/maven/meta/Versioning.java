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
package com.braintribe.model.artifact.maven.meta;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.version.Version;


/**
 * the construct with the actual version information (for unversioned artifacts)
 * @author pit
 *
 */
public interface Versioning extends GenericEntity{
		
	final EntityType<Versioning> T = EntityTypes.T(Versioning.class);
	
	/**
	 * @return - latest {@link Version} (not really clear what's for)
	 */
	Version getLatest();
	void setLatest( Version latest);
	
	/**
	 * @return - {@link Version} that was labelled 'release' (not really clear what it's for)
	 */
	Version getRelease();
	void setRelease( Version release);
	
	/**
	 * @return - the {@link Version} that this artifact supports
	 */
	List<Version> getVersions();
	void setVersions( List<Version> versions);
	
	/**
	 * @return - timestamp when this section was updated
	 */
	String getLastUpdated();
	void setLastUpdated( String lastUpdated);
	
	/**
	 * @return - currently newest snapshot (if any)
	 */
	Snapshot getSnapshot();
	void setSnapshot( Snapshot snapshot);

	/**
	 * @return - the {@link SnapshotVersion} this artifact supports (if any)
	 */
	List<SnapshotVersion> getSnapshotVersions();
	void setSnapshotVersions( List<SnapshotVersion> snapshotVersions);
	
}
