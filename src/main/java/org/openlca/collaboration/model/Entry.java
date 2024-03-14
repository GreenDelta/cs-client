package org.openlca.collaboration.model;

public record Entry(TypeOfEntry typeOfEntry, String path, String name, String commitId, String flowType,
		String processType, int count) {

	public Entry(TypeOfEntry entry, String path, String name, int count) {
		this(entry, path, name, null, null, null, count);
	}

}