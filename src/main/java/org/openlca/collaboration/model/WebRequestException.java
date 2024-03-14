package org.openlca.collaboration.model;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;

import javax.net.ssl.SSLHandshakeException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

public class WebRequestException extends Exception {

	private static final long serialVersionUID = 1423557937866180113L;
	private int errorCode;
	private String host;
	private int port;

	public WebRequestException(String url, ClientResponse response) {
		super(toMessage(response));
		setHostAndPort(url);
		this.errorCode = response.getStatus();
	}

	private void setHostAndPort(String url) {
		if (url.startsWith("https://")) {
			url = url.substring(8);
			port = 443;
		} else if (url.startsWith("http://")) {
			url = url.substring(7);
			port = 80;
		}
		host = url.substring(0, url.indexOf("/"));
		if (host.contains(":")) {
			port = Integer.parseInt(host.substring(host.indexOf(":") + 1));
			host = host.substring(0, host.indexOf(":"));
		}
	}

	public WebRequestException(String url, Exception e) {
		super(e);
		setHostAndPort(url);
		this.errorCode = 500;
	}

	public int getErrorCode() {
		return errorCode;
	}

	@Override
	public String getMessage() {
		if (isConnectException())
			return "Server " + host + " on port " + port + " unavailable";
		if (isUnauthorized() && (super.getMessage() == null || super.getMessage().isEmpty()))
			return "Invalid credentials";
		return super.getMessage();
	}

	public String getOriginalMessage() {
		return super.getMessage();
	}

	public boolean isConnectException() {
		if (getCause() instanceof ConnectException)
			return true;
		if (getCause() instanceof SocketException && getCause().getCause() instanceof ClientHandlerException)
			return true;
		if (!(getCause() instanceof ClientHandlerException))
			return false;
		if (getCause().getCause() instanceof ConnectException)
			return true;
		return false;
	}

	public boolean isSslCertificateException() {
		if ((getCause() instanceof ClientHandlerException))
			if (getCause().getCause() instanceof SSLHandshakeException)
				return true;
		return false;
	}

	private static String toMessage(ClientResponse response) {
		var message = response.getEntity(String.class);
		if (!isValid(message))
			return message;
		var json = new Gson().fromJson(message, JsonElement.class);
		if (!json.isJsonObject())
			return message;
		var obj = json.getAsJsonObject();
		if (!obj.has("message"))
			return message;
		var jsonMessage = obj.get("message");
		if (!jsonMessage.isJsonPrimitive())
			return message;
		return jsonMessage.getAsString();
	}

	static boolean isValid(String json) {
		try {
			new Gson().getAdapter(JsonElement.class).fromJson(json);
		} catch (JsonSyntaxException | IOException e) {
			return false;
		}
		return true;
	}

	public boolean isUnauthorized() {
		return errorCode == Status.UNAUTHORIZED.getStatusCode();
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

}