package de.escalon.hypermedia.spring.halforms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.Link;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import de.escalon.hypermedia.spring.halforms.ValueSuggest.ValueSuggestType;

public class SuggestDeserializer extends JsonDeserializer<Suggest> {

	@Override
	public Suggest deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

		Suggest suggest = null;

		JsonToken currentToken = jp.nextToken();
		if (JsonToken.START_ARRAY.equals(currentToken)) {
			List<Object> list = new ArrayList<Object>();
			while (!JsonToken.END_ARRAY.equals(jp.nextToken())) {

			}
			suggest = new ValueSuggest<Object>(list, "value", "prompt", ValueSuggestType.DIRECT);
		} else {
			if (!JsonToken.FIELD_NAME.equals(jp.getCurrentToken())) {
				throw new JsonParseException("Expected embedded or link property ", jp.getCurrentLocation());
			}

			if ("embedded".equals(jp.getCurrentName())) {
				String valueField = null;
				String promptField = null;
				jp.nextToken();
				String embeddedRel = jp.getText();
				if (JsonToken.FIELD_NAME.equals(jp.nextToken())) {
					if ("prompt-field".equals(jp.getCurrentName())) {
						promptField = jp.getText();
						jp.nextToken();
						jp.nextToken();
					}

					if ("value-field".equals(jp.getCurrentName())) {
						valueField = jp.getText();
						jp.nextToken();
					}
				}

				suggest = new ValueSuggest<Object>(new ArrayList<Object>(), valueField, promptField, ValueSuggestType.EMBEDDED);
			} else if ("href".equals(jp.getCurrentName())) {
				String valueField = null;
				String promptField = null;
				jp.nextToken();
				String href = jp.getText();
				if (JsonToken.FIELD_NAME.equals(jp.nextToken())) {
					if ("prompt-field".equals(jp.getCurrentName())) {
						promptField = jp.getText();
						jp.nextToken();
						jp.nextToken();
					}

					if ("value-field".equals(jp.getCurrentName())) {
						valueField = jp.getText();
						jp.nextToken();
					}
				}

				suggest = new LinkSuggest(new Link(href, "self"), promptField, valueField);
			}
		}

		return suggest;
	}

}
