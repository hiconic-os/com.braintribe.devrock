# com.braintribe.devrock

## For testing

Use the following environment variable for testing configuration:

    DEVROCK_TESTS_REPOSITORY_BASE_URL
    DEVROCK_TESTS_RAVENHURST_BASE_URL
    DEVROCK_TESTS_READ_PASSWORD
    DEVROCK_TESTS_WRITE_PASSWORD

## Automatic repository filters

Repository probing uses the change index for cache invalidation independently of
its use as a content filter. An unspecified `artifactFilter` still uses the derived
index filter. An explicit filter is authoritative and is no longer implicitly
intersected with that filter.

Use `AutoArtifactFilter` to place the derived filter explicitly in a filter expression.
For example, to extend indexed groups with a third-party group:

```yaml
artifactFilter: !com.braintribe.devrock.model.repository.filters.DisjunctionArtifactFilter
  operands:
    - !com.braintribe.devrock.model.repository.filters.AutoArtifactFilter {}
    - !com.braintribe.devrock.model.repository.filters.GroupsArtifactFilter
      groups: [org.example]
```

For a virtual repository with a partial artifact index, allow all coordinates while
continuing to synchronize indexed changes:

```yaml
artifactFilter: !com.braintribe.devrock.model.repository.filters.AllMatchingArtifactFilter {}
```

**Migration:** previously, any explicit filter was implicitly ANDed with the index
filter. To retain that behavior, wrap the existing filter and `AutoArtifactFilter`
in a `ConjunctionArtifactFilter`. An `AllMatchingArtifactFilter` now genuinely
permits unindexed artifacts. This changes repository selection, not authorization.

Automatic placeholders can also appear in nested expressions, including negation
and development-view restrictions. Their positions are replaced in a cloned graph;
the original configuration is preserved for subsequent resolutions. Without an
available derived filter, `AutoArtifactFilter` behaves like `AllMatchingArtifactFilter`,
also when evaluated directly by its standard expert. It is not a Boolean identity
that is removed from an expression: e.g. negating an unresolved Auto matches nothing.

A partial index only reports changes within its coverage. Absence from that index
does not establish absence of an artifact or imply deletion. This change does not
introduce server headers, index completeness negotiation or new repository properties.
