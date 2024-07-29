package org.openlca.collaboration.client;

import org.openlca.collaboration.client.WebRequests.Type;

class CreateRepositoryInvocation extends Invocation<Void, Void> {

	private final String group;
	private final String name;
	
	CreateRepositoryInvocation(String group, String name) {
		super(Type.POST, "repository");
		this.group = group;
		this.name = name;
	}
	
	@Override
	protected void checkValidity() {
		checkNotEmpty(group, "group");
		checkNotEmpty(name, "name");
	}
	
	@Override
	protected String query() {
		return group + "/" + name;
	}

}
