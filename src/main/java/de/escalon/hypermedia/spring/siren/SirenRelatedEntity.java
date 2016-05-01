package de.escalon.hypermedia.spring.siren;

import java.util.List;

/**
 * Created by Dietrich on 22.04.2016.
 */
public class SirenRelatedEntity extends AbstractSirenEntity {

    private List<String> rel;

    SirenRelatedEntity() {
        super();
    }

    public SirenRelatedEntity(List<String> rels, String title, List<String> sirenClasses) {
        super(title,sirenClasses);
        this.rel = rels;
    }

    public List<String> getRel() {
        return rel;
    }
}
