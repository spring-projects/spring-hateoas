package org.springframework.hateoas;

public class EmbeddedResource {

    private String rel;
    private Object resource;

    public EmbeddedResource(String rel, Object resource) {
        this.rel = rel;
        this.resource = resource;
    }

    public String getRel() {
        return rel;
    }

    public Object getResource() {
        return resource;
    }
}
