package org.openlca.collaboration.client;

import java.util.ArrayList;
import java.util.List;

import org.openlca.collaboration.client.WebRequests.Type;
import org.openlca.collaboration.model.Comment;
import org.openlca.collaboration.model.WebRequestException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Invokes a web service call to retrieve comments of a dataset from a
 * repository
 */
public class CommentsInvocation extends Invocation<JsonObject, List<Comment>> {

	private final String repositoryId;
	private final String type;
	private final String refId;

	CommentsInvocation(String repositoryId) {
		this(repositoryId, null, null);
	}

	CommentsInvocation(String repositoryId, String type, String refId) {
		super(Type.GET, "comment", JsonObject.class);
		this.repositoryId = repositoryId;
		this.type = type;
		this.refId = refId;
	}

	@Override
	protected void checkValidity() {
		checkNotEmpty(repositoryId, "repository id");
		checkType(type);
	}

	@Override
	protected String query() {
		var query = "/" + repositoryId;
		if (type != null && !type.isEmpty() && refId != null) {
			query += "/" + type + "/" + refId;
		} else {
			query += "?includeReplies=true";
		}
		return query;
	}

	@Override
	protected List<Comment> process(JsonObject data) {
		if (data == null)
			return new ArrayList<>();
		var field = type != null && refId != null ? "comments" : "data";
		return parseComments(Json.toJsonArray(data.get(field)));
	}

	@Override
	protected List<Comment> handleError(WebRequestException e) throws WebRequestException {
		if (e.isConnectException() || e.getErrorCode() == 503)
			return new ArrayList<>();
		throw e;
	}

	private List<Comment> parseComments(JsonArray value) {
		if (value == null)
			return new ArrayList<>();
		var comments = new ArrayList<Comment>();
		for (var e : value) {
			var o = Json.toJsonObject(e);
			var field = Json.toJsonObject(o.get("field"));
			comments.add(new Comment(
					Json.getLong(o, "id", 0l),
					Json.getString(o.get("user"), "name"),
					Json.getString(o, "text"),
					Json.getString(field, "refId"),
					Json.getString(field, "modelType"),
					Json.getString(field, "path"),
					Json.getDate(o, "date"),
					Json.getBoolean(o, "released", false),
					Json.getBoolean(o, "approved", false),
					Json.getLong(o, "replyTo", 0l)//
			));
		}
		return comments;
	}

}
