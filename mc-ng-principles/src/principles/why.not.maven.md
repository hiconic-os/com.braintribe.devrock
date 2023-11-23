# Braintribe and Maven or *relationship status : it's complicated*


You might be wondering why Braintribe uses a home-grown amalgam of different build tools to build its software. Some reasons are simply historic, and due to what was known or available at the time of the setup of the build system, some reasons however are still as valid as they were then.

One of my personal guideline is as follows: If a tool matches your vision, by all means, use it. If it requires you to deviate one single iota from your vision, build your own tool. We as software developers are in the very comfortable situation to be able to do this - use your talent. 

Maven is a very widespread tool, and even if it's not as advanced as gradle or buildr, it is a kind of a quasi standard in the IT business. Braintribe however doesn't use Maven as a build tool and even will stop to use it as a dependency tool. This paper explains why, and where we are now and where we're going. 

## Where are we now
Currently, we use a mix of Maven, Ant and Eclipse plugins

### Maven :
 Use of the pom format for dependency declarations. Use of the settings format for profile, repository, mirror and server declarations. Full support for Maven update policies. Full support for Maven compatible repositories. Use of maven-ant-tasks as a dependency resolver in build.xml files.

### Ant : 
Use of Ant to build the software. Integration of custom build steps and tasks via Java to support features like 'dependencies', 'install' and 'deploy' via maven libraries.

### Eclipse :
Several plugins to support different aspects of development work, i.e. classpath determination, repository management, debug launches, project analysis.

## So why do we not settle on 'pure' Maven? 
As you see, we are using parts of Maven, but found some aspects of Maven lacking in such a way that it was impossible for us to simply adopt it. 

### violation of SOC, mixing dependency control & build 
If you know Tribefire a bit, you will have noticed that the paradigm of **Separation Of Concerns** (SOC) is very dear to us. We believe that this paradigm is **paramount for building complex software**, be it coding or assembling it. Furthermore we believe that Cortex is not just about Tribefire (or Devrock or your projects), but also about how one builds software (on a side note: we actually believe that it also influences the organizational structure of braintribe or at least should do so).
Maven mixes the dependency management with the actual build instruction, which violates SOC big time. Granted, the dependency declaration is important for building the software, but it's also for distributing, licensing and running the software. For instance, Cartridge Embedding, Model Synchronization both rely on proper dependency declaration yet have nothing to do with actually building the cartridge respectively the models. The fact that pom files also contain profile and repository information is a further violation of SOC. 


### lack of origin control
For us it is absolutely important to know what artifacts are used within a project, and from where they have been fetched. As a commercially active company that binds itself into a contractually regulated obligation versus the respective customer, we are responsible for all building blocks of our software - within reasonable limits of course. We cannot know all future problems (or attack vectors) in the first hand, but once the information is made available  - for instance via the National Vulnerability Database (NVD) of the US National Institute of Standards And Technology (NIST) - we are made liable. Ignorance is not a good legal excuse as your parents already must have told you.

The way Maven handles importing dependencies, i.e. the way that any artifact can have its own origin (repository information) - combined with the update policies - leads to a situation that you cannot know what the cat drags in. 

So, internally, we allow access to one repository or at least a controlled number of repositories that are constantly managed by ourselves. There is no direct access to any repository not hosted by Braintribe - partially this is done via a strict mirror declaration in the settings, but also by actually removing any repository references from the pom of any artifact we incorporate into our managed repositories.

