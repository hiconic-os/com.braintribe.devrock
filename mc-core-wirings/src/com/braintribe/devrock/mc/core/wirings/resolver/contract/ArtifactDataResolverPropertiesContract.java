package com.braintribe.devrock.mc.core.wirings.resolver.contract;

import com.braintribe.devrock.mc.core.wirings.properties.contract.PropertyLookupContract;
import com.braintribe.wire.api.annotation.Default;
import com.braintribe.wire.api.annotation.Name;

public interface ArtifactDataResolverPropertiesContract extends PropertyLookupContract {

	@Name("MC_DISABLE_PARALLEL_RESOLVING")
	@Default("false")
	boolean disableParallelResolving();

	// This was originally 5 Seconds. But we had a problem behind a proxy that files > ~1MB would fail with a SocketTimeoutException
	// Maven/Gradle allegedly have infinite socket timeout by default
	@Name("MC_SOCKET_TIMEOUT")
	@Default("150000")
	int socketTimeout();

}
