package org.openlca.collaboration.api;

import java.io.InputStream;
import java.net.CookieManager;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.openlca.collaboration.api.WebRequests.Type;
import org.openlca.collaboration.model.WebRequestException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

abstract class Invocation<E, T> {

	private static final List<String> MODEL_TYPES = Arrays.asList("PROJECT", "IMPACT_METHOD", "IMPACT_CATEGORY",
			"PRODUCT_SYSTEM", "PROCESS", "FLOW", "FLOW_PROPERTY", "UNIT_GROUP", "ACTOR", "SOURCE", "CATEGORY",
			"LOCATION", "SOCIAL_INDICATOR", "CURRENCY", "PARAMETER", "DQ_SYSTEM", "RESULT", "EPD");

	private final Type type;
	private final String path;
	private final TypeToken<E> entityType;
	private final Class<E> entityClass;
	protected String baseUrl;
	protected CookieManager cookieManager;

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
			if (entityClass != null && InputStream.class.isAssignableFrom(entityClass)) {
				var response = WebRequests.stream(type, url, cookieManager, data());
				if (response.statusCode() == 204)
					return process(null);
				return process((E) response.body());
			}
			var isString = entityType == null
					&& (entityClass == null || entityClass == String.class);
			var response = isString
					? WebRequests.string(type, url, cookieManager, data())
					: WebRequests.json(type, url, cookieManager, data());
			if (response.statusCode() == 204)
				return process(null);
			var string = response.body();
			if (string == null || string.isEmpty())
				return process(null);
			if (isString)
				return process((E) string);
			if (entityType == null)
				return process(new Gson().fromJson(string, entityClass));
			return process(new Gson().fromJson(string, entityType.getType()));
		} catch (WebRequestException e) {
			if (e.getErrorCode() == 404)
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
	protected T process(E response) throws WebRequestException {
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
