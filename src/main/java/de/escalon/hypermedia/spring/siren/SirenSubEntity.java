package de.escalon.hypermedia.spring.siren;

import java.util.List;

/**
 * Common subtype for Siren subentities
 * Created by Dietrich on 17.04.2016.
 */
public abstract class SirenSubEntity extends SirenRelatedEntity {

	public SirenSubEntity(List<String> rels, String title, List<String> sirenClasses) {
		super(rels, title, sirenClasses);
	}

	public SirenSubEntity() {
		super();
	}
}
