package de.escalon.hypermedia.affordance;

public enum SuggestType {

	/**
	 * Values are serialized as a list
	 */
	INTERNAL,

	/**
	 * Values are known in the client because they were previously sent somehow
	 */
	EXTERNAL,

	/**
	 * Values show be retrieved from a remote URL
	 */
	REMOTE
}
