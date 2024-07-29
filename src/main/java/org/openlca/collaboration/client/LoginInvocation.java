package org.openlca.collaboration.client;

import java.net.CookieManager;
import java.net.http.HttpResponse;
import java.util.HashMap;

import org.openlca.collaboration.client.WebRequests.Type;
import org.openlca.collaboration.model.Credentials;
import org.openlca.collaboration.model.WebRequestException;

class LoginInvocation {

	private static final String PATH = "public/login";
	String baseUrl;
	Credentials credentials;
	CookieManager cookieManager;

	String execute() throws WebRequestException {
		var response = _execute(credentials.token());
		if (response.statusCode() != 200)
			return null;
		var result = response.body();
		if ("tokenRequired".equals(result)) {
			var token = credentials.promptToken();
			if (token == null)
				return null; // TODO throw exception?
			response = _execute(token);
		}
		return response.headers().firstValue("JSESSIONID").orElse(null);
	}

	private HttpResponse<String> _execute(String token) throws WebRequestException {
		if (baseUrl == null || baseUrl.isEmpty())
			throw new IllegalArgumentException("No base url set");
		if (credentials == null)
			throw new IllegalArgumentException("No credentials set");
		var username = credentials.username();
		if (username == null || username.isEmpty())
			throw new IllegalArgumentException("No username set");
		var password = credentials.password();
		if (password == null || password.isEmpty())
			throw new IllegalArgumentException("No password set");
		var url = baseUrl + "/" + PATH;
		var data = new HashMap<String, String>();
		data.put("username", username);
		data.put("password", password);
		if (token != null && !token.isEmpty()) {
			data.put("token", token);
		}
		return WebRequests.string(Type.POST, url, cookieManager, data);
	}

}
