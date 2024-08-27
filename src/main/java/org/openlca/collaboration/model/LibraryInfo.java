package org.openlca.collaboration.model;

import java.util.ArrayList;
import java.util.List;

public record LibraryInfo(String name, String description, boolean isRegionalized, List<String> linkedIn) {

	public LibraryInfo(String name) {
		this(name, null, false, new ArrayList<>());
	}

	public LibraryInfo(String name, String description) {
		this(name, description, false, new ArrayList<>());
	}

}
