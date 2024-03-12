package org.openlca.collaboration.api;

import org.openlca.collaboration.api.WebRequests.Type;

/**
 * Invokes a web service call to logout
 */
class LogoutInvocation extends Invocation<Void, Void> {

	LogoutInvocation() {
		super(Type.POST, "public/logout", Void.class);
	}

}
