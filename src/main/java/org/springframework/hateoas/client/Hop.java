package org.springframework.hateoas.client;

import java.util.HashMap;
import java.util.Map;

/**
 * Container for cuztomizations to a single traverson "hop"
 *
 * @author Greg Turnquist
 */
public class Hop {

	private final String rel;
	private final Map<String, String> params = new HashMap<String, String>();

	/**
	 * Every hop requires a rel
	 *
	 * @param rel
	 */
	public Hop(String rel) {
		this.rel = rel;
	}

	/**
	 * Add a {@link org.springframework.hateoas.UriTemplate} parameter.
	 * For /foo/{value}, .withParam("value", "bar") will expand to /foo/value
	 * For /foo{?value}, .withParam("value, "bar") will expand to /foo?value=bar
	 * @param name
	 * @param value
	 * @return
	 */
	public Hop withParam(String name, String value) {
		this.params.put(name, value);
		return this;
	}

	public String getRel() {
		return rel;
	}

	public Map<String, String> getParams() {
		return params;
	}

}
