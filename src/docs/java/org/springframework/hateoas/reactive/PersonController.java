package org.springframework.hateoas.reactive;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@Controller
@RequestMapping("/people")
class PersonController {

    @GetMapping
    Flux<PersonModel> showAll() {
        return Flux.just(new PersonModel(new Person(1L)));
    }

    @GetMapping("/{person}")
    Mono<PersonModel> show(@PathVariable Long person) {
        return Mono.just(new PersonModel(new Person(person)));
    }

    private Mono<ResponseEntity<PersonModel>> getHeader(Long person) {
        var baseLink = linkTo(methodOn(PersonController.class).showAll());
        return baseLink.slash(person.toString()).withSelfRel().toMono()
                .map(l -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setLocation(l.toUri());
                    return new ResponseEntity<>(headers, HttpStatus.CREATED);
                });
    }

    record Person(Long id) {
    }

    static class PersonModel extends EntityModel<Person> {
        public PersonModel(Person person) {
            super(person);
        }
    }
}

