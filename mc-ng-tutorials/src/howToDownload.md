# how to download files

At a certain point in managing dependencies, you'll want to download files.

## single file download
If you only need a single file, you can use the PartResolver. You'll need to get the DataResolverContract from you current wirings, and then you can use its features to download the file.

``` java
    CompiledPartIdentification cpi = ...;
    
    Maybe<ArtifactDataResolution> resolutionOptional = artifactResolver.resolvePart( cpi, cpi);

    if (resolutionOptional.isSatisfied()) {
        ArtifactDataResolution adr = resolutionOptional.get();
    
        try (OutputStream out = new FileOutputStream(myFiletoWriteTo) {
            IoTools.transferBytes( adr.getResource().openStream(), out);
        }
        catch (Exception e) {
            ... 
        }        
    }
```

## multiple file download
If you have more than one file to download, for instance after a resolution you'll want to enrich the solution with the appropriate part files, then you can use a faster and - more importantly - fairer download. 

- faster : the download manager is multi-threaded, that means that it will download multiple files at the same time. 
- fairer : a slow download on one part will not block other downloads, as the download activity is shared among all requested downloads (and scopes)


### PartDownloadScope
The basic interface [PartDownloadManager](javadoc:com.braintribe.devrock.mc.api.download.PartDownloadManager) that the download manager implements has one single function

```
    PartDownloadScope openDownloadScope();
```
You can have multiple scopes active at the same time, and happily download from any of them. The idea is that the download manager makes sure that the downloads are distributed fairly across the currently active download-scopes (i.e. scopes that have downloads queued).

The [PartDownloadScope](javadoc:com.braintribe.devrock.mc.api.download.PartDownloadScope) returned is what use to actually trigger the downloads:

``` java
    Promise<Maybe<ArtifactDataResolution>> download(CompiledArtifactIdentification identification, PartIdentification partIdentification);
    
```

As you can see, the function actually returns a Promise<ArtifactDataResolution>, that means that your download request has been scheduled for processing. In order to retrieve it, you'll need to call the future. 

``` java
    PartDownloadManager downloadManager = ...;
    CompiledPartIdentification pi = ...;
    
    PartDownloadScope myScope = downloadManager.openDownloadScope();
    Promise<Maybe<ArtifactDataResolution>> myPromise = myScope.download(pi, pi);
    
    try {
        Maybe<ArtifactDataResolution> resolutionMaybe = myPromise.get();
        if (resolutionMaybe.isSatisified) {
	        Resource resource = resolution.getResource();
	        ...
        }
    }
    catch( Throwable t) {
        ...
    }    
```

This example is of course not efficient - it enqueues a download request and then accesses it immediately. 

The example below actually enqueues the download requests and only collects them later, so while enqueuing, some downloads already happen and you only have to wait for the few that haven't be served yet.  

``` java
    PartDownloadManager downloadManager = ...;
    List<CompiledPartIdentification> pis = ...;
    
    PartDownloadScope myScope = downloadManager.openDownloadScope();
    List<Promise<Maybe<ArtifactDataResolution>>> promises = new ArrayList<>( pis.size());
    // start downloading 
    for (CompiledPartIdentification pi : pis) {
        promises.add( myScope.download(pi, pi));
    }
    // collect results 
    for (Maybe<Optional<ArtifactDataResolution>> promise : promises) {
        try {
            ArtifactDataResolution resolution = myPromise.get();
            if (resolution.isBacked() {
                Resource resource = resolution.getResource();
                ...
            }
            else {
                // no download available as part doesn't exist, e.g. 404 on remote HTTP repository 
                ... 
            }
         }
        catch( Throwable t) {
            // some catastrophic failure during download 
            ...
        }   
    }
    ... 
    
```


### artifacts required 
You'll need the obvious dependencies to use the download manager 

```
    com.braintribe.devrock:mc-core
    com.braintribe.devrock:mc-core-wirings
```
    

### the implementation 
The [BasicPartDownloadManager](javadoc:com.braintribe.devrock.mc.core.download.BasicPartDownloadManager) implements the [PartDownloadManager](javadoc:com.braintribe.devrock.mc.api.download.PartDownloadManager) interface. 

As it requires the [ArtifactPartResolver](javadoc:com.braintribe.devrock.mc.api.resolver.ArtifactPartResolver), it is part of the middle tier, so you'll need to get [ArtifactDataResolverContract](javadoc:com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract) contract.

``` java
    RepositoryConfiguration configuration = ...;

    try (

                WireContext<ArtifactDataResolverContract> resolverContext = Wire.contextBuilder( ArtifactDataResolverModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
                    .bindContract(RepositoryConfigurationContract.class, () -> configuration)     
                    .build();
        ) {

            ArtifactDataResolverContract artifactDataResolverContract = resolverContext.contract();
            
            PartDownloadManager partDownloadManager = artifactDataResolverContract.partDownloadManager();
            ... 
        }
    
```

Now while the [classpath resolver](./howToResolveClasspaths.md) already has the feature on board, i.e. actually calls the download manager after all contributing artifacts have been determined, the other resolvers, notably the [TransitiveDependencyResolver](javadoc:com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver) as explained in the section about [how to run a repository extraction](./howToRunRepositoryExtractions.md), doesn't directly call it. So in such a case, you'll need to process their result with the download manager if you're interested in parts other than poms (they're always retrieved as they are required for the traversing of the dependency tree).


### notes

#### hashes
The download process will only check for valid hashes if configured accordingly. Currently, this is done via the pertinent property of the MavenHttpRepository, the ChecksumPolicy. If not set, it defaults to 'ignore hashes'. If you do want to have mc-core to check the hashes, it must be at least on level 'warn'. Level 'fail' is of course the safest, yet slowest one (well as fast as 'warn' in this respect). 

Mc-core checks whether the repository it is downloading from is including the hashes of the requested files directly in the HTTP headers of the response. If not - and forced by the ChecksumPolicy not being 'ignore' - it will request a single hash file from the repository. It will request MD5, SHA1 or SHA256 in order to do the check.

