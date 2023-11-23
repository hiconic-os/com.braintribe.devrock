# format of the expressive file

## basics

The file itself is a standard text file (you can used tabs or spaces as you wish), indenting doesn't play a role. Line spacing does play a role, see below

Basically, the file consist of key-words and their respective values. The preceding key word specifies the parser that reads the respective content.

The data is read by a *state engine*. The keywords set the engine into a certain state. Without any other keyword, the state remains and all subsequently found lines (with only data, no keywords) are automatically associated with the current state.  

Some things are important here:

Semicolon is 'line comment', i.e. if a (trimmed) line starts with a semicolon, the whole line is regarded as a comment.

Empty lines are delimiters, i.e. they switch the state into an undefined state (which then expects an artifact declaration), and can be used instead of the -x switch

```
; : identifies a end of line comment - the whole line is regarded to be a comment
-v: identifies a version 'override', i.e. explicitly sets the version of the artifact to the expression
-r: identifies a parent reference - the expression on the next line is a dependency
-d: identifies a dependency reference - the expression is condensed dependency name, followed by scope:type, and optional a list exclusions, group:artifact, preceded and delimited by semicolons.
-m: identifies dependency reference to appear in the managed dependency section of a parent.
-c: identifies the files to create - the expressions are simple part classifiers, i.e. classifer:extension tuples.
-p: identifies properties - the expressions are simple key:value tuples
-x: identifies a state 'pop' - the parser reverts to its initial state

@ : assigns a repository id to the content, used to tag the maven-metadata.xml. May occur anywhere in the file.
```

## example
The following example is a simple structure - in this case to create content for a local repository.


```
@local
com.braintribe.devrock.test:a#1.0
	-r
		com.braintribe.devrock.test:parent#[1.0,1.1)
	-d
		com.braintribe.devrock.test:b#
		com.braintribe.devrock.test:c#1.0:compile:jar;:d		
	-c
	:jar
	sources:jar


com.braintribe.devrock.test:b#1.0
	-r
		com.braintribe.devrock.test:parent#[1.0,1.1)
	-c
	:jar
	sources:jar
	-x

com.braintribe.devrock.test:c#1.0
	-r
		com.braintribe.devrock.test:parent#[1.0,1.1)
	-d
		com.braintribe.devrock.test:d#1.0-c
	-c
	:jar
	sources:jar


com.braintribe.devrock.test:d#1.0
	-r
		com.braintribe.devrock.test:parent#[1.0,1.1)
	-c
	:jar
	sources:jar
	c:jar
	-x

com.braintribe.devrock.test:e#1.0
	-r
		com.braintribe.devrock.test:parent#[1.0,1.1)
	-c
	:jar
	sources:jar
	-x

; parent pom
com.braintribe.devrock.test:parent#1.0|pom
	-p
		V.com.braintribe.gm:[1.0,1.1)
	-m
		com.braintribe.devrock.test:import#[1.0,1.1):import
		com.braintribe.devrock.test:e#1.0-c		

; imported dependency management pom
com.braintribe.devrock.test:import#1.0|pom
	-m
		com.braintribe.devrock.test:b#1.0			
```

## formatting notes

### artifact

the format of the artifact declaration is as follows

```
<groupId>:<artifactId>#<version>[|<packaging>]
```

So the following expressions are valid

```
com.braintribe.devrock.test:a#1.0
com.braintribe.devrock.test:import#1.0|pom
```

### artifact version override
By default, the version of the artifact is taken from its single line declaration. However, the version in this declaration must be resolved, i.e. may not contain any variables. Still, we might want to use variables there. So this is possible:

```
com.braintribe.devrock.test:e#1.0
	-p
		major:1
		minor:0		
		revision:1-pc
	-v
		${major}.${minor}.${revision}
	-r
		com.braintribe.devrock.test:parent#[1.0,1.1)
	-c
	:jar
	sources:jar
	-x
```

Please note the generator will write the version override no matter whether the variables are defined as properties or not. It's solely your responsibility to define the appropriate variables. 


### dependency
the format of a dependency is as follows

```
[<groupId>]:<artifactId>[#<version>[-<classifier>]][:[scope]:[type]];[[<groupid>]:[<artifactId>],..]][|<pi-tag[:<pi-value>],[<pi-tag>[:<pi-value>],..]
```

Basically, it consist of four parts:
- the identification part (groupid, artifactid, version, classifier)
- the scope-ing part (scope and type)
- the exclusions (pairs of groupId and artifactId)
- a list of processing instructions, split into tag:value

Valid expressions are :

```
com.braintribe.devrock.test:a#1.0
com.braintribe.devrock.test:e#1.0-c
com.braintribe.devrock.test:import#[1.0,1.1):import
com.braintribe.devrock.test:t#1.0:compile:jar;javax.servlet:javax.servlet-api
com.braintribe.devrock.test:t#1.0:compile:jar;javax.servlet:javax.servlet-api|tag:system
com.braintribe.devrock.test:c#1.0:compile:jar;:d;:e
com.braintribe.devrock.test:b#1.0:compile:jar;:
com.braintribe.devrock.test:x#1.0;:
com.braintribe.devrock.test:l#
com.braintribe.devrock.test:x#1.0|tag:asset
```

Note that the 'version delimiter', the _#_ always needs to be added to the dependency declaration. However, it doesn't need to have a valid version expression.

Note that there's no sanity check. For instance, a dependency declared in the '-r' state (parent reference) or in the '-m' state (dependency management section) need to be *fully declared*, whereas dependencies for the '-d' state may be groupId & artifactId only (if backed by a '-m' dependency somewhere).

### properties
Properties are simple key:value pairs, as such

```
<key>:<value>
```

so valid properties are

```
V.com.braintribe.gm:[1.0,1.1)
major:1
revision:23-pc
```

### parts
Part declarations consist of the 'part identification/classification tuple', the classifier and the type of the part. In the descriptive repolet content, it's quite useful to be able to describe the content of the parts as only the pom is automatically created, and - by default - all other parts will have size 0. 

You can either directly specify the contents in the declaration or reference an external file (denoted by a preceding @)

The full syntax for the part declaration is as follows:

```
[<classifier>]:[<type>][;[@]<expression>]
```

Valid expressions are 

```
asset:man;$nature="bla"
:jar;@myJar.jar

```

If you are using a file, make sure that the repolet will later be able to find the file. It needs to be there at the time of accessing the associated part and not while parsing the expressive text file.

