package org.openlca.collaboration.model;

import java.util.Date;

public record Comment(long id, String user, String text, String type, String refId, String path, Date date,
		boolean released, boolean approved, long replyTo) {

}