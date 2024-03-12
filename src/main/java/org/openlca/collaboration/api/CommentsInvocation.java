package org.openlca.collaboration.api;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.openlca.collaboration.api.CommentsInvocation.Comment;
import org.openlca.collaboration.api.WebRequests.Type;
import org.openlca.collaboration.api.WebRequests.WebRequestException;

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
		if (e.isConnectException() || e.getErrorCode() == Status.SERVICE_UNAVAILABLE.getStatusCode())
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
					getDate(o, "date"),
					Json.getBoolean(o, "released", false),
					Json.getBoolean(o, "approved", false),
					Json.getLong(o, "replyTo", 0l)//
			));
		}
		return comments;
	}

	private Date getDate(JsonObject obj, String property) {
		var time = Json.getLong(obj, property, 0l);
		if (time == 0l)
			return null;
		var calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		return calendar.getTime();
	}

	public record Comment(long id, String user, String text, String type, String refId, String path, Date date,
			boolean released, boolean approved, long replyTo) {

	}
}
