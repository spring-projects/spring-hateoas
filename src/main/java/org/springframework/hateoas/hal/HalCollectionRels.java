package org.springframework.hateoas.hal;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Class to maintain relations that must always be serialized to an array regardless of cardinality.
 *
 * In HAL, if there is only one element behind a rel, it can be serialized directly into a JSON object; otherwise
 * the elements behind the rel are serialized into a JSON array of JSON objects.
 *
 * However, the HAL specification also says that the API designer can mandate that certain rels will always be
 * represented as an array regardless of the cardinality of elements behind that rel. This class helps you do that
 * If a rel is specified here, elements behind that rel will always be serialized into an array wherever it is
 * used.
 *
 * @author Vivin Paliath
 */
public class HalCollectionRels {

	private final Set<String> rels;

	public HalCollectionRels(String... rels) {
		this.rels = new HashSet<String>(Arrays.asList(rels));
	}

	public boolean containsRel(String rel) {
		return rels.contains(rel);
	}
}
