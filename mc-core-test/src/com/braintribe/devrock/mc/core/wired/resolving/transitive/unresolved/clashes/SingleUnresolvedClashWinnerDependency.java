package com.braintribe.devrock.mc.core.wired.resolving.transitive.unresolved.clashes;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.mc.reason.IncompleteArtifactResolution;
import com.braintribe.devrock.model.mc.reason.IncompleteClashResolving;
import com.braintribe.devrock.model.mc.reason.IncompleteResolution;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * test for the 'lenient handling' of clash resolving issues, i.e. when the winning dependency has no solution backing it up.
 * Makes sure that a) no exception is thrown in mc-core, b) the resolution is correctly flagged, c) the clash issue is correctly reported
 * in the failure reason.
 * NOTE: doesn't fully check the content of the failure, i.e. no check on the actual dependency that failed.
 * 
 * @author pit
 */
public class SingleUnresolvedClashWinnerDependency extends AbstractClashUnresolvedTest {

	@Test
	public void runSingleUnresolvedClashWinnerTest() {
		try {
			AnalysisArtifactResolution resolution = run( "com.braintribe.devrock.test:t#1.0.1", standardClasspathResolutionContext);
			Assert.assertTrue("resolution hasn't failed", resolution.hasFailed());
			
			Reason failure = resolution.getFailure();
			
			Validator validator = new Validator();
			
			if (failure instanceof IncompleteResolution == false) {
				validator.assertTrue("unexpected main reason : " + failure.getClass().getName(), false);
			}
			else {
				List<Reason> reasons = failure.getReasons();
				int size = reasons.size();
				if (size != 2) {
					validator.assertTrue( "unexpected number of reasons:" + size, false);
				}
				else {
					Reason reason1 = reasons.get(0);
					validator.assertTrue("instead of a \"IncompleteArtifactResolution\" reason, another reason is given: " + reason1.getClass().getName() , reason1 instanceof IncompleteArtifactResolution);
					Reason reason2 = reasons.get(1);
					validator.assertTrue("instead of a \"IncompleteClashResolving\" reason, another reason is given: " + reason2.getClass().getName() , reason2 instanceof IncompleteClashResolving);
				}				
			}								
			validator.assertResults();
			
			System.out.println(failure.stringify());
			
		} catch (Exception e) {
			Assert.fail("unexpectedly, exception is thrown: " + e.getMessage());
		}
	}

}
