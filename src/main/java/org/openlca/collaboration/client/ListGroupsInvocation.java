package org.openlca.collaboration.client;

import java.util.List;

import org.openlca.collaboration.client.WebRequests.Type;
import org.openlca.collaboration.model.SearchResult;

import com.google.gson.reflect.TypeToken;

class ListGroupsInvocation extends Invocation<SearchResult<String>, List<String>> {

	private final boolean canWrite;

	static ListGroupsInvocation readable() {
		return new ListGroupsInvocation(false);
	}

	static ListGroupsInvocation writable() {
		return new ListGroupsInvocation(true);
	}
	
	private ListGroupsInvocation(boolean canWrite) {
		super(Type.GET, "groups", new TypeToken<SearchResult<String>>() {
		});
		this.canWrite = canWrite;
	}
	
	@Override
	protected List<String> process(SearchResult<String> response) {
		return response.data();
	}
	
	@Override
	protected String query() {
		return "?onlyIfCanWrite=" + canWrite + "&page=0";
	}

}
