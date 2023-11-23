# cycles

As one might expect, cycles in a dependency tree are not allowed - at least in most cases.

A cycle is when an artifact indirectly references it self, as in this sketch

```
a:x#1.0 -> b:y#1.0 -> c:z#1.0 -> a:x#1.0
```

Such cycles can also occur in a parent-, in an import- and even in a redirection-chain. 

All these cycles are not allowed, detected and reflected in the resulting resolution.

There is however one single exemption to that rule :

An artifact can reference itself as long as the referencing dependency has differing classifiers as in the example below.

```xml
	<project>
		<groupId>a</groupId>
		<artifactId>x</artifactId>
		<version>1.0</version>
		<packaging>jar</packaging>
		
		<dependencies>
			<dependency>
				<groupId>a</groupId>
				<artifactId>x</artifactId>
				<version>1.0</version>
				<classifier>foo</classifier>
			</dependency>
			<dependency>
				<groupId>a</groupId>
				<artifactId>x</artifactId>
				<version>1.0</version>
				<classifier>bar</classifier>
			</dependency>			
		</dependencies>
	</project>
```

Now, it doesn't make big sense (I'm questioning whether it makes sense at all), but I think it's about - again and again - about the build process interfering with a comprehensive analysis of the tree, and as the information about the build process is not of interest an a binary repository, it's about as useful as a broken toe.

Still, we do support it:

If somebody is referencing this artifact 'a:x#1.0', it will contribute all 3 jar files to the classpath:

```
	a#1.0.jar
	a#1.0-foo.jar
	a#1.0-bar.jar
```

And of course, as a dependency, in the analysis, you will see the self-reference as well, so if you are actually processing the AnalysisArtifactResolution you must keep that in mind:


	