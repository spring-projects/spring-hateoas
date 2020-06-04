package org.springframework.hateoas.support;

import java.util.Collection;

import org.springframework.hateoas.config.HypermediaMappingInformation;
import org.springframework.hateoas.config.MediaTypeConfigurationProvider;
import org.springframework.http.MediaType;

public class SpringFactoriesCustomMediaProvider implements MediaTypeConfigurationProvider {

	@Override
	public Class<? extends HypermediaMappingInformation> getConfiguration() {
		return SpringFactoriesCustomMediatype.class;
	}

	@Override
	public boolean supportsAny(Collection<MediaType> providedMediaTypes) {
		return providedMediaTypes.contains(SpringFactoriesCustomMediatype.BILBO_MEDIATYPE);
	}
}
