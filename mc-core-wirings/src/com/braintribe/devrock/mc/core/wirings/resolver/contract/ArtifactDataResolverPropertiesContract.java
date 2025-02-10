package com.braintribe.devrock.mc.core.wirings.resolver.contract;

import com.braintribe.devrock.mc.core.wirings.properties.contract.PropertyLookupContract;
import com.braintribe.wire.api.annotation.Default;
import com.braintribe.wire.api.annotation.Name;

public interface ArtifactDataResolverPropertiesContract extends PropertyLookupContract {
	@Name("MC_DISABLE_PARALLEL_RESOLVING")
	@Default("false")
	boolean disableParallelResolving();
}
