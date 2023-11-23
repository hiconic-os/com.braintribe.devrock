# Multilevel design of mc-ng


while examining mc's artifact model(s), you find that there quite a few.

For instance, the 'old' artifact-model now comes in four different installments.

- essential-artifact-model : contains light-weight declarations
- declared-artifact-model : contains the data that reflects a pom based artifact
- compiled-artifact-model : contains compiled (resolved, finalized) data built from the declared level
- consumable-artifact-model : contains the high-level data that is actually used by the transitive resolvers (tree traversers, walkers).
- analysis-artifact-model : contains processed data from the resolvers representing the dependency tree for instance. 

the order to read them (and the order of increasing expressiveness) is

```
essential -> declared -> compiled -> consumable -> analysis
```

## essential

the essential level is at the bottom and it defines the really basic entities. Note that the entities of this model only contain string-type member properties. They are lightweight and are intended to be used quite frequently.
It only contains means to logically identify both unversioned and versioned artifact and a part (analog to the old PartTuple).

You'll find things like the [ArtifactIdentification](javadoc:com.braintribe.model.artifact.essential.ArtifactIdentification), [PartIdentification](javadoc:com.braintribe.model.artifact.essential.PartIdentification) here.

More about identifications is found [here](./identifications.md).

```
com.braintribe.devrock:essential-artifact-model : contains all entities of this level 
```


## declared

the declared level builds on the essential model, and introduces more complex entities. In a nut shell, these entities represent what you can read from the various xml files.

```
declared-artifact-model : contains a full pom as it was declared
declared-settings-model : contains a full settings.xml as it was declared
declared-maven-metadata-model : contains a full maven-metadata.xml
```

These models each are linked to marshallers that can handle the models (all read & some also write). As they are intended to be invariant (as long as the files don't change), they can be cached. Note that NOTHING other than what is in the file can appear in them here. For instance, variable references are not resolved on this level, and processing instructions are not interpreted    


## compiled

the compiled level, the environment plays a role. Variables are resolved at this stage (through the parent-chain if necessary), processing instructions are applied, so it about reflects the model level of the old mc. It still reflects the same structure as in the declared level, but its values have been 'made real'.

```
compiled-artifact-model : contains a full pom, fully compiled (variables in expressions replaced by their values, complex properties assigned)
repository-configuration-model : the model that contains configuration data for the mid-tier (potentially distilled from the declared-settings-model)
```

## consumable

This is were 'abstracted' artifacts come into play. At this stage, the entities are not linked anymore to the other levels, as they are optimized to be consumed. For instance, on this level, an artifact's dependencies are artifacts while on the level below, there are logically separate (a dependency is a request to be resolved, once resolved, it's an artifact just like its terminal).  

```
consumable-artifact-model : contains a top-level view of an artifact
```


## analysis
The analysis level is basically what the transitive resolver communicates with.

```
analysis-artifact-model : contains the entities that are returned by the transitive resolvers (TDR/CPR)
```

These entities contain the biggest amount of information as - as their name implies - can also be used for analysis purposes. The entities within a resolution are linked, but alternating from artifact to calling dependency to calling artifact and so on. 