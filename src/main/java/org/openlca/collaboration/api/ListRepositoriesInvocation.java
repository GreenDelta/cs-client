package org.openlca.collaboration.api;

import java.util.List;

import org.openlca.collaboration.api.WebRequests.Type;
import org.openlca.collaboration.model.Repository;
import org.openlca.collaboration.model.SearchResult;

import com.google.gson.reflect.TypeToken;

public class ListRepositoriesInvocation extends Invocation<SearchResult<Repository>, List<Repository>> {

	ListRepositoriesInvocation() {
		super(Type.GET, "repository", new TypeToken<SearchResult<Repository>>() {
		});
	}
	
	@Override
	protected List<Repository> process(SearchResult<Repository> response) {
		return response.data();
	}

}
