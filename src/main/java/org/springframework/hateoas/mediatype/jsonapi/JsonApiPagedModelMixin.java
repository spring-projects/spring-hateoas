package org.springframework.hateoas.mediatype.jsonapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(value = {"data", "links", "meta"})
public class JsonApiPagedModelMixin {
    private List<JsonApiData> data;
    private PagedModel.PageMetadata meta;
    private Links links;

    @JsonCreator
    public JsonApiPagedModelMixin(
            @JsonProperty List<JsonApiData> data,
            @JsonProperty PagedModel.PageMetadata meta,
            @JsonProperty Links links) {
        this.data = data;
        this.meta = meta;
        this.links = links;
    }

}
