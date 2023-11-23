# configuring mc-core 

The mc-core has its very own configuration model which provides all information mc-core needs to set itself up. 

```
com.braintribe.devrock:repository-configuration-model
```

The [RepositoryConfiguration](javadoc:com.braintribe.devrock.model.repository.RepositoryConfiguration) contains all information the mc-core requires (actually it also contains the [filter](./filtering.md) entities). If you have an inkling of what Maven does with its settings.xml, you're halfway there to understand the model. 

*Please note that this configures repositories, so it builds the base for the different transitive resolvers, but doesn't fully configure them - they have their own means of configuration.*

Still, this configuration data must be present in any wiring, i.e. it must be injected.

For that, a module (and a contract) exist. 

## per dev environment
The [dev-environment](./devenvironment.md) is a way to inject a configuration into mc-core. Our toolings - the ant integration and the plugins - are wired accordingly. They do support our standard way of configuring mc-core and - as a fallback - the Maven style. 

The basic idea - more can be found in the proper documentation - is that the configuration should be location-specific, i.e. depending where your current working root is, you'll get a different configuration. This ties in into the working pattern of getting a subset of the sum of the available sources to work on a topic. Other topics (or groups of topics) have different sources, checked-out to different local directories. All these environment *can* be disjunct, so you can work on a topic without interfering with others.

The module itself is easy to use if you just want it make depend on the enviroment and the current working directory :

``` java
    OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();

    EnvironmentSensitiveConfigurationWireModule  escwm  = new EnvironmentSensitiveConfigurationWireModule( ove);
    try (
        WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( escwm).build();        
        TransitiveDependencyResolver transitiveDependencyResolver = resolverContext.contract().transitiveDependencyResolver();
        ...
    }
    catch( Exception e) {
        ...
    }
```

If you want it to use a specific directory, you need to add a line to the module building :

``` java
    OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
    File devEnvRootFolder = ..... 
    EnvironmentSensitiveConfigurationWireModule  escwm  = new EnvironmentSensitiveConfigurationWireModule( ove);

    try (
        WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( escwm)
        .bindContract( DevelopmentEnvironmentContract.class, () -> devEnvRootFolder);
        .build();        
        TransitiveDependencyResolver transitiveDependencyResolver = resolverContext.contract().transitiveDependencyResolver();
        ...
    }
    catch( Exception e) {
        ...
    }
```


## per maven

If you want to use maven (settings.xml and such), you would want to use the [MavenConfigurationModule](./mavenconfiguration.md).

It doesn't require much more than that ..

``` java
    OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
    ove.addEnvironmentOverride("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings.getAbsolutePath());
    
    try (               
            WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
                .bindContract(VirtualEnvironmentContract.class, () -> ove)               
                .build();
        ) {
        
        TransitiveDependencyResolver transitiveDependencyResolver = resolverContext.contract().transitiveDependencyResolver();
        ...
    }
    catch( Exception e) {
        ...
    }
```

a detailed description of the MavenConfigurationModule (or rather what it does) can be found [here](./mavenconfiguration.md). Suffices here to say that you only have to bind the [VirtualEnvironmentContract](javadoc:com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract) if you want to inject some environment variables that aren't set by the OS. In this example, it makes sure that mc-core ONLY uses a specially selected 'settings.xml' and doesn't get it from Maven's standard locations. 


## directly

If you however want you use the configuration directly without generating it from the maven data, you can directly use an instance of the RepositoryConfiguration.

How you get to it is your choice, you could generate in code, load it from a persisted file using one of our marshallers (YAML for instance, but anyone would do), and then just inject it into the wiring. 


``` java
   RepositoryConfigurationContract myRepositoryContract = new RepositoryConfigurationContract() {        
        @Override
        public RepositoryConfiguration repositoryConfiguration() {
            return ...;                
        }
    };
    ... 

    try (               
            WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE)
                .bindContract(RepositoryConfigurationContract.class, myRepositoryContract);
                .build();
        ) {
        
        TransitiveDependencyResolver transitiveDependencyResolver = resolverContext.contract().transitiveDependencyResolver();
        ...
    }
    catch( Exception e) {
        ...
    }

```

or even simpler using a lambda :

``` java
    RepositoryConfiguration myRepositoryConfiguration = RepositoryConfiguration.T.create();
    ....

    try (               
            WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE)
                .bindContract(RepositoryConfigurationContract.class, () -> myRepositoryConfiguration);
                .build();
        ) {
        
        TransitiveDependencyResolver transitiveDependencyResolver = resolverContext.contract().transitiveDependencyResolver();
        ...
    }
    catch( Exception e) {
        ...
    }

```

## origination 
If you are using the modules (i.e. not configuring your configuration manually), the resulting configuration has [origination data](./origination.md) attached that gives you insight of how the configuration was processed within the modules.

