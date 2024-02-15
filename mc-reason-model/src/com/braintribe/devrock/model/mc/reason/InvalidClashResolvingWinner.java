package com.braintribe.devrock.model.mc.reason;

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("winner dependency [${dependency}] amongst [${choices}] has no solution")
public interface InvalidClashResolvingWinner extends McReason {
	
	EntityType<InvalidClashResolvingWinner> T = EntityTypes.T(InvalidClashResolvingWinner.class);
	
	String dependency = "dependency";
	String choices = "choices";

	String getDependency();
	void setDependency(String value);

	String getChoices();
	void setChoices(String value);

	
}
