package org.springframework.hateoas.mediatype.jsonapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.springframework.hateoas.Links;


@JsonPropertyOrder(value = {"data", "links"})
@Getter
public class JsonApiEntityModelMixin {
    private JsonApiData data;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Links links;


    @JsonCreator
    public JsonApiEntityModelMixin(@JsonProperty("data") JsonApiData data, Links links) {
        this.data = data;
        this.links = links;
    }
}