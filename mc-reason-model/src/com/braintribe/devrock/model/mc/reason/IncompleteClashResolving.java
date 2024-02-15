package com.braintribe.devrock.model.mc.reason;

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("clash resolving has encountered issues")
public interface IncompleteClashResolving extends McReason {
	
	EntityType<IncompleteClashResolving> T = EntityTypes.T(IncompleteClashResolving.class);

}
