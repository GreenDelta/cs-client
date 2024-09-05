package org.openlca.collaboration.client;

import java.io.File;
import java.io.InputStream;
import java.net.CookieManager;
import java.util.List;
import java.util.function.Supplier;

import org.openlca.collaboration.model.Comment;
import org.openlca.collaboration.model.Credentials;
import org.openlca.collaboration.model.Dataset;
import org.openlca.collaboration.model.Entry;
import org.openlca.collaboration.model.LibraryInfo;
import org.openlca.collaboration.model.Repository;
import org.openlca.collaboration.model.SearchResult;
import org.openlca.collaboration.model.WebRequestException;

public class CSClient {

	public static final String API_VERSION = "2.0.0";
	public final String url;
	private final Supplier<Credentials> credentialsSupplier;
	private Credentials credentials;
	private final String apiUrl;
	private CookieManager cookieManager = new CookieManager();

	public CSClient(String url, Supplier<Credentials> credentialsSupplier) {
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

	public static boolean isCollaborationServer(String url) throws WebRequestException {
		var invocation = new ServerCheckInvocation();
		invocation.baseUrl = url + "/ws";
		invocation.cookieManager = new CookieManager();
		var result = invocation.execute();
		if (result != null)
			return result;
		return false;
	}

	public Announcement getAnnouncement() throws WebRequestException {
		var invocation = new AnnouncementInvocation();
		invocation.baseUrl = apiUrl;
		invocation.cookieManager = cookieManager;
		return invocation.execute();
	}

	public void createRepository(String repositoryId) throws WebRequestException {
		executeLoggedIn(new CreateRepositoryInvocation(repositoryId));
	}

	public List<String> listReadableGroups() throws WebRequestException {
		return executeLoggedIn(ListGroupsInvocation.readable());
	}

	public List<String> listWritableGroups() throws WebRequestException {
		return executeLoggedIn(ListGroupsInvocation.writable());
	}

	public List<Repository> listRepositories() throws WebRequestException {
		return executeLoggedIn(new ListRepositoriesInvocation());
	}

	public List<LibraryInfo> listLibraries() throws WebRequestException {
		return executeLoggedIn(new ListLibrariesInvocation());
	}

	public void deleteRepository(String repositoryId) throws WebRequestException {
		executeLoggedIn(new DeleteRepositoryInvocation(repositoryId));
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

	public void downloadJson(String repositoryId, String type, String refId, File toFile)
			throws WebRequestException {
		var token = executeLoggedIn(new DownloadJsonPrepareInvocation(repositoryId, type, refId));
		executeLoggedIn(new DownloadJsonInvocation(token, toFile));
	}

	public List<Entry> browse(String repositoryId, String path) throws WebRequestException {
		return executeLoggedIn(new BrowseInvocation(repositoryId, path));
	}

	public SearchResult<Dataset> search(String query, String type, int page, int pageSize) throws WebRequestException {
		return executeLoggedIn(new SearchInvocation(query, type, page, pageSize));
	}

	private boolean isLoggedIn() {
		for (var cookie : cookieManager.getCookieStore().getCookies())
			if (cookie.getName().equals("JSESSIONID") && cookie.getValue() != null)
				return true;
		return false;
	}

	private <T> T executeLoggedIn(Invocation<?, T> invocation) throws WebRequestException {
		invocation.baseUrl = apiUrl;
		if (!isLoggedIn() && !login())
			return null;
		invocation.cookieManager = cookieManager;
		try {
			return invocation.execute();
		} catch (WebRequestException e) {
			if (e.getErrorCode() != 403)
				throw e;
			// session might be invalidated, try again with same credentials
			cookieManager = new CookieManager();
			if (!login())
				return null;
			try {
				invocation.cookieManager = cookieManager;
				return invocation.execute();
			} catch (WebRequestException e2) {
				if (e2.getErrorCode() != 403)
					throw e;
				// notify about unauthorized response
				// and check if should try again
				if (!credentials.onUnauthorized())
					return null;
				credentials = null;
				cookieManager = new CookieManager();
				return executeLoggedIn(invocation);
			}
		}
	}

	private boolean login() throws WebRequestException {
		var invocation = new LoginInvocation();
		invocation.baseUrl = apiUrl;
		invocation.credentials = credentials();
		invocation.cookieManager = cookieManager;
		if (invocation.credentials == null)
			return false;
		try {
			invocation.execute();
		} catch (WebRequestException e) {
			if (e.getErrorCode() == 401) {
				cookieManager = new CookieManager();
				// notify about unauthenticated response
				// and check if should try again
				if (!credentials.onUnauthenticated())
					return false;
				credentials = null;
				return login();
			}
			throw e;
		}
		return isLoggedIn();
	}

	public void close() throws WebRequestException {
		logout();
	}

	private void logout() throws WebRequestException {
		if (!isLoggedIn())
			return;
		try {
			var invocation = new LogoutInvocation();
			invocation.baseUrl = apiUrl;
			invocation.cookieManager = cookieManager;
			invocation.execute();
		} catch (WebRequestException e) {
			if (e.getErrorCode() != 401 && e.getErrorCode() != 409)
				throw e;
		}
		cookieManager = new CookieManager();
	}
}
