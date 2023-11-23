# how to find what parts of an artifact are available

The new malaclypse can actively identify what parts of artifacts are available on the various configured repositories. It supports all currently known repository types (maven-compatible HTTP- and filesystem-repositories, artifactory-based repositories, the local repository and codebase-repositories). It will use the appropriate techniques to get that data.

That being said, please note that for maven-compatible repositories it will use the HTML output intended for humans. Maven Central doesn't like that, so make sure that you are not using the feature excessively with Maven Central. 

In case of a smarter repository, like the one we use - artifactory, the new malaclypse can use the more powerful methods. Even if Artifactory for instance cannot reflect on the full contents, it can give you the overview of an artifact with all its parts via a REST API call. How mc-ng detects the REST support is explained in the text about [probing]([AnalysisResolution](javadoc:com.braintribe.model.artifact.analysis.AnalysisArtifactResolution) implements [HasFailure](javadoc:com.braintribe.gm.model.reason.HasFailure) and therefore is able to tell you quite clearly what went [wrong](asset://com.braintribe.devrock:mc-core-documentation/configuration/probing.md).


In the middle-tier contract, the [ArtifactDataResolverContract](javadoc:com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract), exposes all you need to access mc-ng's part reflection but depending on your use case, you can also use a higher-tier contract.

You'll find it in the following artifact :

```
    com.braintribe.devrock:mc-core-wirings
```


You can access it via the middle tier : 

``` java
  RepositoryConfiguration configuration = ...;

    try (

                WireContext<ArtifactDataResolverContract> resolverContext = Wire.contextBuilder( ArtifactDataResolverModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
                    .bindContract(RepositoryConfigurationContract.class, () -> configuration)     
                    .build();
        ) {

            ArtifactDataResolverContract artifactDataResolverContract = resolverContext.contract();
            
            PartAvailabilityReflection partAvailabilityReflection = artifactDataResolverContract.partAvailabilityReflection();
            ... 
        }
```

As ALL top tiers - the intermediate tier that exposes the TransitiveDependencyResolver, the apex tier for classpath resolving, even the apex tier for platform-asset resolving (in tribefire.cortex) - are built upon the middle-tier, ALL of these top tiers can deliver the reflection.

## part availability reflection

The reflection works with entities of [PartReflection](javadoc:com.braintribe.model.artifact.consumable.PartReflection). It combines the PartIdentification with the RepositoryOrigin.


So if you have a list of Artifacts, or [CompiledArtifactIdentification](javadoc:com.braintribe.model.artifact.compiled.CompiledArtifactIdentification) you can use something like this here : 


``` java
  RepositoryConfiguration configuration = ...;
  List<CompiledArtifactIdentification> artifacts = ...;

    try (

                WireContext<ArtifactDataResolverContract> resolverContext = Wire.contextBuilder( ArtifactDataResolverModule.INSTANCE)
                    .bindContract(RepositoryConfigurationContract.class, () -> configuration)     
                    .build();
        ) {

            ArtifactDataResolverContract artifactDataResolverContract = resolverContext.contract();
                   
                    
            PartAvailabilityReflection partAvailabilityReflection = artifactDataResolverContract.partAvailabilityReflection();
            
            for (CompiledArtifactIdentification cai : artifacts) {
                List<PartReflection> partsOf = partAvailabilityReflection.getAvailablePartsOf( cai);
                ...;
            }
            
        }
```


Now if you wanted to download the all available parts of an artifact from a given repository, all you'd do is to step over the result and let the [PartDownloadManager](javadoc:com.braintribe.devrock.mc.api.download.PartDownloadManager) do its magick.

``` java

    ...;
    
    String repoId = ...;
        
	List<PartReflection> allKnownPartsOf = partAvailabilityReflection.getAvailablePartsOf(compiledTargetIdentification);
	List<PartReflection> partsOfRepository = allKnownPartsOf.stream()
														.filter( p -> repoId.equals(p.getRepositoryOrigin().getName()))
														.collect( Collectors.toList());
			
	PartDownloadManager downloadManager = resolverContext.contract().dataResolverContract().partDownloadManager();
			
	PartDownloadScope partDownloadScope = downloadManager.openDownloadScope();
			
	if (partsOfRepository.size() != 0) {
			Map<CompiledPartIdentification, Promise<Maybe<ArtifactDataResolution>>> promises = new HashMap<>( partsOfRepository.size());
			
			List<String> assertions = new ArrayList<>( partsOfRepository.size());
			for (PartReflection pr : partsOfRepository) {
				CompiledPartIdentification cpi = CompiledPartIdentification.from(compiledTargetIdentification, pr);
				promises.put( cpi, partDownloadScope.download(cpi, cpi));
			}
			List<CompiledPartIdentification> successfullyDownloadedParts = new ArrayList<>( promises.size());
			
			for (Map.Entry<CompiledPartIdentification, Promise<Maybe<ArtifactDataResolution>>> entry : promises.entrySet()) {					
				Maybe<ArtifactDataResolution> optional = entry.getValue().get();
				if (!optional.isSatisfied()) {
					assertions.add( "couldn't download [" + entry.getKey().asString() +"]");
				}
				else {
					successfullyDownloadedParts.add( entry.getKey());
				}
			}
			if (assertions.size() > 0) {
				Assert.fail( assertions.stream().collect(Collectors.joining("\t\n")));
			}
			
			return successfullyDownloadedParts;
		}
		
		return null;
    ...;
        
```


