package de.escalon.hypermedia.spring.siren;

import java.util.List;

/**
 * Created by Dietrich on 17.04.2016.
 */
public class SirenLink extends SirenRelatedEntity {

    protected String href;
    protected String type;

    /**
     * Siren embedded link.
     *
     * @param sirenClasses
     *         Describes the nature of an entity's content based on the current representation. Possible values are
     *         implementation-dependent and should be documented. MUST be an array of strings. Optional.
     * @param rels
     *         Defines the relationship of the sub-entity to its parent, per Web Linking (RFC5899). MUST be an array of
     *         strings. Required.
     * @param href
     *         The URI of the linked sub-entity. Required.
     * @param type
     *         Defines media type of the linked sub-entity, per Web Linking (RFC5899). Optional.
     */
    public SirenLink(List<String> sirenClasses, List<String> rels, String href, String type, String title) {
        super(rels, title, sirenClasses);
        this.href = href;
        this.type = type;
    }

    public String getHref() {
        return href;
    }

    public String getType() {
        return type;
    }



}
