package org.openlca.collaboration.api;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;

import org.openlca.collaboration.Ssl;
import org.openlca.collaboration.model.WebRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

class WebRequests {

	private static final Logger log = LoggerFactory.getLogger(WebRequests.class);

	static {
	}

	static String encodeQuery(String query) {
		try {
			return new URI(null, null, query, null, null).toString();
		} catch (URISyntaxException e) {
			return query;
		}
	}

	static ClientResponse call(Type type, String url, String sessionId) throws WebRequestException {
		return call(type, url, sessionId, null);
	}

	static ClientResponse call(Type type, String url, String sessionId, Object data) throws WebRequestException {
		log.info(type.name() + " " + url);
		var request = builder(url, sessionId, data);
		try {
			var response = call(type, request);
			if (response.getStatus() >= 400 && response.getStatus() <= 599)
				throw new WebRequestException(url, response);
			if (response.getStatusInfo().getFamily() == Family.REDIRECTION)
				return call(type, response.getLocation().toString(), sessionId, data);
			return response;
		} catch (Exception e) {
			if (e instanceof WebRequestException)
				throw e;
			throw new WebRequestException(url, e);
		}
	}

	private static ClientResponse call(Type type, Builder builder) {
		switch (type) {
		case GET:
			return builder.get(ClientResponse.class);
		case POST:
			return builder.post(ClientResponse.class);
		case PUT:
			return builder.put(ClientResponse.class);
		case DELETE:
			return builder.delete(ClientResponse.class);
		default:
			return null;
		}
	}

	private static Builder builder(String url, String sessionId, Object data) {
		var resource = createClient().resource(url);
		var builder = resource.accept(MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_PLAIN_TYPE,
				MediaType.APPLICATION_OCTET_STREAM_TYPE);
		builder.header("lca-cs-client-api-version", CollaborationServer.API_VERSION);
		if (sessionId != null) {
			builder.cookie(new Cookie("JSESSIONID", sessionId));
		}
		if (data instanceof InputStream) {
			builder.entity(data, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		} else if (data != null) {
			builder.entity(new Gson().toJson(data), MediaType.APPLICATION_JSON_TYPE);
		}
		return builder;
	}

	private static Client createClient() {
		var config = new DefaultClientConfig();
		var context = Ssl.createContext();
		if (context != null) {
			config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
					new HTTPSProperties(HttpsURLConnection.getDefaultHostnameVerifier(), context));
		}
		config.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, false);
		var client = Client.create(config);
		client.setChunkedEncodingSize(1024 * 100); // 100kb
		return client;
	}

	static enum Type {
		GET, POST, PUT, DELETE;
	}

}
