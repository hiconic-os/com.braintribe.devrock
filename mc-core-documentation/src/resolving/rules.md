# Rules

There are two different rule logics that you can apply. While they have their own string representation, their processors are implemented as predicates as they - in short - filter-out some dependencies during resolving.


## tag rule

A tag rule acts on the tags a dependency shows.
``` xml
        <dependency>
            <groupId>tribefire.extension.setup</groupId>
            <artifactId>test-module-initializer</artifactId>
            <version>${V.tribefire.extension.setup}</version>
            <classifier>asset</classifier>
            <type>man</type>
            <?tag asset?>
        </dependency>
```
    
As you can see in this example, a tag is nothing more than a specific Processing Instruction attached to a dependency. You can of course have multiple tags for a dependency.

Tag rules now act on these tags, and depending whether they apply or not, the dependency is filtered-out (aka ignored) or processed.

### format 
A tag rule in its string notation is a simple comma-delimited list of tag values, optionally negated via a prefixed '!',
```
    [!]<tag>[,[!]<tag>,...]
```
The negation exclamation mark denotes that the expression following it is to be taken as an exclusion rather than an inclusion. Best explained with the use of the wild-card asterics.
```
    null -> no filtering on tags at all 
    "*" -> any tags (no non-tagged)
    "!*" -> nothing with tags
```
You can of course combine them 
```
    "<tag>" -> only with this tag
    "<tag1>,<tag2>" -> any of these tags

    "<!tag>" -> nothing with this tag
    "<!tag1>,<tag2>" -> nothing with <tag1> but only the ones with <tag2>
    !*,<tag2> -> only the ones with tag2..
```    

### examples

Consider these dependencies where the rules will be applied to (versions are omitted because they are transparent for the rule)
```
    com.braintribe.devrock.test.tags:none : no tags
    com.braintribe.devrock.test.tags:standard : tagged as 'standard'
    com.braintribe.devrock.test.tags:one : tagged as 'one'
    com.braintribe.devrock.test.tags:one-and-two : tagged as 'one' AND tagged as 'two'
    com.braintribe.devrock.test.tags:asset : tagged as 'asset'
```    

Applying a 'null' rule
```
    com.braintribe.devrock.test.tags:none
    com.braintribe.devrock.test.tags:standard
    com.braintribe.devrock.test.tags:one
    com.braintribe.devrock.test.tags:one-and-two
    com.braintribe.devrock.test.tags:asset
```    

Applying a '*' rule
```
    com.braintribe.devrock.test.tags:standard
    com.braintribe.devrock.test.tags:one
    com.braintribe.devrock.test.tags:one-and-two
    com.braintribe.devrock.test.tags:asset
```

Applying a '!*' rule
```
    com.braintribe.devrock.test.tags:none
```
Applying a '!one' rule
```
    com.braintribe.devrock.test.tags:none
    com.braintribe.devrock.test.tags:standard
    com.braintribe.devrock.test.tags:asset
```
Applying a 'one' rule
```
    com.braintribe.devrock.test.tags:one
    com.braintribe.devrock.test.tags:one-and-two
```

Applying a 'one,!two' rule   
```    
    com.braintribe.devrock.test.tags:one
```
    
    
    
## type rule
A type rule is very similar to the tag rule, but rather than on tags, it acts on the type of a dependency. In this respect, the type actually contains classifier as well, so it's a combination of classifier and type.

 A type rule acts on the type and the classifier of a dependency.
``` xml
        <dependency>
            <groupId>tribefire.extension.setup</groupId>
            <artifactId>test-module-initializer</artifactId>
            <version>${V.tribefire.extension.setup}</version>
            <classifier>asset</classifier>
            <type>man</type>
        </dependency>
```    

### format 
A type rule is a simple comma-delimited list of a combination of 'classifier' and 'type' where :

    [<classifier>:][<type>][,[<classifier>:][<type>], ..]

While the tag rule above can negate an expression, the type rule cannot. It's simply a list of valid classifier:type tuples.

You can omit parts of the tuple's declaration, so the 
```
    '<classifier>:' -> classifier = '<classifier>', type = '*'
    '<type>' -> classifier = '*', type = '<type>'
    ':<type> -> classifier = '*', type = '<type>'
```

### examples

