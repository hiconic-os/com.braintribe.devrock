// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package com.braintribe.devrock.mc.core.deploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Date;
import java.util.Optional;

import com.braintribe.devrock.mc.api.commons.ArtifactAddress;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.model.mc.reason.PartUploadFailed;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.generic.session.OutputStreamer;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;

public abstract class AbstractFileSystemDeployer<R extends Repository> extends AbstractArtifactDeployer<R> {
	private static Logger log = Logger.getLogger(AbstractFileSystemDeployer.class);

	@Override
	protected TransferContext openTransferContext() {
		return new FileSystemTransferContext();
	}
	
	protected abstract String getRoot();
	protected abstract boolean isLocal();
	
	class FileSystemTransferContext implements TransferContext {
		@Override
		public ArtifactAddressBuilder newAddressBuilder() {
			return ArtifactAddressBuilder.build().root(getRoot());
		}
		
		@Override
		public ArtifactAddress metaDataAddress(Artifact artifact, boolean versioned) {
			ArtifactAddressBuilder builder = versioned?
					newAddressBuilder().versionedArtifact(artifact):
					newAddressBuilder().artifact(artifact);

			if (isLocal())
				return builder.metaData("local");
			else
				return builder.metaData();
		}
		
		@Override
		public Maybe<InputStream> openInputStreamReasoned(ArtifactAddress address) {
			File file = address.toPath().toFile();
			
			if (!file.exists())
				return Reasons.build(NotFound.T).text("File at " + file.getAbsolutePath() + " not found").toMaybe();
			
			try {
				InputStream in = new FileInputStream(file);
				return Maybe.complete(in);
			} catch (FileNotFoundException e) {
				return Reasons.build(NotFound.T).text("File at " + file.getAbsolutePath() + " not found").toMaybe();
			}
		}
		
		@Override
		public Optional<InputStream> openInputStream(ArtifactAddress address) {
			File file = address.toPath().toFile();
			
			if (!file.exists())
				return Optional.empty();
			
			try {
				InputStream in = new FileInputStream(file);
				return Optional.of(in);
			} catch (FileNotFoundException e) {
				throw new UncheckedIOException(e);
			}
		}
		
		@Override
		public Maybe<Resource> transfer(ArtifactAddress address, OutputStreamer outputStreamer, boolean hashWorthy) {
			File file = address.toPath().toFile();
			
			File directory = file.getParentFile();
			directory.mkdirs();
			
			if (!directory.exists()) {
				return Reasons.build(PartUploadFailed.T).text("Could not create directory: " + directory.getAbsolutePath()).toMaybe();
			}
			
			try (OutputStream out = new FileOutputStream(file)) {
				outputStreamer.writeTo(out); 
				FileResource resource = FileResource.T.create();
				
				resource.setName(file.getName());
				resource.setFileSize(file.length());
				resource.setPath(file.getAbsolutePath());
				resource.setCreated(new Date());
				
				return Maybe.complete(resource);
			}
			catch (IOException e) {
				return InternalError.from(e, "Error while writing file: " + file).asMaybe();
			}
		}
		
		@Override
		public Maybe<Void> onUploadComplete(Artifact artifact) {
			return Maybe.complete(null);
		}
		
		@Override
		public void close() {
			// currently nothing to do
		}
	}
	
}
