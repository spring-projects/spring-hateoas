package org.springframework.hateoas.mediatype.jsonapi.mixins;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;

import java.util.Collection;

public class PagedModelMixin extends PagedModel<EntityModelMixin> {
    public PagedModelMixin() {
        super();
    }

    public PagedModelMixin(Collection<EntityModelMixin> content, PageMetadata metadata, Link... links) {
        super(content, metadata, links);
    }

    public PagedModelMixin(Collection<EntityModelMixin> content, PageMetadata metadata, Iterable<Link> links) {
        super(content, metadata, links);
    }
}