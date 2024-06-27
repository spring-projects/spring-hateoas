package org.springframework.hateoas;

import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.util.*;

class LinkParser {
    public static List<Link> parseLinks(String source) {
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
                    Link link = parseLink(source, pos);
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

        return links;
    }

    /**
     * Internal method to parse and consume one link from input string.
     *
     * @param input The input string
     * @param pos Position to start from. It must be a 1-element array. The element will be
     *              mutated to point to the first non-consumed character (either ',' or the end of input).
     * @return a non-null Link
     */
    @NonNull
    static Link parseLink(@NonNull String input, @NonNull int[] pos) {
        assert pos.length == 1;
        int l = input.length();
        while (pos[0] < l && Character.isWhitespace(input.charAt(pos[0]))) {
            pos[0]++;
        }
        if (input.charAt(pos[0]) != '<') {
            throw new IllegalArgumentException("Expecting '<' at index " + pos[0]);
        }
        pos[0]++;
        int urlEnd = input.indexOf('>', pos[0]);
        if (urlEnd < 0) {
            throw new IllegalArgumentException("Missing closing '>' at index " + input.length());
        }
        String url = input.substring(pos[0], urlEnd);
        pos[0] = urlEnd + 1;

        // parse parameters
        Map<String, String> params = new HashMap<>();
        enum State { INITIAL, IN_KEY, BEFORE_VALUE, IN_VALUE };
        State state = State.INITIAL;
        StringBuilder
                key = new StringBuilder(),
                value = new StringBuilder();

        outer:
        while (pos[0] <= l) {
            boolean eoi = pos[0] == l; // EOI - end of input
            char ch = eoi ? 0 : input.charAt(pos[0]);
            switch (state) {
                // searching for the initial `;`
                case INITIAL:
                    if (Character.isWhitespace(ch)) {
                        pos[0]++;
                    }
                    else if (ch == ';') {
                        state = State.IN_KEY;
                        pos[0]++;
                    }
                    else {
                        // if there's something else, it's the end of this link
                        break outer;
                    }
                    break;

                // consuming the key up to `=`
                case IN_KEY:
                    if (ch == '=') {
                        state = State.BEFORE_VALUE;
                    }
                    // value isn't mandatory, so param separator, link separator, or end of input all create a new param
                    else if (ch == ';' || ch == ',' || eoi) {
                        if (!key.isEmpty()) {
                            params.put(key.toString().trim(), "");
                            key.setLength(0);
                        }
                    } else {
                        key.append(ch);
                    }
                    pos[0]++;
                    break;

                case BEFORE_VALUE:
                    if (Character.isWhitespace(ch)) {
                        pos[0]++;
                    }
                    else if (ch == '"' || ch == '\'') {
                        consumeQuotedString(input, value, pos);
                        params.putIfAbsent(key.toString().trim(), value.toString());
                        key.setLength(0);
                        value.setLength(0);
                        state = State.INITIAL;
                    } else {
                        state = State.IN_VALUE;
                    }
                    break;

                case IN_VALUE:
                    if (ch == ';' || ch == ',' || eoi) {
                        params.putIfAbsent(key.toString().trim(), value.toString().trim());
                        key.setLength(0);
                        value.setLength(0);
                        state = State.INITIAL;
                    } else {
                        value.append(ch);
                        pos[0]++;
                    }
                    break;

                default:
                    throw new AssertionError();
            }
        }

        String sRel = params.get("rel");
        if (!StringUtils.hasText(sRel)) {
            throw new IllegalArgumentException("Missing 'rel' attribute at index " + pos[0]);
        }
        LinkRelation rel = LinkRelation.of(sRel);
        String hrefLang = params.get("hreflang");
        String media = params.get("media");
        String title = params.get("title");
        String type = params.get("type");
        String deprecation = params.get("deprecation");
        String profile = params.get("profile");
        String name = params.get("name");

        return new Link(rel, url, hrefLang, media, title, type, deprecation, profile, name, Link.templateOrNull(url),
                Collections.emptyList());
    }

    /**
     * Consume a quoted string from `input`, adding its contents to `target`. The starting position should be at
     * starting quote. After consuming, the ending position will be just after the last final quote.
     */
    private static void consumeQuotedString(String input, StringBuilder target, int[] pos) {
        int l = input.length();
        char quotingChar = input.charAt(pos[0]);
        assert quotingChar == '"' || quotingChar == '\'';
        // skip quoting char
        pos[0]++;
        for (; pos[0] < l; pos[0]++) {
            char ch = input.charAt(pos[0]);
            if (ch == quotingChar) {
                pos[0]++; // consume the final quote
                return;
            }
            if (ch == '\\') {
                ch = input.charAt(++pos[0]);
            }
            target.append(ch);
        }
        throw new IllegalArgumentException("Missing final quote at index " + pos[0]);
    }
}
