# how to run an repository extraction

As mentioned, the repository extract needs the [intermediate top tier](asset://com.braintribe.devrock:mc-core-documentation/using/using.md) to determine all artifacts (with parts) that are required to build a certain artifact (a terminal).

Again, you'll need some artifacts to run it

```
com.braintribe.devrock:mc-core
com.braintribe.devrock:mc-core-wirings
com.braintribe.devrock:analyis-artifact-model
```

## configuring the extraction
What you need to do is to parameterize the TransitiveDependencyResolver to behave in such a way that you get all the relevant files. This is done via the [TransitiveResolutionContext](javadoc:com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext).

 There's a builder API for it:
 
```
 TransitiveResolutionContext trc = new BasicTransitiveResolutionContext()
                                    .includeParentDependencies(true)
                                    .includeImportDependencies(true)
                                    .includeRelocationDependencies(true)
                                .done;

```

You can configure the following :

- an artifact filter : 
a predicate that gets an artifact (a solution to a dependency) presented and can decide whether it should be traversed any further. Needs to return *true* to proceed with the artifact. Default is pass-thru. 


- an artifact path filter :  
a predicate that gets the artifact embedded within its path (i.e. contains all the alternating artifact/dependency pairings required to reach this artifact) and can decided whether the dependency should be processed. Needs to return *true* to proceed with the dependency. Default is pass-thru.


- a dependency filter : 
a predicate that gets a dependency presented and can decide whether the dependency should be processed. Needs to return *true* to proceed with the dependency. Default is pass-thru.


- a dependency path filter : 
a predicate that gets the dependency embedded within its path (i.e. contains all the alternating artifact/dependency pairings required to reach this dependency) and can decided whether the dependency should be processed. Needs to return *true* to proceed with the dependency. Default is pass-thru.


- include relocations : 
if true, a relocating artifact will be part of the final result. Default is *false*. The relocation source (i.e. the actual dependency that was referenced) will appear as a dependency of the requesting artifact, yet with the scope 'relocation', and the relocation target (i.e. the dependency that was redirected to) will replace the relocation source with the scope this had.


- include parents : 
if true, the parents encountered will be part of the final result.Default is *false*. Parent will appear as a dependency of the referencing artifact, with the scope set to to 'parent'.


- include imports : 
if true, imported parents (or actually pom-packaged artifacts that only contribute to the dependency management section of a parent) will be part of the final result. Default is *false*. Imports will appear as dependencies of the requesting parent with the scope set to 'import'.


- include standard dependencies : if true all 'standard' dependencies (i.e. plain vanilla, no parent, import or relocation) will be put into the result. Default is *true*.


- respect exclusions : if true, exclusions attached to dependencies are respected, otherwise ignored. Default is *true*.

- global exclusions:
a Set of [ArtifactIdentification](javadoc:com.braintribe.model.artifact.essential.ArtifactIdentification) that will be automatically added as exclusions for the dependency tree.


- lenient : if true, a unresolved dependency is not treated as fatal. Otherwise you'll get wacked with an exception. Default is *false*


As a rule of thumb, relocations, parents and imports should be included for a proper repository extraction support - think of it as a collection of all things that another traversion would need. 

### how are the relocations, parents and imports represented in the result?

If you do include the relocations, parents and imports in the resolution, they will appear in the solution list of the resolution, and they will appear in the dependencies of the relevant artifacts. 

- parent : parent reference is simply added to the dependencies of the referencing child with scope 'parent'
    
- import : import reference is simply added to the dependencies of the referencing parent with scope 'import'
    
- relocation : relocation source is added to the dependencies of the referencing parent with scope 'relocation', relocation target is added with the actual scope (as the relocation source was declared)



## running the extraction 

But first, you'll need to think about what the entry point is for your resolution, as there are [different possibilities](./theThingWithTheTerminals.md).


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

After the resolution, you should check whether there were any issues encountered during the extract:

``` java
    if (artifactResolution.hasFailed() { 
        Reason reason = artifactResolution.getFailure();
        ... 
    }    
```

All that remains to do is to traverse the resolution  and start copying/packing/transferring the data 

``` java
    for (AnalysisArtifact analysisArtifact : artifactResolution.getSolutions()) {
        for (Part part : analysisArtifact.getParts().values()) {
            Resource resource = part.getResource();
            ...
        
        }
    }
```

Please note the only parts you'll get here are the poms as only these are required during the traversing of the dependency tree. If you need more - and I guess you would, you'll need to use the [PartDownloadManager](javadoc:com.braintribe.devrock.mc.api.download.PartDownloadManager) as explained in the section about [downloading parts](./howToDownload.md).

You just have to traverse the resolution and for each artifact, try to download the parts you want.. of course, if you want make sure that the parts you need in your use-case, you'll need to check whether you were able to download it... in this example, a standard jar needs to be present, otherwise it would fail. 


Also note that you'll not know whether the file was actually downloaded or whether it was already in the local repository's cache. 

``` java
    PartDownloadManager downloadManager = ...;
    PartDownloadScope myScope = downloadManager.openDownloadScope();
    Map<CompiledPartIdentification, Promise<Maybe<ArtifactDataResolution>>> promises = new HashMap<>();
    
    for (AnalysisArtifact analysisArtifact : artifactResolution.getSolutions()) {

        CompiledArtifactIdentification cai = analysisArtifact.getOrigin();        


        // jar
        CompiledPartIdentification jcpi = CompiledPartIdentification.from( cai, PartIdentifications.jar);
        promises.put( jCpi, myScope.download( cai, PartIdentifications.jar));

        // sources:jar
        PartIdentification sPi = PartIdentification.parse( "sources:jar");
        
        promises.put( sCpi, myScope.download( cai, sPi));

        // javadoc jar
        PartIdentification jPi = PartIdentification.parse( "javadoc:jar");
        CompiledPartIdentification jCpi = CompiledPartIdentification.from( cai, jPi);
        promises.put( jCpi, myScope.download( cai, jPi));

        // asset:man 
        PartIdentification aPi = PartIdentification.parse( "asset:man");
        CompiledPartIdentification aCpi = CompiledPartIdentification.from( cai, aPi);
        promises.put( aCpi, myScope.download( cai, aPi));                                
    }
    // process promises 
    for (Map.Entry<CompiledPartIdentification, Promise<Maybe<ArtifactDataResolution>>> entry : promises) {
        try {
            
            ArtifactDataResolution adr = entry.getValue().get();
            if (adr.isBacked()) {
                ...     
            }
            else {
                // check whether the part was really needed ... 
                CompiledPartIdentification cpi = entry.getKey();
                if (cpi.getType().equals( "jar") && (cpi.getClassifier() == null) {
                    throw new IllegalStateException( "jar part is required, but not present for [" + cpi.asString() + "]");
                }
            }
        }
        catch (Throwable t) {
            ...
        }
        
    }
```

## relocations

An artifact can redirect to another artifact, so that any dependency to this artifact doesn't deliver the artifact itself but rather the artifact it redirects to. 
As you can see above, you can tell whether the transitive dependency resolver should include the source of the redirection in the solution list. In any case, whether you want it in the solution list or not, it will be still accessible using the terminal structure. This is how it's done:

- the owner, i.e. the artifact that has the 'redirection source' amongst its dependencies will retain the dependency to the 'redirection source'.
- the 'redirection source' gets a new dependency, scoped 'relocation', to the 'redirection target', and its solution is the target of the redirection.

In that way, a consumer of the transitive dependency resolver doesn't see the redirection in the list of solutions (unless it wants to) and therefore isn't forced to deal with it (unless it wants to). Still, an analysis is possible, as the information who redirected to whom is still accessible within the terminal-structure.



