# The thing with terminals

All resolvers have entry points, termed 'terminal' in our lingo. Terminals are the root of a dependency tree, and have nothing to do with what they actually are - yes, it could be the application's main thingi, but any other artifact can be a terminal - for instance when you want to build it, or simply get the transitive dependencies of it.

You need to understand what a terminal is in mc-ng, because depending on how you declare the entry-point, you'll get different results.

CompiledTerminals are used by all resolvers based on the TransitiveDependencyResolver and of course, itself. 

## CompiledTerminal
The [CompiledTerminal](javadoc:com.braintribe.model.artifact.compiled.CompiledTerminal) is an abstract base class and can be created from different sources, but basically there only two different types of CompiledTerminals.

Native CompiledTerminals are both the CompiledArtifact and the CompiledDependencyIdentification, that means that you can pass them directly to the resolvers. 


### based on a CompiledArtifact 
This would the standard when you have a previously resolved artifact and you would want its classpath. Typically, this can react on special logic when it comes to the terminal, such as including dependencies flagged as 'optional'. 

Note that in this case, obviously, the terminal is not contained in the solution-list. 

Now if you only have a string representation of your terminal artifact, you cannot just use one of the parser functions of the CompiledTerminal, but you have to first resolve it.

``` java
    String terminal = ...;
    CompiledArtifactResolver caResolver = ..;
    ClasspathResolver cpResolver = ...;
    ClasspathResolverContext cpContext = ....;
    
    CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse(terminal);
    Maybe<CompiledArtifact> compiledArtifactOptional = caResolver.resolve( cai);                        
    if (compiledArtifactOptional.isSatisfied()) {
        CompiledTerminal cdT = compiledArtifactOptional.get(); 
        AnalysisArtifactResolution resolution = cpResolver.resolve( cdT, cpContext);
        ...;
    }        

```



### based on a CompiledDependency
This is a perhaps less common entry-point, as it is mostly used to simulate a virtual artifact. Again - obviously - the dependencies flagged as 'optional' will never be included (as the virtual artifact doesn't exist and 'optionals' are only relevant for the artifact). Note that in this case, the terminal is part of the solution, hence appears in the solution list of the resolution.

``` java
    String terminal = ...;
    ClasspathResolver cpResolver = ...;
    ClasspathResolverContext cpContext = ....;    
   
    CompiledTerminal cdT = CompiledTerminal.parse( terminal);
    AnalysisArtifactResolution resolution = cpResolver.resolve( cdT, cpContext);       
    ...
```


## optional dependencies
Dependencies are marked as 'optional' if they bring in required classes for a feature that the author of the artifact deems to be optional, i.e. only a subset of comprised features require the classes contained in the optional dependency.
The optional dependencies are required to build the owning artifact (or to run the owning artifact's main class), so only if the owning artifact is the actual terminal, they will be added to the solution set. 

If a developer wants to use the 'optional' feature of an artifact, he needs to add the dependency to his artifact's own dependencies, obviously without the optional-marker. 

