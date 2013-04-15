package org.springframework.hateoas.hal;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;

import org.springframework.hateoas.Resources;

public abstract class ResourcesMixin<T> extends Resources<T> {

	@Override
	@XmlElement(name = "embedded")
	@org.codehaus.jackson.annotate.JsonProperty("_embedded")
	@com.fasterxml.jackson.annotation.JsonProperty("_embedded")
	@org.codehaus.jackson.map.annotate.JsonSerialize(include = org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_EMPTY, using = org.springframework.hateoas.hal.Jackson1HalModule.HalResourcesSerializer.class)
	@org.codehaus.jackson.map.annotate.JsonDeserialize(using = org.springframework.hateoas.hal.Jackson1HalModule.HalResourcesDeserializer.class)
	@com.fasterxml.jackson.databind.annotation.JsonSerialize(include = com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_EMPTY, using = org.springframework.hateoas.hal.Jackson2HalModule.HalResourcesSerializer.class)
	@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = org.springframework.hateoas.hal.Jackson2HalModule.HalResourcesDeserializer.class)
	public abstract Collection<T> getContent();

}
