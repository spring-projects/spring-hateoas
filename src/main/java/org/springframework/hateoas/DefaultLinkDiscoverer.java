package org.springframework.hateoas;

import org.springframework.hateoas.core.JsonPathLinkDiscoverer;
import org.springframework.http.MediaType;

public class DefaultLinkDiscoverer extends JsonPathLinkDiscoverer {

	public DefaultLinkDiscoverer() {
		super("$links[?(@.rel=='%s')].href", MediaType.APPLICATION_JSON);
	}
	 
}
