package org.springframework.hateoas.reactive;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

class PersonControllerTest {
    @Test
    public void testLink() {
        Mono<Link> link = linkTo(methodOn(PersonController.class).showAll()).withRel("people").toMono();

        StepVerifier.create(link)
                .expectNextMatches(l -> {
                    assertThat(l.getRel()).isEqualTo(LinkRelation.of("people"));
                    assertThat(l.getHref()).endsWith("/people");
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void testLink2() {
        var person = new PersonController.Person(1L);
        //                                                     /people
        var baseLink = linkTo(methodOn(PersonController.class).showAll());
        //                  /            1
        var link = baseLink.slash(person.id().toString()).withSelfRel().toMono();

        StepVerifier.create(link)
                .expectNextMatches(l -> {
                    assertThat(l.getRel()).isEqualTo(IanaLinkRelations.SELF);
                    assertThat(l.getHref()).endsWith("/people/1");
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void testLink3() {
        var link = linkTo(methodOn(PersonController.class).show(1L))
                .withSelfRel().toMono();

        StepVerifier.create(link)
                .expectNextMatches(l -> {
                    assertThat(l.getRel()).isEqualTo(IanaLinkRelations.SELF);
                    assertThat(l.getHref()).endsWith("/people/1");
                    return true;
                })
                .verifyComplete();
    }
}
