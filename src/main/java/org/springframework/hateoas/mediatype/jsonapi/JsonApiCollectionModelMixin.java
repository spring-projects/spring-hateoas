package org.springframework.hateoas.mediatype.jsonapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.springframework.hateoas.Links;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(value = {"data", "links"})
public class JsonApiCollectionModelMixin {
    private List<JsonApiData> data;
    private Links links;

    public JsonApiCollectionModelMixin(@JsonProperty List<JsonApiData> data, Links links) {
        this.data = data;
        this.links = links;
    }

}