package org.openlca.collaboration.model;

import java.util.List;

public record Dataset(String type, String refId, List<Version> versions) {
	
	public record Version(String category, String name, List<Repo> repos) {
	}
	
	public record Repo(String path, String commitId) {
	}
	
}