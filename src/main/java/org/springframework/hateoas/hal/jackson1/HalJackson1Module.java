package org.springframework.hateoas.hal.jackson1;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.hal.LinkMixin;
import org.springframework.hateoas.hal.ResourceSupportMixin;

public class HalJackson1Module extends SimpleModule {

	public HalJackson1Module() {
		super("json-hal-module", new Version(1, 0, 0, null));

		setMixInAnnotation(Link.class, LinkMixin.class);
		setMixInAnnotation(ResourceSupport.class, ResourceSupportMixin.class);
	}

}
