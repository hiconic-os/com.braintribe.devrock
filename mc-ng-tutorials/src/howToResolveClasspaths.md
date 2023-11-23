 # how to resolve classpaths

As with every task in this section, you'll need some wirings and some models. 

```
com.braintribe.devrock:mc-core
com.braintribe.devrock:mc-core-wirings
com.braintribe.devrock:analyis-artifact-model
```


### configuration
Basically, in order get a classpath you'll need to think first of what you want it produce and how you want it to. The vehicle to configure the classpath resolving is a context, the [ClasspathResolutionContext]((javadoc:com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext).. Of course, with [ClasspathResolutionContextBuilder](javadoc:com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContextBuilder) there's a builder, so you can easily create such a context.

```
        ClasspathResolutionContext resolutionContext = ClasspathResolutionContext.build() //
            .clashResolvingStrategy(ClashResolvingStrategy.firstOccurrence) // 
            .lenient(false) // 
            .scope(ClasspathResolutionScope.compile) //
            .done(); //
```

Basically, you'll need to specify three main parameters, leniency, clash resolving strategy and the 'magic' scope to be used during the resolution.

#### the leniency 
if not lenient, any single error during resolution of the classpath will lead to a direct exception. If true, the resolver *will* always return a resolution, and in case of an issue, mark the resolution as failed. More about that below or [here](asset://com.braintribe.devrock:mc-ng-principles/principles/reasons.md)


#### the strategy 
how to resolve clashes between contradicting dependencies. You can choose between 

- ClashResolvingStrategy.firstOccurence : like with Maven, the first encounter of an artifact identification (groupId and artifactId) always wins and any contradicting occurrences are ignored.
- ClashResolvingStrategy.highestVersion : the artifact with the highest version wins, regardless where it appeared in the transitive dependency tree. 

#### what the 'magick' scope is 

- ClasspathResolutionScope.compile : the classpath is to be used to *compile* the terminal
- ClasspathResolutionScope.runtime : the classpath is to be used to *run* the terminal
- ClasspathResolutionScope.test : the classpath is to be used to *test* the terminal
    
 These scopes are 'magick' scopes in that way that they actually denote a certain combination of dependency scopes, i.e. they influence how dependencies are filtered based on the scope their declaration is attached to. 
 
 - compile : includes 'compile', 'provided' and may process 'optional' dependencies.
    
 - runtime : includes 'compile', 'runtime'.
 - test : includes 'compile', 'runtime' and 'test'
    
About the 'optional', please consult the special section about the [entry points of resolutions](./theThingWithTheTerminals.md).

These three are the mandatory parameters. Furthermore, you can specify other parameters 

```
        ClasspathResolutionContext resolutionContext = ClasspathResolutionContext.build() //
            ...
            .enrichJars( true)
            .enrichSources( true)
            .enrichJavadoc( true)
            .enrichmentExpert( this::myExpertise)
            .dependencyFilter( d -> true)
            ....
            .done(); //
```


#### whether to enrich jars 
 if true, the resolver will automatically download attached jars. Any jar missing will lead to the failed resolution. *Default is 'true'.*

#### whether to enrich sources
 if true, the resolver will automatically download attached -sources.jar. A missing sources.jar will not lead to a failed resolution. *Default is 'false'.* If you want to have a different behavior, use an 'enriching expert', see below
 
#### whether to enrich javadocs
 if true, the resolver will automatically download attached -javadoc.jar. A missing javadoc.jar will not lead to a failed resolution. *Default is 'false'.* If you want to have a different behavior, use an 'enriching expert', see below
 
#### what special parts you want to enrich for a given artifact - an 'enrichment expert'
you'll need to plug-in a function with this signature here : 

```
    Function<AnalysisArtifact, List<Pair<PartIdentification, Boolean>>>
```

As you can see, the function gets the AnalysisArtifact and can return a list of pairs of each part identification to download and whether the part is required (will lead to a failed resolution if missing). 

#### global exclusions 
A set of [ArtifactIdentification](javadoc:com.braintribe.model.artifact.essential.ArtifactIdentification) that will be automatically added as exclusions for the dependency tree.

#### what dependencies you want filtered out 
this predicate can be used to filter-out any unwanted dependencies. 

```
    Predicate<AnalysisDependency> 
```

The [AnalysisDependency](javadoc:com.braintribe.model.artifact.analysis.AnalysisDependency) passed to the filter is already a higher order. It has a [CompiledDependency](javadoc:com.braintribe.model.artifact.compiled.CompiledDependency) that was the origin of it, and therefore gives access to the all pertinent information about the dependency. 
 
Default is a 'pass through' filter, i.e. any dependency is taken (of course, the classpath resolver has its own filter, so any dependency that doesn't contribute to the classpath is filtered out per default).


### running the resolution 
Once you have the instance of the context, pass it to the resolver during 'resolve'.

But first, you'll need to think about what the entry point is for your resolution, as there are [different possibilities](./theThingWithTheTerminals.md).


``` java
        OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
        ves.addEnvironmentOverride("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", mySettings.getAbsolutePath());

        try (               
                WireContext<ClasspathResolverContract> resolverContext = Wire.contextBuilder( ClasspathResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
                    .bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))               
                    .build();
            ) {
            
            ClasspathDependencyResolver classpathResolver = resolverContext.contract().classpathResolver();
            
            CompiledTerminal cdi = CompiledTerminal.from ( CompiledDependencyIdentification.parse( "com.braintribe.devrock.test:t#1.0.1"));
            AnalysisArtifactResolution artifactResolution = classpathResolver.resolve( resolutionContext, cdi);
            ....                                             
        }

```

As you can see, running is straight forward - again, the example above uses a maven-style configuration for the local and remote repositories. Once you have the resolution, you'll need - dependency whether you set the resolution to lenient - to look at it. 

### looking at the result
As described above, if you told the resolver not to be lenient, the first issue occurring during the resolution will lead immediately to an exception. If you however told the resolver to be lenient, you need to look at the resolution. An [AnalysisResolution](javadoc:com.braintribe.model.artifact.analysis.AnalysisArtifactResolution) can have [failed](asset://com.braintribe.devrock:mc-ng-principles/principles/validity.md), and it will tell you that in various ways

If the resolution hasn't failed, and you're interested in the list of dependencies only, you just look at the resolution's *solution* collection. 

If you want to extract more information about the resolution, look at [how to analyze resolutions](./howToAnalyzeResolutions.md).

In that collection, you find all the [AnalysisArtifact](javadoc:com.braintribe.model.artifact.analysis.AnalysisArtifact) that make up the dependency tree of the [terminal](./theThingWithTheTerminals.md) passed.

Each [AnalysisArtifact](javadoc:com.braintribe.model.artifact.analysis.AnalysisArtifact) has parts, which is a map of the [PartIdentification](javadoc:com.braintribe.model.artifact.essential.PartIdentification) and a [Resource](javadoc:com.braintribe.model.resource.Resource) with gives to access the data of the part. 



#### HasFailure
[AnalysisResolution](javadoc:com.braintribe.model.artifact.analysis.AnalysisArtifactResolution) implements [HasFailure](javadoc:com.braintribe.gm.model.reason.HasFailure) and therefore is able to tell you quite clearly what went [wrong](asset://com.braintribe.devrock:mc-ng-principles/principles/validity.md).

``` java
    boolean hasFailed = resolution.hasFailed();
    Reason reason = resolution.getFailure();
```
If hasFailed returns false or you do not get a [Reason](javadoc:com.braintribe.gm.model.reason.Reason), then all's fine. Otherwise, you'll get a 'container' reason which then lists all different issues in form of reason entities. 

There is quite some ['magick'](asset://com.braintribe.devrock:mc-ng-principles/principles/validity.md) going on in the resolver itself - that are not the obvious ones like incomplete artifact declarations or unresolved dependencies, such as invalid combination of the packaging of an artifact and the type of its reference. All these issues will appear as entries.
