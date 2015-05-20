package org.springframework.hateoas.client;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Greg Turnquist
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Item {

	String image;
	String description;

	Item(@JsonProperty("image") String image, @JsonProperty("description") String description) {
		this.image = image;
		this.description = description;
	}

}
