// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom.marshaller.experts;

public interface HasPomTokens {
	final static String PROJECT = "project";
	final static String GROUPID = "groupId";
	final static String ARTIFACTID = "artifactId";
	final static String VERSION = "version";
	final static String PARENT = "parent";
	final static String PACKAGING = "packaging";
	
	final static String LICENSES = "licenses";
	final static String LICENSE = "license";
	final static String NAME = "name";
	final static String URL = "url";
	final static String DISTRIBUTION = "distribution";
	
	final static String PROPERTIES = "properties";
	
	final static String DEPENDENCIES = "dependencies";
	final static String DEPENDENCY = "dependency";
	final static String TYPE = "type";
	final static String CLASSIFIER = "classifier";
	final static String SCOPE = "scope";
	final static String OPTIONAL = "optional";
	
	final static String PI_GROUP = "group";
	final static String PI_ENRICH = "enrich";
	final static String PI_PART = "part";
	final static String PI_TAG = "tag";
	final static String PI_REDIRECT = "redirect";
	
	final static String EXCLUSIONS = "exclusions";
	final static String EXCLUSION = "exclusion";
	
	final static String DEPENDENCY_MANAGEMENT = "dependencyManagement";
	
	final static String DISTRIBUTION_MANAGEMENT = "distributionManagement";
	final static String RELOCATION = "relocation";
	
}
