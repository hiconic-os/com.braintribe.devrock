# how to read a pom

This text describes what you need to do to be able to read a pom file, both on the low-level as well as on a higher level, i.e. reading the pom as is into a modeled representation or reading and compiling the pom into a expressive representation.

Basically, depending on your starting point, you can use the feature to let it resolve the artifact and then compile it into a fully qualified pom representation or provide the data by yourself (via a File, an InputStream or the declared artifact itself).

While we do support most of what Maven allows you to do with a POM, there are some [limitations](asset://com.braintribe.devrock:mc-core-documentation/configuration/mavenconfiguration.md#-limitations).

There are three basic distinctions when it comes to how they are read.


## read into a modelled instance
you just want to read the pom into an assembly. No translation of the data is done, it's simply restructed from the XML file to a modeled view. 

This is an easy task. The tool you need is the [DeclaredArtifactMarshaller](javadoc:com.braintribe.artifact.declared.marshaller.DeclaredArtifactMarshaller). It's a standard marshaller, so it supports all standard ways how to use it. 

Note that the marshaller cannot unmarshall, i.e. cannot write a pom. Until now, it's not needed and therefore missing. 


### how to read a pom into a low-level modeled assembly 

Low level in this context simply means that you'll get a modeled representation of the pom AS IT IS DECLARED in the file. You could read the org.w3c.dom.Document, but our model is better supported with our tooling :-)

If you want to read a pom into an [DeclaredArtifact](javadoc:com.braintribe.model.artifact.declared.DeclaredArtifact), you simply use the respective [DeclaredArtifactMarshaller](javadoc:com.braintribe.artifact.declared.marshaller.DeclaredArtifactMarshaller).

``` java
    DeclaredArtifactMarshaller marshaller = new DeclaredArtifactMarshaller();
    File file = new File( myDirectory, "pom.xml");

    DeclaredArtifact declaredArtifact = null;
    try ( InputStream in = new FileInputStream(file)) {
        declaredArtifact = (DeclaredArtifact) marshaller.unmarshall( in);
    } catch (Exception e) {
        log.erro( "cannot read [" + file.getAbsolutePath() + ", e);
    }       

```

Dependencies you need are 

```
com.braintribe.devrock:declared-artifact-marshaller
com.braintribe.devrock:declared-artifact-model
```

You can feed any pom file into the marshaller (as long as its valid on the XML level).

## Identification only 
you only want to know what artifact it is. In this mode, no resolving takes place and only the identity (groupId, artifactId, version) is extracted. This however can only work when your pom has all the pertinent information and no resolving is required (sic). If the groupId is missing, it's taken from the parent reference. If the version is missing, it is also taken from there. Note that this means that if you use a range inside the parent reference, you must declare the version in your pom. 

To get the identification of the artifact, that is groupId, artifactId and version, you can use the [DeclaredArtifactIdentificationExtractor](javadoc:com.braintribe.devrock.mc.core.declared.DeclaredArtifactIdentificationExtractor). 

The extractor has several methods that you can use 

``` java
	File pomFile = ..;
	Maybe<CompiledArtifactIdentification> maybe = DeclaredArtifactIdentificationExtractor.extractIdentification(pomFile);
	if (!maybe.isSatisfied()) {
		// process error		
	}
	else {
		CompiledArtifactIdentification cai = maybe.get();
	}
```
You can also pass an InputStream, a Resource, a DeclaredArtifact, or pass an [InputStreamProvider](javadoc:com.braintribe.model.generic.session.InputStreamProvider)

Dependencies you need are (simplest case)

```
com.braintribe.devrock:mc-core
com.braintribe.devrock:compiled-artifact-model
```



## Full read 
you want to get all contained data, all variables resolved and all dependencies properly instrumented. This mode requires that eventual parents and their imports can be resolved, so you need a full wired configuration.

### how to read a pom into a high-level modeled assembly

High level means that you want to have a pom as it effectively is used by the tooling.

As a pom may (and in our case most often has) have a reference to a parent-pom, resolving (i.e. preparing it for use by ensuring all variables, and following up references to parents and imports) cannot be done on such a simple basis as in the former case.

That means that such a pom can only be read if the back-end is in place (managing local and remote repositories to resolve references across poms). Therefore, you need to do some wiring to get it.

*The following example is how to read a pom taken from the local or remote repository. It's NOT about how to read a pom from a source artifact (which would be how to read a pom.xml). This can only be achieved by working with a code-base repository* 

The following example is using a specific settings.xml, and configures the MavenConfigurationModule to use this. If you want to use the standard means to find a pertinent settings.xml, simply omit the instantiation and injection of the VirtualEnvironment. 

``` java
        OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
        ves.addEnvironmentOverride("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", mySettings.getAbsolutePath());

        try (

                WireContext<ArtifactDataResolverContract> resolverContext = Wire.contextBuilder( ArtifactDataResolverModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
                    .bindContract(VirtualEnvironmentContract.class, () -> ove)              
                    .build();
        ) {

            ArtifactDataResolverContract artifactDataResolverContract = resolverContext.contract();

            CompiledArtifactResolver compiledArtifactResolver = artifactDataResolverContract.compiledArtifactResolver();
        
            CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse(myArtifactAsString);
        
            Maybe<CompiledArtifact> resolvedCai = compiledArtifactResolver.resolve( cai);        
            if (resolvedCai.isSatisfied()) {
                CompiledArtifact ca = resolvedCai.get();
                ...
            }
        }

```

The [CompiledArtifact](javadoc:com.braintribe.model.artifact.compiled.CompiledArtifact) you retrieved reflects a the fully resolved (if possible in the first place) of the artifact. 

In order to get this feature, you need to get the following dependencies

```
com.braintribe.devrock:mc-core-wirings
com.braintribe.devrock:compiled-artifact-model
```


#### reading a local pom.xml

As explained above, one must distinguish between simply transposing a pom file into a modeled representation that simply reflects the file's content or actually turning it into a full-fledged interpretation of the data contained. In mc-ng's world, the former would be the 'declared artifact', the latter the 'compiled artifact'.

The obvious reason why there is this distinction is that a pom-file may not stand alone, that it, in itself, doesn't contain all required information to 'instrument' the artifact. For instance, some data may reside in parent-poms or in dependency-management imports (actually, a pom must only contain the id of the artifact, all other stuff can be retrieved from parent references). And of course, these additional files need to be found. For this simple reason, 'compiling' a pom requires the full back-end. 


##### using the DeclaredArtifactCompiler

The [DeclaredArtifactCompiler](javadoc:com.braintribe.devrock.mc.api.resolver.DeclaredArtifactCompiler) has the functionality it takes the compile poms directly. Of course, you can always use the feature to resolve and compile the pom directly, but in case you already know the pom file, you can use the compiler directly. 

It has three means to compile a pom: 

- from an DeclaredArtifact obtained by the DeclaredArtifactMarshaller.

- from an InputStream of the pom file
    
- from the pom file.


All three methods will return a CompiledArtifact in any case - save a catastrophic failure. Problems in the pom - incomplete instrumentation of identification and dependencies - are reported in the returned CompiledArtifact.

You can check whether the CompiledArtifact is valid by checking it via its validity flag, and you can also inspect the [Reason](asset://com.braintribe.devrock:mc-ng-principles/principles/reasons.md).

##### codebase repository

So as we do need a lookup structure, we decided to normalize the different repository-types, so we introduced the codebase repository to handle the sources.
In order to allow you the compile an artifact from a local pom.xml, you need to modify the [RepositoryConfiguration](javadoc:com.braintribe.devrock.model.repository.RepositoryConfiguration) to include a [CodebaseRepository](javadoc:com.braintribe.devrock.model.repository.CodebaseRepository). A description of how it has to be instrumented, can be found [here](asset://com.braintribe.devrock:mc-core-documentation/configuration/configuration.md){:name=codebaserepositorymodule}


##### configuring the resolver
As mentioned [here](asset://com.braintribe.devrock:mc-core-documentation/configuration/configuration.md), there are multiple ways to introduce this codebase repository, either by injecting is as an YAML add-on via the settings.xml or simply be adding it to the basic configuration.

The example below uses a direct configuration :

```
    RepositoryConfiguration configuration = RepositoryConfiguration.T.create();
    configuration.setLocalRepositoryPath( "f:/.m2/repository");
    
    
    // add any remote repository you want
    Repository thirdPartyRepository = Repository.T.create();
    thirdPartyRepository.setName("third-party");
    thirdPartyRepository.setRootPath("https://artifactory.example.com/artifactory/third-party");
    thirdPartyRepository.setUser("myself");
    thirdPartyRepository.setPassword("mypwd");
    configuration.getRepositories().add( thirdPartyRepository);
    
    Repository coreDevRepository = Repository.T.create();
    coreDevRepository.setName("core-dev");
    coreDevRepository.setRootPath("https://artifactory.example.com/artifactory/core-dev");
    coreDevRepository.setUser("myself");
    coreDevRepository.setPassword("mypwd");
    configuration.getRepositories().add( coreDevRepository);


    File myPom = ...;
    

    try (

            WireContext<ArtifactDataResolverContract> resolverContext = Wire.contextBuilder( 
                                                                            ArtifactDataResolverModule.INSTANCE,
                                                                            new CodebaseRepositoryModule( 
                                                                                Pair.of( "f:/works/COREDR-10/com.braintribe.devrock", "${artifactId}"),
                                                                                Pair.of( "f:/works/COREDR-10/com.braintribe.gm", "${artifactId}")
                                                                            )
                                                                        )

                                                                        .bindContract(RepositoryConfigurationContract.class, () -> configuration)     
                                                                        .build();
        ) {

            ArtifactDataResolverContract artifactDataResolverContract = resolverContext.contract();

            DeclaredArtifactCompiler compiler = artifactDataResolverContract.declaredArtifactCompiler();
        
            CompiledArtifact compiled = compiler.compile( myPom);
            if (compiled.getInvalid()) {
                Reason mainReason = compiled.getWhyInvalid();
                throw new IllegalStateException("can't read [" + myPom.getAbsolutePath() + "]" as " + mainReason.asFormattedString());                
            }
                        
        }
 ```
 
As you can see, you need not only to declare the codebase repositories you want to use, but also all repositories that contain artifacts your pom is referencing. So in our case, the two common repositories 'third-party' and 'core-dev' are used. 
 
And one thing that is changed from old malaclypse: You can have more codebase repositories than one. 
 

That means the in the example above, the two codebase repositories take precedence over the local repository which in turn takes precedence over the two remote repositories.Note that in mc's view, the local repository in itself is just another repository (granted with some special logic behind it).


