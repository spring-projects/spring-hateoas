package de.escalon.hypermedia.spring.siren;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by Dietrich on 22.04.2016.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"class", "title", "rel", "properties", "entities", "actions", "links"})
public class AbstractSirenNode {

	private String title;

	public AbstractSirenNode(String title) {
		this.title = title;
	}

	public AbstractSirenNode() {
	}

	public String getTitle() {
		return title;
	}
}
