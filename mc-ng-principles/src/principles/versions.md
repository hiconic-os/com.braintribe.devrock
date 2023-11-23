# Versions in mc-core

As versions are generally important, they are not longer part of the com.braintribe.devrock group, but have been moved into

```
com.braintribe.gm:version-model
```

As the model also contains parsers, to-string conversions and comparators, the model is no longer a canonical model, i.e. it can only live up to its full potential in the Java world and will loose functionality if transferred into (for canonical models) isomorphic forms.

The fundamental entity is the [VersionExpression](javadoc:com.braintribe.model.version.VersionExpression). It abstracts all different possible version-like entities. You can parse expressions in string from into it.

``` java
VersionExpression ve = VersionExpression.parse( "1.0");

ve = VersionExpression.parse( "(1.0,]");

ve = VersionExpression.parse( "[1.0,1.1)");

ve = VersionExpression.parse( "[1.0,1.2)");

ve = VersionExpression.parse("(,1.0],[1.2,)");

```

Note that the VersionExpression actually return is not a VersionExpression, but rather one of the following

```
1.0 -> Version (soft : negotiable)
[1.0] -> Version (hard : non-negotiable)
(1.0,] -> VersionRange
[1.0,1.2) -> VersionRange
[1.0,1.1) -> FuzzyVersion (special version that reflects a range on the revision only)
(,1.0],[1.2,) -> VersionIntervals
```

Hard and soft versions differ in how they may be treated during clash resolving. On the level of the version-model, they do not differ at all, both are simple version. If you want to be able to react on these cases, you need to use another parser that can signal to a Consumer<Boolean> whether to version expressed is negotiable or it isn't.

``` java
Consumer<Boolean> versionIsNegotiableConsumer = ...;

VersionExpression ve = VersionExpression.parse( "1.0", versionIsNegotiableConsumer);
VersionExpression ve = VersionExpression.parse( "[1.0]", versionIsNegotiableConsumer);
```

the consumer is simply

``` java
  Consumer<Boolean> - passed true if the version can be overridden during clash resolving, false it cannot be overridden.
```

>As a rule of thumb : CompiledArtifactIdentifcation use Versions, CompiledDependencyIdentifications use VersionExpression, details on that can be found [here](./identifications.md)


## qualifiers in versions

There exists a special logic dealing with qualifiers. We do support it just as Maven does, with a slight difference when it comes to our 'pc' qualifier which Maven doesn't know.

as defined by Maven :

```
<major>.<minor>[.<revision>][-<qualifier>][-<buildNumber>][<nonConform>]
```

and implicitly when doing metrics (what Maven calls a 'ComparableVersion')

```
<major>.<minor>[.<revision>][-<qualifier>[-<buildNumber>][<nonConformSequence>[noConformSequence]..]
```

This has the following consequence:

```
1.0.1-alpha-1 -> major 1, minor 0, revision 1, qualifier "alpha", buildnumber 1

1.0.1-alpha1 -> major 1, minor 0, revision 1, qualifier null, buildnumber 0, nonconform "alpha1"

```

the non-conform part is expanded during metric comparision, so that the latter expression is turned into 

```
1.0.1-alpha1 -> major 1, minor 0, revision 1, qualifier null, buildnumber 0, nonconform: delimiter "-", qualifier "alpha", number 1.

```

this allows the following in metrics

```
1.0.1-alpha1 < 1.0.1-alpha2 < 1.0.1-alpha10

```


### well known qualifiers

Well known qualifiers in Maven are (with the exception of pc) - most of them have alternative notations.. 

```
alpha, a
beta, b
milestone, m
rc, cr, pc
SNAPSHOT
<no qualifier>, ga, final
sp
<any other qualifier>
```

of course, the same applies all 'qualifiers' the standard ones and the ones in the nonconform part, so these here - as examples - are also equivalent (n stands for the build number) 

```
1.0.1-alpha<n> == 1.0.1-a<n>
1.0.1-alpha-<n> == 1.0.1-a-<n>

```

these 'well known' qualifiers have their internal 'weight', so the following is true:

```
alpha < beta < milestone < rc < SNAPSHOT < 'no qualifier' < sp < 'any other qualifier'
```

Unfortunately, it gets worse. In order to Maven to recognize a known qualifier, it must be *LOWERCASE*. Unless it's the *SNAPSHOT* qualifier, which needs to be *UPPERCASE*. Unrecognized qualifiers are automatically higher than any recognized qualifier.

That leads to this:

```
1.0-SNAPSHOT < 1.0 < 1.0-snapshot

1.0-milestone < 1.0 < 1.0-MILESTONE
```

two identical qualifiers are, compared using the build numbers, 

