#Reasons

The new mc-ng is in it's nature 'lenient', i.e. it doesn't abort if it finds a problem, but tries to continue. Of course, not all problems are recoverable and in some cases, it cannot make sense to continue. It also depends on how the configuration of the task at hand is setup - you can influence the leniency of the most complex features in mc-ng.

In most cases though, the different features are lenient - and will tell you in one or the other way what went wrong after they ran through.

It does that using the [Reason](javadoc:com.braintribe.gm.model.reason.Reason) entity.

Reasons are containers of 'error messages' as produced by mc-ng, but can also contain exceptions. They are used to explain why a rather complex process wasn't completed successfully. 

Reasons are defined here :

```    
    com.braintribe.gm:reason-model
```

The reason types used in mc-ng are defined here : 

```    
    com.braintribe.devrock:mc-reason-model
```


You'll find the pattern all throughout mc-ng, for instance an [ArtifactResolution](javadoc:com.braintribe.model.artifact.analysis.AnalysisArtifactResolution), [AnalysisArtifact](javadoc:com.braintribe.model.artifact.analysis.AnalysisArtifact) and [CompiledArtifact](javadoc:com.braintribe.model.artifact.compiled.CompiledArtifact)(while slightly different in expressing it) can all be tagged as failed or invalid and if so, can tell you what their problem was. 

For instance, the pom compiler can tell you that : 

``` java
 CompiledArtifact compiled = ...;
    if (compiled.getInvalid()) {
        Reason mainReason = compiled.getWhyInvalid();
        throw new IllegalStateException("pom file [" + myPom.getAbsolutePath() + "] is invalid because: " + mainReason.asFormattedString());                
    }
```

but also the TDR does so 

``` java
   AnalysisArtifactResolution artifactResolution = transitiveDependencyResolver.resolve( .... );
    if (artifactResolution.hasFailed() { 
        Reason mainReason = artifactResolution.getFailure();
                throw new IllegalStateException("pom file [" + myPom.getAbsolutePath() + "] is invalid because: " + mainReason.asFormattedString());                        
    }        
   
```

and of course, all higher tier resolvers based on the TDR do so :

``` java
    AnalysisArtifactResolution artifactResolution = classpathResolver.resolve( ...);
    if (artifactResolution.hasFailed() { 
        Reason mainReason = artifactResolution.getFailure();
                throw new IllegalStateException("pom file [" + myPom.getAbsolutePath() + "] is invalid because: " + mainReason.asFormattedString());                
        
    }       
```

in an [ArtifactResolution](javadoc:com.braintribe.model.artifact.analysis.AnalysisArtifactResolution) you have more ways to find out what went wrong. It will tell you directly what artifacts caused issues during the transitive resolving. Of course, you'll find the same reasons in the ArtifactResolution itself, but you can also look at this :


``` java
   AnalysisArtifactResolution artifactResolution = transitiveDependencyResolver.resolve( .... );
   Collection<AnalysisArtifact> incompleteArtifacts = artifactResolution.getIncompleteArtifacts();
   for (AnalysisArtifact incompleteArtifact : incompleteArtifacs) {
        Reason whyIncomplete = incompleteArtifact.getFailure();
        ..
   }
```

## structure

The 'top most' reason you get is a 'collator' reason. It act as an umbrella or container of the collected reasons. Each reason can have child-reasons, and if you call the formatter, it will generate a String with all reasons' messages.

## reason types 

There are several ways to create a [Reason](javadoc:com.braintribe.gm.model.reason.Reason), with a simple message, an exception, another Reason or a collection of Reasons. The reasons in earlier iterations had String type codes that told of what type they were - in the current iteration, the different types of reasons are modelled. 

These derivations are domain-specific, while the basic Reason itself is not if course (everybody in BT is encouraged to use the entity and it's good buddy [Maybe](javadoc:com.braintribe.gm.model.reason.Maybe)).

The builder you can use to create Reasons is [Reasons](javadoc:com.braintribe.gm.model.reason.Reasons) and makes it quite convenient to create reasons :

``` java
    CompiledDependencyIdentification cdi = ...;
    
    Maybe<CompiledArtifactIdentification> resolvedDependencyMaybe = dependencyResolver.resolveDependency( cdi);
    if (resolvedDependencyMaybe.isUnSatisfied()) {    
        Reason reason = Reasons.build( UnresolvedDependency.T).text("unresolved dependency " + cdi.asString()).cause( resolvedDependencyMaybe.whyUnsatisfied()).toReason();
    }
```

Malaclypse has its own set of reasons. You can find them in 
```
    com.braintribe.devrock:mc-reason-model
``` 

### Reason types of the pom compilation process

- PomCompileError : umbrella reason, the compilation of the pom had at least one problem
- PomValidationError: the pom has internal problems, base of the others
- InvalidPomFormatReason: the pom is invalid on the XSD level
- UnresolvedParent : a referenced parent doesn't exist 
- MalformedDependency : a dependency declaration couldn't be properly resolved. 
- MalformedArtifactDescriptor : a pom couldn't properly identify the artifact it stands for (i.e. issues with groupId, artifactId, version)

    
### Reason types of the transitive dependency resolver

- IncompleteInstrumentation : the artifact's referenced parents and/or their imports cannot be properly resolved.        
- UnresolvedDependency : a dependency cannot be found.
- DependencyCycle : cycle within the dependencies - one branch of the dependency tree contains itself again     
- UnresolvedDependencyVersion : no matching artifact was found in the version list (metadata) for the passed dependency
- UnresolvedArtifact :  even a match was found amongst the version, the referenced artifact doesn't exist or a direct reference to an artifact couldn't be resolved.

### Reason types of the classpath resolver 

- UnresolvableClash : a clash between one or more dependencies cannot be resolved.


### Common reason types of the ArtifactAnalysisResolution

- IncompleteResolution : umbrella reason, the ArtifactResolution is incomplete
- IncompleteArtifact : umbrella reason, the AnalysisArtifact is incomplete

    

