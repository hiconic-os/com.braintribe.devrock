# Using mc-core

In order to use the mc-core, you should rely on the wiring found in

```
com.braintribe.devrock:mc-core-wirings#[1.0, 1.1)
```

while there are three tiers (or levels), you'd be only interested in the middle and the top tier.

## middle tier
The middle tier contains things like finding the best match for a dependency, finding a specific artifact, downloading parts of an artifact, and working with a repository configuration (based on Maven's settings.xml for instance).

The middle tier also comes with a cache, so as long as the scope is alive, it will automatically cache requests.

A simple setup would look something like this :

``` java
		try (
			WireContext<ArtifactDataResolverContract> resolverContext = Wire.contextBuilder(
								ArtifactDataResolverModule.INSTANCE,
								MavenConfigurationWireModule.INSTANCE)
							.build();

			ArtifactDataResolverContract artifactDataResolverContract = resolverContext.contract();

			...
			}

```
In this example, the [MavenConfigurationWireModule](../configuration/mavenconfiguration.md) would load the configuration using the standard environment data to find the appropriate settings. You could inject another Module that can handle a [repository configuration](../configuration/configuration.md) of course.

You can of course bind the Virtual Environment into the setup as in this example if you need to overload the standard environment, in this case it used to redirect the settings loader to use a single settings.xml outside of the standard Maven locations.

``` java
		OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
		ves.addEnvironmentOverride("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", mySettings.getAbsolutePath());

		try (

				WireContext<ArtifactDataResolverContract> resolverContext = Wire.contextBuilder( ArtifactDataResolverModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> ove)				
					.build();
		) {

			ArtifactDataResolverContract artifactDataResolverContract = resolverContext.contract();

			....						
		}

```

>Note : the try{...}  wrapper makes sure that Wire's auto-closing is called.

>Note: the three described feature have all been tested in a parallel process (using an ExcecutorService) and passed, yet only the implementation of the final level (the 'transitive resolvers' or 'traversing resolvers' or 'dependency tree walkers' - whatever) will be the final test.

Once you have the contract, you can access the three features of the middle tier :

### dependency resolving
in order to find the best matching artifact to a given dependency :

``` java
		DependencyResolver dependencyResolver = artifactDataResolverContract.dependencyResolver();		
		CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parse( "com.braintribe.devrock:mc-core#[1.0,1.1)");		
		Maybe<CompiledArtifactIdentification> resolvedDependency = dependencyResolver.resolveDependency( cdi);
		if (resolvedDependency.isSatisfied()) {
			...
		}
```

### artifact resolving
in order to find an artifact and access its data (such as dependencies):

``` java
		CompiledArtifactResolver compiledArtifactResolver = artifactDataResolverContract.compiledArtifactResolver();
		CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse("com.braintribe.devrock:mc-core#1.0.5");
		Maybe<CompiledArtifact> resolvedCai = compiledArtifactResolver.resolve( cai);		
		if (resolvedCai.isSatisfied()) {
			...
		}
```


### part resolving
in order to get a part (the jar or pom) as a resource :

``` java
		ArtifactPartResolver artifactPartResolver = artifactDataResolverContract.artifactPartResolver();
		CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse("com.braintribe.devrock:mc-core#1.0.5");
		PartIdentification pi = PartIdentification.of("pom");
		Maybe<ArtifactDataResolution> pom = resolver.resolvePart(cai, pi);
		if (pom.isSatisfied()) {
			ArtifactDataResolution res = pom.get();
			Resource resource = res.getResource();
			....
		}
```

### intermediate top tier 

Somewhat a higher tier, but still not the top most tier - the tier with the basic transitive resolver. All other resolver (the real apex resolvers) are built on top of this resolver. On its own, it's a generic resolver - comparable to the parallel-build-resolver of mc-legacy. You can configure it to be used as the build resolver (returning the artifacts required to build the terminal), as a repository extractor (returning all artifacts that a terminal requires in a repository to be able to be built. )


Setting your use is split into two parts:

- setting up the back-end configuration (local repository, remote repositories, RH access et al)
- setting up the rules for the resolving (scopes, filters, inclusions/exclusion et al).

The former is done via Wire and the latter via specifying a context to the actual resolver call. 


The wire part is simple to use : 

``` java
    OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
    ove.addEnvironmentOverride("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings.getAbsolutePath());
    
    try (               
            WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
                .bindContract(VirtualEnvironmentContract.class, () -> ove)               
                .build();
        ) {
        
        TransitiveDependencyResolver transitiveDependencyResolver = resolverContext.contract().transitiveDependencyResolver();
        
        CompiledTerminal cdi = CompiledTerminal.from ( CompiledDependencyIdentification.parse( terminal));
        AnalysisArtifactResolution artifactResolution = transitiveDependencyResolver.resolve( resolutionContext, cdi);
        ...
    }
    catch( Exception e) {
        ...
    }

```

In this example, the [MavenConfigurationWireModule](../configuration/mavenconfiguration.md) is used. This may require a virtual environment (overloading variables etc), which is injected. You can of course do without this module, but then you need to inject the configuration differently.

In the following example, the more modern approach without Maven is used:

``` java
    ... 
    RepositoryConfigurationContract myConfig = ...;
    try (               
            WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE)
                .bindContract(RepositoryConfigurationContract.class, () -> myConfig);
                .build();
        ) {
        
        TransitiveDependencyResolver transitiveDependencyResolver = resolverContext.contract().transitiveDependencyResolver();
        
        CompiledTerminal cdi = CompiledTerminal.from ( CompiledDependencyIdentification.parse( terminal));
        AnalysisArtifactResolution artifactResolution = transitiveDependencyResolver.resolve( resolutionContext, cdi);
        ...
    }
    catch( Exception e) {
        ...
    }

```

As you can see, an instance of the [RepositoryConfigurationContract](javadoc:com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract) is injected. It's a very simple contract, as it contains only one function: returning the [RepositoryConfiguration](javadoc:com.braintribe.devrock.model.repository.RepositoryConfiguration). So in the simplest case, you can do this:

``` java

        RepositoryConfigurationContract myConfig = new RepositoryConfigurationContract() {
            
            @Override
            public RepositoryConfiguration repositoryConfiguration() {
                RepositoryConfiguration bean = RepositoryConfiguration.T.create();
                cfg.setLocalRepositoryPath( "c:/users/pit/.m2");
                
                MavenHttpRepository repository = MavenHttpRepository.T.create();
                repository.setUrl( "http://localhost:8080/archive");
                repository.setName("archive");
                
                bean.getRepositories().add( repository);
                
                return bean;                
            }
        };
```

This illustrates the basic configuration and setup of TransientDependencyResolver. It can be reused, i.e. resolve called several times with different rules. In order to specify these rules, you need to create a [TransitiveResolutionContext](javadoc:com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext). You'll find more detailed description in the [how to section](asset://com.braintribe.devrock:mc-ng-tutorials/howToRunRepositoryExtractions.md)



## top tier 
The top tier is where the apex resolvers roam. One of these beasts is Jinni's platform-asset-resolver, another is the classpath resolver.

This being said, the TransitiveResolver of the intermediate tier can also act as an apex resolver - it's absolutely sufficient for a [repository extract](asset://com.braintribe.devrock:mc-ng-tutorials/howToRunRepositoryExtractions.md). 


One top tier resolver is the tribefire.cortex.asset.resolving.ng.impl.PlatformAssetResolver, but this is part of the tribefire.cortex group and in its construction (even based on mc-core) uses a different pattern. 

The top tier resolver that I would want to sketch its use is the classpath resolver. 

``` java
        try (               
                WireContext<ClasspathResolverContract> resolverContext = Wire.contextBuilder( ClasspathResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
                    .bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))               
                    .build();
            ) {
            
            ClasspathDependencyResolver classpathResolver = resolverContext.contract().classpathResolver();
            
            CompiledTerminal cdi = CompiledTerminal.from ( CompiledDependencyIdentification.parse( terminal));
            AnalysisArtifactResolution artifactResolution = classpathResolver.resolve( resolutionContext, cdi);
            return artifactResolution;                                                  
        }
        
```

This is only a sketch - more information is [here](asset://com.braintribe.devrock:mc-ng-tutorials/howToResolveClasspaths.md).
