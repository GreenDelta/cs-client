package org.openlca.collaboration.client;

import java.io.InputStream;

import org.openlca.collaboration.client.WebRequests.Type;

class DownloadJsonInvocation extends Invocation<InputStream, InputStream> {

	private final String token;

	DownloadJsonInvocation(String token) {
		super(Type.GET, "public/download/json", InputStream.class);
		this.token = token;
	}

	@Override
	protected void checkValidity() {
		checkNotEmpty(token, "token");
	}

	@Override
	protected String query() {
		return "/" + token;
	}

}