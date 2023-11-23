# identifications

## essential identifications

The most basic identifications are contained in a specific model, they are found in

```
com.braintribe.devrock:essential-artifact-model.
```

These identifications are kept as lightweight as possible.


[ArtifactIdentification](javadoc:com.braintribe.model.artifact.essential.ArtifactIdentification)

An ArtifactIdentification identifies an 'unversioned' artifact, i.e. consists only of groupId and artifactId.

``` java
ArtifactIdentification ai = ArtifactIdentification.parse( "com.braintribe.devrock:mc-core");
String name = ai.asString();
```

[VersionedArtifactIdentification](javadoc:com.braintribe.model.artifact.essential.VersionedArtifactIdentification)

A VersionedArtifactIdentification identifies a versioned artifact, i.e. it also contains a string value for the version (not a Version, that would be the [CompiledArtifactIdentifcation](javadoc:com.braintribe.model.artifact.compiled:CompiledArtifactIdentifcation))

``` java
VersionedArtifactIdentification vai = VersionedArtifactIdentification.parse("com.braintribe.devrock:mc-core#1.0.1");

String name = vai.asString();
```

[PartIdentification](javadoc:com.braintribe.model.artifact.essential.PartIdentification)

A PartIdentification identifies a part, i.e. consists of a classifier and a type

``` java
PartIdentification pi = PartIdentification.parse("sources:jar");

pi = PartIdentification.of("javadoc", "jar");

pi = PartIdentification.of( "pom");

String name = pi.asString();
```


## declared identifications

There are no identifications on the [declared  level](multilevel.md).


## compiled identifications

The next level for identifications is the compiled level.

The entities are found in

```
com.braintribe.devrock:compiled-artifact-model.
```


[CompiledArtifactIdentification](javadoc:com.braintribe.model.artifact.compiled.CompiledArtifactIdentifcation)

The CompiledArtifactIdentifcation contains the same information as the VersionedArtifactIdentification, differing in that the version information contained is actually expression with a [Version](javadoc:com.braintribe.model.version.Version).

``` java
CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse("com.braintribe.devrock:mc-core#1.0.1");

VersionedArtifactIdentification vai = VersionedArtifactIdentification.parse( "com.braintribe.devrock:mc-core#1.0.1");
cai = CompiledArtifactIdentifcation.from( vai);


ArtifactIdentification ai = ArtifactIdentification.parse( "com.braintribe.devrock:mc-core");
cai = CompiledArtifactIdentifcation.from( ai, Version.parse("1.0"));

String name = vai.asString();
```

apart from the different parsers, it also implements Comparable<CompiledArtifactIdentification>, so this is possible:

``` java
CompiledArtifactIdentification cai1 = CompiledArtifactIdentification.parse("com.braintribe.devrock:mc-core#1.0.1");

CompiledArtifactIdentification cai2 = CompiledArtifactIdentification.parse("com.braintribe.devrock:mc-core#1.0.1");

cai1.compareTo( cai2) == 0;

```

[CompiledPartIdentifcation](javadoc:com.braintribe.model.artifact.compiled.CompiledPartIdentifcation)

The CompiledPartIdentifcation combines the PartIdentification and the CompiledArtifactIdentifcation into one entity. You can create it from a [CompiledArtifactIdentifcation](javadoc:com.braintribe.model.artifact.compiled.CompiledArtifactIdentification) and a [PartIdentification](javadoc:com.braintribe.model.artifact.essential.PartIdentification). If can return the expected file name or simply a string representation

``` java
CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse("com.braintribe.devrock:mc-core#1.0.1");
PartIdentification pi = PartIdentification.parse("sources:jar");

CompiledPartIdentifcation cpi = CompiledPartIdentifcation.from( cai, pi);

cpi.asString() -> 'com.braintribe.devrock:mc-core#1.0.1 / sources:jar'

cpi.asFileName() -> 'mc-core-1.0.1-sources.jar'

```

[CompiledDependencyIdentification](javadoc:com.braintribe.model.artifact.compiled.CompiledDependencyIdentification)

The CompiledDependencyIdentification represents a fully qualified dependency, where the version part is modelled as an [VersionExpression](./versions.md). It can parse a String or build from different data.

``` java
CompiledDependencyIdentification cdi1 = CompiledDependencyIdentification.parse("com.braintribe.devrock:mc-core#1.0.1");
CompiledDependencyIdentification cdi2 = CompiledDependencyIdentification.parse("com.braintribe.devrock:mc-core#[1.0,]");
CompiledDependencyIdentification cdi3 = CompiledDependencyIdentification.create( "com.braintribe.devrock", "mc-core", [2.0]);

VersionedArtifactIdentification vai = VersionedArtifactIdentification.parse("com.braintribe.devrock:mc-core#1.0.1");
CompiledDependencyIdentification cdi4 = CompiledDependencyIdentification.from( vai);

```
