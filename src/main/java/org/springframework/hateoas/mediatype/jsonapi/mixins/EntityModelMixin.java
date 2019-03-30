package org.springframework.hateoas.mediatype.jsonapi.mixins;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.jsonapi.Identifiable;

public class EntityModelMixin<T extends Identifiable> extends EntityModel<T> {
    public EntityModelMixin() {
    }

    public EntityModelMixin(T content, Link... links) {
        super(content, links);
    }

    public EntityModelMixin(T content, Iterable<Link> links) {
        super(content, links);
    }
}
