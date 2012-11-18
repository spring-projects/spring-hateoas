package org.springframework.hateoas.hal;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

public abstract class ResourceSupportMixin extends ResourceSupport {

	@Override
	@XmlElement(name = "link")
	@org.codehaus.jackson.annotate.JsonProperty("_links")
	@com.fasterxml.jackson.annotation.JsonProperty("_links")
	@org.codehaus.jackson.map.annotate.JsonSerialize(include = org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_EMPTY, using = org.springframework.hateoas.hal.jackson1.HalLinkListSerializer.class)
	@com.fasterxml.jackson.databind.annotation.JsonSerialize(include = com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_EMPTY, using = org.springframework.hateoas.hal.jackson2.HalLinkListSerializer.class)
	public abstract List<Link> getLinks();
}
