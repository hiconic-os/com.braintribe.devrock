# POM in mc-core

mc-core supports POM somewhat differently than Maven does.

First of all, mc-core's pom compiler supports variable everywhere in the pom - inclusively within version specifications. Secondly, it also supports YAML syntax in some properties that Maven doesn't know of.

On the other hand, mc-core ignores some features of the POM that Maven uses. The basic difference is that we use POMs only to identify an artifact and to declare its dependencies. We do not use the POM to build that artifact, i.e. there is no build information within the POM. mc-core can read all these unsupported tags and sections of a POM but don't support any of these features.


## variables

This is a valid pom when it comes to mc-core :

``` xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.braintribe.devrock.test</groupId>
        <artifactId>parent</artifactId>
        <version>[${major}.${minor},${major}.${nextMinor})</version>
    </parent>
    <artifactId>artifact</artifactId>
    <version>${major}.${minor}</version>
    <properties>
        <major>1</major>
        <minor>0</minor>
        <nextMinor>1</nextMinor>
        <overwrite>true</overwrite>               
    </properties>
    <dependencies>
        <dependency>
            <groupId>com.braintribe.devrock.test</groupId>
            <artifactId>a</artifactId>
            <version>${V.com.braintribe.devrock.test}</version>
        </dependency>    
		<dependency>
            <groupId>com.braintribe.devrock.test</groupId>
            <artifactId>b</artifactId>
        </dependency>                     
		<dependency>
            <groupId>com.braintribe.devrock.test</groupId>
            <artifactId>c</artifactId>           
            <classifier>classifier</classifier>          
        </dependency>
    </dependencies>    
</project>
```

Maven cannot read this POM, as the usage of variables in both the version tags of the artifact itself and in the parent reference is not supported. The use of variables in the version tag of a dependency declaration (both in the _dependencies_ section and the _dependencyManagement_ section) is however supported by Maven.

Our build system handles this discrepancy by making sure that published artifacts only have _resolved_ version tags, i.e. both the version tags of the artifact itself and the parent references are resolved and their current value are used.

So in our case, the _published_ aka _installed_ aka _deployed_ POM will look like this :

``` xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.braintribe.devrock.test</groupId>
        <artifactId>parent</artifactId>
        <version>[1.0,1.1)</version>
    </parent>
    <artifactId>artifact</artifactId>
    <version>1.0</version>
    <properties>
        <major>1</major>
        <minor>0</minor>
        <nextMinor>1</nextMinor>
        <overwrite>true</overwrite>               
    </properties>
    <dependencies>
        <dependency>
            <groupId>com.braintribe.devrock.test</groupId>
            <artifactId>a</artifactId>
            <version>${V.com.braintribe.devrock.test}</version>
        </dependency>    
		<dependency>
            <groupId>com.braintribe.devrock.test</groupId>
            <artifactId>b</artifactId>
        </dependency>                     
		<dependency>
            <groupId>com.braintribe.devrock.test</groupId>
            <artifactId>c</artifactId>           
            <classifier>classifier</classifier>          
        </dependency>
    </dependencies>    
</project>
```


## YAML properties

mc-core supports some special properties (and features) that Maven doesn't. Instead of defining a specialized format and syntax that needs to be parsed explicitly, we decided to use YAML syntax.


### artifact-redirects
In some cases, you want to redirect artifact references, i.e. you want to change a dependency pointing to artifact A automatically to artifact B. In Maven, you can do that by changing the POM of A to redirect to B. However, that means that A will always reroute to B.

In most cases - and especially in our case - this is not really what you want to achieve, but rather that in some circumstances, not A but rather B is to be used. And the place to decide that, is obviously not in A, but a consumer of A, i.e. a terminal.

To achieve that, we introduced the _artifact-redirects_ property.

```yaml
<artifact-redirects>
	 {
           "com.braintribe.model:gm-core-api#[1.0,1.1)": "tribefire.cortex.gwt:gm-core-api#[1.0,1.1)",
           "com.braintribe.model:gm-core-impl#[1.0,1.1)": "tribefire.cortex.gwt:gm-core-impl#[1.0,1.1)"
	 }
```

This now declares that in the dependency tree branches below this artifact, any reference to the two gm-core-* artifacts are to redirected from the group _com.braintribe-gm_ to the group _tribefire.cortex.gwt_. Transitive resolvers (dependency tree traversers or dependency walkers) will respect these redirections.

As you can see, it's a simple YAML notation of a String -> String map.

### global-exclusions
Exclusions are a common pattern to resolve certain issues with dependencies. Again, Maven supports that in a way, but - again too - would require you the introduce 'fake' dependencies into your terminal: you'd have to add an actually unneeded dependency to carry the exclusions you want to influence a dependency somewhere in the dependency tree.

To be able to declare that in the terminal without interfering with the actual dependencies, we introduced the _global_exclusions_ property.

``` yaml
<global-exclusions>
		[
           "com.braintribe.model:platform-api",
           "com.braintribe.model:*"
    ]
	</global-exclusions>
```

This now declares that any reference to either any version of _com.braintribe.model:platform-api_ or any artifact of the group _com.braintribe.model_ are automatically to be excluded, no matter where they do appear within the dependency tree.

As you can see, it's a simple list of condensed groupId and artifactId pairs, where you can use wildcards.


### global-dominants
Clash resolving is a big issue in complex dependency trees. Running an 'first come wins' strategy like Maven (aka _adhoc_ clash resolving) allows you to introduce your dominants by changing the order of dependencies in your dependency tree, but that firstly introduces again 'fake' dependencies, and secondly doesn't work in the optimistic (aka _posthoc_) clash resolving as in Gradle for instance. mc-legacy supports both manners (with the former implemented as true _adhoc_ clash resolving - clash resolving at the time of dependency detection, the latter _posthoc_ - clash resolving after the full dependency tree has been traversed). Mc-core also supports both manners, but both done _posthoc_, i.e after the full dependency tree has been traversed.

To be able to influence how clashes are resolved with neither changing the order of dependencies nor introducing 'fake' dependencies, we introduced the _global_dominants_ property.

```yml
<global-dominants>
	 [
           "com.braintribe.model:platform-api#2.0",
           "com.braintribe.model:common-api#2.0"
	 ]
	</global-dominants>
```

This now declares that during clash resolving of any versions of the artifact _com.braintribe.model:platform-api_, the version _2.0_ wins - not matter which came first or which version would be the highest one.

Again, it's a simple array of condensed artifact names.

## POM compiler 

needs text about the leniency levels of the compiler, and how it communicates to the caller, 'HasFailure ?? or boolean && whyInvalid?? 
