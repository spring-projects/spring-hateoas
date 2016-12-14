/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.hal;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.NonTypedScalarSerializerBase;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Jackson 2 module implementation to render {@link Link} and {@link ResourceSupport} instances in HAL compatible JSON.
 *
 * @author Alexander Baetz
 * @author Oliver Gierke
 */
public class Jackson2HalModule extends SimpleModule {

    private static final long serialVersionUID = 7806951456457932384L;

    /**
     * TODO: This constant was commented, because
     * 1) it's usage in code were connected with with jackson 2.5.6 api
     * 2) jsonOrder in {@link ResourcesMixin} which was override.
     */
//    private static final Link CURIES_REQUIRED_DUE_TO_EMBEDS = new Link("__rel__", "¯\\_(ツ)_/¯");
    public Jackson2HalModule() {

        super("json-hal-module", new Version(1, 0, 0, null, "org.springframework.hateoas", "spring-hateoas"));

        setMixInAnnotation(Link.class, LinkMixin.class);
        setMixInAnnotation(ResourceSupport.class, ResourceSupportMixin.class);
        setMixInAnnotation(Resources.class, ResourcesMixin.class);
    }

    /**
     * Returns whether the module was already registered in the given {@link ObjectMapper}.
     *
     * @param mapper must not be {@literal null}.
     * @return
     */
    public static boolean isAlreadyRegisteredIn(ObjectMapper mapper) {

        Assert.notNull(mapper, "ObjectMapper must not be null!");
        return LinkMixin.class.equals(mapper.findMixInClassFor(Link.class));
    }

    /**
     * Custom {@link JsonSerializer} to render Link instances in HAL compatible JSON.
     *
     * @author Alexander Baetz
     * @author Oliver Gierke
     */
    public static class HalLinkListSerializer extends ContainerSerializer<List<Link>> implements ContextualSerializer {

        private static final long serialVersionUID = -1844788111509966406L;

        private static final String RELATION_MESSAGE_TEMPLATE = "_links.%s.title";

        private final BeanProperty property;
        private final CurieProvider curieProvider;
        private final EmbeddedMapper mapper;
        private final MessageSourceAccessor messageSource;

        public HalLinkListSerializer(CurieProvider curieProvider, EmbeddedMapper mapper,
                                     MessageSourceAccessor messageSource) {
            this(null, curieProvider, mapper, messageSource);
        }

