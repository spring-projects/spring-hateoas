package de.escalon.hypermedia.spring.siren;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

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
