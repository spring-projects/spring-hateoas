package de.escalon.hypermedia.spring.siren;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Dietrich on 17.04.2016.
 */
public class SirenEntity extends AbstractSirenEntity implements SirenEntityContainer {

	protected Map<String, Object> properties;
	protected List<? super SirenSubEntity> entities;
	protected List<SirenLink> links;
	protected List<SirenAction> actions;


	SirenEntity() {

	}

	/**
	 * Siren entity.
	 *
	 * @param sirenClasses Describes the nature of an entity's content based on the current representation. Possible values are
	 * implementation-dependent and should be documented. MUST be an array of strings. Optional.
	 * @param properties A set of key-value pairs that describe the state of an entity. In JSON Siren, this is an object such as
	 * <code>{ "name": "Kevin", "age": 30 }</code>. Optional.
	 * @param entities A collection of related sub-entities. If a sub-entity contains an href value, it should be treated as an
	 * embedded link. Clients may choose to optimistically load embedded links. If no href value exists, the
	 * sub-entity is an embedded entity representation that contains all the characteristics of a typical
	 * entity. One difference is that a sub-entity MUST contain a rel attribute to describe its relationship to
	 * the parent entity.
	 * In JSON Siren, this is represented as an array. Optional.
	 * @param links A collection of items that describe navigational links, distinct from entity relationships. Link items
	 * should contain a rel attribute to describe the relationship and an href attribute to point to the target
	 * URI. Entities should include a link rel to self. In JSON Siren, this is represented as "links": [{ "rel":
	 * ["self"], "href": "http://api.x.io/orders/1234" }] Optional.
	 * @param actions A collection of action objects, represented in JSON Siren as an array such as { "actions": [{ ... }] }.
	 * See Actions. Optional
	 * @param title Descriptive text about the entity. Optional.
	 */
	public SirenEntity(List<String> sirenClasses, Map<String, Object> properties, List<SirenSubEntity> entities,
					   List<SirenAction> actions, List<SirenLink> links, String title) {
		super(title, sirenClasses);
		this.properties = properties;
		this.entities = entities;
		this.actions = actions;
		this.links = links;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	@Override
	public List<SirenSubEntity> getEntities() {
		@SuppressWarnings("unchecked")
		List<SirenSubEntity> ret = (List<SirenSubEntity>) entities;
		return ret;
	}

	public List<SirenLink> getLinks() {
		return links;
	}

	public List<SirenAction> getActions() {
		return actions;
	}

	@Override
	public void setLinks(List<SirenLink> links) {
		this.links = links;
	}

	@Override
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	@Override
	public void addSubEntity(SirenSubEntity sirenSubEntity) {
		if (this.entities == null) {
			this.entities = new ArrayList<SirenSubEntity>();
		}
		entities.add(sirenSubEntity);
	}

	@Override
	public void setEmbeddedLinks(List<SirenEmbeddedLink> embeddedLinks) {
		if (this.entities == null) {
			this.entities = new ArrayList<SirenSubEntity>();
		}
		this.entities.addAll(embeddedLinks);
	}

	@Override
	public void setActions(List<SirenAction> actions) {
		this.actions = actions;
	}
}
