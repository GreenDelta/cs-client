package org.openlca.collaboration.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.openlca.collaboration.client.WebRequests.Type;
import org.openlca.collaboration.model.Entry;
import org.openlca.collaboration.model.SearchResult;

import com.google.gson.reflect.TypeToken;

final class BrowseInvocation extends Invocation<SearchResult<Entry>, List<Entry>> {

	private final String repositoryId;
	private final String path;

	BrowseInvocation(String repositoryId, String path) {
		super(Type.GET, "public/browse", new TypeToken<SearchResult<Entry>>() {
		});
		this.repositoryId = repositoryId;
		this.path = path;
	}

	@Override
	protected void checkValidity() {
		checkNotEmpty(repositoryId, "repository id");
	}

	@Override
	protected String query() {
		var query = "/" + repositoryId;
		if (path != null && !path.isEmpty()) {
			try {
				query += "?categoryPath=" + URLEncoder.encode(path, StandardCharsets.UTF_8.toString());
			} catch (UnsupportedEncodingException e) {
				throw new IllegalArgumentException("path can not be url encoded: " + e.getMessage() + ": " + path);
			}
		}
		return query;
	}

	protected List<Entry> process(SearchResult<Entry> response) {
		return response.data();
	}

}