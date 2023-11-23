# Format of the YAML file

The file is formatted as standard YAML. For ease of use, all the examples here are 'flow-style' YAML. 'Block-style' YAML also works of course, but it's harder to write. 

    NOTE: at the time of the writing this text, the YamlMarshaller can only write 'block-style' YAML files. It can of course read both formats. 



## basics
The YAML format of course follows the [model of the repolet content entities](./generator.md). So if you follow the model and use standard YAML practices, your are easily able to create meanigfull content.


## parsing extensions
While most the content simply follows the model and therefore can be loaded by the standard YAML parser, there is support for 3 convenient formats:


### dependency 
A dependency can have the same format as in the [expressive format](./expressive.md). So every time the format is expecting a dependency entry, a simple string can be used...

```
    [<groupId>]:<artifactId>[#<version>[-<classifier>]][:[scope]:[type]];[[<groupid>]:[<artifactId>],..]][|<pi-tag[:<pi-value>],[<pi-tag>[:<pi-value>],..]
```

This is most probably not that understandable and I would still recommend to use the full YAML format for complex dependecies. However, for a simplest dependency, it's tempting:

```

    { artifactId: "t", groupId: "com.braintribe.devrock.test", version: "1.0.1", parts: {":jar": null},       
	    dependencies: [                
	        { artifactId: "a",  groupId: "com.braintribe.devrock.test", version: "1.0.1"}, 
	        { artifactId: "b",  groupId: "com.braintribe.devrock.test", version: "1.0.1"}, 	
	    ],        
	  }
```

and with the convenience : 
```
    { artifactId: "t", groupId: "com.braintribe.devrock.test", version: "1.0.1", parts: {":jar": null},       
	    dependencies: [                
	        "com.braintribe.devrock.test:a#1.0.1",
	        "com.braintribe.devrock.test:b#1.0.1",
	    ],        
	  }
```
### part content 
In some cases, you want to specify not only a resource. By default, all files other than the .pom and the maven-metadata.xml files are always empty in the repolet, as it is intended to check for the existance of the files, and downloading can also happen on zero byte content. 

So if you want some content, you can either switch to the [filesystem based repolet](./repolet.md) or you can directly add some content as follows.

A part map consists of the PartIdentification (classifier and type) to an instance of a Resource. If the value is a simple string, then a TransientResource is created that will deliver the content you specified. In this example, the repolet wll return the declared content if the t-1.0.1-asset:man is downloaded.
```
    { artifactId: "t", groupId: "com.braintribe.devrock.test", version: "1.0.1", 
        parts: {":jar": null,
                "asset:man": "$natureType = com.braintribe.model.asset.natures.ModelPriming"
                },       
	    dependencies: [                
	        { artifactId: "a",  groupId: "com.braintribe.devrock.test", version: "1.0.1"}, 
	        { artifactId: "b",  groupId: "com.braintribe.devrock.test", version: "1.0.1"}, 	
	    ],        
	  }
```

### file resource
If you have a complex file - a .jar for instance with binary stuff - then the repolet itself cannot generate it for you. But you can specifiy source data that the repolet will deliver if tasked to download the file. 

Similar to the example above, but this time, a FileResource is declared. If the repolet is told to deliver the t-1.0.1.jar, it will actually read myJar.jar and deliver its content. 
```
    { artifactId: "t", groupId: "com.braintribe.devrock.test", version: "1.0.1", 
        parts: {":jar": !com.braintribe.model.resource.FileResource {"myJar.jar"}}
	    dependencies: [                
	        { artifactId: "a",  groupId: "com.braintribe.devrock.test", version: "1.0.1"}, 
	        { artifactId: "b",  groupId: "com.braintribe.devrock.test", version: "1.0.1"}, 	
	    ],        
	  }
```
or a little bit more expanded : 
```
    { artifactId: "import-task", groupId: "com.braintribe.devrock.test", version: "1.0.1",
        parts: { "import:xml": 
          !com.braintribe.model.resource.FileResource {
                        name: "my-import.xml",
                        path: "<my path>/my-import.xml"
          }
        }
    } 
```

### processing instructions

Processing instructions are key-value pairs. The difference to a standard java-style map is that a dependency can multiple processing instructions sharing a key (where a map can't). As the vehicle to read it here via YAML is a java-style map, the syntax had to be adapted.

Currently, if you want to specifiy multiple tags you need to use the following syntax - only active for PIs used a 'tag'

``` YAML
{ artifactId: "one-and-two",  groupId: "com.braintribe.devrock.test", version: "1.0.1", processingInstructions: {"tag": "one,two,three"}}, 
```

This construct will  lead to this :

``` XML
dependency>
	<groupId>com.braintribe.devrock.test</groupId>
	<artifactId>one-and-two</artifactId>
	<version>1.0.1</version>
	<?tag one?>
	<?tag two?>
	<?tag three?>
</dependency>
```
However, a sequence like 

``` YAML
{ artifactId: "one-and-two",  groupId: "com.braintribe.devrock.test", version: "1.0.1", processingInstructions: {"other": "one,two,three"}}, 
```

This construct will  lead to this :

``` XML
dependency>
	<groupId>com.braintribe.devrock.test</groupId>
	<artifactId>one-and-two</artifactId>
	<version>1.0.1</version>
	<?other one,two,three?>	
</dependency>
```

So if there's a further *well-known* tag that needs to be able to added multiple times, this needs to re-iterated.  
