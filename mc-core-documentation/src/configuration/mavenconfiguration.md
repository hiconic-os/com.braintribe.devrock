# Configuration via Maven

While mc-core tries to be Maven compatible, there are some limitations to that. Still, the support goes further than it did in mc-legacy. Follows are notes about some Maven issues.

## settings.xml

While mc-core internally has a decoupled configuration from Maven, it still can be configured using the standard means, the (two) settings.xml. Maven works with two settings.xml files, the installation file and the user file. Their locations are normed:

### installation settings.xml
```
${env.M2_HOME}/conf/settings.xml
```

### user settings.xml
```
${user.home}/.m2/settings.xml
```

If you use two settings.xml files, they are merged into one, with the user file being the dominant one, i.e. if settings contradict, the one defined in the user file takes precedence. Other values (such as profiles for instance) are added.

## using the MavenConfigurationWireModule

You can inject the Maven settings compiler into ANY mc-core's wirings that requires a repository configuration, in this example into the middle tier. 

``` java
WireContext<ArtifactDataResolverContract> resolverContext = Wire.contextBuilder(
          ArtifactDataResolverModule.INSTANCE,
          MavenConfigurationWireModule.INSTANCE)
        .build();

```

The MavenConfigurationWireModule injected here will build up a [RepositoryConfiguration](javadoc:com.braintribe.model.repository.RepositoryConfiguration) from found settings.xml.
There are several ways to influence where the settings.xml are loaded from.

In the example above, the environment variables mentioned below need to be defined in the shell mc-core is running in. If you want to programmatically overload them, you'll need to inject an OverrideableVirtualEnvironment, as in this example:

``` java
OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
```

``` java
WireContext<ArtifactDataResolverContract> resolverContext = Wire.contextBuilder( ArtifactDataResolverModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
  .bindContract(VirtualEnvironmentContract.class, () -> ove)				
  .build();
```

### using a single specific file

ARTIFACT\_REPOSITORIES\_EXCLUSIVE\_SETTINGS -> points to a single settings.xml

This environment variable is deemed to point to the single settings.xml you want to use. In case of the OverrideableVirtualEnvironment, you just specify it like this:

```
ove.addEnvironmentOverride( "ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", mySettings.getAbsolutePath());
```

### using two specific files

ARTIFACT\_REPOSITORIES\_USER\_SETTINGS -> points to the file to be used as the user file
ARTIFACT\_REPOSITORIES\_GLOBAL\_SETTINGS  -> points to the file to be used as the installation files

``` java
ove.addEnvironmentOverride( "ARTIFACT_REPOSITORIES_USER_SETTINGS", userSettings.getAbsolutePath());
ove.addEnvironmentOverride( "ARTIFACT_REPOSITORIES_GLOBAL_SETTINGS", installationSettings.getAbsolutePath());
```


## maven central

While we discourage to actually depend on Maven Central and suggest rather to use a well sanitized repository for 'third-party' artifacts for the obvious reasons, we do support the direct usage. The thing is that if you do *not* deactivate the access to Maven Central explicitly (rather use mirrors to redirect it), it is *always* active, disregarding whether you actually declared to use it your settings.xml. It's kind of a 'default', 'automatically' injected repository. 

Using Maven central however has consequences for our setup. As described in [dynamic configuration properties](./probing.md), some properties that you would have had to declare in the settings.xml with mc-legacy, are now automatically detected by mc-core. For instance, Ravenhurst support and REST support are such properties.

Furthermore, mc-core will check for the existence of artifacts and their parts not simply be trying to download it, but actually by doing some 'repository probing' which is much faster and carries less weight. You can configure all these settings, but unless you 'deactivate' the implicit use of Maven central (using a mirror to redirect it to an internal repository standing in for it), Maven central's reflection in the RepositoryConfiguration needs to be touched as it doesn't support the lightweight 'HttpHead' and 'HttpOptions' calls, but only 'HttpGet'.

This is done in several steps by the 'settings compiler':

1. the settings compiler determines how the combined settings.xml (in our lingo: the declared settings.xml) came into being: either resolved using the standard Maven logic or using the environment variables (see above). In the latter case, it is expected it doesn't need to support Maven Central. However, as in the former case, if the declared settings were loaded using the standard Maven way, it will do some post processing.

2. It will detect whether the configuration already contains a repository named 'central'. If not, it will inject it, with a standard URL, "https://repo1.maven.org/maven2"

3. After the mirror configuration has been applied, it will check whether the URL of the repository named 'central' is still pointing to "https://repo1.maven.org/maven2". If not, it has been redirected by a mirror, and nothing needs to be done. If it still points to the URL of maven central, the probing method and target of the respective parts of the RepositoryConfiguration are changed to reflect values that are compatible with Maven (_HttpGet_ as probing method, and _org/apache/maven/apache-maven/maven-metadata.xml_ as probing path).

## mixing settings.xml and RepositoryConfiguration with YAML
There are more possibilities to configure mc-core than a settings.xml can achieve. While we would actually want everybody to move to a direct modeled configuration (RepositoryConfiguration), we made it possible to enhance a settings.xml to be able to incorporate the additional configuration features.

