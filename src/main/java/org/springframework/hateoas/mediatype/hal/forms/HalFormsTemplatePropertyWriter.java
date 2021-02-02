/*
 * Copyright 2021 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal.forms;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter;
import com.fasterxml.jackson.databind.util.Annotations;

/**
 * @author Oliver Drotbohm
 */
@SuppressWarnings("null")
class HalFormsTemplatePropertyWriter extends VirtualBeanPropertyWriter {

	private static final long serialVersionUID = 6271264033606657428L;

	private final HalFormsTemplateBuilder builder;

	/**
	 * @param builder must not be {@literal null}.
	 */
	public HalFormsTemplatePropertyWriter(HalFormsTemplateBuilder builder) {

		Assert.notNull(builder, "HalFormsTemplateBuilder must not be null!");

		this.builder = builder;
	}

	/**
	 * @param builder2
	 * @param config
	 * @param declaringClass
	 * @param propDef
	 * @param type
	 */
	public HalFormsTemplatePropertyWriter(HalFormsTemplateBuilder builder, MapperConfig<?> config,
			Annotations annotations, BeanPropertyDefinition propDef, JavaType type) {

		super(propDef, annotations, type);

		this.builder = builder;
	}

	/*
	 * (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter#value(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
	 */
	@Override
	protected Object value(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {

		RepresentationModel<?> model = (RepresentationModel<?>) bean;

		return builder.findTemplates(model);
	}

	/*
	 * (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter#withConfig(com.fasterxml.jackson.databind.cfg.MapperConfig, com.fasterxml.jackson.databind.introspect.AnnotatedClass, com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition, com.fasterxml.jackson.databind.JavaType)
	 */
	@Override
	public VirtualBeanPropertyWriter withConfig(MapperConfig<?> config, AnnotatedClass declaringClass,
			BeanPropertyDefinition propDef, JavaType type) {
		return new HalFormsTemplatePropertyWriter(builder, config, declaringClass.getAnnotations(), propDef, type);
	}
}
