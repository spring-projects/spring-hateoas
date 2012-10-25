package org.springframework.hateoas.hal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.hateoas.AbstractResourceSupport;
import org.springframework.hateoas.Link;

/**
 * Base class for DTOs that collect links and follow JSON-HAL
 * 
 * @author Alexander Bätz
 * 
 */
public class HalResourceSupport extends AbstractResourceSupport {

    @XmlElement(name = "resource")
    @JsonProperty("_embedded")
    private Map<String, List<HalResourceSupport>> embedded = new HashMap<String, List<HalResourceSupport>>();
    @XmlElement(name = "link")
    @JsonProperty("_links")
    private Map<String, List<Link>> halLinks = new HashMap<String, List<Link>>();

    @Override
    public Link getId() {
        List<Link> selfRefs = halLinks.get(Link.REL_SELF);
        return null == selfRefs ? null : selfRefs.get(0);
    }

    @Override
    public void add(Link link) {
        List<Link> relLinks = null == halLinks.get(link.getRel()) ? halLinks.get(link.getRel()) : halLinks.put(link.getRel(), new ArrayList<Link>());
        relLinks.add(link);
    }

    public Map<String, List<Link>> getMappedLinks() {
        return halLinks;
    }

    public Map<String, List<HalResourceSupport>> getEmbeddeds() {
        return embedded;
    }

    @Override
    public boolean hasLink(String rel) {
        return (null != halLinks.get(rel)) && (!halLinks.get(rel).isEmpty());
    }

    @Override
    public boolean hasLinks() {
        for (String rel : halLinks.keySet()) {
            if (!halLinks.get(rel).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
