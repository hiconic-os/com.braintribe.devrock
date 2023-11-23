# how to upload

sometimes you might need to upload an artifact to a repository, even if in most cases, a friendly CI pipeline does that for you.

## where to find the feature 
Basically, it's very easy to get an artifact uploaded. You'll need however a full middle tier wiring to achieve that as a full repository configuration is required. So easiest is to use the 

    com.braintribe.devrock.wirings.resolver.contract.ArtifactDataResolverContract
    
it's defined with its module in 

    com.braintribe.devrock:mc-core-wirings
    
## the ArtifactDeployer
So what you do is to pull the [ArtifactDeployer](javadoc:com.braintribe.devrock.mc.api.deploy.ArtifactDeployer) from the wired contract and then use it. 

The ArtifactDeployer is an abstraction of the actual upload procedure. Depending on the type of the repository it is attached to it will automatically use the appropriate method to transfer the files (file copying if it's a file system based repo or HTTP if it's a remote repository).

Basically, it uploads an instance of [Artifact](javadoc:com.braintribe.model.artifact.consumable.Artifact) and returns a [ArtifactResolution](javadoc:com.braintribe.model.artifact.consumable.ArtifactResolution).


### using it 
You can upload a single artifact 

``` java
    Artifact artifact = ...;
    ArtifactResolution resolution = artifactDeployer.deploy( artifact);
        
    if (resolution.hasFailed()) {
        throw new IllegalStateException("uploading of .. has failed as " + resolution.getFailure().asFormattedText());
    }
```

or you can upload a Collection of artifacts 

``` java
    List<Artifact> artifacts = ....;

    ArtifactResolution resolution = artifactDeployer.deploy( artifacts);
        
    if (resolution.hasFailed()) {
        throw new IllegalStateException("uploading of .. has failed as " + resolution.getFailure().asFormattedText());
    }
```

### how to get it from the wirings 
The full sequence - getting the contract et al - could look like this to upload a single artifact 

``` java
        String repositoryId = ...;
        Artifact artifact = ...;

        try (               
                WireContext<ArtifactDataResolverContract> resolverContext = Wire.contextBuilder( ArtifactDataResolverModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
                    .bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))               
                    .build();
            ) {
            Repository repository = resolverContext.contract().repositoryReflection().getRepository( repositoryId);
            if (repository == null) {
                // no repository found with that name
                throw new IllegalStateException("uploading of .. has failed as there's no repository [" + respositoryId + "]");
                
            }
            ArtifactDeployer artifactDeployer = resolverContext.contract().backendContract().artifactDeployer( repository);
                                   

            ArtifactResolution resolution = artifactDeployer.deploy( artifact);
            
            if (resolution.hasFailed()) {
                throw new IllegalStateException("uploading of .. has failed as " + resolution.getFailure().asFormattedText());
            }
            
            ... 

        } 
```

### analyzing the return values    
The returned ArtifactResolution may not be the perfect vehicle for the return, but it will tell you all you need to know.

``` java
    ArtifactResolution resolution = ...;
    
    if (resolution.hasFailed()) {
        for (Artifact terminal : resolution.getTerminals()) {
            if (terminal.hasFailed()) {
                for (Part part : terminal.getParts()) {
                    if (part.hasFailed()) {
                        System.err.println("Part [" + part.asString() + "] has failed because " + part.getFailure().asFormattedText());
                    }
                }
            }
        }
    }
```

As you can see, all involved entities reflect on the problem they had, and if a child is flagged as failed, then its parent will also be flagged. 

>Note : if the upload of a part has failed, the reason contains the status line text of the responding server. 


## notes

### hashes
If used on a remote repository based on HTTP, the ArtifactDeployer will generate hashes of the files to be uploaded. Not only will it include these hashes in the HTTP headers it sends, but also as separate files (extension as the message digest, i.e. md5, sha1 and sha256).

However, if the remote repository is file-system based (one could argue that it's not remote, but it could be shared drive on an remote server, so..) then no hashes are created, and hence, no files uploaded. 


