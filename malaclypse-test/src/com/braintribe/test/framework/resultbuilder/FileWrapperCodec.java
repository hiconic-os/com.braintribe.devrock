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
package com.braintribe.test.framework.resultbuilder;

import java.io.File;

import com.braintribe.cc.lcd.HashSupportWrapperCodec;

public class FileWrapperCodec extends HashSupportWrapperCodec<File> {

	@Override
	protected int entityHashCode(File e) {
		return e.getName().hashCode();
	}

	@Override
	protected boolean entityEquals(File e1, File e2) {
		return e1.getName().equalsIgnoreCase(e2.getName());
	}

}
