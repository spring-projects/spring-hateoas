/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.mediatype.hal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.EmbeddedWrappers;
import org.springframework.lang.Nullable;
import org.springframework.ui.Model;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * Builder API to create complex HAL representations exposing a HAL idiomatic API. It's built around the notion of a the
 * representation consisting of an optional primary entity and e set of embeds. There's also explicit API for common HAL
 * patterns like previews contained in {@literal _embedded} for links present in the representation.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 * @since 1.1
 */
public class HalModelBuilder {

	private final EmbeddedWrappers wrappers;

	private Object model;
	private Links links = Links.NONE;
	private final List<Object> embeddeds = new ArrayList<>();

	private HalModelBuilder(EmbeddedWrappers wrappers) {
		this.wrappers = wrappers;
	}

	/**
	 * Creates a new {@link HalModelBuilder}.
	 *
	 * @return will never be {@literal null}.
	 */
	public static HalModelBuilder halModel() {
		return new HalModelBuilder(new EmbeddedWrappers(false));
	}

	/**
	 * Creates a new {@link HalModelBuilder} using the given {@link EmbeddedWrappers}.
	 *
	 * @param wrappers must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public static HalModelBuilder halModel(EmbeddedWrappers wrappers) {

		Assert.notNull(wrappers, "EmbeddedWrappers must not be null!");

		return new HalModelBuilder(wrappers);
	}

	/**
	 * Creates a new {@link HalModelBuilder} with the given entity as primary payload.
	 *
	 * @param entity must not be {@literal null}.
	 * @return
	 */
	public static HalModelBuilder halModelOf(Object entity) {
		return halModel().entity(entity);
	}

	/**
	 * Creates a new {@link HalModelBuilder} without a primary payload.
	 *
	 * @return
	 */
	public static HalModelBuilder emptyHalModel() {
		return halModel();
	}

	/**
	 * Embed the entity, but with no relation.
	 *
	 * @param entity
	 * @return
	 */
	public HalModelBuilder entity(Object entity) {

		Assert.notNull(entity, "Entity must not be null!");

		if (model != null) {
			throw new IllegalStateException("Model object already set!");
		}

		this.model = entity;

		return this;
	}

	/**
	 * Embed the entity and associate it with the {@link LinkRelation}.
	 *
	 * @param entity must not be {@literal null}.
	 * @param linkRelation must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public HalModelBuilder embed(Object entity, LinkRelation linkRelation) {

		Assert.notNull(entity, "Entity must not be null!");
		Assert.notNull(linkRelation, "Link relation must not be null!");

		this.embeddeds.add(wrappers.wrap(entity, linkRelation));

		return this;
	}

	/**
	 * Embeds the given entity into the {@link RepresentationModel}.
	 *
	 * @param entity must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public HalModelBuilder embed(Object entity) {

		Assert.notNull(entity, "Entity must not be null!");

		this.embeddeds.add(wrappers.wrap(entity));

		return this;
	}

	/**
	 * Embeds the given collection in the {@link RepresentationModel}. If the collection is empty nothing will be added to
	 * the {@link RepresentationModel}.
	 *
	 * @param collection must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public HalModelBuilder embed(Collection<?> collection) {
		return embed(collection, Void.class);
	}

	/**
	 * Embeds the given collection in the {@link RepresentationModel} and the {@link LinkRelation} derived from the given
	 * type. If the collection is empty nothing will be added to the {@link RepresentationModel} an empty embed for the
	 * derived {@link LinkRelation} will be added.
	 *
	 * @param collection must not be {@literal null}.
	 * @param type the type to derive the {@link LinkRelation} to be used, must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @see #embed(Collection, LinkRelation)
	 */
	public HalModelBuilder embed(Collection<?> collection, Class<?> type) {

		Assert.notNull(collection, "Collection must not be null!");
		Assert.notNull(type, "Type must not be null!");

		if (!collection.isEmpty()) {
			return embed(wrappers.wrap(collection));
		}

		return Void.class.equals(type) ? this : embed(wrappers.emptyCollectionOf(type));
	}

