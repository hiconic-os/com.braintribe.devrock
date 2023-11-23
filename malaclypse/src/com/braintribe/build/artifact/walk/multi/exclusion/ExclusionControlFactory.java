// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.exclusion;

import java.util.function.Function;

import com.braintribe.model.malaclypse.cfg.denotations.ExclusionControlDenotationType;


/**
 * a factory that creates and parameterizes a {@link ExclusionControl} from the {@link ExclusionControlDenotationType}
 * 
 * @author Pit
 *
 */
public class ExclusionControlFactory implements Function<ExclusionControlDenotationType, ExclusionControl> {

	@Override
	public ExclusionControl apply(ExclusionControlDenotationType denotation) throws RuntimeException {
		try {
			ExclusionControl exclusionControl = new ExclusionControl( denotation.getExclusions());			
			return exclusionControl;
		} catch (ExclusionControlException e) {
			throw new RuntimeException(e);
		}
	}
	

}
