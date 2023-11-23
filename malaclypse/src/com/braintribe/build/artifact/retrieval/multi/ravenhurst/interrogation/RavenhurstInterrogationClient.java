// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation;

import java.util.Date;

import com.braintribe.build.artifact.retrieval.multi.retrieval.access.http.HttpAccess;
import com.braintribe.common.lcd.Pair;
import com.braintribe.logging.Logger;
import com.braintribe.model.ravenhurst.Artifact;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstRequest;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstResponse;

/**
 * an implementation that can communicate with a Ravenhurst instance, and is otherwise 
 * reliant on HTML support for the rest.
 *  
 * @author Pit
 *
 */
public class RavenhurstInterrogationClient extends AbstractRepositoryHttpInterrogationClient implements RepositoryInterrogationClient {
	private static Logger log = Logger.getLogger(RavenhurstInterrogationClient.class);
	private HttpAccess httpAccess;
	
	public RavenhurstInterrogationClient( HttpAccess httpAccess) {
		super( httpAccess);
		this.httpAccess = httpAccess; 
	}

	@Override
	public RavenhurstResponse interrogate(RavenhurstRequest request) throws RepositoryInterrogationException {
		String url = request.getUrl();		
		return interrogate(request, url);
	}
	
	
	
	@Override
	public RavenhurstResponse extractIndex(RavenhurstRequest request) throws RepositoryInterrogationException {
		String url = request.getIndexUrl();		
		return interrogate(request, url);
	}

	private RavenhurstResponse interrogate(RavenhurstRequest request, String url) throws RepositoryInterrogationException {
		Pair<Date,String> ravenhurstResponse;
		RavenhurstResponse response = RavenhurstResponse.T.create();
		long before = System.nanoTime();
		try {
			ravenhurstResponse = httpAccess.detailedRequire(url, request.getServer(), null);
			response.setResponseDate(ravenhurstResponse.first());
		} catch (Exception e) {
			String msg = "cannot interrogate repository via [" + request.getUrl() + "]";
			log.error( msg, e);
			response.setErrorMsg(msg + " as " + e.getLocalizedMessage());
			response.setIsFaulty(true);
			String logmsg = "unsuccessful access to [" + request.getUrl() + "]";
			response.setElapsedTime(logDif( logmsg, before));
			return response;
		}
		String payload = ravenhurstResponse.getSecond();
		if (payload == null || payload.length() == 0) {
			String logmsg = "empty answer from [" + request.getUrl() + "]";			
			response.setElapsedTime(logDif( logmsg, before));
			return response;
		}
	
		// as string value 
		response.setPayload(payload);
		// decode to actual artifacts 
		try {
			String [] values = payload.split("\n");
			for (String value : values) {
				int gp = value.indexOf(':');
				int vs = value.indexOf( '#');
				Artifact artifact = Artifact.T.create();
				artifact.setGroupId( value.substring(0, gp));
				artifact.setArtifactId( value.substring(gp+1, vs));
				artifact.setVersion( value.substring(vs+1));
				response.getTouchedArtifacts().add( artifact);
			}
		} catch (Exception e) {
			String msg="invalid response [" + payload + "] received for request [" + request.getUrl() + "]";
			log.error( msg);
			String logmsg = "invalid response from [" + request.getUrl() + "]";
			response.setElapsedTime(logDif( logmsg, before));
			return response;
			
		}
		String logmsg = "processing valid answer from [" + request.getUrl() + "]";
		response.setElapsedTime(logDif( logmsg, before));
		return response;
	}
	
	private double logDif( String msg, long before) {
		double dif = getElapsedMillis(before, System.nanoTime());
		//log.debug( msg + " took [" + dif + "] ms");
		return dif;
	}
	private double getElapsedMillis( long before, long after) {
		double dif = after - before;
		return (dif / 1E6);
	}

}
