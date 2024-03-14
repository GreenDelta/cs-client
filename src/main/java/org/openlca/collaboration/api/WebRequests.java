package org.openlca.collaboration.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;

import org.openlca.collaboration.Ssl;
import org.openlca.collaboration.model.Credentials;
import org.openlca.collaboration.model.WebRequestException;

import com.google.gson.Gson;

class WebRequests {

	static {
	}

	static String encodeQuery(String query) {
		try {
			return new URI(null, null, query, null, null).toString();
		} catch (URISyntaxException e) {
			return query;
		}
	}

	static HttpResponse<String> string(Type type, String url, CookieManager cookieManager, Object data)
			throws WebRequestException {
		return call(type, url, cookieManager, data, "plain/text", BodyHandlers.ofString());
	}

	static HttpResponse<String> json(Type type, String url, CookieManager cookieManager, Object data)
			throws WebRequestException {
		return call(type, url, cookieManager, data, "application/json", BodyHandlers.ofString());
	}

	static HttpResponse<InputStream> stream(Type type, String url, CookieManager cookieManager, Object data)
			throws WebRequestException {
		return call(type, url, cookieManager, data, "application/octet-stream", BodyHandlers.ofInputStream());
	}

	private static <T> HttpResponse<T> call(Type type, String url, CookieManager cookieManager, Object data,
			String accept, BodyHandler<T> handler) throws WebRequestException {
		try {
			HttpResponse<T> response = call(type, url, data, cookieManager, accept, handler);
			if (response.statusCode() >= 400 && response.statusCode() <= 599)
				throw new WebRequestException(url, response.statusCode(), response.body().toString());
			if (response.statusCode() >= 300 && response.statusCode() <= 399)
				return call(type, response.headers().firstValue("location").get(), cookieManager, data, accept,
						handler);
			return response;
		} catch (Exception e) {
			if (e instanceof WebRequestException w)
				throw w;
			throw new WebRequestException(url, e);
		}
	}

	private static <T> HttpResponse<T> call(Type type, String url, Object data, CookieManager cookieManager,
			String accept, BodyHandler<T> handler) throws URISyntaxException, IOException, InterruptedException {
		var builder = HttpRequest.newBuilder()
				.header("Accept", accept)
				.header("Content-Type", getContentType(data))
				.header("lca-cs-client-api-version", CollaborationServer.API_VERSION)
				.uri(new URI(url));
		return createClient(cookieManager).send(builder.method(type.name(), getBodyPublisher(data)).build(), handler);
	}

	private static BodyPublisher getBodyPublisher(Object data) {
		if (data instanceof InputStream stream)
			return BodyPublishers.ofInputStream(() -> stream);
		if (data != null)
			return BodyPublishers.ofString(new Gson().toJson(data));
		return BodyPublishers.noBody();
	}

	private static String getContentType(Object data) {
		if (data instanceof InputStream stream)
			return "application/octet-stream";
		if (data != null && !(data instanceof String))
			return "application/json";
		return "text/plain";
	}

	private static HttpClient createClient(CookieManager cookieManager) {
		return HttpClient.newBuilder()
				.cookieHandler(cookieManager)
				.followRedirects(Redirect.NEVER)
				.sslContext(Ssl.createContext()).build();
	}

	static enum Type {
		GET, POST, PUT, DELETE;
	}

	public static void main(String[] args) throws WebRequestException {
		var cs = new CollaborationServer("https://collab.greendelta.com", () -> new Credentials() {

			@Override
			public String username() {
				return "greve";
			}

			@Override
			public String token() {
				return null;
			}

			@Override
			public String promptToken() {
				return null;
			}

			@Override
			public String password() {
				return "KaIsEr!4=5/";
			}

			@Override
			public boolean onUnauthorized() {
				return false;
			}

			@Override
			public boolean onUnauthenticated() {
				return false;
			}
		});
		cs.listRepositories().forEach(repo -> System.out.println(repo.group() + "/" + repo.name()));
	}

}