        public HalLinkListSerializer(BeanProperty property, CurieProvider curieProvider, EmbeddedMapper mapper,
                                     MessageSourceAccessor messageSource) {

            super(List.class, false);
            this.property = property;
            this.curieProvider = curieProvider;
            this.mapper = mapper;
            this.messageSource = messageSource;
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
         * 
         * TODO: This method was modified to be compatible with jackson 2.4.6 API. Version 2.4.6 is required to be 
         * compatible with jackson version in CW API. Curies generation logic was modified.
         * Please see <a href="https://wiki.inbcu.com/display/NEWSCONTAPI/Spring+HATEOAS">wiki</a> 
         * for more information.
         */
        @Override
        public void serialize(List<Link> value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
                JsonGenerationException {

            // sort links according to their relation
            Map<String, List<Object>> sortedLinks = new LinkedHashMap<String, List<Object>>();
            List<Link> links = new ArrayList<Link>();

            boolean prefixingRequired = curieProvider != null;
            boolean curiedLinkPresent = false;
            //TODO : Was reverted to previous version.
            // Please see <a href="https://wiki.inbcu.com/display/NEWSCONTAPI/Spring+HATEOAS">wiki</a>for more information.

            //boolean skipCuries = !jgen.getOutputContext().getParent().inRoot();

//            TODO: Was disabled because 2.4.6 don't support jgen.getCurrentValue().
//            Object currentValue = jgen.getCurrentValue();
//
//            if (currentValue instanceof Resources) {
//                if (mapper.hasCuriedEmbed((Resources<?>) currentValue)) {
//                    curiedLinkPresent = true;
//                }
//            }
//            
            for (Link link : value) {

//                if (link.equals(CURIES_REQUIRED_DUE_TO_EMBEDS)) {
//                    continue;
//                }

                // CURIE_REQUIRED_LINK link is used to trigger curies on paginated resources. We don't serialize it.
                if (link.equals(PagedResources.CURIE_REQUIRED_LINK)) {
                    curiedLinkPresent = true;
                    continue;
                }

                // Apply curie prefix
                String rel = prefixingRequired ? curieProvider.getNamespacedRelFrom(link) : link.getRel();

                if (!link.getRel().equals(rel)) {
                    curiedLinkPresent = true;
                }

                if (sortedLinks.get(rel) == null) {
                    sortedLinks.put(rel, new ArrayList<Object>());
                }

                links.add(link);
                Link halLink = getHalLink(link);
                sortedLinks.get(rel).add(halLink);

            }

            //TODO : Was reverted to previous version.
            // Please see <a href="https://wiki.inbcu.com/display/NEWSCONTAPI/Spring+HATEOAS">wiki</a>for more information.

            //if (!skipCuries && prefixingRequired && curiedLinkPresent) {
            if (prefixingRequired && curiedLinkPresent) {

                sortedLinks.put("curies", new ArrayList<Object>(curieProvider.getCurieInformation(new Links(links))));
            }

            provider.findValueSerializer(Map.class, property).serialize(unwrapValues(sortedLinks), jgen, provider);
        }

        private Map<String, Object> unwrapValues(Map<String, List<Object>> sortedLinks) {
            Map<String, Object> serializableLinks = new LinkedHashMap<String, Object>(sortedLinks.size());
            for (Map.Entry<String, List<Object>> entry : sortedLinks.entrySet()) {
                List<Object> values = entry.getValue();
                Object unwrappedValue = values;
                if (values.size() == 1) {
                    Object value = values.get(0);
                    if ((value instanceof Link && !((Link) value).getPreferCollections())) {
                        unwrappedValue = value;
                    }
                }

                serializableLinks.put(entry.getKey(), unwrappedValue);
            }

            return serializableLinks;
        }

        /**
         * Wraps the given link into a HAL specifc extension.
         *
         * @param link must not be {@literal null}.
         * @return
         */
        private HalLink toHalLink(Link link) {

            String rel = link.getRel();
            String title = getTitle(rel);

            if (title == null) {
                title = getTitle(rel.contains(":") ? rel.substring(rel.indexOf(":") + 1) : rel);
            }

            return new HalLink(link, title);
        }

        private Link getHalLink(Link link) {
            String rel = link.getRel();
            String title = getTitle(rel);

            if (title == null) {
                title = getTitle(rel.contains(":") ? rel.substring(rel.indexOf(":") + 1) : rel);
            }

            if (title == null) {
                title = link.getTitle();
            }

            Link.Builder linkBuilder = new Link.Builder()
                    .rel(link.getRel())
                    .href(link.getHref())
                    .name(link.getName())
                    .title(title)
                    .profile(link.getProfile());

            if (link.getPreferCollections()) {
                linkBuilder.preferCollections();
            }
            return linkBuilder.build();
        }

        /**
         * Returns the title for the given local link relation resolved through the configured {@link MessageSourceAccessor}
         * .
         *
         * @param localRel must not be {@literal null} or empty.
         * @return
         */
        private String getTitle(String localRel) {

            Assert.hasText(localRel, "Local relation must not be null or empty!");

            try {
                return messageSource == null ? null
                        : messageSource.getMessage(String.format(RELATION_MESSAGE_TEMPLATE, localRel));
            } catch (NoSuchMessageException o_O) {
                return null;
            }
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.ser.ContextualSerializer#createContextual(com.fasterxml.jackson.databind.SerializerProvider, com.fasterxml.jackson.databind.BeanProperty)
         */
        @Override
        public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property)
                throws JsonMappingException {
            return new HalLinkListSerializer(property, curieProvider, mapper, messageSource);
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#getContentType()
         */
        @Override
        public JavaType getContentType() {
            return null;
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#getContentSerializer()
         */
        @Override
        public JsonSerializer<?> getContentSerializer() {
            return null;
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#isEmpty(java.lang.Object)
         */
        public boolean isEmpty(List<Link> value) {
            return isEmpty(null, value);
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.JsonSerializer#isEmpty(com.fasterxml.jackson.databind.SerializerProvider, java.lang.Object)
         */
        public boolean isEmpty(SerializerProvider provider, List<Link> value) {
            return value.isEmpty();
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#hasSingleElement(java.lang.Object)
         */
        @Override
        public boolean hasSingleElement(List<Link> value) {
            return value.size() == 1;
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#_withValueTypeSerializer(com.fasterxml.jackson.databind.jsontype.TypeSerializer)
         */
        @Override
        protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
            return null;
        }
    }

    /**
     * Custom {@link JsonSerializer} to render {@link Resource}-Lists in HAL compatible JSON. Renders the list as a Map.
     *
     * @author Alexander Baetz
     * @author Oliver Gierke
     */
    public static class HalResourcesSerializer extends ContainerSerializer<Collection<?>>
            implements ContextualSerializer {

        private static final long serialVersionUID = 8030706944344625390L;

        private final BeanProperty property;
        private final EmbeddedMapper embeddedMapper;

        public HalResourcesSerializer(EmbeddedMapper embeddedMapper) {
            this(null, embeddedMapper);
        }

        public HalResourcesSerializer(BeanProperty property, EmbeddedMapper embeddedMapper) {

            //TODO: Was rewritten to be compatible with  2.4.6 jackson.
            //super(TypeFactory.defaultInstance().constructType(Collection.class));
            super(Collection.class, false);

            this.property = property;
            this.embeddedMapper = embeddedMapper;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.codehaus.jackson.map.ser.std.SerializerBase#serialize(java.lang.Object, org.codehaus.jackson.JsonGenerator,
         * org.codehaus.jackson.map.SerializerProvider)
         */
        @Override
        public void serialize(Collection<?> value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonGenerationException {

            Map<String, Object> embeddeds = embeddedMapper.map(value);
            //TODO: Was disabled because 2.4.6 don't support jgen.getCurrentValue().
//            Object currentValue = jgen.getCurrentValue();
//
//            if (currentValue instanceof ResourceSupport) {
//
//                if (embeddedMapper.hasCuriedEmbed(value)) {
//                    ((ResourceSupport) currentValue).add(CURIES_REQUIRED_DUE_TO_EMBEDS);
//                }
//            }

            provider.findValueSerializer(Map.class, property).serialize(embeddeds, jgen, provider);
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
                throws JsonMappingException {
            return new HalResourcesSerializer(property, embeddedMapper);
        }

        @Override
        public JavaType getContentType() {
            return null;
        }

        @Override
        public JsonSerializer<?> getContentSerializer() {
            return null;
        }

        public boolean isEmpty(Collection<?> value) {
            return isEmpty(null, value);
        }

        public boolean isEmpty(SerializerProvider provider, Collection<?> value) {
            return value.isEmpty();
        }

        @Override
        public boolean hasSingleElement(Collection<?> value) {
            return value.size() == 1;
        }

        @Override
        protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
            return null;
        }
    }

    /**
     * Custom {@link JsonSerializer} to render Link instances in HAL compatible JSON. Renders the {@link Link} as
     * immediate object if we have a single one or as array if we have multiple ones.
     *
     * @author Alexander Baetz
     * @author Oliver Gierke
     */
    public static class OptionalListJackson2Serializer extends ContainerSerializer<Object>
            implements ContextualSerializer {

        private static final long serialVersionUID = 3700806118177419817L;

        private final BeanProperty property;
        private final Map<Class<?>, JsonSerializer<Object>> serializers;

        public OptionalListJackson2Serializer() {
            this(null);
        }

        /**
         * Creates a new {@link OptionalListJackson2Serializer} using the given {@link BeanProperty}.
         *
         * @param property
         */
        public OptionalListJackson2Serializer(BeanProperty property) {

            super(List.class, false);
            this.property = property;
            this.serializers = new HashMap<Class<?>, JsonSerializer<Object>>();
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#_withValueTypeSerializer(com.fasterxml.jackson.databind.jsontype.TypeSerializer)
         */
        @Override
        public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
            throw new UnsupportedOperationException("not implemented");
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
         */
        @Override
        public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonGenerationException {

            List<?> list = (List<?>) value;

            if (list.isEmpty()) {
                return;
            }

            // If we only have one element, check whether it is a Link with preferCollections NOT set to true
            if (list.size() == 1) {

                if (list.get(0) instanceof Link) {

                    if (!((Link) list.get(0)).getPreferCollections()) {
                        serializeContents(list.iterator(), jgen, provider);
                        return;
                    }

                } else {
                    serializeContents(list.iterator(), jgen, provider);
                    return;
                }

            }

            jgen.writeStartArray();
            serializeContents(list.iterator(), jgen, provider);
            jgen.writeEndArray();
        }

        private void serializeContents(Iterator<?> value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonGenerationException {

            while (value.hasNext()) {
                Object elem = value.next();
                if (elem == null) {
                    provider.defaultSerializeNull(jgen);
                } else {
                    getOrLookupSerializerFor(elem.getClass(), provider).serialize(elem, jgen, provider);
                }
            }
        }

        private JsonSerializer<Object> getOrLookupSerializerFor(Class<?> type, SerializerProvider provider)
                throws JsonMappingException {

            JsonSerializer<Object> serializer = serializers.get(type);

            if (serializer == null) {
                serializer = provider.findValueSerializer(type, property);
                serializers.put(type, serializer);
            }

            return serializer;
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#getContentSerializer()
         */
        @Override
        public JsonSerializer<?> getContentSerializer() {
            return null;
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#getContentType()
         */
        @Override
        public JavaType getContentType() {
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#hasSingleElement(java.lang.Object)
         */
        @Override
        public boolean hasSingleElement(Object arg0) {
            return false;
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#isEmpty(java.lang.Object)
         */
        public boolean isEmpty(Object value) {
            return isEmpty(null, value);
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.JsonSerializer#isEmpty(com.fasterxml.jackson.databind.SerializerProvider, java.lang.Object)
         */
        public boolean isEmpty(SerializerProvider provider, Object value) {
            return false;
        }

        /*
         * (non-Javadoc)
         *
         * @see com.fasterxml.jackson.databind.ser.ContextualSerializer#createContextual(com.fasterxml.jackson.databind.SerializerProvider,
         * com.fasterxml.jackson.databind.BeanProperty)
         */
        @Override
        public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property)
                throws JsonMappingException {
            return new OptionalListJackson2Serializer(property);
        }
    }

    public static class HalLinkListDeserializer extends ContainerDeserializerBase<List<Link>> {

        private static final long serialVersionUID = 6420432361123210955L;

        public HalLinkListDeserializer() {
            super(TypeFactory.defaultInstance().constructCollectionLikeType(List.class, Link.class));
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase#getContentType()
         */
        @Override
        public JavaType getContentType() {
            return null;
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase#getContentDeserializer()
         */
        @Override
        public JsonDeserializer<Object> getContentDeserializer() {
            return null;
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
         */
        @Override
        public List<Link> deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {

            List<Link> result = new ArrayList<Link>();
            String relation;
            Link link;

            // links is an object, so we parse till we find its end.
            while (!JsonToken.END_OBJECT.equals(jp.nextToken())) {

                if (!JsonToken.FIELD_NAME.equals(jp.getCurrentToken())) {
                    throw new JsonParseException("Expected relation name", jp.getCurrentLocation());
                }

                // save the relation in case the link does not contain it
                relation = jp.getText();

                if (JsonToken.START_ARRAY.equals(jp.nextToken())) {
                    while (!JsonToken.END_ARRAY.equals(jp.nextToken())) {
                        link = jp.readValueAs(Link.class);
                        result.add(new Link(link.getHref(), relation));
                    }
                } else {
                    link = jp.readValueAs(Link.class);
                    result.add(new Link(link.getHref(), relation));
                }
            }

            return result;
        }
    }

    public static class HalResourcesDeserializer extends ContainerDeserializerBase<List<Object>>
            implements ContextualDeserializer {

        private static final long serialVersionUID = 4755806754621032622L;

        private JavaType contentType;

        public HalResourcesDeserializer() {
            this(TypeFactory.defaultInstance().constructCollectionLikeType(List.class, Object.class), null);
        }

        public HalResourcesDeserializer(JavaType vc) {
            this(null, vc);
        }

        private HalResourcesDeserializer(JavaType type, JavaType contentType) {

            super(type);
            this.contentType = contentType;
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase#getContentType()
         */
        @Override
        public JavaType getContentType() {
            return null;
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase#getContentDeserializer()
         */
        @Override
        public JsonDeserializer<Object> getContentDeserializer() {
            return null;
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
         */
        @Override
        public List<Object> deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {

            List<Object> result = new ArrayList<Object>();
            JsonDeserializer<Object> deser = ctxt.findRootValueDeserializer(contentType);
            Object object;

            // links is an object, so we parse till we find its end.
            while (!JsonToken.END_OBJECT.equals(jp.nextToken())) {

                if (!JsonToken.FIELD_NAME.equals(jp.getCurrentToken())) {
                    throw new JsonParseException("Expected relation name", jp.getCurrentLocation());
                }

                if (JsonToken.START_ARRAY.equals(jp.nextToken())) {
                    while (!JsonToken.END_ARRAY.equals(jp.nextToken())) {
                        object = deser.deserialize(jp, ctxt);
                        result.add(object);
                    }
                } else {
                    object = deser.deserialize(jp, ctxt);
                    result.add(object);
                }
            }

            return result;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
                throws JsonMappingException {

            JavaType vc = property.getType().getContentType();
            HalResourcesDeserializer des = new HalResourcesDeserializer(vc);
            return des;
        }
    }

    public static class HalHandlerInstantiator extends HandlerInstantiator {

        private final Map<Class<?>, Object> instanceMap = new HashMap<Class<?>, Object>();

        public HalHandlerInstantiator(RelProvider resolver, CurieProvider curieProvider,
                                      MessageSourceAccessor messageSource) {
            this(resolver, curieProvider, messageSource, true);
        }

        public HalHandlerInstantiator(RelProvider resolver, CurieProvider curieProvider,
                                      MessageSourceAccessor messageSource, boolean enforceEmbeddedCollections) {

            EmbeddedMapper mapper = new EmbeddedMapper(resolver, curieProvider, enforceEmbeddedCollections);

            Assert.notNull(resolver, "RelProvider must not be null!");
            this.instanceMap.put(HalResourcesSerializer.class, new HalResourcesSerializer(mapper));
            this.instanceMap.put(HalLinkListSerializer.class,
                    new HalLinkListSerializer(curieProvider, mapper, messageSource));
        }

        private Object findInstance(Class<?> type) {

            Object result = instanceMap.get(type);
            return result != null ? result : BeanUtils.instantiateClass(type);
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.cfg.HandlerInstantiator#deserializerInstance(com.fasterxml.jackson.databind.DeserializationConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
         */
        @Override
        public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated,
                                                        Class<?> deserClass) {
            return (JsonDeserializer<?>) findInstance(deserClass);
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.cfg.HandlerInstantiator#keyDeserializerInstance(com.fasterxml.jackson.databind.DeserializationConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
         */
        @Override
        public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated,
                                                       Class<?> keyDeserClass) {
            return (KeyDeserializer) findInstance(keyDeserClass);
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.cfg.HandlerInstantiator#serializerInstance(com.fasterxml.jackson.databind.SerializationConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
         */
        @Override
        public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) {
            return (JsonSerializer<?>) findInstance(serClass);
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.cfg.HandlerInstantiator#typeResolverBuilderInstance(com.fasterxml.jackson.databind.cfg.MapperConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
         */
        @Override
        public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated,
                                                                  Class<?> builderClass) {
            return (TypeResolverBuilder<?>) findInstance(builderClass);
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.cfg.HandlerInstantiator#typeIdResolverInstance(com.fasterxml.jackson.databind.cfg.MapperConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
         */
        @Override
        public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
            return (TypeIdResolver) findInstance(resolverClass);
        }
    }

    /**
     * {@link JsonSerializer} to only render {@link Boolean} values if they're set to {@literal true}.
     *
     * @author Oliver Gierke
     * @since 0.9
     */
    public static class TrueOnlyBooleanSerializer extends NonTypedScalarSerializerBase<Boolean> {

        private static final long serialVersionUID = 5817795880782727569L;

        public TrueOnlyBooleanSerializer() {
            super(Boolean.class);
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.JsonSerializer#isEmpty(java.lang.Object)
         */
        public boolean isEmpty(Boolean value) {
            return isEmpty(null, value);
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.JsonSerializer#isEmpty(com.fasterxml.jackson.databind.SerializerProvider, java.lang.Object)
         */
        public boolean isEmpty(SerializerProvider provider, Boolean value) {
            return value == null || Boolean.FALSE.equals(value);
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
         */
        @Override
        public void serialize(Boolean value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonGenerationException {
            jgen.writeBoolean(value.booleanValue());
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.ser.std.StdScalarSerializer#getSchema(com.fasterxml.jackson.databind.SerializerProvider, java.lang.reflect.Type)
         */
        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
            return createSchemaNode("boolean", true);
        }

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.ser.std.StdScalarSerializer#acceptJsonFormatVisitor(com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper, com.fasterxml.jackson.databind.JavaType)
         */
        @Override
        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint)
                throws JsonMappingException {
            if (visitor != null) {
                visitor.expectBooleanFormat(typeHint);
            }
        }
    }

    /**
     * Helper to easily map embedded resources and find out whether they were curied.
     *
     * @author Oliver Gierke
     */
    private static class EmbeddedMapper {

        private RelProvider relProvider;
        private CurieProvider curieProvider;
        private boolean preferCollectionRels;

        /**
         * Creates a new {@link EmbeddedMapper} for the given {@link RelProvider}, {@link CurieProvider} and flag whether to
         * prefer collection relations.
         *
         * @param relProvider          must not be {@literal null}.
         * @param curieProvider        can be {@literal null}.
         * @param preferCollectionRels
         */
        public EmbeddedMapper(RelProvider relProvider, CurieProvider curieProvider, boolean preferCollectionRels) {

            Assert.notNull(relProvider, "RelProvider must not be null!");

            this.relProvider = relProvider;
            this.curieProvider = curieProvider;
            this.preferCollectionRels = preferCollectionRels;
        }

        /**
         * Maps the given source elements as embedded values.
         *
         * @param source must not be {@literal null}.
         * @return
         */
        public Map<String, Object> map(Iterable<?> source) {

            Assert.notNull(source, "Elements must not be null!");

            HalEmbeddedBuilder builder = new HalEmbeddedBuilder(relProvider, curieProvider, preferCollectionRels);

            for (Object resource : source) {
                builder.add(resource);
            }

            return builder.asMap();
        }

        /**
         * Returns whether the given source elements will be namespaced.
         *
         * @param source must not be {@literal null}.
         * @return
         */
        public boolean hasCuriedEmbed(Iterable<?> source) {

            for (String rel : map(source).keySet()) {
                if (rel.contains(":")) {
                    return true;
                }
            }

            return false;
        }
    }

    static class HalLink {

        private final Link link;
        private final String title;

        public HalLink(Link link, String title) {
            this.link = link;
            this.title = title;
        }

        @JsonUnwrapped
        public Link getLink() {
            return link;
        }

        @JsonInclude(Include.NON_NULL)
        public String getTitle() {
            return title;
        }
    }
}
