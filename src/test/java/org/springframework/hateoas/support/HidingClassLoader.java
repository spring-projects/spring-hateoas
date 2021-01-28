/*
 * Copyright 2017-2021 the original author or authors.
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
package org.springframework.hateoas.support;

import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.instrument.classloading.ShadowingClassLoader;
import org.springframework.util.Assert;

/**
 * is intended for testing code that depends on the presence/absence of certain classes. Classes can be:
 * <ul>
 * <li>shadowed: reloaded by this classloader no matter if they are loaded already by the SystemClassLoader</li>
 * <li>hidden: not loaded by this classloader no matter if they are loaded already by the SystemClassLoader. Trying to
 * load these classes results in a {@link ClassNotFoundException}</li>
 * <li>all other classes get loaded by the SystemClassLoader</li>
 * </ul>
 *
 * @author Jens Schauder
 * @author Oliver Gierke
 */
public class HidingClassLoader extends ShadowingClassLoader {

	private final Collection<String> hidden;

	HidingClassLoader(Collection<String> hidden) {

		super(URLClassLoader.getSystemClassLoader(), false);

		this.hidden = hidden;
	}

	/**
	 * Creates a new {@link HidingClassLoader} with the packages of the given classes hidden.
	 *
	 * @param packages must not be {@literal null}.
	 * @return
	 */
	public static HidingClassLoader hide(Class<?>... packages) {

		Assert.notNull(packages, "Packages must not be null!");

		return new HidingClassLoader(Arrays.stream(packages)//
				.map(it -> it.getPackage().getName())//
				.collect(Collectors.toList()));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.classloadersupport.ShadowingClassLoader#loadClass(java.lang.String)
	 */
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {

		checkIfHidden(name);
		return super.loadClass(name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.classloadersupport.ShadowingClassLoader#isEligibleForShadowing(java.lang.String)
	 */
	@Override
	protected boolean isEligibleForShadowing(String className) {
		return isExcluded(className);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.ClassLoader#findClass(java.lang.String)
	 */
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {

		checkIfHidden(name);
		return super.findClass(name);
	}

	private void checkIfHidden(String name) throws ClassNotFoundException {

		if (hidden.stream().anyMatch(it -> name.startsWith(it))) {
			throw new ClassNotFoundException();
		}
	}
}
