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
package com.braintribe.marshaller.artifact.maven.settings.experts;

public interface HasSettingsTokens {
	static final String SETTINGS = "settings";
	static final String LOCAL_REPOSITORY = "localRepository";

	static final String INTERACTIVE_MODE = "interactiveMode";
	static final String OFFLINE = "offline";
	static final String USE_PLUGIN_REGISTRY = "usePluginRegistry";

	static final String ID = "id";
	
	static final String SERVERS = "servers";
	static final String SERVER = "server";
	static final String USERNAME = "username";
	static final String PASSWORD = "password";
	static final String FILE_PERMISSIONS = "filePermissions";
	static final String DIRECTORY_PERMISSIONS = "directoryPermissions";
	static final String PASSPHRASE = "passphrase";
	static final String PRIVATE_KEY = "privateKey";
	static final String CONFIGURATION = "configuration";
	
	static final String MIRRORS = "mirrors";
	static final String MIRROR = "mirror";
	static final String URL = "url";
	static final String MIRROR_OF = "mirrorOf";
	
	static final String PROXIES = "proxies";
	static final String PROXY = "proxy";
	static final String ACTIVE = "active";
	static final String PROTOCOL = "protocol";
	static final String HOST = "host";
	static final String PORT = "port";
	static final String NON_PROXY_HOSTS = "nonProxyHosts";

	static final String PROFILES = "profiles";
	static final String PROFILE = "profile";
	static final String PROPERTIES = "properties";
	static final String PROPERTY = "property";

	static final String ACTIVATION = "activation";
	static final String ACTIVE_BY_DEFAULT = "activeByDefault";
	static final String JDK = "jdk";
	static final String OS = "os";
	static final String NAME = "name";
	static final String FAMILY = "family";
	static final String ARCH = "arch";
	static final String VERSION = "version";

	static final String VALUE = "value";
	static final String FILE = "file";
	static final String EXISTS = "exists";
	static final String MISSING = "missing";

	static final String REPOSITORIES = "repositories";
	static final String REPOSITORY = "repository";
	
	static final String PLUGIN_REPOSITORIES = "pluginRepositories";
	static final String PLUGIN_REPOSITORY ="pluginRepository";
	
	static final String LAYOUT = "layout";
	static final String SNAPSHOTS = "snapshots";
	static final String ENABLED = "enabled";
	static final String UPDATE_POLICY = "updatePolicy";
	static final String CHECKSUM_POLICY = "checksumPolicy";
	static final String RELEASES = "releases";
	
	static final String ACTIVE_PROFILES = "activeProfiles";
	static final String ACTIVE_PROFILE = "activeProfile";
	
	static final String PLUGIN_GROUPS = "pluginGroups";
	static final String PLUGIN_GROUP = "pluginGroup";

}
