package org.springframework.hateoas.mediatype.jsonapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.hateoas.Links;
import org.springframework.util.Assert;

import java.util.*;


public final class JsonApiErrors implements Iterable<JsonApiErrors.JsonApiError> {
    @Getter
    private List<JsonApiError> errors;

    public JsonApiErrors() {
        this.errors = new ArrayList<>();
    }

    public final JsonApiErrors add(JsonApiError error) {
        Assert.notNull(error, "error cannot be null");
        this.errors.add(error);
        return this;
    }

    public final JsonApiErrors add(JsonApiError... errors) {
        Assert.notNull(errors, "errors cannot be null");
        this.errors.addAll(Arrays.asList(errors));
        return this;
    }

    public final JsonApiErrors add(Iterable<JsonApiError> iterable) {
        Assert.notNull(errors, "iterable cannot be null");
        iterable.forEach(e -> this.errors.add(e));
        return this;
    }

    @NotNull
    @Override
    public Iterator<JsonApiError> iterator() {
        return errors.iterator();
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    public static class JsonApiError {
        private String id;
        private String status;
        private String code;
        private String title;
        private String detail;
        private JsonApiSource source;
        private Links links;
        private Map<String, Object> meta;

        public static JsonApiErrorBuilder builder() {
            return new JsonApiErrorBuilder();
        }

        private static class JsonApiErrorBuilder {
            private String id;
            private String status;
            private String code;
            private String title;
            private String detail;
            private JsonApiSource source;
            private Links links;
            private Map<String, Object> meta;

            JsonApiErrorBuilder() {
                this.links = Links.NONE;
                this.meta = new HashMap<>();
            }

            public JsonApiErrorBuilder withId(String id) {
                this.id = id;
                return this;
            }

            public JsonApiErrorBuilder withStatus(String status) {
                this.status = status;
                return this;
            }

            public JsonApiErrorBuilder withCode(String code) {
                this.code = code;
                return this;
            }

            public JsonApiErrorBuilder withTitle(String title) {
                this.title = title;
                return this;
            }

            public JsonApiErrorBuilder withDetail(String detail) {
                this.detail = detail;
                return this;
            }

            public JsonApiErrorBuilder withSource(JsonApiSource source) {
                this.source = source;
                return this;
            }

            public JsonApiErrorBuilder withLinks(Links links) {
                this.links = this.links.and(links);
                return this;
            }

            public JsonApiErrorBuilder withMetaAttribute(String key, Object value) {
                this.meta.put(key, value);
                return this;
            }

            public JsonApiErrorBuilder withMeta(Map<String, Object> meta) {
                this.meta.putAll(meta);
                return this;
            }

            public JsonApiError build() {
                return new JsonApiError(id, status, code, title, detail, source, links, meta);
            }
        }
    }
}