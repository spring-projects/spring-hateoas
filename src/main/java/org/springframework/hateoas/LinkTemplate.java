package org.springframework.hateoas;

import java.util.HashMap;
import java.util.Map;

public class LinkTemplate extends Link {

    private static final long serialVersionUID = 2608991124877231648L;
    private Map<String, Class<?>> params = new HashMap<String, Class<?>>();



    public LinkTemplate(String href, String rel) {
        super(href, rel);
    }

    public LinkTemplate(String href) {
        super(href);
    }

    public LinkTemplate(Link link) {
        super(link.getHref(), link.getRel());
    }

    public void addParam(String param, Class<?> type) {
        params.put(param, type);
    }

    public Map<String, Class<?>> getParams() {
        return params;
    }



}
