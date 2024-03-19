package org.openlca.collaboration.client;

import org.openlca.collaboration.client.WebRequests.Type;

class DownloadJsonPrepareInvocation extends Invocation<String, String> {

	private final String repositoryId;
	private final String type;
	private final String refId;

	DownloadJsonPrepareInvocation(String repositoryId, String type, String refId) {
		super(Type.GET, "public/download/json/prepare", String.class);
		this.repositoryId = repositoryId;
		this.type = type;
		this.refId = refId;
	}

	@Override
	protected void checkValidity() {
		checkNotEmpty(repositoryId, "repository id");
		checkNotEmpty(type, "type");
		checkNotEmpty(refId, "refId");
		checkType(type);
	}

	@Override
	protected String query() {
		return "/" + repositoryId + "/" + type + "/" + refId;
	}
}
