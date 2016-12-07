package org.springframework.hateoas.config;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.Assert;

/**
 * @author Alexander Morozov
 */
final class HateoasConfigurerResolver {

    private HateoasConfigurerResolver() {
        // empty
    }

    public static HateoasConfigurer resolveHateoasConfigurer(BeanFactory beanFactory) {
        Assert.notNull(beanFactory, "Bean factory is required.");
        HateoasConfigurer configurer = null;
        try {
            configurer = beanFactory.getBean(HateoasConfigurer.class);
        } catch (NoSuchBeanDefinitionException ex) {
            // nop
        }
        if (configurer == null) {
            configurer = new DefaultHateoasConfigurer();
        }
        return configurer;
    }

}
