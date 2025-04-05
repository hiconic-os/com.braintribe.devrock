package com.braintribe.devrock.mc.api.repository.configuration;

import java.io.File;

import com.braintribe.devrock.model.repository.RepositoryConfiguration;

public record RawRepositoryConfiguration(RepositoryConfiguration repositoryConfiguration, File file) {}
