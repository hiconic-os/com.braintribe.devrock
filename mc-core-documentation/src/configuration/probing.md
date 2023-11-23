# repository probing

Repository probing is the term we coined when mc-core is actually accessing a remote repository (other than downloading). Partly simply only to find out whether the repository is accessible, but also to check for the feature set the remote repository sports (such as Ravenhurst implementation, REST support, existence of artifacts and their parts).

In the default implementation, probing is done by issuing an HttpHead request on the basic URL of the configured repository.

You can override this by specifying both *ProbingMethod* (either HttpHead, HttpOptions or HttpGet) and *ProbingPath* in a Repository of a RepositoryConfiguration. If you do not override it, *HttpHead* is used as the *ProbingMethod*, and the *root URL* of the repository is used to make the probing. See the part about Maven central in the documentation about the [Maven configuration](./mavenconfiguration.md).


## dynamic properties
There are two dynamic properties that repository-probing retrieves from a remote repository:

### changes URL
The changes-url (formerly configured in settings.xml as [ravenhurst](asset://com.braintribe.devrock:mc-ng-principles/principles/ravenhurst.md)-url in mc-legacy) is now interactively detected by mc-core. At start-up, it will access the remote repository to retrieve this.

You can however override the value in a Repository from the RepositoryConfiguration, for instance by declaring in the [YAML section of the settings.xml](./mavenconfiguration.md).

The changes URL is of course specific to the remote repository's implementation. In our case, with our artifactory instance, we run a servlet that accesses artifactory's database to query for changed artifacts after a given date.

Let's assume that the URL to query for changes is like this:
```
http://localhost:8080/archiveA/rest/changes
```

The requirements on the remote repository's side are quite limited. What is needed that it supports the following rules:

#### full dump

if the changes URL is accessed without a *timestamp* parameter, it is expected to return a UTF-8 text output of new-line delimited condensed (qualified) artifact identifications, i.e.

```
<groupId>:<artifactId>#<version>
```

The URL in this case can be sent just as it is :

```
http://localhost:8080/archiveA/rest/changes
```

#### changes dump
 the changes URL is accessed with the *timestamp* parameter, the date passed as value needs to be URL escaped and follow this format:
 
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


#### persistence
The last successful synchronization time stamp is written - per repository into a single YAML file,

```
<local repository>/last-changes-access-<repositoryId>.yaml
```
and contains a GenericEntity

```
!com.braintribe.model.artifact.changes.ArtifactChanges
lastSynchronization: !!timestamp 2020-03-25T14:26:39.708Z
```

*NOTE* : if you have an issue with the synchronization, i.e. if somehow some RH synchronization messages have been missed, you can revert the date in these YAML files to the last date you are sure that it still worked, and have the synchronization redone.

#### processing

Two things are done while processing the dumped data.
 - a group-index filter is maintained, i.e in order to speed-up accesses, a group-based filter allows mc-core to know beforehand whether a repository can contain an artifact of a given group. More about the group-index filter can be found [here](./filtering.md).
 - the respective index files in the local repository are flagged as being outdated (by creating a file with the same name, but with the suffix '.outdated' attached. 

##### updating local index profiles
Differing from mc-legacy, mc-core doesn't delete outdated files, it actually marks them as being outdated. The marker acts just as an exceeded duration, i.e. any marked file is considered to be out of date as if too much time had elapsed since its last update.

Depending on the type of marked file and the state of accessibility of a remote repository the following happens:

- maven-metadata.xml : These files, if associated with a repository, are updated upon access, *if* the repository is accessible. If not, the files are not updated and the current content is used for resolving.

- part-availability.txt files: These files are automatically updated by rewriting them so that any previously positive entry for a part are retained, all negative entries are removed.
- part-availablity.json files :  are updated upon access, *if* the repository is accessible. If not, the files are not updated and the current content is used for resolving.

- .solution : part of mc-legacy's index files, will be deleted as before.

After a successful update of a file, the marker file is removed. 

### REST support

REST  support is currently restricted to artifactory's feature set. If probing detects an artifactory running the remote repository, it will activate the REST support for this repository. The big difference is that the part-availability file is always complete, i.e. always reflects the contains of the remoted repository, whereas the standard part-availability file only reflects the deduced knowledge about the contents of the remote repository, i.e. is incomplete and grows over time. See [here](../resolving/parts.md) for details.

## error processing
Probing can return a probing status. 

Possible values and their underlying HTTP codes are :
 - available : 200,204
 - unavailable : 404
 - unauthorized : 401 
 - unauthenticated : 403
 - erroneous  : any other code

If a repository does have any issues (i.e. any status other than available), it will be filtered by the 'offline leniencies'. If the status is considered as problematic, the repository will be flagged with the failure reason that shows the state. 
If the overall repository-configuration enriching process finds any repository that is flagged as failed, it will flag the reposiory-configuration itself to be failed.
Depending on the consumer, they will throw a ReasonException.



