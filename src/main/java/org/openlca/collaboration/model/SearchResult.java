package org.openlca.collaboration.model;

import java.util.ArrayList;
import java.util.List;

public record SearchResult<T>(List<T> data, ResultInfo resultInfo) {

	public SearchResult() {
		this(new ArrayList<>(), 0, 10, 0);
	}

	public SearchResult(List<T> data, int currentPage, int pageSize, int totalCount) {
		this(data, new ResultInfo(currentPage, pageSize, data.size(), totalCount));
	}

}