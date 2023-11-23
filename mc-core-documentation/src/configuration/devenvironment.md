# configuration via development environment

This part here is rather technical and while it does tell you a lot on how a dev-environment looks like, there's a text in the [tutorials](asset://com.braintribe.devrock:mc-ng-tutorials/howToSetupADevEnvironment.md) section that is more of step-by-step explanation to set it up.

If you want to know more of the internal workings of the dev-env when it comes to configuration details, you came to the right place. 

The development environment setup is also done via a specific wiring of mc-core. 

Its job is - obviously - to find a valid configuration for mc-core. This is done with a concrete sequence of steps:

## look up logic in the dev environment

- check if there's a 'development environment root' and in its sub-directory 'artifact' a valid configuration file 'repository-configuration.yaml' exists.

    <root>/artifacts/repository-configuration.yaml

- check if the environment variable DEVROCK_REPOSITORY_CONFIGURATION is set and whether it points to a valid configuration file 'repository-configuration.yaml'

    ${env.DEVROCK_REPOSITORY_CONFIGURATION}

- check if there's a configuration file 'repository-configuration.yaml' in '${user.home}/.devrock and whether it's valid.

    ${user.home}/.devrock/repository-configuration.yaml

Note: the 'root' is defined by the different use-cases, i.e. it is injected dynamically be the different wirings of the tooling.

## fallback 
If an invalid configuration is found - or rather : a 'repository-configuration.yaml' is found, but cannot be processed properly - an exception is thrown. 

If no configuration files have been found, a automatic fall-back to the [module for the Maven configuration](./mavenconfiguration.md) is made.


## support for variables in the configuration
Variables - environment variables and system properties - can be accessed in the 'repository-configuration.yaml' file. 

As in Maven, the following rule applies:

- system property 
```
 ${<name of system property>}
 ```

- environment variable 
```
${env.<name of environment variable>}
```


```
Note that if you use variables on non-string values of the YAML, such as boolean, you need a specialized YAML marshaller. The tooling active here is using such a marshaller. So, even if it works here without any hiccups, if you are trying the same feature with a standard YAML marshaller, you are restricted to use only variables where *string values* are accepted.
```


### predefined variables
Predefined variables are injected by the system itself can will shadow any system property with the same name
 
    config.base - the is replaced by *directory* where the configuration file was found. 


## example 

A standard setup as we aspire would probably look like that :

    
    <root>
        artifacts
            repository-configuration.yaml
            cache 
                com
                    braintribe
                        devrock 
    
    
The environment is using its own local repository, called 'cache' here to illustrate.

Within the 'repository-configuration.yaml' file, you simply declare your local repository as such:
``` yml
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


##  origination

The devenv configuration module also supports the [origination scheme](./origination.md), that means that the repository configuration compiled by it has data attached how it was compiled.