This is done by injecting YAML code into a profile's properties. The property mc-core is looking for is called *mc-config*

``` xml
<settings>
    ... 
    <profile>
        <repository>
            <id>partially_declared_repo</id>
        </repositor>
        .... 
         <properties>
            <mc-config>
            !com.braintribe.devrock.model.repository.RepositoryConfiguration {        
                offline: 'true',                            
                repositories: [
                    !com.braintribe.devrock.model.repository.Repository {
                       name: 'added_repo',
                        user: 'added_user',
                        password: 'added_password',
                        url: 'added_url'
                    },
                    !com.braintribe.devrock.model.repository.Repository {
                        name: 'parially_declared_repo',
                        user: 'partially_declared_repo_user',
                        password: 'partially_declared_repo_password',
                        url: 'partially_declared_repo_url'
                    },                                                             
                ]
            }                
            </mc-config>
        </properties>    
    </profile>
    ....
</settings>            
```

This example shows two things. 

Firstly, it shows that you can add a repository. In this case, the repo is called *added_repo*. While it's not declared in the standard settings part, it's simply added to the active set of repositories.
Secondly. it also shows that you can actually override/complete a declaration from the standard settings part. In this case, the repository is called 'partially_declared_repo'. 

The basic logic is as follows: 
- if a repository declared via the mc-config properties is declared in the standard settings part, then the mc-config data is *added* to the existing declaration. Only the properties that you actually declared in the YAML expressions are transferred (only non-absentified properties).
- if a repository is only declared via the mc-config property, an instance of the repository is created using default values, and then the non-absent properties of the YAML expression are transferred. 


A perhaps better - and most probably actually used - example would be this, where [filters](./filtering.md) are declared. Filters are something Maven doesn't support and therefore has no means to declare them in the settings.xml

``` xml
<settings>
    ....
    <profiles>
        ...
        <profile>
            <id>default</id>
            <repositories>
            .... 
                <repository>
                    <id>archiveC</id>
                    ...
                </repository>                                   
            </repositories> 
            <properties>            
                <mc-config>
                        !com.braintribe.devrock.model.repository.RepositoryConfiguration {                                            
                            repositories: [                                
                                !com.braintribe.devrock.model.repository.Repository {
                                   name: 'archiveC',
                                    artifactFilter :  !com.braintribe.devrock.model.repository.NegationArtifactFilter {
                                        operand : !com.braintribe.devrock.model.repository.QualifiedArtifactFilter { 
                                            groupId : 'com.braintribe.devrock.test',
                                        }
                                    }
                                }                                                                                                                           
                            ]
                        }                
                </mc-config>            
            </properties>                       
        </profile>  
    </profiles>
    ....
</settings>    
```

In this example, the declared repository *archiveC* gets a [filter](./filtering.md) attached to it. 

PLEASE NOTE THAT THE FILE CONTAINING THE YAML CANNOT HAVE TABS. ANY WITHSPACE MUST BE SPACES, SO SET YOUR EDITOR TO REPLACE TABS WITH SPACES. 

## using an external file for the YAML (filter data)

As shown above, you can inject filters or additional repositories with the YAML formatted properties directly within the settings.xml. 
The MavenConfigurationModule also supports an additional way to inject that data: using an external file.

It reacts on the following environment variable: 

    DEVROCK_REPOSITORY_CONFIGURATION

If this is set and points to an existing file, it is read. The format the module expects here is also YAML.

The file itself must contain a RepositoryConfiguration, and the same applies as above with the difference, that merging happens only after a - potentially - existing YAML formatted property exists in a profile of the settings.xml.



## repository policies
If you want to use a remote repository for releases, you need to specify this. It doesn't have to be a complete definition, but it must be declared as enabled if you want it to be so. Omitting a repository policy automatically disables it.

So for instance :

``` xml
<repository>
  ...
  <releases>
    <enabled>true</enabled>
  </releases>
  ...
  </repository>
```

will activate this repository for *releases* and deactivate it for *snapshots*.

If you want to qualify the repository policy, you *can* specify also the policy regarding checksums and the update policy.

### checksums

A repository can have a policy regarding CRC checks.

``` xml
<repository>
  ...
  <releases>
    <enabled>true</enabled>
    <checksumPolicy>fail</checksumPolicy>
  </releases>
  ...
  </repository>
```

- fail : if a CRC mismatch is detected, an error is logged and a exception is thrown.
- warn: if a CRC mismatch is detected, a warning is logged.
- ignore : if a CRC mismatch is detected, .. nothing happens.

*default* value for the *checksumPolicy* is *ignore*

Once comment here: mc-core will *ALWAYS* check the CRC if it is supplied by the remote repository (directly with the HTTP headers). So only if the checksums aren't already present during download, mc-core will check the CRC settings. If set to *fail* or *warn* it will try to download the associated hash files from the remote, and then check it. For our case - using our artifactory - all that has no consequence as artifactory - as any modern maven repository that is access with http should do  - sends the hashes in the server. So even if you can set the checksum policy, in our environment, it is hard-wired to be *fail*.

