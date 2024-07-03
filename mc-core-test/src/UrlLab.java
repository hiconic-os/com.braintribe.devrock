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
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;

import com.braintribe.utils.IOTools;

public class UrlLab {
	public static void main(String[] args) {
		try {
			Authenticator.setDefault (new Authenticator() {
			    protected PasswordAuthentication getPasswordAuthentication() {
			        return new PasswordAuthentication ("tribefire@roboyo.de", "RoboTribe2022!".toCharArray());
			    }
			});
			
			URL url = new URL("https://dev.azure.com/roboyo/tribefire-test/_apis");
			
			String text = IOTools.slurp(url);
			
			System.out.println(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
