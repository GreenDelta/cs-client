package org.openlca.collaboration.client;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.HashMap;

import org.openlca.collaboration.client.WebRequests.DataType;
import org.openlca.collaboration.client.WebRequests.Type;
import org.openlca.collaboration.model.Credentials;
import org.openlca.collaboration.model.WebRequestException;

class LoginInvocation {

	private static final String PATH = "public/login";
	String baseUrl;
	Credentials credentials;
	HttpClient client;

	void execute() throws WebRequestException {
		var response = _execute(credentials.token());
		if (response.statusCode() != 200)
			return;
		var result = response.body();
		if ("tokenRequired".equals(result)) {
			var token = credentials.promptToken();
			if (token == null)
				return; // TODO throw exception?
			response = _execute(token);
		}
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
		return WebRequests.string(client, Type.POST, url, data, DataType.JSON);
	}

}
