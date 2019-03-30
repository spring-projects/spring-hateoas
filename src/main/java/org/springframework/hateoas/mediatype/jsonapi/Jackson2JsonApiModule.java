package org.springframework.hateoas.mediatype.jsonapi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.*;
import org.springframework.hateoas.mediatype.jsonapi.mixins.CollectionModelMixin;
import org.springframework.hateoas.mediatype.jsonapi.mixins.EntityModelMixin;
import org.springframework.hateoas.mediatype.jsonapi.mixins.PagedModelMixin;
import org.springframework.hateoas.mediatype.jsonapi.mixins.RepresentationModelMixin;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.*;

public class Jackson2JsonApiModule extends SimpleModule {
    static Logger logger = LoggerFactory.getLogger(Jackson2JsonApiModule.class);

    public Jackson2JsonApiModule() {
        super("jsonapi-module", new Version(1, 0, 0, null, "org.springframework.hateoas", "spring-hateoas"));

        addSerializer(new JsonApiLinksSerializer());
        addSerializer(new JsonApiResourceSerializer());
        addSerializer(new JsonApiResourcesSerializer());
        addSerializer(new JsonApiPagedResourcesSerializer());
        addSerializer(new JsonApiResourceSupportSerializer());

        addDeserializer(Links.class, new JsonApiLinksDeserializer());
    }

    public static JsonApiData toJsonApi(EntityModel<? extends Identifiable> value) {
        Identifiable content = value.getContent();
        String resourceType = Objects.nonNull(content) ? content.getType() : null;
        String resourceId = Objects.nonNull(content) ? content.getId() : null;
        return new JsonApiData(resourceType, resourceId, content, value.getLinks());
    }

    static class JsonApiLinksSerializer extends ContainerSerializer<Links> {
        public JsonApiLinksSerializer() {
            super(Links.class);
        }

        @Override
        public boolean isEmpty(SerializerProvider provider, Links value) {
            return Objects.isNull(value) || value.isEmpty();
        }

        @Override
        public JavaType getContentType() {
            return TypeFactory.defaultInstance().constructType(Link.class);
        }

        @Override
        public JsonSerializer<?> getContentSerializer() {
            return null;
        }

        @Override
        public boolean hasSingleElement(Links value) {
            return false;
        }

        @Override
        protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
            return null;
        }

