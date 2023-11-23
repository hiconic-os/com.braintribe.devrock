# origination

There are many ways a [RepositoryConfiguration](javadoc:com.braintribe.devrock.model.repository.RepositoryConfiguration) comes into being, see [here](./configuration.md). Sometimes, its a simple file being read, but sometimes, there's a quite complex compilation process leading to the final configuration.

As in some cases the configuration may be wrong or not what you expected (for instance if it uses a 'current directory'-based logic or access environment variables and/or system-properties), it is important to be able to trace the assembly process of the configuration. 

Here comes the 'origination' into play. The [Origination](javadoc:com.braintribe.devrock.model.mc.cfg.origination.Origination) is a subset of standard [Reason](javadoc:com.braintribe.gm.model.reason.Reason) entries. 

The entity can be found in 

    com.braintribe.devrock:mc-reason-model


Basically, it reflects what configuration module was compiling the repository configuration and when it did happen. Then, depending on the module, it will show you more details, such as what environment variable was used, what it was pointing to etc. 



