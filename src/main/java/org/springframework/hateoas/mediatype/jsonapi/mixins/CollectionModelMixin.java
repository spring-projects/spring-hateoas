package org.springframework.hateoas.mediatype.jsonapi.mixins;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;

public class CollectionModelMixin extends CollectionModel<EntityModelMixin> {
    public CollectionModelMixin() {
    }

    public CollectionModelMixin(Iterable<EntityModelMixin> content, Link... links) {
        super(content, links);
    }

    public CollectionModelMixin(Iterable<EntityModelMixin> content, Iterable<Link> links) {
        super(content, links);
    }
}
