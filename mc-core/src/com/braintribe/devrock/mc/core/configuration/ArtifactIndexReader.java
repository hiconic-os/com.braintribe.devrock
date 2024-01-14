package com.braintribe.devrock.mc.core.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

import com.braintribe.cfg.Required;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolver;
import com.braintribe.devrock.mc.core.repository.index.ArtifactIndex;
import com.braintribe.devrock.mc.core.resolver.BasicDependencyResolver;
import com.braintribe.devrock.model.mc.reason.UnresolvedDependencyVersion;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.IoError;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.version.Version;

public class ArtifactIndexReader {

	private Function<Repository, ArtifactDataResolver> artifactDataResolverFactory;

	@Required
	public void setArtifactDataResolverFactory(Function<Repository, ArtifactDataResolver> artifactDataResolverFactory) {
		this.artifactDataResolverFactory = artifactDataResolverFactory;
	}

	public Maybe<ArtifactIndex> readFullArtifactIndex(Repository repository) {
		return readArtifactIndex(repository, null, null, false);
	}

	/** @see #readArtifactIndex(Repository, Integer, Version, boolean) */
	public Maybe<ArtifactIndex> readArtifactIndex(Repository repository, Integer sinceSequenceNum) {
		return readArtifactIndex(repository, sinceSequenceNum, null, false);
	}

	/**
	 * @param sinceSequenceNum
	 *            if not <code>null</code>, reads entries with a sequence number higher than this value
	 * @param onlyReadIfNewerVersionAvailable
	 *            if <code>true</code> and there is no newer version of meta:artifact-index than what is specified by
	 *            <code>artifactIndexVersion</code>, returns an empty {@link ArtifactIndex}.
	 * @param artifactIndexVersion
	 *            version to compare if <code>onlyReadIfNewerVersionAvailable</code> is <code>true</code>
	 */
	public Maybe<ArtifactIndex> readArtifactIndex( //
			Repository repository, //
			Integer sinceSequenceNum, //
			Version artifactIndexVersion, boolean onlyReadIfNewerVersionAvailable) {

		ArtifactDataResolver artifactDataResolver = artifactDataResolverFactory.apply(repository);

		BasicDependencyResolver dependencyResolver = new BasicDependencyResolver(artifactDataResolver);

		CompiledDependencyIdentification cdi = CompiledDependencyIdentification.create("meta", "artifact-index", "[1,)");

		Maybe<CompiledArtifactIdentification> caiMaybe = dependencyResolver.resolveDependency(cdi);

		if (caiMaybe.isUnsatisfied()) {
			if (caiMaybe.isUnsatisfiedBy(UnresolvedDependencyVersion.T))
				return Maybe.complete(new ArtifactIndex(false));
			else
				return caiMaybe.whyUnsatisfied().asMaybe();
		}

		CompiledArtifactIdentification artifactIndexCai = caiMaybe.get();

		if (onlyReadIfNewerVersionAvailable && !isCaiVersionHigherThan(artifactIndexCai, artifactIndexVersion)) {
			return Reasons.build(NotFound.T) //
					.text("no higher version found of :" + artifactIndexCai.asString()) //
					.toReason() //
					.asMaybe();
		}

		Maybe<ArtifactDataResolution> dataResMaybe = artifactDataResolver.resolvePart(artifactIndexCai, PartIdentification.create("gz"));

		if (dataResMaybe.isUnsatisfied())
			return dataResMaybe.whyUnsatisfied().asMaybe();

		Maybe<InputStream> inMaybe = dataResMaybe.get().openStream();

		if (inMaybe.isUnsatisfied())
			return inMaybe.whyUnsatisfied().asMaybe();

		InputStream inputStream = inMaybe.get();
		return readFile(sinceSequenceNum, repository, artifactIndexCai, inputStream);
	}

	private static Maybe<ArtifactIndex> readFile( //
			Integer sinceSequenceNum, Repository repository, CompiledArtifactIdentification cai, InputStream inputStream) {

		try (InputStream in = new GZIPInputStream(inputStream)) {
			ArtifactIndex index = ArtifactIndex.read(in, false, sinceSequenceNum != null ? sinceSequenceNum : -1);

			return Maybe.complete(index);

		} catch (IOException e) {
			return Reasons.build(IoError.T) //
					.text("Error while reading " + cai.asString() + " from repository " + repository.getName() + extraInfo(repository)) //
					.cause(InternalError.from(e)) //
					.toMaybe();
		}
	}

	private static String extraInfo(Repository repository) {
		if (repository instanceof MavenHttpRepository mavenRepo)
			return " (url: " + mavenRepo.getUrl() + ")";
		else
			return "";
	}

	private boolean isCaiVersionHigherThan(CompiledArtifactIdentification cai, Version version) {
		return version == null || //
				cai.getVersion().compareTo(version) > 0;
	}

}
