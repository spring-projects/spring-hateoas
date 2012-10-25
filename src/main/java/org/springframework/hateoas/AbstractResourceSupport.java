package org.springframework.hateoas;

import org.springframework.util.Assert;

public abstract class AbstractResourceSupport implements Identifiable<Link> {

    public AbstractResourceSupport() {
        super();
    }

    /**
     * Adds the given link to the resource.
     * 
     * @param link
     */
    public abstract void add(Link link);

    /**
     * Returns whether the resource contains a {@link Link} with the given rel.
     * 
     * @param rel
     * @return
     */
    public abstract boolean hasLink(String rel);

    /**
     * Adds all given {@link Link}s to the resource.
     * 
     * @param links
     */
    public void add(Iterable<Link> links) {
        Assert.notNull(links, "Given links must not be null!");
        for (Link candidate : links) {
            add(candidate);
        }
    }

    /**
     * Returns whether the resource contains {@link Link}s at all.
     * 
     * @return
     */
    public abstract boolean hasLinks();

}