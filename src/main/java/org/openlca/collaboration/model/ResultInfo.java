package org.openlca.collaboration.model;

public record ResultInfo(int currentPage, int pageSize, int count, int totalCount) {

	public int pageCount() {
		return (int) Math.ceil(totalCount / (double) pageSize);
	}

}