### a note on using the marshallers

If you do want to load the configuration with our marshallers (for instance the YamlMarshaller) from the disk, please note that you need to configure the marshaller accordingly:
```java
    GmDeserializationOptions options = GmDeserializationOptions.defaultOptions.derive()
            .setInferredRootType(com.braintribe.devrock.model.repository.RepositoryConfiguration.T)
            .set( EntityFactory.class, EntityType::create)
            .build();
```
Default mode is the 'transparent' mode that doesn't interpret the data, but just un-marshalls them as it finds them. In that case, no default values are set etc. In this case however, we want the marshaller to assign default values to missing properties. 

## a look at the model

The configuration model (unfortunately currently a mongrel consisting of the actual configuration entities and the denotation types of the [filters](./filtering.md)) is easily explained:

The single root is the [RepositoryConfiguration](javadoc:com.braintribe.devrock.model.repository.RepositoryConfiguration). Besides containing the path the to the filesystem of local repository (aka cache) and a global 'offline' switch, it simply lists the repositories that are to be taken into account.

The repository configuration also lists the repositories that are to be used as lookup structures during the resolution. Some properties can be declared in the configuration, some have to be declared, and some are - while present in the entity - are enriched in an active process during the activation of the configuration, see [Probing](./probing.md).


### base repository
The [Repository](javadoc:com.braintribe.devrock.model.repository.Repository) is the base for all other derivations. It is abstract, i.e. cannot be instantiated. 

### filters
Any repositories can have [filters](./filtering.md) attached. Of the two filter types, the dominance filter is of importance here as it influences the sequence of the repositories, that is whether the data returned of a repository takes precedence and others are ignored. 

### cacheable
The cacheable flag is used to declare that the remote repository's data should be cached in the local repository (i.e. shadowed there, with files and metadatadata and indices). If set to false, all this data is not cached, so any resolving that takes place will act on the remote files. For instance if you are using a filesystem-based repository on your machine, it wouldn't make sense to have the files cached in the local repository. Note that it even works with http based repositories : the parts of a resolved artifact however will - if their stream's opened - will read directly from the remote repository. 


### local 
The [LocalRepository](javadoc:com.braintribe.devrock.model.repository.LocalRepository) is not to be configured. It is only used internally and injected automatically when the configuration is activated. So do not use it in a configuration.  
The local repository can gain a dominance via the [pc_bias system](./filtering.md).

### http
The standard repository entity for remote 'maven compatible repositories' is the [MavenHttpRepository](javadoc:com.braintribe.devrock.model.repository.MavenHttpRepository). It is cached, and may even be offline.

### file
The repository entity for 'file based' repositories is the [MavenFileSystemRepository](javadoc:com.braintribe.devrock.model.repository.MavenFileSystemRepository). It is cached, and may even be offline.


### codebase
The [codebase repository](javadoc:com.braintribe.devrock.model.repository.CodebaseRepository) is used to declare a repository that is backed by source files. In this repository - obviously - only poms can be retrieved and it is never cached. 

A codebase repository has one important additional parameter: the 'template'.

The template has 3 distinct keywords 

- ${groupId}            : the groupId as it stands
- ${groupId.expanded}   : the groupId with its 'dots' transposed to directory delimiters
- ${artifactId}         : the artifactId as it stands
- ${version}            : the version as it stands

The combination (or existence) of these 3 keywords specifies how the source directory is structured.

A structure like a 'maven compatible' repository (local or remote) would use this template expression (Note the use of the expanded version here):

    ${groupId.expanded}/${artifactId}/${version}

A structure for our 'cross group builds' would be 

     ${groupId}/${artifactId}
     
and finally, the expression for a 'single group build' is obviously 

    ${artifactId}

