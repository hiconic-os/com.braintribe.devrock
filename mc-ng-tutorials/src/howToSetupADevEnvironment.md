# how to setup a dev environment
The 'dev environment' is nothing more than a structure setup to facilitate the usage of our rather wide-spread codebase. The idea is that one tries to avoid to manage too many sources and projects in the same structure and allow simple branching based on locally checked-out sources. 

In other words, the idea is that you - for that task at hand - generate yourself a new dev-environment that contains only the sources you need to handle the task. For now, you have to create that dev-environment manually, but eventually, a Jinni request will be available. 

Please note that you do not need to create a dev-environment - the mc-ng tooling enables you to use one but doesn't force you to. You can still keep on working in your favorite way. But the reality is that the more sources you managed, the more projects you have in a workspace, the slower the tooling can perform. And too many sources will force you to jiggle all different aspects with git whenever work needs to be done on more than one topic in the same sources. 

So yes, using a dev-env structure is reconmended. 

If you want to know more about the details of the dev-environment, the more technical aspects are explained [here](asset://com.braintribe.devrock:mc-core-documentation/configuration/devenvironment.md).

## setting up the structure 
As mentioned above, your dev-environment is simply a directory structure that follows a small set of rules. Accordingly, setting it up is quite easy.

### create a folder for the environment
The first step is to create a dev-env folder. It is the root of all data to be contained there and will contain your sources, workspaces and (both optionally) your configuration data and even your artifact cache (aka 'local repository).

It will make sense to give it a name to remember it by, for instance the id of a story ticket is quite useful.

So for instance, while I'm writing this, the dev-env-root for me is 'COREDR-10', the story ticket for the mc-ng development. But you can use any name, it just has be a valid name for a directory. 

### create a marker file (dev-environment.yaml)
The folder itself is not enough for mc-core to detect the dev-env structure. It requires a marker file (currently it acts only as a marker, but might be expanded to contain more configuration data)

```
    <dev-env-root>/dev-environment.yaml
```
### create a 'git' folder
The dev-environment has a predefined place to put your sources, i.e. just check-out the sources below it. 

```
    <dev-env-root>/git
```

So it will look like this for instance

```
    <dev-env-root>
        git
            com.braintribe.devrock
            com.braintribe.gm
```

>You can of course use symbolic links, so you could link the sources of multiple dev-envs. 

### create an 'artifacts' folder 

```
    <dev-env-root>
        git
        artifacts
```

If you are using the YAML based configuration feature that the new mc-ng tooling supports, you can simply put it into this directory

```
    <dev-env-root>
        ...
        artifacts
            repository-configuration.yaml
```

The repository-configuration file placed there is a simple YAML file that [configures](asset://com.braintribe.devrock:mc-core-documentation/configuration/configuration.md) the mc-core. 

Please note that you do not have to place that file there and that you don't even have to use the YAML file. There are lots of ways to [configure](asset://com.braintribe.devrock:mc-core-documentation/configuration/configuration.md) it, you can perfectly still use a settings.xml at the usual places.

## placing your Eclipse workspace 
In most cases, you'll have at least one workspace linked to the dev-env. Just created within the dev-env structure 

```
    <dev-env-root>
        ... 
        <my workspace one>
        <my workspace two>
```

The devrock plugins can detect your dev-env and will configure themselves accordingly. See below.


## linking multiple dev-env which each others
While the idea of the dev-env is to enable to work undisturbed on one or several related tickets (or tasks or setup or whatever), you still might want to share parts on your dev-env with other dev-envs. 

Of course, you can keep the configuration and the artifact cache (aka local repository) outside of the dev-env as you are not forced to subsume these files and directories under this approach. 

But you still might want to keep it together in he dev-envs and still share parts. 

### symbolic links on git directories
If you want to share some sources of one of your dev-env rather than have it checked-out again in another, you can simply put 'symbolic links' into your 'git' folder. The tooling will not see any difference. Of course, any changes you do while working on one source will reflect on the other. Simples!


### symbolic link in repository configuration
You can share the cache (aka the local repository) across several dev-env while still using different configurations. 

The idea is that you can keep the configuration seperate (different users, different remote repositories), but still share the cache. 

```java
    !com.braintribe.devrock.model.repository.RepositoryConfiguration {        
	    localRepositoryPath: "${code.base}/cache"
	    repositories: [
	        !com.braintribe.devrock.model.repository.MavenHttpRepository {
	            name: "remote-repo",
	            user: "remote-repo-user"
	            password: "remote-repo-pwd",
	            url: "http://remote-repo/contents"
	        },
	    ],
    }            
```

The expression shown here uses the single inbuilt variable 'code.base'. It points always to the directory where the file containing the configuration resides. So given that 

```
    <dev-env-root>
        ...
        artifacts
            cache 
            repository-configuration.yaml
```

If you now setup 'cache' to be a symbolic link pointing somewhere outside the dev-env, bob's your uncle.

## integration into tooling
All 'consumers' of the new mc-core obviously understand the dev-env approach. So they will find their configuration depending on where your current directory (or workspace) lies (more about the [dev-env configuration logic](asset://com.braintribe.devrock:mc-core-documentation/configuration/devenvironment.md) is here).

### devrock-ant-tasks
When run in one of the directories below git, it will step up to find the marker file. If it finds it, it will consider the configuration to be 'dev-env'-based and proceed along these lines. 

### Jinni
The same applies here. 

### Devrock plugins
The devrock plugins are dependent on the place of the workspace rather than the source directories. But the principle is the same.

## origination
Obviously, having all these different places where you can configure your setup can make it rather complex to follow it through its moves. 
Therefore, a repository-configuration contains (once it is processed and ready to be consumed) information about how it came into being. This is called [origination](asset://com.braintribe.devrock:mc-core-documentation/configuration/origination.md). 

Currently, devrock-ant-tasks will dump the current repository-configuration if it encounters an error during a process that requires the configuration. 

Also the devrock plugin shows it in its preferences.

```
    preferences->devrock->repository configuration
```


