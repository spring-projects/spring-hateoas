package org.springframework.hateoas.mediatype.jsonapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class JsonApiSource {
    public static final String DATA_POINTER = "/data";
    public static final String DATA_ATTRIBUTE_POINTER = "/data/attributes/%s";
    @Getter
    private String pointer;

    @JsonCreator
    public JsonApiSource(@JsonProperty String pointer) {
        this.pointer = pointer;
    }

    public static JsonApiSourceBuilder builder() {
        return new JsonApiSourceBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class JsonApiSourceBuilder {
        private String pointer;

        public JsonApiSourceBuilder withDataPointer() {
            this.pointer = DATA_POINTER;
            return this;
        }

        public JsonApiSourceBuilder withAttributePointer(String attribute) {
            this.pointer = String.format(DATA_ATTRIBUTE_POINTER, attribute);
            return this;
        }

        public JsonApiSourceBuilder pointer(String pointer) {
            this.pointer = pointer;
            return this;
        }

        public JsonApiSource build() {
            return new JsonApiSource(pointer);
        }
    }
}
