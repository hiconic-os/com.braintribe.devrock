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
package com.braintribe.marshaller.artifact.maven.metadata.commons;

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
