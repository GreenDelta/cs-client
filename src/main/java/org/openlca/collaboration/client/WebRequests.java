package org.openlca.collaboration.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

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

	static HttpResponse<String> string(HttpClient client, Type type, String url, Object data, DataType dataType)
			throws WebRequestException {
		return call(client, type, url, data, dataType, "application/json;plain/text", BodyHandlers.ofString());
	}

	static HttpResponse<InputStream> stream(HttpClient client, Type type, String url, Object data, DataType dataType)
			throws WebRequestException {
		return call(client, type, url, data, dataType, "application/octet-stream", BodyHandlers.ofInputStream());
	}

	private static <T> HttpResponse<T> call(HttpClient client, Type type, String url, Object data, DataType dataType,
			String accept, BodyHandler<T> handler) throws WebRequestException {
		try {
			HttpResponse<T> response = _call(client, type, url, data, dataType, accept, handler);
			if (response.statusCode() >= 400 && response.statusCode() <= 599)
				throw new WebRequestException(url, response.statusCode(), response.body().toString());
			if (response.statusCode() >= 300 && response.statusCode() <= 399)
				return call(client, type, response.headers().firstValue("location").get(), data, dataType, accept,
						handler);
			return response;
		} catch (Exception e) {
			if (e instanceof WebRequestException w)
				throw w;
			throw new WebRequestException(url, e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> HttpResponse<T> _call(HttpClient client, Type type, String url, Object data, DataType dataType,
			String accept, BodyHandler<T> handler)
			throws URISyntaxException, IOException, InterruptedException {
		var builder = HttpRequest.newBuilder()
				.header("Accept", accept)
				.header("lca-cs-client-api-version", CSClient.API_VERSION)
				.uri(new URI(url));
		if (dataType == DataType.FORM_DATA) {
			var formData = new FormData((Map<String, Object>) data);
			builder = builder
					.header("Content-Type", formData.contentType())
					.method(type.name(), formData.bodyPublisher());
		} else {
			var contentType = dataType != null
					? dataType.contentType
					: "text/plain";
			builder = builder
					.header("Content-Type", contentType)
					.method(type.name(), getBodyPublisher(data, dataType));
		}
		return client.send(builder.build(), handler);
	}

	private static BodyPublisher getBodyPublisher(Object data, DataType dataType) {
		if (dataType == DataType.STREAM && data instanceof InputStream stream)
			return BodyPublishers.ofInputStream(() -> stream);
		if (dataType == DataType.TEXT && data instanceof String string)
			return BodyPublishers.ofString(string);
		if (dataType == DataType.JSON && data != null)
			return BodyPublishers.ofString(new Gson().toJson(data));
		if (data != null)
			throw new IllegalArgumentException("Data does not fit type " + dataType.contentType);
		return BodyPublishers.noBody();
	}

	private static class FormData {

		private final String boundary = UUID.randomUUID().toString().replace("-", "");
		private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		private final Writer stream = new OutputStreamWriter(bytes, StandardCharsets.UTF_8);
		private final PrintWriter printer = new PrintWriter(stream, true);

		private FormData(Map<String, Object> data) throws IOException {
			var keys = new ArrayList<>(data.keySet());
			for (var i = 0; i < keys.size(); i++) {
				var key = keys.get(i);
				printer.append("--")
						.append(boundary)
						.append("\r\n")
						.append("Content-Disposition: form-data; name=\"" + key + "\"");
				var value = data.get(key);
				if (value instanceof InputStream stream) {
					printer.append("; filename=\"\"\r\n")
							.append("Content-Type: application/octet-stream")
							.append("\r\n\r\n");
					printer.flush();
					stream.transferTo(bytes);
					bytes.flush();
				} else if (value instanceof String string) {
					printer.append("\r\n\r\n")
							.append(string);
				}
				printer.append("\r\n");
				printer.flush();
			}
			printer.append("--")
					.append(boundary)
					.append("--\r\n");
			printer.flush();
		}

		private String contentType() {
			return "multipart/form-data; boundary=" + boundary;
		}

		private BodyPublisher bodyPublisher() {
			return BodyPublishers.ofByteArray(bytes.toByteArray());
		}

	}

	static enum Type {

		GET, POST, PUT, DELETE;

	}

	static enum DataType {

		FORM_DATA("multipart/form-data"),

		STREAM("application/octet-stream"),

		TEXT("text/plain"),

		JSON("application/json");

		private String contentType;

		private DataType(String contentType) {
			this.contentType = contentType;
		}

	}

}
