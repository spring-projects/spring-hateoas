package org.springframework.hateoas.hal;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

public class HalJacksonModule extends SimpleModule {

	public HalJacksonModule() {
		super("json-hal-module", new Version(1, 0, 0, null));

		setMixInAnnotation(Link.class, LinkMixin.class);
		setMixInAnnotation(ResourceSupport.class, ResourceSupportMixin.class);
	}

}
