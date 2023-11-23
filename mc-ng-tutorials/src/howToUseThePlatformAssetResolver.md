# How to use the platform asset resolver

The platform asset resolver (PAR) is not part of the devrock group by itself, but it is built on mc-core, on the transitive dependency resolver (TDR). 

It is used by Jinni to build a tribefire setup, and as this requires a special logic that standard Maven cannot handle, it requires a special resolver. 

You'll find the PAR in

    tribefire.cortext:platform-asset-resolving-ng


It is quite simple to be used as the standard pattern of all resolvers is used as well.

``` java
    try (     
         WireContext<AssetResolverContract> context = Wire.contextBuilder(AssetResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
				 	.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))               
	                .build();
    ){
    
        AssetDependencyResolver assetResolver = context.contract().assetDependencyResolver();
		
		CompiledDependencyIdentification setupDependency = CompiledDependencyIdentification.parseAndRangify("tribefire.extension.demo:demo-setup#2.0", true);

		AssetResolutionContext resolutionContext = AssetResolutionContext.build().selectorFiltering(true).done();
		
		PlatformAssetResolution assetResolution = assetResolver.resolve(resolutionContext, setupDependency);
        
        ....


			
    }
```
