package de.escalon.hypermedia.spring.siren;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by Dietrich on 24.04.2016.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SirenFieldValue {
    private Object value;
    private Boolean selected;

    public SirenFieldValue(String title, Object value, Boolean selected) {
        this.value = value;
        this.selected = selected != null && selected == true ? selected : null;
    }

    public Object getValue() {
        return value;
    }

    public Boolean isSelected() {
        return selected;
    }
}
