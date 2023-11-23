// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.metadata;

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
