package org.openlca.collaboration.client;

import java.util.List;

import org.openlca.collaboration.client.WebRequests.Type;
import org.openlca.collaboration.model.Repository;
import org.openlca.collaboration.model.SearchResult;

import com.google.gson.reflect.TypeToken;

class ListRepositoriesInvocation extends Invocation<SearchResult<Repository>, List<Repository>> {

	ListRepositoriesInvocation() {
		super(Type.GET, "repository", new TypeToken<SearchResult<Repository>>() {
		});
	}
	
	@Override
	protected List<Repository> process(SearchResult<Repository> response) {
		return response.data();
	}
	
	@Override
	protected String query() {
		return "?page=0";
	}

}
