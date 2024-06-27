package org.springframework.hateoas;

import java.util.ArrayList;

class LinkParser {
    public static Links parse(String source) {
        var links = new ArrayList<Link>();
        int[] pos = {0}; // single-element array used as a mutable integer
        int l = source.length();
        boolean inLink = true; // true if we're expecting to find a link; false if we're expecting end of input, or a comma
        while (pos[0] < l) {
            char ch = source.charAt(pos[0]);
            if (Character.isWhitespace(ch)) {
                pos[0]++;
                continue;
            }
            if (inLink) {
                if (ch == '<') {
                    // start of a link, consume it using the Link class
                    Link link = Link.valueOfInt(source, pos);
                    // In a single link there can be multiple rels separated by whitespace. The Link class doesn't handle this
                    // because it doesn't have API to handle it. However, at this level, we can split the rels and create a
                    // separate Link for each rel.
                    String[] rels = link.getRel().value().split("\\s");
                    if (rels.length == 0) {
                        throw new IllegalArgumentException("A link with missing rel at " + pos[0]);
                    }
                    for (String rel : rels) {
                        links.add(link.withRel(rel));
                    }
                    inLink = false;
                    continue;
                }
                else if (ch == ',') {
                    pos[0]++;
                    continue;
                }
            } else {
                // there must be a comma to move on to another link
                if (ch == ',') {
                    pos[0]++;
                    inLink = true;
                    continue;
                }
            }
            // The parsing algorithm in appendix B.2 of RFC-8288 suggests ignoring unexpected content at the end of a link.
            // At the same time it specifies that implementations aren't required to support them. We believe that missing
            // terminal `>` or unexpected data after the end point to more serious problem, and we throw an exception
            // in that case.
            throw new IllegalArgumentException("Unexpected data at the end of Link header at index " + pos[0]);
        }

        if (links.isEmpty()) {
            return Links.NONE;
        }

        return new Links(links);
    }
}
