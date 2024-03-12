package org.openlca.collaboration.api;

public interface Credentials {
	
	String username();
	String password();
	String token();
	String promptToken();
	boolean onUnauthenticated();
	boolean onUnauthorized();
	
}