package org.openlca.collaboration.client;

public final class Announcement {

	private final String id;
	private final String message;
	
	Announcement(String id, String message) {
		this.id = id;
		this.message = message;
	}
	
	public String id() {
		return id;
	}
	
	public String message() {
		return message;
	}

}