You can read here what the [Ivy guys](http://ant.apache.org/ivy/history/latest-milestone/bestpractices.html) have to say about that - see the section 'use your own enterprise repository' 

And this [document here](https://img2.helpnetsecurity.com/dl/articles/fortify_attacking_the_build.pdf) here (which they also link) should also remove all doubts: 

>amazingly, now in view of the current 'log4j' issue, I'm even more amazed that using an unfiltered open source repository is still ok with most developers.. Granted, the current crisis is not directly linked to Maven, but it shows that it is important to have all access to external code (brought in via jndi, js-module or  java-artifact) controlled and monitored. 


### dumb update policies
Maven knows the following update policies, i.e. methods to synchronize the local repository (the cache so to say) with the remote repository (the actual company/release/project repository):
always : maven will synchronize an artifact every time you access it
never : once an artifact is in the cache, it will never be synchronized again.
daily : if at least a day has passed after the last access of an artifact, it will be synchronized
interval : if the amount of time specified has elapsed since the last access, it will be synchronized.
We think that it should work differently, i.e. the repository should deliver the information if, and if so, what artifact has been changed since the last access to the repository. It doesn't make sense to synchronize an artifact if it hasn't changed, nor makes it sense to wait a day until the local repository reflects the state of an artifact. We implemented that as an add-on to Archiva repository (Ravenhurst), and it will be an integral part of our own repository cartridge. 


### dumb clash resolving
Maven uses an ad-hoc clash resolving method, i.e. the first dependency encountered with a dependency tree analysis will overrule any dependency that is following deeper in the tree. This of course means that the sequence of dependencies as they appear in an artifact determines the final output. This may be fine if you only work with a single (or a few) terminal artifacts (i.e. products). But with such a huge codebase that we have, combined with our paradigm of SOC, separation of API- and implementation-artifacts, and our aim to act as a platform for other developers, this approach is not sufficient. 

The final aim must be that any complex dependency tree is clash-free. In the meantime, a post-hoc clash resolving can be of help, as it can be used to implement a clash resolving not based on the sequence of dependencies, but actually on a metric based on the versions involved.

Amazingly, the official rationale within the Maven community why its logic is to be preferred, is that it produces 'stable builds'. That is of course bogus: while it allows you to influence the result by making sure that the appropriate versions are taken by simply adding the dependency to your terminal artifact in the correct sequence, it actually introduces 'fake' dependencies - a dependency should only be declared if there really is a dependency, as this may have an influence in branching, or licensing. 

A further way in Maven to influence clash resolving is to use a parent and let its 'distribution management' section deliver hints of what versions are to be preferred in case of clashes. While this is better than adding "fake dependencies", it also leads to fact a different branches in a dependency tree will clash differently (depending on where the parent is referenced). In order to influence the full dependency tree, it must be a parent of the terminal - and as a consequence, the terminal must know what clashes may appear in some dependencies within the tree. 

### lacking tools
In our view, it is absolutely important to empower developers during the life cycle of a project. We found the support of the standard tools in the Maven community as lacking and decided to implement our own set of tools. 
One example are the analysis features in the Eclipse plugins : we stated above that clash resolving - or rather to manually resolve all clashes so that the project is free of clashes - is utterly important, so the support for it must reflect that importance.  

## Why we still use (parts of) Maven 
So, if Maven's so bad, why are we still compatible? 

### compatibility with Maven
Even if we believe that there a way better ways to build software than with Maven, we still want to be compatible. It is - unfortunately I'm tempted to say - still the currently most widespread build tool. So if someone is indoctrinated or forced kicking and screaming to use Maven with our codebase, let him do so. 
Furthermore, our setup can use Maven settings.xml, to set up profiles, repositories, mirrors and servers. We do require this information anyhow, and it wouldn't make sense to come up with a proprietary format just to differ. 

That being said: in mc-core, the settings.xml is **not the only way** to configure a build (or resolving). It can now also be completely replaced by the YAML format representation of the 'repository configuration'. 


### standard for dependency declaration
We still use poms - for the same reason as stated above: we do need dependency declarations, and there's no need to create our own incompatible format to do so. 


### maven compatible remote repositories
Again, even if we manage our own repositories, and even implement one, we are grateful for the standard that Maven introduced when it comes to the remote repository. Not only do we fully support it (you can use our tool to download from any remote repository and to upload to any as well), but our Repository Cartridge (and current Tribefire Repolet) behave as 100% compliant Maven repositories.

## Why ranges
Since Maven 3.0, any version tag in a reference to another pom (parent, dependency and dependencyManagement section) is actually not a version as an Artifact's version, but a version range. If it looks like a version, e.g. 1.0, it's a collapsed range where lower and upper boundaries are the same. Standard range is something like [1.0, 1.1), which specifies a range of matching versions and conveys information about how the boundaries should be treated (in this example, the lower boundary is open, i.e. 1.0 would match, yet the upper boundary is closed, so 1.0.9 would match, but 1.1 wouldn't).

Ranges allow to dynamize dependencies. If you use versions, then you must specify the exact match. Once you have created a hotfix, you need to change any reference to it. With an appropriate range, this happens automatically. 

'the use of version ranges are discouraged in the community'
This amazing statement shows how all these things we complain about are interrelated. The dislike for version ranges is linked to the requirement of having 'stable build results'. Again, this is bogus, as it just says that if you do not control the origin of your artifact, you cannot control what the cat (Maven) drags in (somebody outside of your organization might have added a new version that just happens to match your range). Once you accept that the set of artifacts your build process chooses from has to be controlled by you and only you, you then in extension control what goes into your build. It's not the range that is an issue, but the uncontrolled use of remote repositories. 


Ranges allow us to adapt to hotfixes and that's what we want to use them exclusively for  - we do not suggest using ranges like [1.0,], which anything including 1.0 and higher fits. We only are using ranges like this [1.0, 1.1), which would match any 1.0.<hotfix> versions, but not 1.1. 

## Why multiple repositories
We are using multiple repositories as it makes sense to adhere to the SOC paradigm also in this respect. We as the Braintribe share a common third party repository that is managed by us all. As Tribefire is a product and a platform teams code against, its artifacts are also exposed as a repository. If a team wants to use an official release, it will choose the appropriate repository published by Braintribe. If it wants to use the hotfixes of a more recent version, i.e. nightly build for instance, it will add this repository. And of course it will use its own repository for artifacts that only the team requires. So if I counted right, we're already up to at least three repositories.  

## Why publishing
You might have heard about the 'publishing scheme'
This represents that paradigm that we do not transitively build (and rebuild and rebuild) our artifacts, but once they reach a certain level of maturity (and pass all tests we throw at them), they are 'published'. From this point on, nobody will build them again, but just use them as if they were a third party artifact like a JDBC driver or a HTTP client jar. 
Redeploying, i.e. publishing a changed artifact with the same version is prohibited, that means that any changes will lead to an increase in the version of the artifact. The automatic publishing service will create incremented hotfix version parts only. Changes that reflect major and minor version changes are still done manually. 


## Where do we want to go
While the direction of our work will not change, we want to achieve a few things in the nearer future

### Widen the support
Granted, ANT is somehow outdated, and there are sexier build tools, such as the community driven gradle, buildr and sbt, but also bazel, buck and pants driven by big companies (google, respectively facebook and twitter). While we'll still support ANT for the foreseeable future, we believe that there are a lot of reasons to decide to use one or the other build system, and that there's no 'one to bind them all'. 
The approach will be to reduce the build system specific code to a minimum, and only implement little adapters in the actual build system. Most of the logic will happen outside of the build tool and will be uninfluenced by the build system use. One way to achieve that is the use of server based components. 
Still, no matter which build tool we decide to use internally, the SOC mentioned above may never be violated, even if this means that we need to tweak the out-of-the-box solution the tool provides. 

### Server-based development
Server-based development, both for single developers and teamed developers, is what we currently envisage. Tribefire is a stable and powerful environment that gears perfectly to our requirements. A local server will export DDSA based services like dependency analysis required to build compile classpaths, install to local repository, deploy to remote repository, that can be used from the command-line via the build tools or from the plugins such as AC. A remote server will provide services like publishing, source repository querying, collaborating cortex features. All transparent for the actual command tool.
Most UI will be generated by the servers and presented in form of HTML5 renderings. By that means, SWT programming as currently required in the Eclipse plugins will become obsolete, and reduced a minimum required, the smaller footprint of the actual feature set required to reside in Eclipse makes it imaginable to port that feature set to alternative IDE, such as IntelliJ.

# Conclusion
In closing let me reiterate what I started with saying: We believe that going our own way is integral to what we are doing, and that is how Tribefire came into being. If we'd now say, let's stick to what is around, how can we ask our customer to use Tribefire rather than what's been around in his IT department? 

There's only one way to get there where we want to go : by making bold moves. You cannot cross an abyss with two small jumps - or as I like to say : If you're going to jump, you best jump far. 