```
1.0-milestone-1 < 1.0-milestone-2
```


if unknown, compared alphabetically, so this is how that affects it

```
 1.0-milestone-2 < 1.0-SNAPSHOT < 1.0 < 1.0-MILESTONE-1 < 1.0-snapshot
 ```

It's kind of quite weird, but that's how it is. You might ask why we reflect Maven's erratic behavior (a euphemism, I personally would rather call it abysmally stupid) instead of moving to a more stable implementation: the reason is simply that out there, quite some Maven developers have not the foggiest idea how it should be and not only got used to how it is in Maven, but *actually* (frightfully) designed their dependency logic based on that behavior - and we don't want to break their things when it's run through our tooling.


### metrics on non-conform sequences

As shown above, mc-core does support metrics on the various non-conform sequences that may be attached to a version: 


Consider the following version:

```
1.0-myQualifier6-1a.bla15
```

The internal representation of the version - as in the com.braintribe.gm.Version - looks like this:  

```
major : 1
minor : 0
revision : null
qualifier : null
nonconform : -myQualifier6-1a.bla15
```

during metrics, it will look like this

```
major : 1
minor : 0
revision : null
qualifier : null
nonconform 1:
	 delimiter : -
	 qualifier: myQualifier
	 number: 6
nonconform 2:
	delimiter: -
	qualifier: null
	number: 1
nonconform 3:
	delimiter : null
	qualifier: a
	number: 0	  
nonconform 4:
	delimiter : .
	qualifier : bla
	number : 15
```
  
this logic during metrics allows the following sorting result:

```
1.0-myQualifier5-1a.bla15 < 1.0-myQualifier6-1a.bla15 < 1.0-myQualifier6-1a.bla16 < 1.0-myQualifier6-2a.bla15 < 1.0-myQualifier7-1a.bla15
```

i.e. the comparison is done within the non-conform sequences from left to right


and - as a result of 'a' being well known:

```
1.0-myQualifier5-1a.bla15 == 1.0-myQualifier5-1alpha.bla15
```
 
### the pc qualifier

The qualifier 'pc' is exclusive to our build system. It stands for 'publishing candidate' and is rated just below the actual version, so

```
<v>-pc < <v>
```

so as an example

```
1.1.1-pc < 1.1.1 < 1.1.2-pc < 1.1.2 < 1.1.3-pc < 1.1.3
```

We might want to switch to 'rc' or 'cr' in order to increase Maven compatibility. For now, rc, cr and pc are equivalent.


## diminutive classifiers and their impact
You could say that all the classifiers that make a version with the classifier smaller than the same version without a classifier is a monad or rather simply said: the highest possible version still smaller that the actual version. 

As a consequence we never start with a 1.0.0-pc, because this version would definitively be outside a range like [1.0,1.1), because 1.0.0-pc is smaller than 1.0. To address that, all our first versions of an artifact are 1.0.1-pc and once published will be 1.0.1.


## special version ranges 
A version range normally has two boundaries, a lower one and a upper one. Both can be declared to be inclusive/exclusive, i.e. whether a match on a boundary is to be treated as inside or outside the range. 
There is however a case where one or both boundaries are not specified.  

So if you simply use 

``` java
	VersionRange range = VersionRange.T.create();
```

you get a valid version range, but a 'wide open' one, so that any version will match it. 

If you only specify one of the boundaries, only the existing boundary will be tested, so if it's the lower one, a version lower than the boundary won't match, any version higher however will. The same obviously is correct of the upper boundary respectively.

You can use the parsing feature of the range to create such ranges:

``` java
	VersionRange wideOpenRange = VersionRange.parse("[,]");
	VersionRange lowerOpenRange = VersionRange.parse("[,2.0]");
	VersionRange upperOpenRange = VersionRange.parse("[1.1,]");
```


## 'well known version ranges'
The are some ranges that we used frequently, hence the name 'well known'. 

The 'standard range' is a range that opens on the major/minor level.

	[1.0, 1.1)

The range's lower bound is 

	anything higher or exactly 1.0 

and the upper bound 

	anything less than 1.1
	
You'll find this range quite often in the poms of our artifacts. The idea is that it remains stable on major and minor, yet is flexible on the revision. The basic idea is that within the same major/minor sequence, the contract of the artifact may not be changed.

Just as a rule of thumb :

	change in major: everything is permitted
	change in minor: no breaking changes, yet new contracts may be added
	CompiledDependencyIdentification dep = CompiledDependencyIdentification.parseAndRangify(dependencyAsStr);


So in our realm, a range across the revisions is absolutely fine. 

You can directly turn a dependency declaration into a dependency declaration with a 'standard range' using the [CompiledDependencyIdentification](javadoc:com.braintribe.model.artifact.compiled.CompiledDependencyIdentification) via its 'parseAndRangify' functions

