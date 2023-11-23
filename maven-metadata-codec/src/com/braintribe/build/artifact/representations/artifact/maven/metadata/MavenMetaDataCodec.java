// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.metadata;


import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.artifact.meta.Plugin;
import com.braintribe.model.artifact.meta.Snapshot;
import com.braintribe.model.artifact.meta.SnapshotVersion;
import com.braintribe.model.artifact.meta.Versioning;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

public class MavenMetaDataCodec extends AbstractMetaDataCodec implements Codec<MavenMetaData, Document>, HasTokens {
	
	private static DateTimeFormatter timeFormat = DateTimeFormat.forPattern("yyyyMMddHHmmss");
	private static DateTimeFormatter altTimeFormat = DateTimeFormat.forPattern("yyyyMMdd.HHmmss");
	
	private Function<EntityType<?>, GenericEntity> instanceProvider = t -> t.create();
	
	@Configurable
	public void setInstanceProvider( Function<EntityType<?>, GenericEntity> instanceProvider) {
		this.instanceProvider = instanceProvider;
	}
	
	
	@Override
	public Document encode(MavenMetaData value) throws CodecException {
		try {
			Document document = DomParser.create().makeItSo();
			Element metaDataE = document.createElement( tag_metaData);
			document.appendChild(metaDataE);
			
			// header
			attachAsTextContent(value.getGroupId(), tag_groupId, metaDataE);						
			attachAsTextContent(value.getArtifactId(), tag_artifactId, metaDataE);
			Version versionAsString = value.getVersion();
			if (versionAsString != null) {
				attachAsTextContent( VersionProcessor.toString(versionAsString), tag_version, metaDataE);
			}
			
			//
			// versioning
			//
			Versioning versioning = value.getVersioning();
			if (versioning != null) {
				Element versioningE = document.createElement(tag_versioning);
				metaDataE.appendChild(versioningE);
				
				// latest
				if (versioning.getLatest() != null) {
					attachAsTextContent( VersionProcessor.toString(versioning.getLatest()), tag_latest, versioningE);					
				}
				
				// release
				if (versioning.getRelease() != null) {
					attachAsTextContent( VersionProcessor.toString(versioning.getRelease()), tag_release, versioningE);
				}
				

				
				// snapshot
				Snapshot snapshot = versioning.getSnapshot();
				if (snapshot != null) {
					Element snapshotE = document.createElement(tag_snapshot);
					versioningE.appendChild(snapshotE);
					attachAsTextContent( "" + snapshot.getBuildNumber(), tag_buildNumber, snapshotE);
					attachAsTextContent( "" + snapshot.getLocalCopy(), tag_localCopy, snapshotE);
					attachAsTextContent( timeFormat.print( new DateTime(snapshot.getTimestamp().getTime())), tag_timestamp, snapshotE);					
				}			
				
				// versions
				List<Version> versions = versioning.getVersions();
				if (versions.size() > 0) {
					Element versionsE = document.createElement( tag_versions);
					versioningE.appendChild(versionsE);
					for (Version version : versions) {
						attachAsTextContent( VersionProcessor.toString( version), tag_version, versionsE);
					}
				}
				// lastUpdated
				Date lastUpdated = versioning.getLastUpdated();
				if (lastUpdated != null) {
					attachAsTextContent( timeFormat.print( new DateTime(lastUpdated.getTime())), tag_lastUpdated, versioningE);
				}
				
				// snapshot versions
				List<SnapshotVersion> snapshotVersions = versioning.getSnapshotVersions();
				if (snapshotVersions.size() > 0) {
					Element snapshotVersionsE = document.createElement( tag_snapshotVersions); 
					versioningE.appendChild(snapshotVersionsE);
					for (SnapshotVersion snapshotVersion : snapshotVersions) {
						Element snapshotVersionE = document.createElement(tag_snapshotVersion);
						snapshotVersionsE.appendChild(snapshotVersionE);
						
						attachAsTextContent( snapshotVersion.getClassifier(), tag_classifier, snapshotVersionE);
						attachAsTextContent( snapshotVersion.getValue(), tag_value, snapshotVersionE);
						attachAsTextContent( snapshotVersion.getExtension(), tag_extension, snapshotVersionE);
						attachAsTextContent( timeFormat.print( new DateTime(snapshotVersion.getUpdated().getTime())), tag_updated, snapshotVersionE);
					}
				}

			}
			//
			// plugins
			//
			List<Plugin> plugins = value.getPlugins();
			if (plugins.size()>0) {
				Element pluginsE = document.createElement( tag_plugins);
				metaDataE.appendChild(pluginsE);
				for (Plugin plugin : plugins) {
					Element pluginE = document.createElement(tag_plugin);
					pluginsE.appendChild(pluginE);
					attachAsTextContent(plugin.getName(), tag_name, pluginE);
					attachAsTextContent(plugin.getPrefix(), tag_prefix, pluginE);
					attachAsTextContent(plugin.getArtifactId(), tag_artifactId, pluginE);
				}
			}
			
			return document;
			
		} catch (DOMException e) {
			String msg ="cannot append node";
			throw new CodecException(msg);
		} catch (DomParserException e) {
			String msg ="cannot create document";
			throw new CodecException(msg, e);
		} 
							
	}

