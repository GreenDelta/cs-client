package org.openlca.collaboration.api;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.openlca.collaboration.api.WebRequests.Type;
import org.openlca.collaboration.model.WebRequestException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.ClientResponse.Status;

abstract class Invocation<E, T> {

	private static final List<String> MODEL_TYPES = Arrays.asList("PROJECT", "IMPACT_METHOD", "IMPACT_CATEGORY",
			"PRODUCT_SYSTEM", "PROCESS", "FLOW", "FLOW_PROPERTY", "UNIT_GROUP", "ACTOR", "SOURCE", "CATEGORY",
			"LOCATION", "SOCIAL_INDICATOR", "CURRENCY", "PARAMETER", "DQ_SYSTEM", "RESULT", "EPD");

	private final Type type;
	private final String path;
	private final TypeToken<E> entityType;
	private final Class<E> entityClass;
	protected String baseUrl;
	protected String sessionId;

	protected Invocation(Type type, String path, TypeToken<E> entityType) {
		this.type = type;
		this.path = path;
		this.entityClass = null;
		this.entityType = entityType;
	}

	protected Invocation(Type type, String path, Class<E> entityClass) {
		this.type = type;
		this.path = path;
		this.entityClass = entityClass;
		this.entityType = null;
	}

	@SuppressWarnings("unchecked")
	public final T execute() throws WebRequestException {
		checkNotEmpty(baseUrl, "base url");
		checkValidity();
		var url = baseUrl + "/" + path;
		var part = query();
		if (part != null && !part.isEmpty()) {
			url += part;
		}
		try {
			var response = WebRequests.call(type, url, sessionId, data());
			if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
				return process(null);
			if (entityClass != null && InputStream.class.isAssignableFrom(entityClass))
				return process((E) response.getEntityInputStream());
			var string = response.getEntity(String.class);
			if (string == null || string.isEmpty())
				return process(null);
			if (entityType == null && (entityClass == null || entityClass == String.class))
				return process((E) string);
			if (entityType == null)
				return process(new Gson().fromJson(string, entityClass));
			return process(new Gson().fromJson(string, entityType.getType()));
		} catch (WebRequestException e) {
			if (e.getErrorCode() == Status.NOT_FOUND.getStatusCode())
				return null;
			return handleError(e);
		}
	}

	protected void checkValidity() {
		// subclasses may override
	}

	protected String query() {
		// subclasses may override
		return "";
	}

	protected Object data() {
		// subclasses may override
		return null;
	}

	@SuppressWarnings("unchecked")
	protected T process(E response) {
		// subclasses may override
		return (T) response;
	}

	protected T handleError(WebRequestException e) throws WebRequestException {
		// subclasses may override
		throw e;
	}

	protected void checkNotEmpty(String value, String name) {
		if (value == null || value.isEmpty())
			throw new IllegalArgumentException("No " + name + " set");
	}

	protected void checkNotEmpty(Collection<?> value, String name) {
		if (value == null || value.isEmpty())
			throw new IllegalArgumentException("No " + name + " set");
	}

	protected void checkNotEmpty(Object value, String name) {
		if (value == null)
			throw new IllegalArgumentException("No " + name + " set");
	}

	protected void checkType(String type) {
		if (type == null)
			return;
		if (!MODEL_TYPES.contains(type))
			throw new IllegalArgumentException("Unsupported model type " + type);
	}

}
