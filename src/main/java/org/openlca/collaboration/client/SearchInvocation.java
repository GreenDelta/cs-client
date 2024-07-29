package org.openlca.collaboration.client;

import org.openlca.collaboration.client.WebRequests.Type;
import org.openlca.collaboration.model.Dataset;
import org.openlca.collaboration.model.SearchResult;

import com.google.gson.reflect.TypeToken;

class SearchInvocation extends Invocation<SearchResult<Dataset>, SearchResult<Dataset>> {

	private final String query;
	private final String type;
	private final int page;
	private final int pageSize;

	SearchInvocation(String query, String type, int page, int pageSize) {
		super(Type.GET, "public/search", new TypeToken<SearchResult<Dataset>>() {
		});
		this.query = query;
		this.type = type;
		this.page = page;
		this.pageSize = pageSize;
	}

	@Override
	protected void checkValidity() {
		checkType(type);
	}

	@Override
	protected String query() {
		var query = "?page=" + page
				+ "&pageSize=" + pageSize;
		if (this.query != null && !this.query.isEmpty()) {
			query += "&query=" + WebRequests.encodeQuery(this.query);
		}
		if (type != null && !type.isEmpty()) {
			query += "&type=" + type;
		}
		return query;
	}

}
