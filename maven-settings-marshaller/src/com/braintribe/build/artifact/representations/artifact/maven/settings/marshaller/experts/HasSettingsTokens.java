// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.settings.marshaller.experts;

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
