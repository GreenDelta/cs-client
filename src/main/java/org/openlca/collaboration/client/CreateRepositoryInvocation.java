package org.openlca.collaboration.client;

import org.openlca.collaboration.client.WebRequests.Type;

class CreateRepositoryInvocation extends Invocation<Void, Void> {

	private final String repositoryId;
	
	CreateRepositoryInvocation(String repositoryId) {
		super(Type.POST, "repository");
		this.repositoryId = repositoryId;
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
