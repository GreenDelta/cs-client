package org.openlca.collaboration.api;

import java.util.HashMap;

import org.openlca.collaboration.api.WebRequests.Type;
import org.openlca.collaboration.model.Credentials;
import org.openlca.collaboration.model.WebRequestException;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Invokes a web service call to login
 */
class LoginInvocation {

	private static final String PATH = "public/login";
	String baseUrl;
	Credentials credentials;

	String execute() throws WebRequestException {
		var response = _execute(credentials.token());
		if (response.getStatus() != Status.OK.getStatusCode())
			return null;
		var result = response.getEntity(String.class);
		if ("tokenRequired".equals(result)) {
			var token = credentials.promptToken();
			if (token == null)
				return null; // TODO throw exception?
			response = _execute(token);
		}
		for (var cookie : response.getCookies())
			if (cookie.getName().equals("JSESSIONID"))
				return cookie.getValue();
		return null;
	}

	private ClientResponse _execute(String token) throws WebRequestException {
		if (baseUrl == null || baseUrl.isEmpty())
			throw new IllegalArgumentException("No base url set");
		if (credentials == null)
			throw new IllegalArgumentException("No credentials set");
		var username = credentials.username() ;
		if (username == null || username.isEmpty())
			throw new IllegalArgumentException("No username set");
		var password = credentials.password() ;
		if (password == null || password.isEmpty())
			throw new IllegalArgumentException("No password set");
		var url = baseUrl + "/" + PATH;
		var data = new HashMap<String, String>();
		data.put("username", username);
		data.put("password", password);
		if (token != null && !token.isEmpty()) {
			data.put("token", token);
		}
		return WebRequests.call(Type.POST, url, null, data);
	}

}
