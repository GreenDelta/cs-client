package org.openlca.collaboration.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.openlca.collaboration.api.WebRequests.Type;
import org.openlca.collaboration.model.Entry;
import org.openlca.collaboration.model.TypeOfEntry;

import com.google.gson.JsonObject;

/**
 * Invokes a webservice call to browse the given repository
 */
public final class BrowseInvocation extends Invocation<JsonObject, List<Entry>> {

	private final String repositoryId;
	private final String path;

	BrowseInvocation(String repositoryId, String path) {
		super(Type.GET, "public/browse", JsonObject.class);
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

	protected List<Entry> process(JsonObject response) {
		if (response == null || !response.has("data"))
			return new ArrayList<>();
		var array = Json.toJsonArray(response.get("data"));
		if (array == null)
			return new ArrayList<>();
		var entries = new ArrayList<Entry>();
		for (var e : array) {
			var o = Json.toJsonObject(e);
			entries.add(new Entry(
					TypeOfEntry.valueOf(Json.getString(o, "typeOfEntry")),
					Json.getString(o, "path"),
					Json.getString(o, "name"),
					Json.getString(o, "commitId"),
					Json.getString(o, "flowType"),
					Json.getString(o, "processType"),
					Json.getInt(o, "count", 0)));
		}
		return entries;
	}

}