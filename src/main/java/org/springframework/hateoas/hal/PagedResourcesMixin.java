package org.springframework.hateoas.hal;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(value = { "page", "_links", "_embedded" })
public class PagedResourcesMixin {
}
