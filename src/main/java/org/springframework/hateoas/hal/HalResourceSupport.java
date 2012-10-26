package org.springframework.hateoas.hal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.springframework.hateoas.AbstractResourceSupport;
import org.springframework.hateoas.Link;
import org.springframework.util.Assert;

/**
 * Base class for DTOs that collect links and follow JSON-HAL
 * 
 * @author Alexander Bätz
 * 
 */
public class HalResourceSupport extends AbstractResourceSupport {

    @XmlElement(name = "resource")
    @JsonProperty("_embedded")
    @JsonSerialize(contentUsing = OptionalListSerializer.class, include = Inclusion.NON_EMPTY)
    private Map<String, List<AbstractResourceSupport>> embeddedResources = new HashMap<String, List<AbstractResourceSupport>>();
    @XmlElement(name = "link")
    @JsonProperty("_links")
    @JsonSerialize(contentUsing = OptionalListSerializer.class, include = Inclusion.NON_EMPTY)
    private Map<String, List<Link>> links = new HashMap<String, List<Link>>();

    @Override
    @JsonIgnore
    public Link getId() {
        List<Link> selfRefs = links.get(Link.REL_SELF);
        return null == selfRefs ? null : selfRefs.get(0);
    }

    @Override
    public void add(Link link) {
        Assert.notNull(link, "Link can not be null");

        if (null == links.get(link.getRel())) {
            links.put(link.getRel(), new ArrayList<Link>());
        }
        links.get(link.getRel()).add(link);
    }

    public void addEmbeddedResource(String relation, AbstractResourceSupport resource) {
        Assert.notNull(relation, "relation can not be null");
        Assert.notNull(resource, "embedded resource can not be null");

        if (null == embeddedResources.get(relation)) {
            embeddedResources.put(relation, new ArrayList<AbstractResourceSupport>());
        }
        embeddedResources.get(relation).add(resource);
    }

    public Map<String, List<Link>> getLinks() {
        return links;
    }

    public Map<String, List<AbstractResourceSupport>> getEmbeddedResources() {
        return embeddedResources;
    }

    @Override
    public boolean hasLink(String rel) {
        return (null != links.get(rel)) && (!links.get(rel).isEmpty());
    }

    @Override
    public boolean hasLinks() {
        for (String rel : links.keySet()) {
            if (!links.get(rel).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
