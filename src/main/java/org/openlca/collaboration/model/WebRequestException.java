package org.openlca.collaboration.model;

import java.io.IOException;
import java.net.ConnectException;

import javax.net.ssl.SSLHandshakeException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

public class WebRequestException extends Exception {

	private static final long serialVersionUID = 1423557937866180113L;
	private int errorCode;
	private String host;
	private int port;

	public WebRequestException(String url, int statusCode, String message) {
		super(toMessage(message));
		setHostAndPort(url);
		this.errorCode = statusCode;
	}

	private void setHostAndPort(String url) {
		if (url == null || url.isEmpty())
			return;
		if (url.startsWith("https://")) {
			host = url.substring(8);
			port = 443;
		} else if (url.startsWith("http://")) {
			host = url.substring(7);
			port = 80;
		}
		if (host.contains("/")) {
			host = host.substring(0, host.indexOf("/"));
		}
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
		return is(getCause(), ConnectException.class);
	}

	public boolean isSslCertificateException() {
		return is(getCause(), SSLHandshakeException.class);
	}

	private boolean is(Throwable e, Class<? extends Throwable> c) {
		if (c.isInstance(e))
			return true;
		if (e != null && c.isInstance(e.getCause()))
			return true;
		if (e != null && e.getCause() != null && c.isInstance(e.getCause().getCause()))
			return true;
		return false;
	}

	private static String toMessage(String message) {
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
		return errorCode == 401;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

}