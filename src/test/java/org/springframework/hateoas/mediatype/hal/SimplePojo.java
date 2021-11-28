package org.springframework.hateoas.mediatype.hal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"text", "number"})
@Data
@Getter(onMethod = @__(@JsonProperty))
@NoArgsConstructor
@AllArgsConstructor
public class SimplePojo {

	private String text;
	private int number;
}
