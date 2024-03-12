package org.openlca.collaboration.api;

import org.openlca.collaboration.api.WebRequests.Type;

import com.google.gson.JsonObject;

public class ServerCheckInvocation extends Invocation<JsonObject, Boolean> {

	protected ServerCheckInvocation() {
		super(Type.GET, "public", JsonObject.class);
	}

	@Override
	protected Boolean process(JsonObject currentUser) {
		return currentUser != null && currentUser.get("id") != null && currentUser.get("id").isJsonPrimitive()
				&& currentUser.get("id").getAsLong() == 0l;
	}

}
