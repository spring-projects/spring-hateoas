package de.escalon.hypermedia.affordance;

import org.springframework.util.Assert;

/**
 * Resource of a certain semantic type which may or may not be identifiable.
 */
public class TypedResource {

	private String semanticType;
	private String identifyingUri;

	/**
	 * Creates a resource whose semantic type is known, but which cannot be identified as an individual.
	 *
	 * @param semanticType semantic type of the resource as string, either as Uri or Curie or as type name within the default vocabulary. Example: <code>Order</code> in a context where the default vocabulary is <code>http://schema.org/</code>
	 * @see <a href="http://www.w3.org/TR/curie/">Curie</a>
	 */
	public TypedResource(String semanticType) {
		Assert.notNull(semanticType, "semanticType must be given");
		this.semanticType = semanticType;
	}

	/**
	 * Creates identified resource of a semantic type.
	 *
	 * @param semanticType semantic type of the resource as string, either as Uri or Curie
	 * @param identifyingUri identifying an individual of the typed resource
	 * @see <a href="http://www.w3.org/TR/curie/">Curie</a>
	 */
	public TypedResource(String semanticType, String identifyingUri) {
		Assert.notNull(semanticType, "semanticType must be given");
		Assert.notNull(identifyingUri, "identifyingUri must be given");
		this.semanticType = semanticType;
		this.identifyingUri = identifyingUri;
	}

	public String getSemanticType() {
		return semanticType;
	}

	public String getIdentifyingUri() {
		return identifyingUri;
	}
}
