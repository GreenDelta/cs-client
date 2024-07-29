package org.openlca.collaboration.model;

public interface Credentials {

	String username();

	String password();

	default String token() {
		return null;
	}

	default String promptToken() {
		return null;
	}

	default boolean onUnauthenticated() {
		return false;
	}

	default boolean onUnauthorized() {
		return false;
	}

	static Credentials of(String username, String password) {
		return new Credentials() {
			@Override
			public String username() {
				return username;
			}

			@Override
			public String password() {
				return password;
			}
		};
	}

}