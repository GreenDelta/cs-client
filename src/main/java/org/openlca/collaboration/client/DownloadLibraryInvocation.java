package org.openlca.collaboration.client;

import java.io.InputStream;

import org.openlca.collaboration.client.WebRequests.Type;

class DownloadLibraryInvocation extends Invocation<InputStream, InputStream> {

	private final String library;

	DownloadLibraryInvocation(String library) {
		super(Type.GET, "libraries", InputStream.class);
		this.library = library;
	}

	@Override
	protected void checkValidity() {
		checkNotEmpty(library, "library");
	}

	@Override
	protected String query() {
		return "/" + WebRequests.encodeQuery(library);
	}

}
