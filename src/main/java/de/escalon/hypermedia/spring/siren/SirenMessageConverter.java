package de.escalon.hypermedia.spring.siren;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.escalon.hypermedia.spring.DocumentationProvider;
import org.springframework.hateoas.RelProvider;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;

/**
 * Http message converter which converts Spring Hateoas resource beans to siren messages.
 *
 * Treats the following rels as navigational by default: "self", "next", "previous", "prev".
 *
 * Created by Dietrich on 18.04.2016.
 */
public class SirenMessageConverter extends AbstractHttpMessageConverter<Object> {

    private final SirenUtils sirenUtils;
    ObjectMapper objectMapper = new ObjectMapper();
    public SirenMessageConverter() {
        sirenUtils = new SirenUtils();
    }

    /**
     * Used to derive siren class (because Spring Hateoas rel providers normally derive rels from class names or class annotations).
     * @param relProvider to determine siren class
     */
    public void setRelProvider(RelProvider relProvider) {
        sirenUtils.setRelProvider(relProvider);
    }

    /**
     * Tells converter about rels which should be treated as navigational, in addition to the default ones.
     * @param additionalNavigationalRels to add
     */
    public void setAdditionalNavigationalRels(Collection<String> additionalNavigationalRels) {
        sirenUtils.setAdditionalNavigationalRels(additionalNavigationalRels);
    }

    /**
     * Sets request media type to be used as action type, instead of the default application/x-www-formurlencoded.
     * @param requestMediaType type
     */
    public void setRequestMediaType(String requestMediaType) {
        sirenUtils.setRequestMediaType(requestMediaType);
    }

    /**
     * Sets documentation provider, used to calculate rels.
     * @param documentationProvider to use
     */
    public void setDocumentationProvider(DocumentationProvider documentationProvider) {
        sirenUtils.setDocumentationProvider(documentationProvider);
    }


    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return null;
    }

    @Override
    protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        SirenEntity entity = new SirenEntity();
        sirenUtils.toSirenEntity(entity, o);

        JsonEncoding encoding = getJsonEncoding(outputMessage.getHeaders().getContentType());
        JsonGenerator jsonGenerator = this.objectMapper.getFactory().createGenerator(outputMessage.getBody(), encoding);

        // A workaround for JsonGenerators not applying serialization features
        // https://github.com/FasterXML/jackson-databind/issues/12
        if (this.objectMapper.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
            jsonGenerator.useDefaultPrettyPrinter();
        }

        try {
            this.objectMapper.writeValue(jsonGenerator, entity);
        } catch (JsonProcessingException ex) {
            throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
        }

    }

    /**
     * Determine the JSON encoding to use for the given content type.
     *
     * @param contentType the media type as requested by the caller
     * @return the JSON encoding to use (never {@code null})
     */
    protected JsonEncoding getJsonEncoding(MediaType contentType) {
        if (contentType != null && contentType.getCharSet() != null) {
            Charset charset = contentType.getCharSet();
            for (JsonEncoding encoding : JsonEncoding.values()) {
                if (charset.name().equals(encoding.getJavaName())) {
                    return encoding;
                }
            }
        }
        return JsonEncoding.UTF8;
    }
}