An example of how such codebase repositories are used can be found in the section about [reading pom files](asset://com.braintribe.devrock:mc-ng-tutorials/howToReadPoms.md).

A codebase repository should always have a global dominance, i.e. any result it can return is considered to be dominant, and no other repository is consulted. Only if the codebase repository cannot return any data, the next repository in turn is asked. If you are using the CodebaseRepositoryModule as explained below, you don't have to worry about the dominance (and cache).


### workspace
The [workspace repository](javadoc:com.braintribe.devrock.model.repository.WorkspaceRepository) is similar to the codebase repository.It is used to declare a repository that is backed projects in Eclipse's workspace. Therefore, it is only active when mc-core is used within Eclipse (by the plugins for instance). In this repository - obviously - only poms can be retrieved and it is never cached. 
Please note that you should never use the name 'workspace' for any of the repositories in your configuration as it is exclusively reserved for usage by the plugins in Eclipse.

## Ordering repositories in the configuration

It is quite obvious that codebase repositories are to take precedence. That means that if they do have matching content, no other repository is asked. This is of course due to the fact that a codebase repository more often than not contains something that is already present in other repositories. The same applies to the local repository with its 'locally installed' artifacts. 

While mc's configuration compiler ensures that there is an intrinsic order of repositories depending on their type, the order WITHIN the same types must be organized by you the user. 

The intrinsic order is like follows:

    codebase repositories -> local repository ->  remote repositories
 
So ordering within the category respected by the 'intrinsic order' depends on the sequence you add them to the RepositoryConfiguration's 'repositories' list: 



So consider this setup : 
``` java
    RepositoryConfiguration configuration = RepositoryConfiguration.T.create();
    configuration.setLocalRepositoryPath( "f:/.m2/repository");
    
    
    // one codebase repository for the com.braintribe.devrock group
    CodebaseRepository devrockCodebaseRepository = CodebaseRepository.T.create();
    devrockCodebaseRepository.setName("codebase-devrock");
    devrockCodebaseRepository.setRootPath("f:/works/COREDR-10/com.braintribe.devrock");
    devrockCodebaseRepository.setTemplate("${artifactId}");    
    devrockCodebaseRepository.setDominanceFilter( () -> true);
    devrockCodebaseRepository.setCachable( false);
    configuration.getRepositories().add( devrockCodebaseRepository);
    
    // another codebase repository, this one for the com.braintribe.gm group
    CodebaseRepository gmCodebaseRepository = CodebaseRepository.T.create();
    gmCodebaseRepository.setName("codebase-gm");
    gmCodebaseRepository.setRootPath("f:/works/COREDR-10/com.braintribe.gm");
    gmCodebaseRepository.setTemplate("${artifactId}");    
    gmCodebaseRepository.setDominanceFilter( () -> true);
    gmCodebaseRepository.setCachable( false);
    configuration.getRepositories().add( gmCodebaseRepository);
    
    LocalRepository localRepository = LocalRepository.T.create();
    localRepository.setRootPath( "f:/.m2/repository");
    configuration.getRepositories().add( localRepository);
    
    
    // add any remote repository you want
    
    MavenHttpRepository coreDevRepository = MavenHttpRepository.T.create();
    coreDevRepository.setName("core-dev");
    coreDevRepository.setRootPath("https://artifactory.example.com/artifactory/core-dev");
    coreDevRepository.setUser("myself");
    coreDevRepository.setPassword("mypwd");
    configuration.getRepositories().add( coreDevRepository);

    MavenHttpRepository thirdPartyRepository = MavenHttpRepository.T.create();
    thirdPartyRepository.setName("third-party");
    thirdPartyRepository.setRootPath("https://artifactory.example.com/artifactory/third-party");
    thirdPartyRepository.setUser("myself");
    thirdPartyRepository.setPassword("mypwd");
    configuration.getRepositories().add( thirdPartyRepository);
```

Note the declaration of the dominance-filter and the switch-off of the cache feature for both codebase repositories. Furthermore, using this approach, you *have* to declare the actual LocalRepository here (to define its place between codebase- and 'real'-repositories. 

As you can see, there are two codebase repositories and two remote repositories in place.

    codebase-devrock -> codebase-gm -> local repository -> core-dev -> third-party

Both codebase repositories are automatically dominant for any result they have, i.e. if they can serve version data ( i.e. a ranged version request has matches inside the codebase repository), all subsequent repositories are not asked for their version data. See the part about [filtering](./filtering.md) for more information.

## CodebaseRepositoryModule 

Now that you have the configuration, you could use as this here :
``` java
    resolverContext = Wire.contextBuilder( ArtifactDataResolverModule.INSTANCE)
                .bindContract(RepositoryConfigurationContract.class, () -> configuration)               
                .build();       
```
Ok, this is rather cumbersome - note that you have to a) set the dominance-filter and b) switch-off caching for every codebase repository, and c) inject the local repository at the right place. 


The easier way is to build up your configuration with the module that has been created for your convenience: the [CodebaseRepositoryModule](javadoc:com.braintribe.devrock.mc.core.wirings.codebase.CodebaseRepositoryModule)

So what you do is to declare the repository configuration like this : 

``` java

    RepositoryConfiguration configuration = RepositoryConfiguration.T.create();
    configuration.setLocalRepositoryPath( "f:/.m2/repository");
    
    MavenHttpRepository coreDevRepository = MavenHttpRepository.T.create();
    coreDevRepository.setName("core-dev");
    coreDevRepository.setRootPath("https://artifactory.example.com/artifactory/core-dev");
    coreDevRepository.setUser("myself");
    coreDevRepository.setPassword("mypwd");
    configuration.getRepositories().add( coreDevRepository);
    
    MavenHttpRepository thirdPartyRepository = MavenHttpRepository.T.create();
    thirdPartyRepository.setName("third-party");
    thirdPartyRepository.setRootPath("https://artifactory.example.com/artifactory/third-party");
    thirdPartyRepository.setUser("myself");
    thirdPartyRepository.setPassword("mypwd");
    configuration.getRepositories().add( thirdPartyRepository);    
```
 
