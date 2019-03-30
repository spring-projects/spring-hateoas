package org.springframework.hateoas.mediatype.jsonapi;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Identifiable {
    @JsonIgnore
    String getId();

    @JsonIgnore
    String getType();
}
