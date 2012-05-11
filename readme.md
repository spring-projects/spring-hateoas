# Spring Hateoas
This project provides some APIs to ease creating REST representations that follow the [HATEOAS](http://en.wikipedia.org/wiki/HATEOAS) principle when working with Spring and especially Spring MVC. The core problem it tries to address is link creation and representation assembly.

## JAXB / JSON integration
As representations for REST web services are usually rendered in either XML or JSON the natural choice of technology to achieve this is either JAXB, JSON or both in combination. To follow HATEOAS principles you need to incorporate links into those representation. Spring HATEOAS provides a set of useful types to ease working with those.

## Links
The `Link` value object follows the Atom link definition and consists of a `rel` and an `href` attribute. It contains a few constants for well known reels such as `self`, `next` etc. The XML representation will render in the Atom namespace.

```java
Link link = new Link("http://localhost:8080/something");
assertThat(link.getHref(), is("http://localhost:8080/something"));
assertThat(link.getRel(), is(Link.SELF));
    
Link link = new Link("http://localhost:8080/something", "my-rel");
assertThat(link.getHref(), is("http://localhost:8080/something"));
assertThat(link.getRel(), is("my-rel"));
```
    
## Resources
As pretty much every representation of a resource will contain some links (at least the `self` one) we provide a base class to actually inherit from when designing representation classes.
```java
class PersonResource extends ResourceSupport {

  String firstname;
  String lastname;
}
```
    
Inheriting from `ResourceSupport` will allow adding links easily:

```java
PersonResource resource = new PersonResource();
resource.firstname = "Dave";
resource.lastname = "Matthews";
resource.add(new Link("http://myhost/people"));
```
    
This would render as follows in JSON:
```java
{ firstname : "Dave",
  lastname : "Matthews",
  _links : [ { rel : "self", href : "http://myhost/people" } ] }
```
      
… or slightly more verbose in XML …
```xml
<person xmlns:atom="http://www.w3.org/2005/Atom">
  <firstname>Dave</firstname>
  <lastname>Matthews</lastname>
  <links>
    <atom:link rel="self" href="http://myhost/people" />
  </links>
</person>
```
    
You can also easily access links contained in that resource:
```java
Link selfLink = new Link("http://myhost/people");
assertThat(resource.getId(), is(selfLink);
assertThat(resource.getLink(Link.SELF), is(selfLink));
```

## Link builder
Now we've got the domain vocabulary in place, but the main challenge remains: how to create the actual URIs to be wrapped into `Link`s in a less fragile way. Right now we'd have to duplicate URI strings all over the place which is brittle and unmaintainable. 

Assume you have your Spring MVC controllers implemented as follows:

```java
@Controller
@RequestMapping("/people")
class PersonController {
  
  @RequestMapping(method = RequestMethod.GET)
  public HttpEntity<PersonResource> showAll() { … }
  
  @RequestMapping(value = "/{person}", method = RequestMethod.GET)
  public HttpEntity<PersonResource> show(@PathVariable Long person) { … }
}
```

We see two conventions here. There's a collection resource exposed through the controller class' `@RequestMapping` annotation with individual elements of that collections exposed as direct sub resource. The collection resource might be exposed at a simple URI (as just shown) or more complex ones like `/people/{id}/addresses`.
Let's say you would like to actually link to the collection resource of all people. Following the approach from up above would cause two problems:

1. To create an absolute URI you'd need to lookup the protocol, hostname, port, servlet base etc. This is cumbersome and requires ugly manual string concatenation code.
2. You probably don't want to concatenate the `/people` on top of your base URI because you'd have to maintain the information in multiple places then. Change the mapping, change all the clients pointing to it.

Spring Hateoas now provides a `ControllerLinkBuilder` that allows to create links by pointing to controller classes:

```java
import static org.sfw.hateoas.mvc.ControllerLinkBuilder.*;

Link link = linkTo(PersonController.class).withRel("people");
assertThat(link.getRel(), is("people"));
assertThat(link.getHref(), endsWith("/people"));
```
``
The `ControllerLinkBuilder` uses Springs `ServletUriComponentsBuilder` under the hood to obtain the basic URI information from the current request. Assuming your application runs at `http://localhost:8080/your-app` This will be exactly the URI you're constructing additional parts on top. The builder now inspects the given controller class for its root mapping and thus end up with `http://localhost:8080/your-app/people`. You can also easily build more nested links as well:

```java
Person person = new Person(1L, "Dave", "Matthews");
//                 /person                 /     1
Link link = linkTo(PersonController.class).slash(person.getId()).withSelfRel();
assertThat(link.getRel(), is(Link.SELF));
assertThat(link.getHref(), endsWith("/people/1"));
```

If your domain class implements the `Identifiable` interface the `slash(…)` method will rather invoke `getId()` on the given object instead of `toString()`. Thus the just shown link creation can be abbreviated to:

```java
class Person implements Identifiable<Long> { 
  public Long getId() { … } 
}
    
Link link = linkTo(PersonController.class).slash(person).withSelfRel();
```
    
The builder also allows creating URI instances to build up e.g. response header values:

```java
HttpHeaders headers = new HttpHeaders();
headers.setLocation(linkTo(PersonController.class).slash(person).toUri());
return new ResponseEntity<PersonResource>(headers, HttpStatus.CREATED);
```

## Resource assembler
As the mapping from an entity to a resource type will have to be used in multiple places it makes sense to create a dedicated class responsible for doing so. The conversion will of course contain very custom steps but also a few boilerplate ones:

1. Instantiation of the resource class
2. Adding a link with rel `self` pointing to the resource that gets rendered.

Spring Hateoas now provides a `ResourceAssemblerSupport` base class that helps reducing the amount of code needed to be written:

```java
class PersonResourceAssembler extends ResourceAssemblerSupport<Person, PersonResource> {
  
  public PersonResourceAssembler() {
    super(PersonController.class, PersonResource.class);
  }
  
  @Override
  public PersonResource toResource(Person person) {
  
    PersonResource resource = createResource(person);
    // … do further mapping
    return resource;
  }
}
```
    
Setting the class up like this gives you the following benefits: there are a hand full of `createResource(…)` methods that will allow you to create an instance of the resource and have it a `Link` with a rel of `self` added to it. The href of that link is determined by the configured controllers request mapping plus the id of the `Identifiable` (e.g. `/people/1` in our case). The resource type gets instantiated by reflection and expects a no-arg constructor. Simply override `instantiateResource(…)` in case you'd like to use a dedicated constructor or avoid the reflection performance overhead.

The assembler can then be used to either assemble a single resource or an `Iterable` of them:

```java
Person person = new Person(…);
Iterable<Person> people = Collections.singletonList(person);

PersonResourceAssembler assembler = new PersonResourceAssembler();
PersonResource resource = assembler.toResource(person);
List<PersonResource> resources = assembler.toResource(people);
```