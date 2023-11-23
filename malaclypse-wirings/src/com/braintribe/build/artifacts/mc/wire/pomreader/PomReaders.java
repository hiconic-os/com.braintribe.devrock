// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifacts.mc.wire.pomreader;

import java.util.function.Consumer;

import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifacts.mc.wire.pomreader.contract.ConfigurablePomReaderExternalContract;
import com.braintribe.build.artifacts.mc.wire.pomreader.contract.PomReaderContract;
import com.braintribe.build.artifacts.mc.wire.pomreader.external.contract.PomReaderExternalContract;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.context.WireContextBuilder;

public interface PomReaders {
	static WireContext<PomReaderContract> buildContext(PomReaderExternalContract externalConfiguration) {
		WireContext<PomReaderContract> wireContext = Wire
			.context(PomReaderContract.class)
			.bindContracts(PomReaders.class.getPackage().getName())
			.bindContract(PomReaderExternalContract.class, externalConfiguration)
			.build();
	
		return wireContext;
	}
	
	static PomReaderSetupBuilder build() {
		return new PomReaderSetupBuilder() {
			private Consumer<WireContextBuilder<PomReaderContract>> contextConfigurer;
			
			@Override
			public PomReaderSetupBuilder configureWireContext(Consumer<WireContextBuilder<PomReaderContract>> contextConfigurer) {
				this.contextConfigurer = contextConfigurer;
				return this;
			}
			
			
			@Override
			public PomReaderSetup done() {
				WireContextBuilder<PomReaderContract> contextBuilder = Wire.context(PomReaderContract.class)
						.bindContracts(PomReaders.class.getPackage().getName());
				
				
				if (contextConfigurer != null)
					contextConfigurer.accept(contextBuilder);
				
				
				if (!contextBuilder.isContractBound(PomReaderExternalContract.class)) {
					contextBuilder.bindContract(PomReaderExternalContract.class, new ConfigurablePomReaderExternalContract());
				}
				
				WireContext<PomReaderContract> wireContext = contextBuilder.build();
				

				return new PomReaderSetup() {
					
					@Override
					public void close() throws Exception {
						wireContext.shutdown();
					}
					
					@Override
					public WireContext<PomReaderContract> wireContext() {
						return wireContext;
					}
					
					@Override
					public ArtifactPomReader pomReader() {
						return wireContext.contract().pomReader();
					}
				};
			}
		};
	}
	
}
