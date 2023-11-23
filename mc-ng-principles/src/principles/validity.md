# Validity of dependency structures

It seems to be obvious that dependency trees can be valid or invalid, or rather : the way artifacts are linked can be correct or wrong. In the dependency world, we could even speak of 'legal' and 'illegal', because some situations will lead to an error or even an exception.

## basic pom validity

The basic validity is easy to explain:

*Any artifact within the dependency tree must be identifiable, that means its coordinates (packaging, groupId, artifactId, version) need to be declared, either directly (artifactId must be) or indirectly via the parent reference within the pom.*

>NOTE: in old mc, the values could've been taken from the parent's pom. That required that parent needed to be resolved and hence all possible locations (local repo, codebase, remote repos) must be known to whoever reads the pom. In new mc, we do no longer resolve the parent, but take the missing data (again, it can be only the groupId and the version of the pom being read) from the parent reference rather than the parent. Rationale is of course that if you omit the groupId, and want to take it from the parent, it must be the groupId entered for the parent reference (as it must be the same). Same applies to the version. However, there's a catch: the parent reference is actually a dependency. Therefore, the version declaration may be a range rather than a version. Hence, in that case, if you want to use a range there, you *must* declare the version for the actual pom and cannot simply omit it - see below for more on this topic.

Parent references and imported dependency management statements must also be resolvable, and the referenced targets must be resolvable. 

Any dependency within an artifact must also be identifiable, that means again that its coordinates (groupId, artifactId, version, classifier, type, scope) need to be available, either by defaulting, resolvable variable, declared directly or indirectly via the parent-chain.

### automatically instrumenting artifacts

As stated above, the coordinates, but most importantly what is required for the identification i.e. groupId, artifactId and version, must be declared or to able to be derived from the parent. For these values, the following stands:

- artifactId : this always must be declared within the pom itself. 
- groupId : it may be omitted, *if* a parent reference is declared within the pom. In that case, the groupId is taken from the parent-reference. 
- version: it may be omitted, *if* a parent reference is declared within the pom *and* the version expression in the parent reference is a version and *not* a version-range. 


>Note: why is omitting the version invalid if the version expression in the parent-reference is a range? The answer is simply once you come to think of it : while it would be quite easy to resolve the parent first, then look at its version and use this for the version of the artifact, it would mean the version of the artifact in question changes depending on the version of the parent. This may be valid in case of the pom of the source project, it's definitively invalid in the case of a standard repository as there the location of the artifact is dependent on its version (as it's repeated in the directory structure).


### validating
Validation of a single pom happens during processing it. 

The pom reader will always return a [CompiledArtifact](javadoc:com.braintribe.model.artifact.compiled.CompiledArtifact), but will flag it as invalid if it found of these issues declared below.

``` java
    CompiledArtifact compiled = ...;
    if (compiled.getInvalid()) {
        Reason mainReason = compiled.getWhyInvalid();
        throw new IllegalStateException("pom file [" + myPom.getAbsolutePath() + "] is invalid because: " + mainReason.asFormattedString());                
    }
```

See more about [Reason](javadoc:com.braintribe.gm.model.reason.Reason) [here](./reasons.md).

## transitive tree validity 
Of course, a dependency tree can only be considered to be complete if all dependencies can be resolved, i.e. turned into artifacts.
Now, apart from that basic validity which is centered about a node in the tree, there's another aspect of validity that concerns itself with the relation between artifacts, i.e. the branches in the tree. 

As stated above, an artifact has a packaging (defaulting to 'jar') and a dependency has a type (defaulting to 'jar'), and only some of these combinations are valid. There are some 'well known' types, but mostly, the packaging can be declared as wanted. There is some internal logic however:


### traversion 
Transitive processing means in this context that the artifact's dependencies are traversed if found.

The criteria is that for the standard packaging (no packaging or 'jar' packaging), all dependencies are traversed. Whether the other packagings are being traversed depends on their combination with the 'classifier' and 'type'.

Always traversed are the following packagings:

```
    jar (or no declared packaging)
    bundle
```

the following packagings are only traversed if some conditions apply

```
    pom : if the type is also 'pom', it will be traversed.
    war : if the classifier is 'classes' and the type is 'jar', it will be traversed 
    ear : if the classifier is 'classes' and type is 'jar' it will be traversed    
```

Other combinations will not be traversed. 


### contribution 

Contribution in this context means that a part (mostly the jar) is included into the classpath.

The following packagings do always contribute to the classpath :

```
    jar (or no declared packaging)
    bundle
```

Other packagings' contributions depend - as in case of the traversing - on the combination of the 'classifier' and the 'type':

```
    war : if the classifier is 'classes' and the type is 'jar', the classes.jar will be taken, and its non-existence is an error
    ear : if the classifier is 'classes' and the type is 'jar', the classes.jar will be taken, and its non-existence is an error
```

>Note here that packaging 'pom' does never contribute to the classpath, as no 'jar' file is expected here. 

```
    pom : will not contribute to the classpath
 ```
### validation 
Validation of a transitive resolution can happen in several places, depending on what resolution you are using. The intermediate-top tier, the TransitiveResolverWireModules's tier does validate internally, so do the tow apex tiers, the ClasspathResolverWireModule and the PlatformAssetResolverModule. All of these resolvers actually return the same result type, the [AnalysisArtifactResolution](javadoc:com.braintribe.model.artifact.analysis.AnalysisArtifactResolution).

``` java

    AnalysisArtifactResolution resolution = ...;
    if (resolution.getInvalid()) {
        Reason mainReason = resolution.getWhyInvalid();
        throw new IllegalStateException("resolution is invalid because: " + mainReason.asFormattedString());                
    }
```

See more about [Reason](javadoc:com.braintribe.gm.model.reason.Reason) [here](./reasons.md).