package org.openlca.collaboration.client;

import org.openlca.collaboration.client.WebRequests.Type;

class LogoutInvocation extends Invocation<Void, Void> {

	LogoutInvocation() {
		super(Type.POST, "public/logout");
	}

}
