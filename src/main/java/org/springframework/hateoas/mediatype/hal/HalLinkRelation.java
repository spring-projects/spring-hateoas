/*
 * Copyright 2019-2021 the original author or authors.
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

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.IanaUriSchemes;
import org.springframework.hateoas.LinkRelation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Value object for HAL based {@link LinkRelation}, i.e. a relation that can be curied.
 *
 * @author Oliver Drotbohm
 */
public class HalLinkRelation implements LinkRelation, MessageSourceResolvable {

	public static final HalLinkRelation CURIES = HalLinkRelation.uncuried("curies");

	private static final String RELATION_MESSAGE_TEMPLATE = "_links.%s.title";

	private final @Nullable String curie;
	private final String localPart;

	private HalLinkRelation(@Nullable String curie, String localPart) {

		Assert.notNull(localPart, "Local part must not be null!");

		this.curie = curie;
		this.localPart = localPart;
	}

	/**
	 * Returns a {@link HalLinkRelation} for the given general {@link LinkRelation}.
	 *
	 * @param relation must not be {@literal null}.
	 * @return
	 */
	public static HalLinkRelation of(LinkRelation relation) {

		Assert.notNull(relation, "LinkRelation must not be null!");

		if (HalLinkRelation.class.isInstance(relation)) {
			return (HalLinkRelation) relation;
		}

		return of(relation.value());
	}

	/**
	 * Creates a new {@link HalLinkRelation} from the given link relation {@link String}.
	 *
	 * @param relation must not be {@literal null}.
	 * @return
	 */
	@JsonCreator
	private static HalLinkRelation of(String relation) {

		int firstColonIndex = relation.indexOf(':');

		String curie = firstColonIndex == -1 ? null : relation.substring(0, firstColonIndex);

		return curie == null || IanaUriSchemes.isIanaUriScheme(curie)
				? new HalLinkRelation(null, relation)
				: new HalLinkRelation(curie, relation.substring(firstColonIndex + 1));
	}

	/**
	 * Creates a new {@link HalLinkRelation} for a curied relation.
	 *
	 * @param curie the curie, must not be {@literal null} or empty.
	 * @param rel the link relation to be used, must not be {@literal null}.
	 * @return
	 */
	public static HalLinkRelation curied(String curie, String rel) {

		Assert.hasText(curie, "Curie must not be null or empty!");

		return new HalLinkRelation(curie, rel);
	}

	/**
	 * Creates a new uncuried {@link HalLinkRelation}.
	 *
	 * @param rel the link relation to be used, must not be {@literal null}.
	 * @return
	 */
	public static HalLinkRelation uncuried(String rel) {
		return new HalLinkRelation(null, rel);
	}

	/**
	 * Creates a new {@link HalLinkRelationBuilder} for the given curie.
	 *
	 * @param curie must not be {@literal null} or empty.
	 * @return will never be {@literal null}.
	 */
	public static HalLinkRelationBuilder curieBuilder(String curie) {
		return relation -> new HalLinkRelation(curie, relation);
	}

	/**
	 * Creates a new {@link HalLinkRelation} curied to the given value.
	 *
	 * @param curie must not be {@literal null} or empty.
	 * @return
	 */
	public HalLinkRelation curie(String curie) {

		Assert.hasText(curie, "Curie must not be null or empty!");

		return new HalLinkRelation(curie, localPart);
	}

	/**
	 * Returns a curied {@link HalLinkRelation} either using the existing curie or the given one if previously uncuried.
	 *
	 * @param curie must not be {@literal null} or empty.
	 * @return
	 */
	public HalLinkRelation curieIfUncuried(String curie) {

		Assert.hasText(curie, "Curie must not be null or empty!");

		return isCuried() || IanaLinkRelations.isIanaRel(localPart) ? this : curie(curie);
	}

	/**
	 * Returns whether the link relation is curied.
	 *
	 * @return
	 */
	public boolean isCuried() {
		return curie != null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkRelation#map(java.util.function.Function)
	 */
	@Override
	public HalLinkRelation map(Function<String, String> mapper) {

		String mappedLocalPart = mapper.apply(localPart);

		return localPart.equals(mappedLocalPart) //
				? this //
				: new HalLinkRelation(curie, mappedLocalPart);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkRelation#value()
	 */
	@JsonValue
	@Override
	public String value() {
		return isCuried() ? String.format("%s:%s", curie, localPart) : localPart;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.MessageSourceResolvable#getCodes()
	 */
	@Override
	@org.springframework.lang.NonNull
	public String[] getCodes() {

		return Stream.of(value(), localPart) //
				.map(it -> String.format(RELATION_MESSAGE_TEMPLATE, it)) //
				.toArray(String[]::new);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.MessageSourceResolvable#getDefaultMessage()
	 */
	@Override
	@org.springframework.lang.NonNull
	public String getDefaultMessage() {
		return "";
	}

	public String getLocalPart() {
		return this.localPart;
	}

	/**
	 * Simple builder interface to easily create multiple {@link HalLinkRelation}s for a single curie.
	 *
	 * @author Oliver Drotbohm
	 */
	public interface HalLinkRelationBuilder {

		/**
		 * Creates a new {@link HalLinkRelation} based on the current curie settings.
		 *
		 * @param relation must not be {@literal null} or empty.
		 * @return will never be {@literal null}.
		 */
		HalLinkRelation relation(String relation);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(@Nullable Object o) {

		if (this == o) {
			return true;
		}

		if (!(o instanceof HalLinkRelation)) {
			return false;
		}

		HalLinkRelation that = (HalLinkRelation) o;

		return Objects.equals(this.curie, that.curie) //
				&& Objects.equals(this.localPart, that.localPart);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.curie, this.localPart);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return value();
	}
}
