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
package com.braintribe.marshaller.maven.metadata;

public interface HasTokens {

	static String tag_metaData = "metadata";
	
	static String tag_groupId = "groupId";
	static String tag_artifactId = "artifactId";
	static String tag_version = "version";
	
	static String tag_versioning = "versioning";
	static String tag_release = "release";
	static String tag_latest = "latest";
	static String tag_lastUpdated = "lastUpdated";
	static String tag_versions = "versions";
	
	static String tag_snapshot = "snapshot";
	static String tag_buildNumber = "buildNumber";
	static String tag_timestamp = "timestamp";
	static String tag_localCopy = "localCopy";
	
	static String tag_snapshotVersions = "snapshotVersions";
	static String tag_snapshotVersion = "snapshotVersion";
	static String tag_classifier = "classifier";
	static String tag_extension = "extension";
	static String tag_value = "value";
	static String tag_updated = "updated";
	
	static String tag_plugins = "plugins";
	static String tag_plugin = "plugin";
	static String tag_name = "name";
	static String tag_prefix = "prefix";
}
