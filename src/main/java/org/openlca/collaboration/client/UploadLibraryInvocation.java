package org.openlca.collaboration.client;

import java.io.InputStream;
import java.util.Map;

import org.openlca.collaboration.client.WebRequests.DataType;
import org.openlca.collaboration.client.WebRequests.Type;

class UploadLibraryInvocation extends Invocation<Void, Void> {

	private final InputStream library;

	UploadLibraryInvocation(InputStream library) {
		super(Type.POST, "libraries");
		this.library = library;
	}

	@Override
	protected void checkValidity() {
		checkNotEmpty(library, "library");
	}

	@Override
	protected Map<String, Object> data() {
		return Map.of("file", library, "access", "MEMBER");

	}

	@Override
	protected DataType dataType() {
		return DataType.FORM_DATA;
	}

}