        @Override
        public void serialize(Links links, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            for (Link link : links) {
                gen.writeStringField(link.getRel().value(), link.getHref());
            }
            gen.writeEndObject();
        }
    }

    static class JsonApiLinksDeserializer extends ContainerDeserializerBase<Links> {

        protected JsonApiLinksDeserializer() {
            super(TypeFactory.defaultInstance().constructCollectionLikeType(List.class, Link.class));
        }

        @Override
        public JsonDeserializer<Object> getContentDeserializer() {

            return null;
        }

        @Override
        public Links deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JavaType type = ctxt.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
            List<Link> links = new ArrayList<>();
            Map<String, String> values = jp.getCodec().readValue(jp, type);
            values.forEach((k, v) -> links.add(new Link(v, k)));
            return Links.of(links);
        }
    }

    static class JsonApiResourceSerializer extends ContainerSerializer<EntityModelMixin>
            implements ContextualSerializer {

        private final BeanProperty property;

        JsonApiResourceSerializer() {
            this(null);
        }

        JsonApiResourceSerializer(@Nullable BeanProperty property) {

            super(EntityModelMixin.class, false);
            this.property = property;
        }

        @Override
        public JavaType getContentType() {
            return null;
        }

        @Override
        public JsonSerializer<?> getContentSerializer() {
            return null;
        }

        @Override
        public boolean hasSingleElement(EntityModelMixin value) {
            return true;
        }

        @Override
        protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
            return null;
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
            return new JsonApiResourceSerializer(property);
        }

        @Override
        public void serialize(EntityModelMixin value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (Objects.isNull(value)) {
                gen.writeStartObject();
                gen.writeStringField("data", null);
                gen.writeEndObject();
            } else {
                provider.findValueSerializer(JsonApiEntityModelMixin.class, property).serialize(new JsonApiEntityModelMixin(toJsonApi(value), null), gen, provider);
            }
        }
    }

    static class JsonApiResourcesSerializer<T extends Identifiable> extends ContainerSerializer<CollectionModelMixin> {


        JsonApiResourcesSerializer() {
            super(CollectionModel.class, false);
        }

        @Override
        public boolean isEmpty(SerializerProvider provider, CollectionModelMixin value) {
            return Objects.isNull(value) || Objects.isNull(value.getContent()) || value.getContent().isEmpty();
        }

        @Override
        public JavaType getContentType() {
            return null;
        }

        @Override
        public JsonSerializer<?> getContentSerializer() {
            return null;
        }

        @Override
        public boolean hasSingleElement(CollectionModelMixin value) {
            return value.getContent().size() == 1;
        }

        @Override
        protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
            return null;
        }

        @Override
        public void serialize(CollectionModelMixin value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            List<JsonApiData> data = new ArrayList<>();
            Links links = Links.NONE;
            if (Objects.nonNull(value)) {
                Collection<EntityModelMixin> content = value.getContent();
                links = links.and(value.getLinks());
                if (Objects.nonNull(content)) {
                    for (EntityModelMixin c : content) {
                        data.add(toJsonApi(c));
                    }
                }
            }

            JsonApiCollectionModelMixin collectionModelMixin = new JsonApiCollectionModelMixin(data, links);
            provider.findValueSerializer(JsonApiCollectionModelMixin.class).serialize(collectionModelMixin, gen, provider);

        }
    }

    static class JsonApiPagedResourcesSerializer extends ContainerSerializer<PagedModelMixin>
            implements ContextualSerializer {

        private final BeanProperty property;

        JsonApiPagedResourcesSerializer() {
            this(null);
        }

        JsonApiPagedResourcesSerializer(@Nullable BeanProperty property) {

            super(PagedModelMixin.class, false);
            this.property = property;
        }

        @Override
        @SuppressWarnings("null")
        public void serialize(PagedModelMixin value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            PagedModel.PageMetadata metadata = null;
            Links links = Links.NONE;
            List<JsonApiData> data = new ArrayList<>();
            if (Objects.nonNull(value)) {
                links = links.and(value.getLinks());
                metadata = value.getMetadata();
                Collection<EntityModelMixin> collection = value.getContent();
                if (Objects.nonNull(collection)) {
                    for (EntityModelMixin entityModel : collection) {
                        data.add(toJsonApi(entityModel));
                    }
                }
            }
            JsonApiPagedModelMixin pagedModelMixin = new JsonApiPagedModelMixin(data, metadata, links);
            provider.findValueSerializer(JsonApiPagedModelMixin.class, property).serialize(pagedModelMixin, jgen, provider);
        }

        @Override
        @SuppressWarnings("null")
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
                throws JsonMappingException {
            return new JsonApiPagedResourcesSerializer(property);
        }

        @Override
        @Nullable
        public JavaType getContentType() {
            return null;
        }


        @Override
        @Nullable
        public JsonSerializer<?> getContentSerializer() {
            return null;
        }

        @Override
        public boolean isEmpty(SerializerProvider provider, PagedModelMixin value) {
            return value.getContent().isEmpty();
        }

        @Override
        @SuppressWarnings("null")
        public boolean hasSingleElement(PagedModelMixin value) {
            return value.getContent().size() == 1;
        }

        @Override
        @Nullable
        @SuppressWarnings("null")
        protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
            return null;
        }
    }

    static class JsonApiResourceSupportSerializer extends ContainerSerializer<RepresentationModelMixin>
            implements ContextualSerializer {

        private final BeanProperty property;

        JsonApiResourceSupportSerializer() {
            this(null);
        }

        JsonApiResourceSupportSerializer(@Nullable BeanProperty property) {

            super(RepresentationModelMixin.class, false);
            this.property = property;
        }


        @Override
        @SuppressWarnings("null")
        public void serialize(RepresentationModelMixin value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            provider.findValueSerializer(JsonApiEntityModelMixin.class).serialize(new JsonApiEntityModelMixin(null, value.getLinks()), jgen, provider);

        }

        @Override
        @SuppressWarnings("null")
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
                throws JsonMappingException {
            return new JsonApiResourceSupportSerializer(property);
        }


        @Override
        @Nullable
        public JavaType getContentType() {
            return null;
        }


        @Override
        @Nullable
        public JsonSerializer<?> getContentSerializer() {
            return null;
        }


        @Override
        @SuppressWarnings("null")
        public boolean hasSingleElement(RepresentationModelMixin value) {
            return true;
        }

        @Override
        @Nullable
        @SuppressWarnings("null")
        protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
            return null;
        }
    }

}
