# footprint 

Of course, mc-ng also caches data locally. Some of it are maven compatible (the local repository for instance), but some data is cached that Maven never dreamed of. 

Follows a description of the persisted files 

## standard maven files 

The standard maven files are the maven-metadata-*.xml files, both first-level and second-level. 

Basically, their name is linked to the repository they stand for:

```
    maven-metadata-<repositoryId>.xml
```
the local repository (which is actually a bad name for it, as it's a) a cache and b) a repository in its own right, while puts a dent into SOC) has also metadata files

```
     maven-metadata-local.xml
```

The naming (other than 'local') is defined by your configuration (which should give you a hint that you shouldn't juggle with the repository ids in the configuration while expecting to work with a pre-existing local repository). In our world, you'd see files like this:

```
    maven-metadata-third-party.xml
    maven-metadata-core-dev.xml
```


### first level

First level metadata appear in the unversioned directory of an artifact, i.e. one directory above the directories of the actual versions, as a sibling to these directories. 

First level metadata are used as an index to the different versions available on a remote repository. They are used (actually required) if version ranges need to be resolved. If a version doesn't show up in a maven-metadata file, the pertinent repository doesn't contain the version (at least that's how it's treated).

Mc-ng acts like Maven in that respect: if you resolve a direct version (say 1.0.1), not maven-metadata all consulted (and pulled if not present), and only if you resolve a range (say [1.0,1.1)) then mc-ng will consult the maven-metadata files.


### second level 
Second level metadata appear in a versioned directory on an artifact, i.e. they are siblings to the pom file for instance.

Second level metadata are in most cases superfluous. Differing from mc-legacy, mc-ng doesn't pull theses files. If you see them, then they are most probably produced by older style toolings based on mc-legacy.

There is only one reason to deal with second-level maven-metadata : SNAPSHOTs

While we do support Maven's SNAPSHOTs we do not use it internally - our 'publishing candidate scheme' seems to be a way better match for us. Suffice to say here that you cannot resolve the proper parts of a SNAPSHOT without these second-level metadata.


## mc-core files 
Mc-ng caches more data as Maven does as we trimmed it for speed. Some principles are completely unknown to Maven.

### group index
The group-index is  used as a fast lookup during traversing: if a repository has such a file (they are based on RH's notifications and therefore purely our breed), and the groupId of the artifact to resolve isn't listed, it will not be accessed, so no version- and existence-checks - which may lead to HTTP requests) need to happen. The files are automatically updated when RH notifications are processed. The data is automatically added to the filtering active in mc-core's configuration.


The group index files can be found in the root of your local repository. 

```
    <local-repository>/group-index-<repositoryId>.txt
```

The format is a line-delimited list of groupIds.

```
    <groupId>[\n<groupId>]..
```

>NOTE: the file may be deleted any time. It will be recreated if not present. In that moment, a RH request is sent that triggers a full list of artifacts in the repository. 

## last-changes-access 
These files contain the date of the last successful RH interaction. It is used to synchronize the state of the cache with the state of remote repository. 

```
    <local-repository>/last-changes-access-<repositoryId>.yaml
```

It is a dump of com.braintribe.model.artifact.changes.[ArtifactChanges](javadoc:com.braintribe.model.artifact.changes.ArtifactChanges).

NOTE: the file may be deleted any time. It will be recreated if not present. In that moment, a RH request is sent that triggers a full list of artifacts in the repository. This means that any existing cache file of these artifacts will be marked as obsolete, and the next access to one of these artifacts will update the chache files automatically (note: the caches of the index files, not the actual files, jar, pom et al). If present, the time-stamp found is used to get RH to report changes after that moment. 


### probing results
As mentioned in the text about probing, the configuration is enriched with live data that mc-core gets from the hosts of the remote repository. These findings need to be persisted so that during a offline situation mc-core at least can know what the classification of the now offline repositories were. 

Again, the files can be found in your local repository 

```
    <local-repository>/last-probing-result-<repositoryId>.yaml
```
### part-availability 
The part-availability files are indices that reflect what parts of an artifact are available in a certain repository. The files are found amongst the parts of an artifact.

## part availability

The part availability feature make sure that only as few as absolutely necessary round-trips to a repository are required while resolving parts of an artifact. Contrary to Maven that doesn't have such a concept and will always try (and try and try and try) to download the same file over and over (and over and over) if it cannot be resolved, this feature notes what files it could or couldn't download. In the latter case, it will not try again (unless triggered by RH that changes happened on the remote repo).


### standard part availability

The standard part availability file contains potentially incomplete information about the contents of an artifact folder, i.e. a list of part that an artifact consists of. It is potentially incomplete as it reflects the discovery process, that means if a part is never requested, no information about it is contained - see the special case of REST API driven part availability.

```
part-availablity-<repo>.txt
```

using the following syntax, each entry delimited by a new line character.

```
(+|-)[classifier:]type
```

```
<UUID>
+pom
+jar
-sources:jar
-javadoc:jar
```

A part-availability file will be cleared when the respective maven-metadata file is cleared (RH delivered an out-dated notification of the artifact).

If the file doesn't exist (never existed or cleared), it is written with an UUID in the first line. By means of this, the corresponding part-availablity-access can know whether it's his file (written or read last time), and whether it can modify the file as if the UUID has changed in the meantime, another copy of the access has taken over (perhaps cleared and rewritten the file) and it may not change the file.


### REST driven part availability

If the repository supports REST access - as our artifactory does - mc-core will rather use the REST API to extract the availability information in one single query than to interactively update the locally stored information.
The files obtained via the REST API are named differently,

```
part-availablity-<repo>.artifactory.json
```

Their content is expressed as JSON, and is reflected by the

```
com.braintribe.devrock:artifactory-api-model#[1.0,1.1)
```

It is always complete, i.e. it contains the full list of parts the artifact had at the time of creation.

