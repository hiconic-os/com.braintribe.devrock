# how to analyze resolutions

AnalysisArtifactResolution is the type that reflects the resulting data of both the [transitive dependency resolver (TDR)](javadoc:com.braintribe.devrock.mc.core.resolver.transitive.BasicTransitiveDependencyResolver) and the [classpath dependency resolver (CPR)](javadoc:com.braintribe.devrock.mc.core.resolver.classpath.BasicClasspathDependencyResolver).

Both are very similar anyhow, the get their respective configuration context passed and a CompiledDependencyIdentification (or CompiledTerminal to be precise) and will return an
[AnalysisArtifactResolution](javadoc:com.braintribe.model.artifact.analysis.AnalysisArtifactResolution).


This would be the call of the TDR

```java

    TransitiveDependencyResolver transitiveDependencyResolver = resolverContext.contract().transitiveDependencyResolver();
    
    CompiledTerminal cdi = CompiledTerminal.from ( CompiledDependencyIdentification.parse( terminal));

     AnalysisArtifactResolution artifactResolution = transitiveDependencyResolver.resolve(resolutionContext, cdi);
```

And this the call of the CPR

```java
       
    ClasspathDependencyResolver classpathResolver = resolverContext.contract().classpathResolver();
            
    CompiledTerminal cdi = CompiledTerminal.from ( CompiledDependencyIdentification.parse( terminal));

    AnalysisArtifactResolution artifactResolution = classpathResolver.resolve( resolutionContext, cdi);
```

Common to both is the [AnalysisArtifactResolution](javadoc:com.braintribe.model.artifact.analysis.AnalysisArtifactResolution).

[AnalysisResolution](javadoc:com.braintribe.model.artifact.analysis.AnalysisArtifactResolution) implements [HasFailure](javadoc:com.braintribe.gm.model.reason.HasFailure) and therefore is able to tell you quite clearly what went [wrong](asset://com.braintribe.devrock:mc-ng-principles/principles/validity.md).
Some aspects of the resolution have already been described in the part about [reasoning](asset://com.braintribe.devrock:mc-ng-principles/principles/reasons.md), especially about how to deal with *failures*. 

Actually, there's more to the resolution and you can use the data to better understand of what happened during resolution. 

This text here rather discusses the structure of the resolution.


## terminals
Terminals are of course the [entry points of the resolution](./theThingWithTheTerminals.md). They can be an artifact itself or a dependency, or a collection of both. In most cases - you can configure it - they are not part of the solutions returned. 

## solutions
Solutions are simply AnalysisArtifacts that populate the (combined) dependency tree of the passed terminal(s). As explained [here](./howToResolveClasspaths.md), they each give access the parts of an artifact (via a map of  [PartIdentification](javadoc:com.braintribe.model.artifact.essential.PartIdentification) to [Resource](javadoc:com.braintribe.model.resource.Resource)). 

>Note that the [AnalysisArtifact](javadoc:com.braintribe.model.artifact.analysis.AnalysisArtifact) has lost all dependencies that were filtered-out, see below about that. 


#### Filtered dependencies 
While not something to be interpreted as a failure per se, you'll still might want to check what the diverse filters in excluded from processing. 

``` java
    Set<AnalysisDependency> filteredDependencies = resolution.getFilteredDependencies();
```

Dependencies can be filtered for a multitude of reasons - basically the collections contains dependencies that were filtered out by the diverse filters (see [here](./howToResolveClasspaths.md) about how the depedency filter can be configured). In case of the classpath resolution, which has a specific filter active, all dependencies that do not contribute to the classpath appear here, such as - for instance - any depdendency that is typed as 'javadoc'. What is important to know is that dependency itself has been removed from the owning AnalysisArtifact, but the [AnalysisDependency](javadoc:com.braintribe.model.artifact.analysis.AnalysisDependency) in the collection retains the pointer to owning artifact. The idea is that if you want to iterate over the dependency of an artifact that was created during the run of the resolver, you should only see dependencies that were relevant for the current use case. So we tried to separate the wheat from the chaff for you. 
    

Filtered dependencies do not count as failures, so you can have filtered dependencies without the 'failed' flag being raised on the resolution.


### Incomplete artifacts
You'll also find a list of 'incomplete' artifacts, which are artifacts that couldn't be parsed sufficiently enough to identify them and their dependencies. If that list's empty, you'll know that all artifacts have been parsed successfully. 

``` java
    Set<AnalysisArtifact> incompleteArtifacts = resolution.getIncompleteArtifacts();
```

The existence of such artifacts are considered to be a failure, and by looking that the failure member 'hasFailure' you can access the reasoning of why they remained incomplete. In the most cases, this is due to unresolved parents, versions of dependencies that cannot be resolved or, in case of parents, unresolved import-scoped dependencies in the dependency management section. 

#### Unresolved dependencies
``` java
    Set<AnalysisDependency> unresolvedDependencies = resolution.getUnresolvedDependencies();
```
And you'll find a list of dependencies (the artifacts referenced by the dependencies) that could not have been found during the resolution. If this list's empty, all referenced artifact have been found.


#### clashes
``` java
    Set<DependencyClash> clashes = resolution.getClashes();
```

The entries in this list, all [DependencyClash](javadoc:com.braintribe.model.artifact.analysis.DependencyClash), will tell you everything you need to know about the contradictions regarding referenced artifacts in the transitive dependency tree. 

##### winning artifact
``` java
   AnalysisArtifact winner = dependencyClash.getSolution();
```
The winning artifact is the artifact that was finally chosen and what appears in your classpath. All occurrences of the 'losing' artifacts are replaced in the resolution, that means you'll find no traces of the clashes in the resolution.

##### winning dependency
``` java
   AnalysisDependency winner = dependencyClashgetSelectedDependency();
```

The winning dependency is the dependency that 'won' the contest. Depending on the strategy, it's either the first dependency of the artifact found in the transitive dependency tree or the dependency with the highest (resulting - in case of a range) version. 

##### contradicting dependencies
``` java
   List<AnalysisDependency> involvedDependencies = dependencyClash.getInvolvedDependencies();
```
This list will give you all dependencies that were involved in the clash. While the dependencies (other than the winning one) are disconnected from their solution (i.e. downwards in the hierarchy), the 'depender chain' can still be followed (i.e. upwards in the hierarchy).

##### remapped artifacts
``` java
   Map<AnalysisDependency, AnalysisArtifact>  winner = dependencyClash.getReplacedSolutions();
```
Finally, you'll get the correlation of the losing dependencies with their actual (now former) solutions, so you can actually see what artifacts didn't make into the resolution.
