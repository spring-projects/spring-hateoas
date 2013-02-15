package org.springframework.hateoas.jackson;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.HandlerInstantiator;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringManagedJackson1HandlerInstantiator extends HandlerInstantiator implements ApplicationContextAware {

	private ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}

	@Override
	public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated,
			Class<? extends JsonDeserializer<?>> deserClass) {
		return context.getBean(deserClass);
	}

	@Override
	public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated,
			Class<? extends KeyDeserializer> keyDeserClass) {
		return context.getBean(keyDeserClass);
	}

	@Override
	public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated,
			Class<? extends JsonSerializer<?>> serClass) {
		return context.getBean(serClass);
	}

	@Override
	public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated,
			Class<? extends TypeResolverBuilder<?>> builderClass) {
		return context.getBean(builderClass);
	}

	@Override
	public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated,
			Class<? extends TypeIdResolver> resolverClass) {
		return context.getBean(resolverClass);
	}

}
