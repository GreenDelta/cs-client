package org.openlca.collaboration.client;

import org.openlca.collaboration.client.WebRequests.Type;

/**
 * Invokes a web service call to logout
 */
class LogoutInvocation extends Invocation<Void, Void> {

	LogoutInvocation() {
		super(Type.POST, "public/logout", Void.class);
	}

}