You do not have to declare the local repository (the instance LocalRepository as in the example above) as it's going to be injected at the right place in the sequence. 
``` java
    resolverContext = Wire.contextBuilder( 
                        ArtifactDataResolverModule.INSTANCE, 
                        new CodebaseRepositoryModule( 
                            Pair.of( "f:/works/COREDR-10/com.braintribe.devrock", "${artifactId}"),
                            Pair.of( "f:/works/COREDR-10/com.braintribe.gm", "${artifactId}")
                        )
                        .bindContract(RepositoryConfigurationContract.class, () -> configuration)               
                        .build();       
```                
Here, the [CodebaseRepositoryModule](javadoc:com.braintribe.devrock.mc.core.wirings.codebase.CodebaseRepositoryModule) will automatically parameterize the two codebase repositories and also ensure that the LocalRepository is at the correct place (just behind the two codebase repositories).    
                    
The [CodebaseRepositoryModule](javadoc:com.braintribe.devrock.mc.core.wirings.codebase.CodebaseRepositoryModule) has several exposed parameterizations via its constructor:

``` java
    public CodebaseRepositoryModule(File rootPath, String template);
    
    public CodebaseRepositoryModule(Pair<File,String> ... pairs);
```
If constructed in these fashions, the codebase repository (or -ies that is) will be automatically instrumented in the correct way.

If you do want to directly pass your codebase repositories, you can do so, but then you have - as in the example above - parameterize the codebase repositories manually, i.e. specify the dominance-filter and the 'cachable' switch-off on your own. The local repository however will be injected at the right place (unless you specified it explicitly in your configuration)
``` java
    public CodebaseRepositoryModule(List<CodebaseRepository> codebaseRepositories);
```


## controlling offline status

There are several ways to control whether mc-ng should connect to remote repositories. Basically, it all boils down to a repository being declared offline. 

Using the environment variable 

    if the environment variable MC_CONNECTIVITY_MODE is set to 'offline', all repositories are declared to be offline.
    
Using the respective tag in settings.xml (if MavenConfigurationWireModule is used)
    
    if the tag 'offline' of 'settings' is set to 'true', all repositories are declared to be offline.
    
Using the respective property of the RepositoryConfiguration( if used directly)

    if the property 'offline' of the RepositoryConfiguration entity is set to 'true', all repositories are declared to be offline.
    
Using the respective property of the Repository

    if the property 'offline' of the Repository entity is set to 'true', the owning repository is declared to be offline.
    
    
Finally, even if no switch is declared, mc-ng's active probing will check if the repositories declared can be accessed (during probing phase) and will automatically declare repositories it can't reach as being offline.


 ##  origination

The process that builds the actual configuration also supports the [origination scheme](./origination.md), that means that the repository configuration compiled by it has data attached how it was compiled.
   

## loading a repository configuration
Most of the time you won't need to go low-level and load a repository-configuration on your own as in most cases, you'll simply grab a matching wire-module from mc-core-wirings. However, in some cases, you'll want to load a specific file.

While the standard YAML marshaller can read a repository-configuration stored in such format, it will fail when it comes to the usage of environment variables and system properties. For that purpose, the [StandaloneRepositoryConfigurationLoader](javadoc:com.braintribe.devrock.mc.core.configuration.StandaloneRepositoryConfigurationLoader) has been made (as a matter of interest: this is the full, internally wired [RepositoryConfigurationLoader](javadoc:com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationLoader).)

The standalone version is quite simple to use:

``` java
        File externalConfigurationFile = ....;
		if (externalConfigurationFile.exists()) {
            OverridingEnvironment ove = new OverridingEnvironment( StandardEnvironment.INSTANCE);
            ove.setEnv( ....);

			StandaloneRepositoryConfigurationLoader srcl = new StandaloneRepositoryConfigurationLoader();
			srcl.setVirtualEnvironment( ove);
			srcl.setAbsentify(true);
			
            Maybe<RepositoryConfiguration> loadRepositoryConfigurationMaybe = srcl.loadRepositoryConfiguration(externalConfigurationFile);
			
            if (!loadRepositoryConfigurationMaybe.isSatisfied()) {
				throw new IllegalStateException("cannot load [" + externalConfigurationFilePath + "] as " + loadRepositoryConfigurationMaybe.whyUnsatisfied().asFormattedText());
			}
			else {
				externalRepositoryConfiguration = loadRepositoryConfigurationMaybe.get();
			}							
		}
		else {
			throw new IllegalStateException("cannot find external configuration file [" + externalConfigurationFilePath + "]");
		}		
```

In this example, the loader is configured using a virtual environment, and told to absentify any properties that are not declared in the file itself. 
