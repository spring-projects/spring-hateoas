package org.springframework.hateoas.mediatype.jsonapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.springframework.hateoas.Links;



@JsonPropertyOrder(value = {"type", "id", "attributes", "links"})
@Getter
public class JsonApiData<T> {
    private String type;
    private String id;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Links links;
    private T attributes;

    @JsonCreator
    public JsonApiData(
            @JsonProperty String type,
            @JsonProperty String id,
            @JsonProperty T attributes,
            @JsonProperty Links links) {
        this.attributes = attributes;
        this.type = type;
        this.id = id;
        this.links = links;
    }


}
