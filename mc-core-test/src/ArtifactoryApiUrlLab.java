import java.net.URI;
import java.net.URISyntaxException;

import com.braintribe.utils.paths.UniversalPath;

public class ArtifactoryApiUrlLab {
	public static void main(String[] args) {
		try {
			String root = "https://artifact.swissre.com/artifactory/maven-documentone-remote/";
			URI uri = new URI( root);		
			UniversalPath path = UniversalPath.empty().pushSlashPath( uri.getPath());
			UniversalPath artifactoryContextUrl = path.pop();
			UniversalPath resturl = artifactoryContextUrl.push("api").push("storage").push( path.getName());
			uri.resolve( "/" + resturl.toSlashPath());			
			URI newUri = new URI( uri.getScheme() + "://" + uri.getAuthority() +  resturl.toSlashPath());
			System.out.println(newUri.toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
