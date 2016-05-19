package de.escalon.hypermedia.spring.halforms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class SuggestDeserializer extends JsonDeserializer<Suggest> {

	@Override
	public Suggest deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

		JsonDeserializer<Object> deser = ctxt.findRootValueDeserializer(ctxt.constructType(Object.class));

		String textField = null;
		String valueField = null;
		String embeddedRel = null;
		String href = null;
		List<Object> list = new ArrayList<Object>();
		while (!JsonToken.END_OBJECT.equals(jp.nextToken())) {
			if (JsonToken.START_ARRAY.equals(jp.getCurrentToken())) {
				textField = "prompt";
				valueField = "value";
				while (!JsonToken.END_ARRAY.equals(jp.nextToken())) {
					list.add(deser.deserialize(jp, ctxt));
				}
			} else {
				if (!JsonToken.FIELD_NAME.equals(jp.getCurrentToken())) {
					throw new JsonParseException("Expected suggest property ", jp.getCurrentLocation());
				}
				jp.nextToken();
				if ("embedded".equals(jp.getCurrentName())) {
					embeddedRel = jp.getText();
				} else if ("href".equals(jp.getCurrentName())) {
					href = jp.getText();
				} else if ("prompt-field".equals(jp.getCurrentName())) {
					textField = jp.getText();
				} else if ("value-field".equals(jp.getCurrentName())) {
					valueField = jp.getText();
				}
			}
		}

		SuggestMapper suggest = new SuggestMapper(textField, valueField);
		suggest.setValues(list);
		suggest.setEmbeddedRel(embeddedRel);
		suggest.setHref(href);

		return suggest;
	}

}
