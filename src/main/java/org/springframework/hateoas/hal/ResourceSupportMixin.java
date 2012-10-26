package org.springframework.hateoas.hal;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

public class ResourceSupportMixin extends ResourceSupport {

	@Override
	@XmlElement(name = "link")
	@JsonProperty("_links")
	@JsonSerialize(include = Inclusion.NON_EMPTY, using = HalLinkListSerializer.class)
	public List<Link> getLinks() {
		// TODO Auto-generated method stub
		return super.getLinks();
	}
}
