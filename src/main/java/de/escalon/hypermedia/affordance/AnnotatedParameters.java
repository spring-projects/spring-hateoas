package de.escalon.hypermedia.affordance;

/**
 * Represents a set of named annotated parameters.
 * Created by Dietrich on 17.05.2015.
 */
public interface AnnotatedParameters {
    AnnotatedParameter getAnnotatedParameter(String name);
}
