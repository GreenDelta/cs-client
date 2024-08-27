package org.openlca.collaboration.client;

import java.util.List;

import org.openlca.collaboration.client.WebRequests.Type;
import org.openlca.collaboration.model.LibraryInfo;

import com.google.gson.reflect.TypeToken;

class ListLibrariesInvocation extends Invocation<List<LibraryInfo>, List<LibraryInfo>> {

	ListLibrariesInvocation() {
		super(Type.GET, "libraries", new TypeToken<List<LibraryInfo>>() {
		});
	}

}