### update policies

Basically, they are a few well known update policies (actually, Maven declares them and we are compatible):

- never : If a maven-metadata file is missing, it is retrieved. Otherwise, nothing happens.
- always : Each time the maven-metadata file is accessed, it is retrieved from the remote repository.
- daily : If the date of the maven-metadata file shows it older than one day, it is retrieved from the remote repository.  
- interval : If the date of the maven-metadata file shows it outside a given interval, it is retrieved from the remote repository.
- _dynamic_ : this virtual update policy is unique to us, and is determined by [probing](./probing.md). In order for this update policy to be enabled, you can either use the *default* value or specify *daily* for your maven based update policy.

In Maven, the interval case is specified in minutes, so for an interval of 15 minutes, it would look like this :

```
interval:15
```

The *default* value for update policies is *daily*.

So to sum it up, the two repository declarations below are equivalent:

``` xml
<repository>
  ...
  <releases>
    <enabled>true</enabled>
  </releases>
  ...
</repository>
<repository>
  ...
  <releases>
    <enabled>true</enabled>
    <checksumPolicy>ignore</checksumPolicy>
    <updatePolicy>daily</updatePolicy>
  </releases>
  ...
</repository>

```

## limitations

Are are trying to support any Maven antics no matter if we find them good, superfluous or crazy. Somethings however we do not support because they actually make the use of compiled artifacts in a cooperated environment impossible.

Most of these issues come from Maven's intrinsic blunder: distribution data and build data are intertwined into one single mess. So a published pom still contains information of how to build it - which is rubbish as it's already built when it shows up in a remote repository. 

Some cases are quite simple to illustrate: 

- the omission of group/version in an artifact and retrieving that from a parent    
This may be quite useful when you are working on the source of the artifacts, as you don't have to synchronize the versions across several artifacts (we are tainted here as we also use the parent to declare the major/minor parts of the versions to be used for groups). But once published to the repositories, the position of the artifact actually also identifies the artifact. So no matter what parent is referenced, the artifact's coordinates are fixed. In our view, pom.xml and .pom are two different things, the former being what you need to compile the artifact, the latter being what you need to use it as a pre-built part. In a .pom, no parent-reference is required, no 'dependency management' is required, and no variables are required : it is what it is and it has the dependencies that it has. There's no good reason to patch the referenced pom to some how change an *existing* artifact.


- the use of repository information within a pom    
Some Maven user do actually contain the distribution-management settings withhin the pom, i.e. the pom itself tells Maven where to get the newestr versions of the same artifact. While we think that we can follow the thinking of people doing that somewhat, we think it's a bad idea. Remember, that we are responsible of what we ship - or even only expose - to our customers. Therefore, we simply must control what the sources are of our artifacts. Hence, we cannot allow a distribution management sneaking in under the disguise of a simple pom. GF can trimm that, i.e. it will remove any repository declarations from within a pom, so that our configuration and ONLY our configuration is active (see above about the use of Maven central).
    
    
- the use of environment-dependent values within a pom  
Some Maven users like to use plugins or other means to inject platform-dependent settings into a pom. Using profiles within a pom and have them declare different values for variables depending on the operating system or the JDK used. While that makes sense while building the artifact, it doesn't make sense after it has been published. Again, we can understand where it's coming from, it implies that the environment of where the pom evaluation takes place actually defines the content of the pom. This of course breaks any chance to have a server prepare a solution for the client - unless they actually share the very same environment. Maven can switch profiles (in the settings.xml and actually even in the pom) depending on JDK and operating system - which the client could actually send to the server - but also the existence or non-existence of files or folder. Hidden in a third-party pom, knowing on what such a pom would react is a thing of impossibility.<p/>

The same applies for the plugin - identifiable by the use of ${tc-native} variables - that is used to automatically setup dependency classifiers. It leads to dependency trees being dependent on the operating system the process is running.

In short: in a world of artifacts - rather than monolithic applications - making the resolving process dependent on the environment of the process is a no-go.


Some limitations are however due the performance that we wanted.

- the scope of the dependency mediation
Differing from Maven, we only support the influence of a dependency management section to the direct parent/child chain. Any transitive dependency outside this chain remains untouched - they'll get what versions they requested. Reason here is actually that the parallel algorithm used to transitively resolve the dependency tree can reach the same transitive dependency via multiple differing sub-branches within a tree and the concept of 'first encounter' doesn't apply. Maven's traversing and ad-hoc clashing logic is based on a its synchronous traversion and their 'ad-hoc' clash-resolving, where as ours is based on asynchronous traversing and 'post-hoc' clash-resolving. While Maven only needs to traverse sub-branches it decides to follow, mc-core will traverse the complete tree, but being asynchronous it's still massively faster. 

    

##  origination

The maven configuration module also supports the [origination scheme](./origination.md), that means that the repository configuration compiled by it has data attached how it was compiled.
