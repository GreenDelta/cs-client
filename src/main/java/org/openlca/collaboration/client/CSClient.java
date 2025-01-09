package org.openlca.collaboration.client;

import java.io.InputStream;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.util.List;
import java.util.function.Supplier;

import org.openlca.collaboration.model.Comment;
import org.openlca.collaboration.model.Credentials;
import org.openlca.collaboration.model.Dataset;
import org.openlca.collaboration.model.Entry;
import org.openlca.collaboration.model.LibraryAccess;
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
	private HttpClient client;

	public CSClient(String url, Supplier<Credentials> credentialsSupplier) {
		this.url = url;
		this.apiUrl = url + "/ws";
		this.credentialsSupplier = credentialsSupplier;
		this.client = createClient();
	}

	private Credentials credentials() {
		if (credentials == null) {
			credentials = credentialsSupplier.get();
		}
		return credentials;
	}

	private static HttpClient createClient() {
		return HttpClient.newBuilder()
				.cookieHandler(new CookieManager())
				.followRedirects(Redirect.NEVER)
				.sslContext(Ssl.createContext()).build();
	}

	public static boolean isCollaborationServer(String url) throws WebRequestException {
		var invocation = new ServerCheckInvocation();
		invocation.baseUrl = url + "/ws";
		invocation.client = createClient();
		var result = invocation.execute();
		if (result != null)
			return result;
		return false;
	}

	public Announcement getAnnouncement() throws WebRequestException {
		var invocation = new AnnouncementInvocation();
		invocation.baseUrl = apiUrl;
		invocation.client = client;
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
		return executeLoggedIn(new DownloadLibraryInvocation(library));
	}

	public InputStream downloadJson(String repositoryId, String type, String refId)
			throws WebRequestException {
		var token = executeLoggedIn(new DownloadJsonPrepareInvocation(repositoryId, type, refId));
		return executeLoggedIn(new DownloadJsonInvocation(token));
	}

	public void uploadLibrary(InputStream library) throws WebRequestException {
		uploadLibrary(library, LibraryAccess.MEMBER);
	}

	public void uploadLibrary(InputStream library, LibraryAccess access) throws WebRequestException {
		executeLoggedIn(new UploadLibraryInvocation(library, access));
	}

	public List<Entry> browse(String repositoryId, String path) throws WebRequestException {
		return executeLoggedIn(new BrowseInvocation(repositoryId, path));
	}

	public SearchResult<Dataset> search(String query, String type, int page, int pageSize) throws WebRequestException {
		return executeLoggedIn(new SearchInvocation(query, type, page, pageSize));
	}

	private boolean isLoggedIn() {
		var cookieManager = (CookieManager) client.cookieHandler().get();
		for (var cookie : cookieManager.getCookieStore().getCookies())
			if (cookie.getName().equals("JSESSIONID") && cookie.getValue() != null)
				return true;
		return false;
	}

	private <T> T executeLoggedIn(Invocation<?, T> invocation) throws WebRequestException {
		invocation.baseUrl = apiUrl;
		if (!isLoggedIn() && !login())
			return null;
		invocation.client = client;
		try {
			return invocation.execute();
		} catch (WebRequestException e) {
			if (e.getErrorCode() != 403 && e.getErrorCode() != 401)
				throw e;
			// session might be invalidated, try again with same credentials
			client = createClient();
			if (!login())
				return null;
			try {
				invocation.client = client;
				return invocation.execute();
			} catch (WebRequestException e2) {
				if (e2.getErrorCode() != 403)
					throw e;
				// notify about unauthorized response
				// and check if should try again
				if (!credentials.onUnauthorized())
					return null;
				credentials = null;
				client = createClient();
				return executeLoggedIn(invocation);
			}
		}
	}

	private boolean login() throws WebRequestException {
		var invocation = new LoginInvocation();
		invocation.baseUrl = apiUrl;
		invocation.credentials = credentials();
		invocation.client = client;
		if (invocation.credentials == null)
			return false;
		try {
			invocation.execute();
		} catch (WebRequestException e) {
			if (e.getErrorCode() == 401) {
				client = createClient();
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
			invocation.client = client;
			invocation.execute();
		} catch (WebRequestException e) {
			if (e.getErrorCode() != 401 && e.getErrorCode() != 409)
				throw e;
		}
		client = createClient();
	}
}
