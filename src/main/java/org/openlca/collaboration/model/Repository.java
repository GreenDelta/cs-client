package org.openlca.collaboration.model;

public record Repository(String group, String name, String label) {

	public String id() {
		return group + "/" + name;
	}
	
}
