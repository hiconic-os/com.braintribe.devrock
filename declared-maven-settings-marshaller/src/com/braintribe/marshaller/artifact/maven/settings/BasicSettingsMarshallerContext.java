// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.marshaller.artifact.maven.settings;

import com.braintribe.marshaller.artifact.maven.settings.experts.SettingsMarshallerContext;
import com.braintribe.model.generic.session.GmSession;

public class BasicSettingsMarshallerContext implements SettingsMarshallerContext {
	private StringBuilder builder;
	private GmSession session;

	public BasicSettingsMarshallerContext(StringBuilder builder, GmSession session) {
		this.builder = builder;
		this.session = session;		
	}

	@Override
	public StringBuilder getCommonStringBuilder() {
		return builder;
	}

	@Override
	public GmSession getSession() {
		return session;
	}

}
