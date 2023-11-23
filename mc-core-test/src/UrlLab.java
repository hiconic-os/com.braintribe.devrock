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
