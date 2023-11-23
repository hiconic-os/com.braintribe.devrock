// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.scope;

/**
 * an aggregation interface to collect <br/>
 * <ul>
 * 	<li> {@link MavenFactoryScope} </li>
 * 	<li> {@link RavenhurstFactoryScope} </li>
 *  <li> {@link PomFactoryScope} </li>
 *  <li> {@link WalkFactoryScope} </li>
 * </ul>
 * @author pit
 *
 */
public interface MalaclypseFactoryScope extends MavenFactoryScope, RavenhurstFactoryScope, PomFactoryScope, WalkFactoryScope {
}
