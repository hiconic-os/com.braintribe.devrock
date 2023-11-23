# filtering 

Filtering is a concept introduced to better control what artifacts are taken while resolving during a packaging of a release of one of our terminals, like tribefire itself for instance. The thing is that we do use ranges in our dependency declarations which has of course the advantage that we are very agile when it comes to update an artifact. It has however the draw-back that - of course as the range stipulates - always the highest available version of a matching artifact is taken.

So - as a consequence - if we would want to influence how these ranges are resolved, we would need to limit the number of available versions. One way is of course to use separate remote repositories (one per release, or mix in several), but that requires quite a bit of disk space (and more costs). 

Our system for our 'publishing candidates' already pointed into the direction where we ventured now with the filters. The idea is that we *filter* during resolving what artifacts we want to have resolved and what versions that we want to use. 

As you can imagine, there's a model for the filters (actually, they're part of the content of the configuration model, as they are part of the configuration).

```
    com.braintribe.devrock:repository-configuration-model
```

The base filter is [ArtifactFilter](javadoc:com.braintribe.devrock.model.repository.ArtifactFilter), all filters derive from it. 

You can specify the filters directly with the [RepositorConfiguration](javadoc:com.braintribe.devrock.model.repository.RepositoryConfiguration), by means of attaching them to the [Repository](javadoc:com.braintribe.devrock.model.repository.Repository).

There are two ways to get the filters into mc-core by means of [configuration](./configuration.md). 
- attaching YAML code to the settings.xml if using the MavenConfigurationModule.
- injecting the repository configuration directly into mc-core as a replacement to the MavenConfigurationModule.


## pc_bias filtering
As mentioned above, even old malaclypse knew such a filter. The 'pc bias' filtering, which in its simplest form makes sure that the local content (i.e. the locally installed artifact in your local repository) takes precedence over remote content. 

The information is persisted in a little file:

```
    <local repository>/.pc_bias
```

### file format 
The format is simple: a line delimited listing of partial or fullly qualified artifact names. 

```
    <groupId>[:<artifactId>][;[!][<repoId>][,[!][<repoId>],..]
    [<groupId>[:<artifactId>]][;[!][<repoId>][,[!][<repoId>],..]
```

so expressions like this are valid:

```
    com.braintribe.gm.schemedxml
    com.braintribe.devrock:malaclypse;!third-party
    com.braintribe.devrock:mc-core.*;local
```


The line is split into two parts:
- the identification which consists of a groupId (which may contain regular expressions), and an artifactId (again with regular expressions). 
- a list of repository-ids, where an exclamation mark denotes a negation. 

So knowing that, the examples above are interpreted as this:


- com.braintribe.gm.schemedxml - any artifacts of that group are only taken from the local repository if they match the range.

- com.braintribe.devrock:malaclypse;!third-party - any matching artifact of the repository 'third-party' is not taken even if it matches the range.

- com.braintribe.devrock:mc-core.*;local - any artifact whose artifactId starts with 'mc-core' are taken from the local repository, if they match the range.


Note that while 'local' is the key word for the local repository, it can be omitted completely if only its name would appear.  So the two following lines are equivalent:

```
    com.braintribe.gm.schemedxml
    com.braintribe.gm.schemedxml;local
```

And also please note that as soon you start negating repositories, you'll need to actively allow the other repositories that you still want to access there. 


### integration into the filter system
While the pc-bias filtering is a legacy from 'malaclypse the older', it incorporates in to 'mc-ng', aka 'malaclypse the younger'. If filters and bias settings are used in conjunction, they are merged into the new filter system, i.e mc-ng automatically translates the bias into filters and merges with other filters.


## general filtering info

There are two different filters (aka predicates here) systems active in mc-ng. 

The first filter - the artifact filter - is a simple filter, i.e. it filters the values according its internal setup. It is used the modify the return values, so what it does is to control the content of a repository.

The second filter - the dominance filter - is a filter that controls how the results of a repository stand in *relation* to the result of other repositories. In this case, it means that if a repository is dominant, any results found within it will take precedence over all results found in all other repositories.

Obviously, the pc-bias filter is a a mix of the two filter schemes and - if declared accordingly - the bias information contained in the .pc_bias file is split across the two aspects: both dominance and result filter.


##  origination
The bias filtering does support the [origination scheme](./origination.md). That means the if such a bias file is found and processed, pertinent data is attached to the resulting repository configuration:

### bias file 
The [RepositoryBiasLoaded](javadoc:com.braintribe.devrock.model.mc.cfg.origination.RepositoryBiasLoaded) origination reason marks that the bias mechanism has been activated and points to the file read.

### bias on repository 
The [RepositoryBiasAdd](javadoc:com.braintribe.devrock.model.mc.cfg.origination.RepositoryBiasAdded) origination reason marks that a bias has been added to a specific repository and points to the name/id of the repository.


