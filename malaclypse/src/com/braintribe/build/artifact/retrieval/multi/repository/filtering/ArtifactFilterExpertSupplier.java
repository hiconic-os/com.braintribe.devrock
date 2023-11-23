package com.braintribe.build.artifact.retrieval.multi.repository.filtering;

import com.braintribe.devrock.mc.core.filters.ArtifactFilterExpert;

public interface ArtifactFilterExpertSupplier {

	ArtifactFilterExpert artifactFilter(String repositoryId);
}