	/**
	 * Embeds the given collection in the {@link RepresentationModel} for the given {@link LinkRelation}. If the
	 * collection is empty nothing will be added to the {@link RepresentationModel} an empty embed for the derived
	 * {@link LinkRelation} will be added.
	 *
	 * @param collection must not be {@literal null}.
	 * @param relation must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public HalModelBuilder embed(Collection<?> collection, LinkRelation relation) {

		Assert.notNull(collection, "Collection must not be null!");
		Assert.notNull(relation, "Link relation must not be null!");

		return embed(wrappers.wrap(collection, relation));
	}

	/**
	 * Embeds the given {@link Stream} in the {@link RepresentationModel}. The given {@link Stream} will be resolved
	 * immediately, i.e. the method acts as mere syntactic sugar to avoid clients having to collect the {@link Stream}
	 * into a {@link Collection} beforehand. If the {@link Stream} is empty nothing will be added to the
	 * {@link RepresentationModel}.
	 *
	 * @param stream must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public HalModelBuilder embed(Stream<?> stream) {
		return embed(stream, Void.class);
	}

	/**
	 * Embeds the given {@link Stream} in the {@link RepresentationModel} and the {@link LinkRelation} derived from the
	 * given type. The given {@link Stream} will be resolved immediately, i.e. the method acts as mere syntactic sugar to
	 * avoid clients having to collect the {@link Stream} into a {@link Collection} beforehand. If the {@link Stream} is
	 * empty nothing will be added to the {@link RepresentationModel} an empty embed for the derived {@link LinkRelation}
	 * will be added.
	 *
	 * @param stream must not be {@literal null}.
	 * @param type the type to derive the {@link LinkRelation} to be used, must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @see #embed(Collection, LinkRelation)
	 */
	public HalModelBuilder embed(Stream<?> stream, Class<?> type) {

		Assert.notNull(stream, "Stream must not be null!");
		Assert.notNull(type, "Type must not be null!");

		return embed(stream.collect(Collectors.toList()), type);
	}

	/**
	 * Embeds the given {@link Stream} in the {@link RepresentationModel} for the given {@link LinkRelation}. The given
	 * {@link Stream} will be resolved immediately, i.e. the method acts as mere syntactic sugar to avoid clients having
	 * to collect the {@link Stream} into a {@link Collection} beforehand. If the {@link Stream} is empty nothing will be
	 * added to the {@link RepresentationModel} an empty embed for the derived {@link LinkRelation} will be added.
	 *
	 * @param stream must not be {@literal null}.
	 * @param relation must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public HalModelBuilder embed(Stream<?> stream, LinkRelation relation) {

		Assert.notNull(stream, "Stream must not be null!");
		Assert.notNull(relation, "Link relation must not be null!");

		return embed(stream.collect(Collectors.toList()), relation);
	}

	/**
	 * Initiates the setup of a preview given the current payload. Clients have to conclude the setup calling any of the
	 * {@link EntityPreviewBuilder#forLink(Link)} methods. As an example, the call chain of:
	 *
	 * <pre>
	 * ….preview(…).forLink("…", "relation")
	 * </pre>
	 *
	 * will result in the link added to the representation and an embedded being registered for the link's relation:
	 *
	 * <pre>
	 * {
	 *   "_links" : {
	 *     "relation" : { … }
	 *   },
	 *   "_embedded" : {
	 *     "relation" : …
	 *   }
	 * }
	 * </pre>
	 *
	 * @param entity
	 * @return will never be {@literal null}.
	 */
	public PreviewBuilder preview(Object entity) {

		Assert.notNull(entity, "Preview entity must not be null!");

		return link -> this.previewFor(entity, link);
	}

	/**
	 * Starts a preview setup for the given {@link Collection} as preview.
	 *
	 * @param collection
	 * @return will never be {@literal null}.
	 * @see #preview(Object)
	 */
	public PreviewBuilder preview(Collection<?> collection) {

		Assert.notNull(collection, "Preview collection must not be null!");

		return link -> this.previewFor(collection, link);
	}

