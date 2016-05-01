package de.escalon.hypermedia.spring.siren;

import java.util.List;
import java.util.Map;

/**
 * Created by Dietrich on 22.04.2016.
 */
public interface SirenEntityContainer {
    List<SirenSubEntity> getEntities();

    void setLinks(List<SirenLink> links);

    void setProperties(Map<String, Object> properties);

    void setSirenClasses(List<String> sirenClasses);

    void addSubEntity(SirenSubEntity sirenSubEntity);

    void setEmbeddedLinks(List<SirenEmbeddedLink> embeddedLinks);

    void setActions(List<SirenAction> actions);
}
