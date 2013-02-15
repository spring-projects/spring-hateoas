package org.springframework.hateoas.hal;

public interface RelationResolver {

	/**
	 * Default relation for _embedded objects in JSON-HALL.
	 */
	public static final String DEFAULT_COLLECTION_RELATION = "content";

	String getResourceRelation(Class<?> type);
}
