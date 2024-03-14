package org.openlca.collaboration.model;

public record Dataset(String type, String refId, String name, String category, String repositoryId,
		String commitId) {
}