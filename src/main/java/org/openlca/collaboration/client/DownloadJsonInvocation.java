package org.openlca.collaboration.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.openlca.collaboration.client.WebRequests.Type;
import org.openlca.collaboration.model.WebRequestException;

class DownloadJsonInvocation extends Invocation<InputStream, Void> {

	private final String token;
	private final File toFile;

	DownloadJsonInvocation(String token, File toFile) {
		super(Type.GET, "public/download/json", InputStream.class);
		this.token = token;
		this.toFile = toFile;
	}

	@Override
	protected void checkValidity() {
		checkNotEmpty(token, "token");
	}

	@Override
	protected String query() {
		return "/" + token;
	}

	@Override
	protected Void process(InputStream response) throws WebRequestException {
		try {
			Files.copy(response, toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return null;
		} catch (IOException e) {
			return null;
		}
	}

}