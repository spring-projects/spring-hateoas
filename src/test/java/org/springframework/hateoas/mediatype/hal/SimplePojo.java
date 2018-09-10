package org.springframework.hateoas.mediatype.hal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimplePojo {

	private String text;
	private int number;
}
