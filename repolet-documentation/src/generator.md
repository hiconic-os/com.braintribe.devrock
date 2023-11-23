# repolet content generator

Of course, what the repolet content generator actually does is to create a file-system that follows the internal structure of a repository, both for a remote one or even for a local repository. In the former case, you do not specify an id the repolet content, but let the actual repolet (and the settings.xml for instnance) decide what the id oif it is. If however you want to create data for a local repository, you assign the repository's id in the repolet content.

The repolet content generator can be found here :

```
com.braintribe.devrock:repolet-content-generator

```

### repolet content modelling
The model everything is based on is here :

```
com.braintribe.devrock:repolet-content-model

```
Basically, the model contains a container entity, the [RepoletContent](javadoc:com.braintribe.devrock.model.repolet.content.RepoletContent), and a set of [Artifact](javadoc:com.braintribe.devrock.model.repolet.content.Artifact). Artifacts can be standard artifacts, but also 'parent' and 'import' artifacts. An artifact has a set of [Dependency](javadoc:com.braintribe.model.repolet.content.Dependency), and as set of [Property](javadoc:com.braintribe.devrock.model.repolet.content.Property). That is all that's there to it.


### declaring the contents
You can of course programmatically create the content for the generator. Or you can write it as a YAML file. Finally, you can use a simple DSL to set it up.

While very short and compact, the [expressive format](./expressive.md) or DSL is somewhat cryptic. It is also not as powerful as the YAML style, but for now it supports all features as well. Still, I'd recommend using the YAML.

As simple text definition could look like this: 

	com.braintribe.devrock.test:t#1.0.1
		-d 
			com.braintribe.devrock.test:a#1.0.1
			com.braintribe.devrock.test:b#1.0.1
		-c
			:jar
		-x
		
	com.braintribe.devrock.test:a#1.0.1    
		-c
			:jar
		-x
		
	com.braintribe.devrock.test:b#1.0.1
		-c
			:jar
		-x	



The [YAML format](./yaml.md) is perhaps a bit bloated, but it makes up with better understandability and as it's supported by the marshaller ramifications (the marshaller has an idea what it is used for and supports some abbreviations), I'd recommed to use that. 


Still a simple YAML flow style setup could look like this : 

		# definition for classifier combinations (i.e. same artifact with two different parts in tree)
		!com.braintribe.devrock.model.repolet.content.RepoletContent {
		artifacts: [
		
		    # terminal
		    { artifactId: "t", groupId: "com.braintribe.devrock.test", version: "1.0.1", parts: {":jar": null},       
		       dependencies: [                
		          { artifactId: "a",  groupId: "com.braintribe.devrock.test", version: "1.0.1"}, 
		          { artifactId: "b",  groupId: "com.braintribe.devrock.test", version: "1.0.1"}, 
		
		       ],        
		    }, 
		    # a - first level child      
		   { artifactId: "a", groupId: "com.braintribe.devrock.test", version: "1.0.1", parts: {":jar": null} }, 		
			
			# b - first level child
		    { artifactId: "b", groupId: "com.braintribe.devrock.test", version: "1.0.1", parts: {":jar": null} }, 		    
		  ]
		}
		
Apart from the file format, both formats are interchangeable. At least for now, at the time of writing this. The expressive format will not develop any further, and compatibility may be compromised in the future. So the adivse would be to use the YAML format. Flow-style is pretty nice actually anyhow.

    
    DO NOT DECLARE THE POM AS A PART, AS IT IS IMPLICITLY PRESENT.
    IF YOU DECLARE IT AS A PART, IT WILL APPEAR TWICE IN THE LISTING, BUT YOU WILL ONLY GET THE DATA OF THE IMPLICITELY DECLARED POM.



## using the repolet content generator
Basically, the repolet generator exposes three functions that you can use to generate content

```
generateExpressive( File target, File source);
generateMarshalled( File target, File source);

generate( File target, RepoletContent content);
```

you can of course use the latter function in conjunction with the marshaller/parser, so with the parser it might look like this:

```
try (InputStream in = new FileInputStream(inputParam)) {
    RepoletContent content = RepoletContentParser.INSTANCE.parse(in);
    RepoletContentGenerator.INSTANCE.generate(output, content);

  }
  catch (Exception e) {
    ...
  }
}

```
