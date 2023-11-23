# Ravenhurst

Ravenhurst (or RH) is a servlet to make 'dumb Maven' repositories smarter. 

For any attached repository, Ravenhurst does the following for us: 

- It can tell us what artifacts are stored in the repository
- It can tell us what changes happened since we visited it lastef

That doesn't sound like a big deal, but it's definitively way more that you can get from the remote repositories, even 'smarter' ones like artifactory.

The servlet itself is not topic of this configuration, but some more info here is in order:

- RH is a servlet installed on the same servlet-container as the remote repository
- RH ties into the internal database of the repository
- RH exists (or did exist) for two repository implementations : Archive and Artifactory
- mc-legacy and mc-ng both can comunicate with RH

## features built on Ravenhurst

RH allows us to interact with the supported remote repositories in a smarter way. 

### filtering
We can use its data to build up an index of what groups exist in a specific repository, so we can filter-out repositories that we know would not be able to deliver an artifact of group not present in the index.


>For instance, the 'third-party' repository doesn't contain a single group starting 'com.braintribe', hence this repository is never accessed searching for an artifact of this group.


### dynamic update policy 
Standard 'Maven-style' update policies (or rather : the point-in-time when to regard the local indices as stale) are quite restricted. 

Basically, you can specify three different values for that in your Maven settings.xml:

- never : the indices never go stale once present.
- always : the indices are alway regarded as stale and every access rebuilds them.
- interval : the indices are regarded as stale when the interval has elapsed.

RH allows for an more powerful way to manage indices:

- dynamic : the indices go stale when the remote content they are indexing have changed.

You can read-up on how you configure mc-core in its proper documentation.

## communicating with RH
RH as a servlet is listing on HTTP requests.

It's answering URL is not configured, but actually returned by the supporting repositories (see probing). It will look something like this.

```
https://artifactory.example.com:443/Ravenhurst/rest/devrock/changes
```

### getting a full dump
You can use this 'plain vanilla' URL to get RH to cough-up a full list of all artifacts in the repository. It will return a text file with a comma-delimited list of all artifacts, like this 

``` 
    <groupId>:<artifactId>#<version>[/n<groupId>:<artifactId>#<version>][..]
```

More information about how the group-data is processed can be found in mc-core's documentation.


### getting a 'changes dump'
In order to get a 'delta' we must of course specify the criteria, so we must add the instance of our last access. 


>Please note that it's that database of the remote repository that marks its entries reflecting artifacts whenever an artifact has been changed. This is not handled by RH which only issues a query in the pertinent database and returns the result


The changes URL is accessed with the *timestamp* parameter, the date passed as value needs to be URL escaped and follow this format:
 
```
yyyy-MM-dd'T'HH:mm:ss.SSSZ
````

before URL encoding

```
http://localhost:8080/archiveA/rest/changes?timestamp=2020-03-24T13:24:13.100+0100
```

after URL encoding

```
http://localhost:8080/archiveA/rest/changes?timestamp=2020-03-24T13%3A24%3A13.100%2B0100
```
In this case, it is expected that only the qualified artifacts are returned that have been *touched* after this sent date.

Again, it will return a text file with a comma-delimited list of the artifacts that have been changed since the last access, like this 

``` 
    <groupId>:<artifactId>#<version>[/n<groupId>:<artifactId>#<version>][..]
```

## persistence
The last successful synchronization time stamp is written - per repository into a single YAML file,

```
<local repository>/last-changes-access-<repositoryId>.yaml
```
and contains a GenericEntity

```
!com.braintribe.model.artifact.changes.ArtifactChanges
lastSynchronization: !!timestamp 2020-03-25T14:26:39.708Z
```

>*NOTE* : if you have an issue with the synchronization, i.e. if somehow some RH synchronization messages have been missed, you can revert the date in these YAML files to the last date you are sure that it still worked, and have the synchronization redone.