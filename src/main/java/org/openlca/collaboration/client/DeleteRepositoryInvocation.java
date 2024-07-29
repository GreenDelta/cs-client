package org.openlca.collaboration.client;

import org.openlca.collaboration.client.WebRequests.Type;

class DeleteRepositoryInvocation extends Invocation<Void, Void> {

	private final String repositoryId;
	
	DeleteRepositoryInvocation(String id) {
		super(Type.DELETE, "repository");
		this.repositoryId = id;
	}
	
	@Override
	protected void checkValidity() {
		checkNotEmpty(repositoryId, "repositoryId");
	}
	
	@Override
	protected String query() {
		return "/" + repositoryId;
	}

}
