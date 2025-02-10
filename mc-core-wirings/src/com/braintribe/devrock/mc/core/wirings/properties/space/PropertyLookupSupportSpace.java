package com.braintribe.devrock.mc.core.wirings.properties.space;

import com.braintribe.devrock.mc.core.wirings.properties.contract.PropertyLookupModuleContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;
import com.braintribe.wire.api.space.WireSpace;

@Managed
public class PropertyLookupSupportSpace implements WireSpace {
	@Import
	VirtualEnvironmentContract virtualEnvironment;
	
	@Import
	PropertyLookupModuleContract propertyLookupModule;
	
	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		propertyLookupModule.init(virtualEnvironment.virtualEnvironment());
	}
}
