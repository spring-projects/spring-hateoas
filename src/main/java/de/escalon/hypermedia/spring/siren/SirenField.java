package de.escalon.hypermedia.spring.siren;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by Dietrich on 18.04.2016.
 */
@JsonInclude(NON_EMPTY)
@JsonPropertyOrder({"class", "name", "title", "type", "value"})
public class SirenField extends AbstractSirenEntity {

	private String name;
	private String type;
	private Object value;

	/**
	 * Siren field.
	 *
	 * @param sirenClasses Describes aspects of the field based on the current representation. Possible values are
	 * implementation-dependent and should be documented. MUST be an array of strings. Optional.
	 * @param name A name describing the control. Field names MUST be unique within the set of fields for an action. The
	 * behaviour of clients when parsing a Siren document that violates this constraint is undefined. Required.
	 * @param type The input type of the field. This may include any of the following input types specified in HTML5:
	 * <code>hidden, text, search, tel, url, email, password, datetime, date, month, week, time, datetime-local,
	 * number, range, color, checkbox, radio, file</code>
	 * When missing, the default value is text. Serialization of these fields will depend on the value of the
	 * action's type attribute. See type under Actions, above. Optional.
	 * @param value A value assigned to the field. Optional.
	 * @param title Textual annotation of a field. Clients may use this as a label. Optional.
	 */
	public SirenField(String name, String type, String value, String title, List<String> sirenClasses) {
		super(title, sirenClasses);
		this.name = name;
		this.type = type;
		this.value = value;
	}

	/**
	 * Siren field.
	 *
	 * @param sirenClasses Describes aspects of the field based on the current representation. Possible values are
	 * implementation-dependent and should be documented. MUST be an array of strings. Optional.
	 * @param name A name describing the control. Field names MUST be unique within the set of fields for an action. The
	 * behaviour of clients when parsing a Siren document that violates this constraint is undefined. Required.
	 * @param type The input type of the field. This may include any of the following input types specified in HTML5:
	 * <code>hidden, text, search, tel, url, email, password, datetime, date, month, week, time, datetime-local,
	 * number, range, color, checkbox, radio, file</code>
	 * When missing, the default value is text. Serialization of these fields will depend on the value of the
	 * action's type attribute. See type under Actions, above. Optional.
	 * @param value possible values for radio or checkbox, with actual values selected. Optional.
	 * @param title Textual annotation of a field. Clients may use this as a label. Optional.
	 */
	public SirenField(String name, String type, List<SirenFieldValue> value, String title, List<String> sirenClasses) {
		super(title, sirenClasses);
		this.name = name;
		this.type = type;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}
}