``` java
	String declaration = ...
	CompiledDependencyIdentification dep = CompiledDependencyIdentification.parseAndRangify(declaration);
```
This call will automatically create a dependency identification with a 'standard range'.


The [VersionRange](javadoc:com.braintribe.model.version.VersionRange) also has features required to build such a standard range:

``` java
	Version v = ...
	VersionRange standardRange = Version.toStandardRange( v);
```

A 'narrow range' differs from the standard range by the different level of declaration on the lower bound of the range. While a 'standard range' is only using a lower bound of major/minor without any revision (defaulting to 0), the lower bound of a 'narrow range' comprises the revision.
A simple example would be something like this :

	[1.0.15, 1.1)

Such 'narrow range' is way less frequent, but appears in some places, especially when 'direct'-versioned dependencies are used. Such dependencies for instance can be found in the 'tribefire services' debug projects. 

A direct version declaration will simply have a version such as 

	1.0.15

or even

	1.0.16-pc


The use of such declarations is also absolutely valid, but there are two problems with such declarations:

- The revision is not stable and some revisions may disappear from our repositories. This is simply due the fact that our scheme of using 'publishing canidates' rather that 'snapshot' as propagated by Maven, the point simply being that we never want to have the situation that an artifact may have differing content even if its coordinates (groupId, artifactId, version) haven't changed. Granted, snapshot are (at least when deployed) identifiable, but it's quite a denormalization and requires special logic. Anyhow, using the 'pc' scheme, everything is normalized - yet it leads to a quite big number of artifacts, so regularily, the repositories are pruned of superfluous artifacts. In most cases, this is not a problem as the 'tribefire services' debug project do not have a long life time and hence the state of the repositories when created rarely differs from the one when run.

- The 'publishing scheme' also has the consequence that a published artifact and its source <b>NEVER</b> share the same version. The source will <b>ALWAYS</b> be a 'pc' version, whereas the published artifact will <b>NEVER</b> be a 'pc' version. This of course means that the match to sources will never work with the version expression. This of course happens in our Eclipse plugins that are intended to be able to substitute any dependency reference to a jar with a reference to a project in workspace. So the pertinent plugins will first search for a direct matching the workspace, and if none is found, create a 'narrow range' from the dependency's version and then searches for the best possible match in the workspace. This ensures that - if rule of thumb has been broken and contract changes have been introduced whatsoever - the matching version must at least be higher (hence its artifact more recent) than the originally requested version.

The 'narrow range', being less frequent, only has support via the [VersionRange](javadoc:com.braintribe.model.version.VersionRange).

``` java
	Version v = ...
	VersionRange standardRange = Version.toNarrowRange( v);
```

## strange version constructs
Out in the wild, quite some people have strange ideas what a version should be. 

Just some examples here : 

```
    curvesapi#01.6
    jtidy#0r938
    listenablefuture#9999.0-emptyto-avoid-conflict-with-guava
    jboss-cache#200504122039
```

Mostly, they do not pose a problem to the metrics, but they do pose a problem when it comes to building an address for such artifacts. While Maven seems to hold the version simply as a string, and when metrics are required converts them to temporary instance of a different class representing the version, mc-ng directly uses a representation of the version that is inherently capable of doing metrics. Therefore, the versions are parsed into the 'smart' representation, and - if required - build the version's string representation (the address) on the fly. 
Sometimes, the two string - the original and the one produced - do not match. In order to overcome that, the version actually retains the string passed to the parser if it finds that the parsed data's string representation doesn't correspond the string parsed, and in that case, the retained string is used as the string representation.



The last example in this list however is a different issue. The major version declared is not a valid integer. The version addresses it in such a way that it sets the major to 0, and the string to the 'non-conform' part. In that manner, metrics of versions in the same pattern are fully supported. 

So this is possible

```
     jboss-cache#200504112039 > jboss-cache#200504122039 >  jboss-cache#200504122040
```

However, if compared to a 'standard' version, this would happen :

```
     jboss-cache#200504122039 > jboss-cache#1.0
```

Not an ideal situation, but the best that can be made of this situation. Hopefully, not many developers decide on a whim to use a time-stamp as version and then later decide against it again, all within the same artifact.     

## leading zeros in major/minor/revision
Internally, the numeric parts of a version are represented as Integer. Therefore, leading zeros are obviously inconsequential while version comparison. The are however relevant in deducing the location of an artifact within a repository, local or remote. While mc-ng is internally only using the integers, it detects leading zeros during string parsing and will take this into account when in turn required to produce a string representation. Still, the use of leading zeros is strongly discouraged.