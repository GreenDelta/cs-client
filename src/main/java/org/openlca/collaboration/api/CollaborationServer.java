package org.openlca.collaboration.api;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

import javax.ws.rs.core.Response.Status;

import org.openlca.collaboration.api.AnnouncementInvocation.Announcement;
import org.openlca.collaboration.api.WebRequests.WebRequestException;
import org.openlca.collaboration.model.Comment;
import org.openlca.collaboration.model.Credentials;
import org.openlca.collaboration.model.Dataset;
import org.openlca.collaboration.model.Entry;
import org.openlca.collaboration.model.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollaborationServer {

	private static final Logger log = LoggerFactory.getLogger(CollaborationServer.class);
	public static final String API_VERSION = "2.0.0";
	public final String url;
	private final Supplier<Credentials> credentialsSupplier;
	private Credentials credentials;
	private final String apiUrl;
	private String sessionId;

	public CollaborationServer(String url, Supplier<Credentials> credentialsSupplier) {
		this.url = url;
		this.apiUrl = url + "/ws";
		this.credentialsSupplier = credentialsSupplier;
	}

	private Credentials credentials() {
		if (credentials == null) {
			credentials = credentialsSupplier.get();
		}
		return credentials;
	}

	public boolean isCollaborationServer() throws WebRequestException {
		var invocation = new ServerCheckInvocation();
		invocation.baseUrl = apiUrl;
		var result = invocation.execute();
		if (result != null)
			return result;
		return false;
	}

	public Announcement getAnnouncement() throws WebRequestException {
		var invocation = new AnnouncementInvocation();
		invocation.baseUrl = apiUrl;
		return invocation.execute();
	}

	public List<Comment> getComments(String repositoryId) throws WebRequestException {
		return executeLoggedIn(new CommentsInvocation(repositoryId));
	}

	public List<Comment> getComments(String repositoryId, String type, String refId) throws WebRequestException {
		return executeLoggedIn(new CommentsInvocation(repositoryId, type, refId));
	}

	public InputStream downloadLibrary(String library) throws WebRequestException {
		return executeLoggedIn(new LibraryDownloadInvocation(library));
	}

	public boolean downloadJson(String repositoryId, String type, String refId, File toFile) throws WebRequestException {
		var token = executeLoggedIn(new DownloadJsonPrepareInvocation(repositoryId, type, refId));
		executeLoggedIn(new DownloadJsonInvocation(token, toFile));
		return true;
	}
	
	public List<Entry> browse(String repositoryId, String path) throws WebRequestException {
		return executeLoggedIn(new BrowseInvocation(repositoryId, path));
	}

	public SearchResult<Dataset> search(String repositoryId, String query, String type, int page, int pageSize) throws WebRequestException {
		return executeLoggedIn(new SearchInvocation(repositoryId, query, type, page, pageSize));
	}

	private <T> T executeLoggedIn(Invocation<?, T> invocation) throws WebRequestException {
		invocation.baseUrl = apiUrl;
		if (sessionId == null && !login())
			return null;
		invocation.sessionId = sessionId;
		try {
			return invocation.execute();
		} catch (WebRequestException e) {
			if (e.getErrorCode() == Status.UNAUTHORIZED.getStatusCode()) {
				if (!credentials.onUnauthenticated() || !login())
					return null;
				invocation.sessionId = sessionId;
				return invocation.execute();
			} else if (e.isConnectException()) {
				log.error("Collaboration server request failed", e);
				return null;
			}
			throw e;
		}
	}

	private boolean login() throws WebRequestException {
		var invocation = new LoginInvocation();
		invocation.baseUrl = apiUrl;
		invocation.credentials = credentials();
		if (invocation.credentials == null)
			return false;
		try {
			sessionId = invocation.execute();
		} catch (WebRequestException e) {
			if (e.isConnectException()) {
				log.error("Collaboration server request failed", e);
				return false;
			}
			if (e.getErrorCode() == Status.UNAUTHORIZED.getStatusCode()) {
				if (credentials.onUnauthenticated())
					return login();
			} else if (e.getErrorCode() == Status.FORBIDDEN.getStatusCode()) {
				if (credentials.onUnauthorized())
					return login();
			}
			throw e;
		}
		return sessionId != null;
	}

	public void close() throws WebRequestException {
		logout();
	}

	private void logout() throws WebRequestException {
		if (sessionId == null)
			return;
		try {
			var invocation = new LogoutInvocation();
			invocation.baseUrl = apiUrl;
			invocation.sessionId = sessionId;
			invocation.execute();
		} catch (WebRequestException e) {
			if (e.getErrorCode() != Status.UNAUTHORIZED.getStatusCode()
					&& e.getErrorCode() != Status.CONFLICT.getStatusCode())
				throw e;
		}
		sessionId = null;
	}
}
