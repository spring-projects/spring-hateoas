package org.springframework.hateoas.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ServerSideConfiguration {

	/**
	 * Configure a base URL to apply to all links and endpoints.
	 */
	private final @Getter @With String baseUrl;

	/**
	 * Create a new {@link ServerSideConfiguration} with no special settings applied.
	 */
	public ServerSideConfiguration() {
		this.baseUrl = "";
	}
}