	/**
	 * Starts a preview setup for the given {@link Collection} as preview falling back to the given type if the
	 * {@link Collection} is empty.
	 *
	 * @param collection must not be {@literal null}.
	 * @param type must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @see #preview(Object)
	 */
	public PreviewBuilder preview(Collection<?> collection, Class<?> type) {

		Assert.notNull(collection, "Preview collection must not be null!");
		Assert.notNull(type, "Type must not be null!");

		return link -> this.previewFor(type, link);
	}

	/**
	 * Add a {@link Link} to the whole thing.
	 * <p/>
	 * NOTE: This adds it to the top level. If you need a link inside an entity, then use the {@link Model.Builder} to
	 * define it as well.
	 *
	 * @param link must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public HalModelBuilder link(Link link) {

		this.links = links.and(link);

		return this;
	}

	/**
	 * Adds a {@link Link} with the given href and {@link LinkRelation} to the {@link RepresentationModel} to be built.
	 *
	 * @param href must not be {@literal null}.
	 * @param relation must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public HalModelBuilder link(String href, LinkRelation relation) {
		return link(Link.of(href, relation));
	}

	/**
	 * Adds the given {@link Link}s to the {@link RepresentationModel} to be built.
	 *
	 * @param links must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public HalModelBuilder links(Iterable<Link> links) {

		this.links = this.links.and(links);

		return this;
	}

	/**
	 * Transform the entities and {@link Link}s into a {@link RepresentationModel}. If there are embedded entries, add a
	 * preferred media type of {@link MediaTypes#HAL_JSON} and {@link MediaTypes#HAL_FORMS_JSON}.
	 *
	 * @return will never be {@literal null}.
	 */
	@SuppressWarnings("unchecked")
	public <T extends RepresentationModel<T>> RepresentationModel<T> build() {
		return (T) new HalRepresentationModel<>(model, CollectionModel.of(embeddeds), links);
	}

	/**
	 * A common usage of embedded entries are to define a read-only preview. This method provides syntax sugar for
	 * {@link #embed(Object, LinkRelation)}.
	 *
	 * @param entity
	 * @param link
	 * @return
	 */
	private HalModelBuilder previewFor(Object entity, Link link) {

		link(link);
		embed(entity, link.getRel());

		return this;
	}

	private static class HalRepresentationModel<T> extends EntityModel<T> {

		private final @Nullable T entity;
		private final CollectionModel<?> embeddeds;

		public HalRepresentationModel(@Nullable T entity, CollectionModel<T> embeddeds, Links links) {

			this(entity, embeddeds);

			add(links);
		}

		private HalRepresentationModel(@Nullable T entity, CollectionModel<?> embeddeds) {

			Assert.notNull(embeddeds, "Embedds must not be null!");

			this.entity = entity;
			this.embeddeds = embeddeds;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.EntityModel#getContent()
		 */
		@Nullable
		@Override
		public T getContent() {
			return entity;
		}

		@JsonUnwrapped
		public CollectionModel<?> getEmbeddeds() {
			return embeddeds;
		}
	}

	public interface PreviewBuilder {

		/**
		 * Concludes the set up of a preview for the given {@link Link}.
		 *
		 * @param link must not be {@literal null}.
		 * @return will never be {@literal null}.
		 * @see HalModelBuilder#preview(Object)
		 */
		HalModelBuilder forLink(Link link);

		/**
		 * Concludes the set up of a preview for the {@link Link} consisting ot the given href and {@link LinkRelation}.
		 *
		 * @param href must not be {@literal null}.
		 * @param relation must not be {@literal null}.
		 * @return will never be {@literal null}.
		 * @see HalModelBuilder#preview(Object)
		 */
		default HalModelBuilder forLink(String href, LinkRelation relation) {
			return forLink(Link.of(href, relation));
		}
	}
}
