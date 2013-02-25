package org.springframework.hateoas.hal;


import org.springframework.hateoas.Resource;

public class AnnotationBasedRelationResolver implements RelationResolver {

	@SuppressWarnings("rawtypes")
	@Override
	public String getResourceRelation(Object resource) {

		// check for hateoas wrapper type
		if (Resource.class.isInstance(resource)) {
			resource = ((Resource) resource).getContent();
		}

		HateoasRelation annotation = resource.getClass().getAnnotation(HateoasRelation.class);
		if (annotation == null || HateoasRelation.NO_RELATION.equals(annotation.value())) {
			return null;
		}
		return annotation.value();
	}

}
