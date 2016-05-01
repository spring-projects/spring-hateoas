package de.escalon.hypermedia.spring.siren;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Created by Dietrich on 17.04.2016.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
@JsonPropertyOrder({"name", "title", "method", "href", "type", "fields"})
public class SirenAction extends AbstractSirenEntity {

	private String name;
	private String method;
	private String href;
	private String type;
	private List<SirenField> fields;

	/**
	 * @param sirenClasses
	 * @param name A string that identifies the action to be performed. Action names MUST be unique within the set of actions for an entity. The behaviour of clients when parsing a Siren document that violates this constraint is undefined. Requir
	 * ed.
	 * @param title Descriptive text about the action. Optional.
	 * @param method An enumerated attribute mapping to a protocol method. For HTTP, these values may be GET, PUT, POST, DELETE, or PATCH. As new methods are introduced, this list can be extended. If this attribute is omitted, GET
	 * should be assumed. Option
	 * al.
	 * @param href The URI of the action. Required.
	 * @param type The encoding type for the request. When omitted and the fields attribute exists, the default value is applica
	 * tion/x-www-form-urlencoded. Optional.
	 * @param fields A collection of fields, expressed as an array of objects in JSON Siren such as { "fields" : [{ ... }] }. See Fields. Optional.
	 */
	public SirenAction(List<String> sirenClasses, String name, String title, String method,
					   String href, String type, List<SirenField> fields) {
		super(title, sirenClasses);
		this.name = name;
		this.method = method;
		this.href = href;
		this.type = type;
		this.fields = fields;
	}

	public String getName() {
		return name;
	}

	public String getMethod() {
		return method;
	}

	public String getHref() {
		return href;
	}

	public String getType() {
		return type;
	}

	public List<SirenField> getFields() {
		return fields;
	}
}