	@Override
	public MavenMetaData decode(Document encodedValue) throws CodecException {
		
		MavenMetaData metaData = create( MavenMetaData.T);
		Element parentE = encodedValue.getDocumentElement();
		//
		// header
		//
		metaData.setGroupId( DomUtils.getElementValueByPath(parentE, tag_groupId, false));
		metaData.setArtifactId( DomUtils.getElementValueByPath(parentE, tag_artifactId, false));
		String versionAsString = DomUtils.getElementValueByPath(parentE, tag_version, false);
		if (versionAsString != null) {
			try {
				metaData.setVersion( VersionProcessor.createFromString( versionAsString));
			} catch (VersionProcessingException e) {
				throw new CodecException( "cannot extract valid version from [" + versionAsString + "]", e);
			}
		}
		
		//
		// versioning
		//
		Element versioningE = DomUtils.getElementByPath(parentE, tag_versioning, false);
		if (versioningE != null) {
			Versioning versioning = create(Versioning.T);
			metaData.setVersioning(versioning);
			// latest
			String latest = DomUtils.getElementValueByPath( versioningE, tag_latest, false);
			if (latest != null) {
				try {
					versioning.setLatest( VersionProcessor.createFromString( latest));
				} catch (VersionProcessingException e) {
					throw new CodecException( "cannot extract valid version from latest [" + latest +"]", e);
				}
			}
			// release
			String release = DomUtils.getElementValueByPath( versioningE, tag_release, false);
			if (release != null) {
				try {
					versioning.setRelease( VersionProcessor.createFromString( release));
				} catch (VersionProcessingException e) {
					throw new CodecException( "cannot extract valid version from release : [" + release +"]", e);
				}
			}
			// last updated 
			String lastUpdated = DomUtils.getElementValueByPath( versioningE, tag_lastUpdated, false);
			if (lastUpdated != null) {
				versioning.setLastUpdated( timeFormat.parseDateTime(lastUpdated).toDate());
			}
			// versions 
			Element versionsE = DomUtils.getElementByPath(versioningE, tag_versions, false);
			if (versionsE != null) {
				Iterator<Element> iterator = DomUtils.getElementIterator(versionsE, tag_version);
				while (iterator.hasNext()) {
					Element versionE = iterator.next();
					try {
						Version version = VersionProcessor.createFromString( versionE.getTextContent());
						versioning.getVersions().add( version);
						// TODO: the version process will automatically use GMF to create the version 
						// therefore if a provider's used, the version will not be instantiated by it 
						// requires a "transfer* function that transfers the relevant data (as does the version range processor) 
					} catch (DOMException e) {
						throw new CodecException( "cannot retrieve text content from version node", e);
					} catch (VersionProcessingException e) {
						throw new CodecException( "cannot extract valid version from version string", e);
					}
				}
			}
			// snapshot
			Element snapshotE = DomUtils.getElementByPath(versioningE, tag_snapshot, false);
			if (snapshotE != null) {
				Snapshot snapshot = create( Snapshot.T);
				Date date;
				try {
					date = timeFormat.parseDateTime( DomUtils.getElementValueByPath(snapshotE, tag_timestamp, false)).toDate();
				} catch (Exception e) {
					date = altTimeFormat.parseDateTime( DomUtils.getElementValueByPath(snapshotE, tag_timestamp, false)).toDate();
				}
				
				snapshot.setTimestamp( date);
				
				String buildNumber = DomUtils.getElementValueByPath(snapshotE, tag_buildNumber, false);
				if (buildNumber != null) {
					snapshot.setBuildNumber( Integer.parseInt(buildNumber));
				}
				else {
					snapshot.setBuildNumber(0);
				}
				
				String localCopy = DomUtils.getElementValueByPath(snapshotE, tag_localCopy, false);
				if (localCopy != null) {
					snapshot.setLocalCopy( Boolean.parseBoolean(localCopy));
				}
				else {
					snapshot.setLocalCopy(false);
				}
				versioning.setSnapshot(snapshot);
			}
			
			// snapshot versions
			Element snapshotVersionsE = DomUtils.getElementByPath(versioningE, tag_snapshotVersions, false);
			if (snapshotVersionsE != null) {
				Iterator<Element> iterator = DomUtils.getElementIterator(snapshotVersionsE, tag_snapshotVersion);
				while (iterator.hasNext()) {
					Element snapshotVersionE = iterator.next();
					SnapshotVersion snapshotVersion = create( SnapshotVersion.T);
					snapshotVersion.setClassifier( DomUtils.getElementValueByPath(snapshotVersionE, tag_classifier, false));
					snapshotVersion.setExtension( DomUtils.getElementValueByPath(snapshotVersionE, tag_extension, false));
					snapshotVersion.setValue( DomUtils.getElementValueByPath(snapshotVersionE, tag_value, false));
					String updated = DomUtils.getElementValueByPath(snapshotVersionE, tag_updated, false);
					if (updated != null) {
						try {
							snapshotVersion.setUpdated( timeFormat.parseDateTime(updated).toDate());
						} catch (Exception e) {
							snapshotVersion.setUpdated( altTimeFormat.parseDateTime(updated).toDate());
						}
					}
					versioning.getSnapshotVersions().add( snapshotVersion);
				}				
			}			
		}
		
		
		// 
		// plugins? 
		//
		Element pluginsE = DomUtils.getElementByPath(parentE, tag_plugins, false);
		if (pluginsE != null) {
			Iterator<Element> iterator = DomUtils.getElementIterator(pluginsE, tag_plugin);
			while (iterator.hasNext()){
				Element pluginE = iterator.next();
				Plugin plugin = create( Plugin.T);
				plugin.setName( DomUtils.getElementValueByPath(pluginE, tag_name, false));
				plugin.setPrefix( DomUtils.getElementValueByPath(pluginE, tag_prefix, false));
				plugin.setArtifactId(DomUtils.getElementValueByPath( pluginE, tag_artifactId, false));
				metaData.getPlugins().add( plugin);
			}
		}		 
		return metaData;
	}

	@Override
	public Class<MavenMetaData> getValueClass() {
		return MavenMetaData.class;
	}
	
	@SuppressWarnings("unchecked")
	private <T> T create( EntityType<? extends GenericEntity> entityType) throws CodecException {
		try {
			return (T) instanceProvider.apply(entityType);
		} catch (RuntimeException e) {
			String msg ="instance provider cannot provide new instance of type [" + entityType.getTypeSignature() + "]";				
			throw new CodecException(msg, e);
		}
	}

}
