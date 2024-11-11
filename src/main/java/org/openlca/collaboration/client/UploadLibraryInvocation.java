package org.openlca.collaboration.client;

import java.io.InputStream;
import java.util.Map;

import org.openlca.collaboration.client.WebRequests.DataType;
import org.openlca.collaboration.client.WebRequests.Type;
import org.openlca.collaboration.model.LibraryAccess;

class UploadLibraryInvocation extends Invocation<Void, Void> {

	private final InputStream library;
	private final LibraryAccess access;

	UploadLibraryInvocation(InputStream library, LibraryAccess access) {
		super(Type.POST, "libraries");
		this.library = library;
		this.access = access;
	}

	@Override
	protected void checkValidity() {
		checkNotEmpty(library, "library");
		checkNotEmpty(access, "access");
	}

	@Override
	protected Map<String, Object> data() {
		return Map.of("file", library, "access", access.name());

	}

	@Override
	protected DataType dataType() {
		return DataType.FORM_DATA;
	}

}
