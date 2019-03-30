package org.springframework.hateoas.mediatype.jsonapi.mixins;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

public class RepresentationModelMixin extends RepresentationModel<RepresentationModelMixin> {
    public RepresentationModelMixin() {
    }

    public RepresentationModelMixin(Link initialLink) {
        super(initialLink);
    }

    public RepresentationModelMixin(List<Link> initialLinks) {
        super(initialLinks);
    }
}