Again, consider these dependencies that the type rule will be applied to (versions are omitted because they are transparent for the rule)
```
    com.braintribe.devrock.test.types:none : defaulting to 'null' classifier, defaulting to 'jar' type
    com.braintribe.devrock.test.types:standard : 'null' classifier, 'jar' type
    com.braintribe.devrock.test.types:war : 'null' classifier, 'war' type
    com.braintribe.devrock.test.types:asset-man : 'asset' classifier, 'man' type
    com.braintribe.devrock.test.types:asset-other : 'asset' classifier, 'other' type
    com.braintribe.devrock.test.types:noasset-man : 'null' classifier, 'man' type
    com.braintribe.devrock.test.types:asset-man : 'null' classifier, 'zip' type
```
Applying a 'null' rule
```
    com.braintribe.devrock.test.types:none
    com.braintribe.devrock.test.types:standard
    com.braintribe.devrock.test.types:war
    com.braintribe.devrock.test.types:asset-man
    com.braintribe.devrock.test.types:asset-other
    com.braintribe.devrock.test.types:noasset-man
    com.braintribe.devrock.test.types:zip
```    
Applying a 'jar' rule
```
    com.braintribe.devrock.test.types:none
    com.braintribe.devrock.test.types:standard
```
Applying a 'asset:' rule
```
    com.braintribe.devrock.test.types:asset-man
    com.braintribe.devrock.test.types:asset-other
```    
Applying a 'man' rule
```
    com.braintribe.devrock.test.types:asset-man
    com.braintribe.devrock.test.types:noasset-man
```
Applying a 'asset:man' rule
```
    com.braintribe.devrock.test.types:asset-man
```
Applying a 'war' rule
```
    com.braintribe.devrock.test.types:war
```
Applying a 'zip' rule
```
    com.braintribe.devrock.test.types:zip
```
Applying a 'asset:,man,asset:man' rule
```
    com.braintribe.devrock.test.types:asset-man
    com.braintribe.devrock.test.types:asset-other
    com.braintribe.devrock.test.types:noasset-man
```

## how to use these rules

There is only direct support for these rules in the bt-ant-tasks-ng's (devrock-ant-tasks's) publishing feature. There you can directly specify these rules as attributes to the call.

If you want to use these rules within a resolution (no matter for the [TransitiveDependencyResolver](asset://com.braintribe.devrock:mc-ng-tutorials/howToRunRepositoryExtractions.md) or the [ClasspathDependencyResolver](asset://com.braintribe.devrock:mc-ng-tutorials//howToResolveClasspaths.md), you need to turn them first into dependency filters and inject them as such.

The pattern is the same for both rule-types, just differing instances. Also, it's the same pattern for both resolvers. 

Shown here is how it's done with the ClasspathDependencyResolver for TypeRule
``` java
    ClasspathResolutionContextBuilder contextBuilder = ClasspathResolutionContext.build();

    .... 

    if (typeRule != null) {
        Potential<TypeRuleFilter,Reason> typeRuleFilterPotential = TypeRuleFilter.parse(typeRule);
        
        if (typeRuleFilterPotential.isFilled()) {
            TypeRuleFilter typeRuleFilter = typeRuleFilterPotential.get();
            contextBuilder.filterDependencies(typeRuleFilter);
        }
        else {
            throw new IllegalStateException("the type rule [" + typeRule  + "] is invalid");
        }
    }
```
And that would be it for the tag rule
``` java
    ClasspathResolutionContextBuilder contextBuilder = ClasspathResolutionContext.build();
    if (tagRule != null) {
        Potential<TagRuleFilter,Reason> tagRuleFilterPotential = TagRuleFilter.parse(tagRule);
        
        if (tagRuleFilterPotential.isFilled()) {
            TagRuleFilter tagRuleFilter = tagRuleFilterPotential.get();
            contextBuilder.filterDependencies(tagRuleFilter);
        }
        else {
            throw new IllegalStateException("the tag rule [" + tagRule  + "] is invalid");
        }
    }
    
    ... 

    ClasspathResolutionContext resolutionContext = contextBuilder.done();
```

Note that if you have both a tag and a type rule, you must combine (chain) the filters. Both [TagRuleFilter](javadoc:com.braintribe.devrock.mc.core.resolver.rulefilter.TagRuleFilter) and [TypeRuleFilter](javadoc:com.braintribe.devrock.mc.core.resolver.rulefilter.TypeRuleFilter) implement 
a standard predicate :

```
Predicate<AnalysisDependency>
``` 


Once you have your resolution context setup, you can feed it to the resolver - as described in their documentation.


