# resolving artifacts

Artifact-resolving in mc-core works in two distinct ways, depending on what the expression is you want it to look up.

If you are passing a (ranged) dependency, it will use the metadata on the remote repositories, if you are asking it for a non-ranged dependency (transposable to direct artifact), it will forego testing (and possibly retrieval prior to testing) of the metadata.

## dependency
see the respective part in [using mc-core](../using/using.md), dependency resolving.
If the dependency is ranged, mc-core will access the maven-metadata files of the local and remote repositories in order to find a matching version. In that case, it will update/download these files if required.

## artifact
see the respective part in [using mc-core](../using/using.md), artifact resolving.
If you are simply accessing an artifact, the maven-metadata are not required and will neither be updated nor downloaded. mc-core will always try to access the repository in order to retrieve the artifact.

## part
see the respective part in [using mc-core](../using/using.md), part resolving.
If you are accessing a single part, and it's not marked in the [part-availablity file](../resolving/parts.md), mc-core will determine whether the part exists ([via repository probing](../configuration/probing.md))
