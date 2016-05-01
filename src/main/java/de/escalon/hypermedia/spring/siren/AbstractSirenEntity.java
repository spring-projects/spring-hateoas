package de.escalon.hypermedia.spring.siren;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * All entities that have classes and a title.
 * Created by Dietrich on 17.04.2016.
 */
public abstract class AbstractSirenEntity  extends AbstractSirenNode {

    private List<String> sirenClasses;

    AbstractSirenEntity() {
        
    }

    public AbstractSirenEntity(String title, List<String> sirenClasses) {
        super(title);
        this.sirenClasses = sirenClasses;
    }

    @JsonProperty("class")
    public List<String> getSirenClasses() {
        return sirenClasses;
    }

    public void setSirenClasses(List<String> sirenClasses) {
        this.sirenClasses = sirenClasses;
    }
}