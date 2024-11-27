package org.openlca.collaboration.client;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openlca.collaboration.client.WebRequests.Type;
import org.openlca.collaboration.model.SearchResult;

import com.google.gson.reflect.TypeToken;

class ListGroupsInvocation extends Invocation<SearchResult<Group>, List<String>> {

	private final boolean canWrite;

	static ListGroupsInvocation readable() {
		return new ListGroupsInvocation(false);
	}

	static ListGroupsInvocation writable() {
		return new ListGroupsInvocation(true);
	}

	private ListGroupsInvocation(boolean canWrite) {
		super(Type.GET, "group", new TypeToken<SearchResult<Group>>() {
		});
		this.canWrite = canWrite;
	}

	@Override
	protected List<String> process(SearchResult<Group> response) {
		return response.data().stream()
				.map(Group::name)
				.filter(Predicate.not(Objects::isNull))
				.distinct()
				.sorted((n1, n2) -> n1.toLowerCase().compareTo(n2.toLowerCase()))
				.collect(Collectors.toList());
	}

	@Override
	protected String query() {
		return "?onlyIfCanWrite=" + canWrite + "&page=0";
	}

}
