/*
 * Copyright 2013 the original author or authors.
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
package org.springframework.hateoas.mvc;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

/**
 * Special {@link ResponseEntity} that exposes {@link Link} instances in the contained {@link ResourceSupport} as link
 * headers instead of in the body. Note, that this class is not intended to be used directly from user code but by
 * support code that will transparently invoke the header exposure. If you use this class from a controller directly,
 * the {@link Link}s will not be present in the {@link ResourceSupport} instance anymore when {@link ResourceProcessor}s
 * kick in.
 * 
 * @author Oliver Gierke
 */
public class HeaderLinksResponseEntity<T extends ResourceSupport> extends ResponseEntity<T> {

	/**
	 * Creates a new {@link HeaderLinksResponseEntity} from the given {@link ResponseEntity}.
	 * 
	 * @param entity must not be {@literal null}.
	 */
	private HeaderLinksResponseEntity(ResponseEntity<T> entity) {

		super(entity.getBody(), getHeadersWithLinks(entity), entity.getStatusCode());
		entity.getBody().removeLinks();
	}

	/**
	 * Creates a new {@link HeaderLinksResponseEntity} from the given {@link HttpEntity} by defaulting the status code to
	 * {@link HttpStatus#OK}.
	 * 
	 * @param entity must not be {@literal null}.
	 */
	private HeaderLinksResponseEntity(HttpEntity<T> entity) {
		this(ResponseEntity.ok().headers(entity.getHeaders()).body(entity.getBody()));
	}

	/**
	 * Wraps the given {@link HttpEntity} into a {@link HeaderLinksResponseEntity}. Will default the status code to
	 * {@link HttpStatus#OK} if the given value is not a {@link ResponseEntity}.
	 * 
	 * @param entity must not be {@literal null}.
	 * @return
	 */
	public static <S extends ResourceSupport> HeaderLinksResponseEntity<S> wrap(HttpEntity<S> entity) {

		Assert.notNull(entity, "Given HttpEntity must not be null!");

		if (entity instanceof ResponseEntity) {
			return new HeaderLinksResponseEntity<S>((ResponseEntity<S>) entity);
		} else {
			return new HeaderLinksResponseEntity<S>(entity);
		}
	}

	/**
	 * Wraps the given {@link ResourceSupport} into a {@link HeaderLinksResponseEntity}. Will default the status code to
	 * {@link HttpStatus#OK}.
	 * 
	 * @param entity must not be {@literal null}.
	 * @return
	 */
	public static <S extends ResourceSupport> HeaderLinksResponseEntity<S> wrap(S entity) {

		Assert.notNull(entity, "ResourceSupport must not be null!");

		return new HeaderLinksResponseEntity<>(ResponseEntity.ok(entity));
	}

	/**
	 * Returns the {@link Link}s contained in the {@link ResourceSupport} of the given {@link ResponseEntity} as
	 * {@link HttpHeaders}.
	 * 
	 * @param entity must not be {@literal null}.
	 * @return
	 */
	private static <T extends ResourceSupport> HttpHeaders getHeadersWithLinks(ResponseEntity<T> entity) {

		List<Link> links = entity.getBody().getLinks();

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.putAll(entity.getHeaders());
		httpHeaders.add("Link", new Links(links).toString());

		return httpHeaders;
	}
}
