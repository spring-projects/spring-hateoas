package org.springframework.hateoas.hal.jackson2;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.hal.LinkMixin;
import org.springframework.hateoas.hal.ResourceSupportMixin;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class HalJackson2Module extends SimpleModule {

	public HalJackson2Module() {
		super("json-hal-module", new Version(1, 0, 0, null));

		setMixInAnnotation(Link.class, LinkMixin.class);
		setMixInAnnotation(ResourceSupport.class, ResourceSupportMixin.class);
	}

}
