/*
 * Copyright 2022-2024 the original author or authors.
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
package org.springframework.hateoas.aot;

import static org.springframework.hateoas.aot.AotUtils.*;

import org.springframework.beans.factory.aot.BeanRegistrationAotContribution;
import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;

/**
 * A {@link BeanRegistrationAotProcessor} that inspects {@link RepresentationModelAssembler}'s generics for domain types
 * wrapped in {@link EntityModel} and {@link CollectionModel}.
 *
 * @author Oliver Drotbohm
 * @since 2.0
 */
class RepresentationModelAssemblerAotProcessor implements BeanRegistrationAotProcessor {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.aot.BeanRegistrationAotProcessor#processAheadOfTime(org.springframework.beans.factory.support.RegisteredBean)
	 */
	@Override
	public BeanRegistrationAotContribution processAheadOfTime(RegisteredBean registeredBean) {

		var beanClass = registeredBean.getBeanClass();

		if (!RepresentationModelAssembler.class.isAssignableFrom(beanClass)) {
			return null;
		}

		var modelType = registeredBean.getBeanType().as(RepresentationModelAssembler.class).getGeneric(1);

		return (context, code) -> {

			var reflection = context.getRuntimeHints().reflection();

			registerModelDomainTypesForReflection(modelType, reflection, beanClass);
		};
	}
}
