// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope;

public interface HasRavenhurstTokens {
	public static final String RAVENHURST_DEFAULT_CONTEXT = "rest";
	public static final String RAVENHURST_DEFAULT_FUNCTION = "changes";
	public static final String RAVENHURST_DEFAULT_PARAMETER = "timestamp";	
	public static final String RAVENHURST_DEFAULT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	
		
	public static final String RAVENHURST_URL = "ravenhurst-url";
	public static final String RAVENHURST_CONTEXT = "ravenhurst-context";
	public static final String RAVENHURST_FUNCTION = "ravenhurst-function";
	public static final String RAVENHURST_PARAMETER = "ravenhurst-parameter";
	public static final String RAVENHURST_FORMAT = "ravenhurst-format";
	public static final String RAVENHURST_INTERROGATION_CLIENT = "ravenhurst-client";
	public static final String REPOSITORY_ACCESS_CLIENT = "repository-client";
	
	public static final String INDEX_DECLARATION_PROPERTY_PREFIX = "index-declaration";
	public static final String DYN_UPDATE_REPOS = "updateReflectingRepositories";
	public static final String TRUSTWORTHY_REPOS = "trustworthyRepositories";
	public static final String WEAK_CERTIFIED_REPOS = "weaklyCertifiedRepositories";
	public static final String LISTING_LENIENT_REPOS = "listingLenientRepositories";

	public static final String UPDATE_INTERVAL = "interval:";
	public static final String UPDATE_NEVER = "never";
	public static final String UPDATE_ALWAYS = "always";
	public static final String UPDATE_DAILY = "daily";
	
	public static final int UPDATE_DAILY_IN_MS = 24 * 60 * 60 * 1000;
	
	public static final String DEVROCK_REPOSITORY_CONFIGURATION = "DEVROCK_REPOSITORY_CONFIGURATION";
	
}
