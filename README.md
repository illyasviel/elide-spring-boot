# Elide integration with Spring Boot
[![Build Status](https://travis-ci.org/illyasviel/elide-spring-boot.svg?branch=master)](https://travis-ci.org/illyasviel/elide-spring-boot)
[![Coverage Status](https://coveralls.io/repos/github/illyasviel/elide-spring-boot/badge.svg?branch=master)](https://coveralls.io/github/illyasviel/elide-spring-boot?branch=master)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.illyasviel.elide/elide-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.illyasviel.elide/elide-spring-boot-starter)

🎉 **Now, Elide has official support for Spring, please check out [Elide Spring Artifacts](https://github.com/yahoo/elide/tree/master/elide-spring).** 🎉

Elide Spring-Boot-Starter will help you use [Elide](https://github.com/yahoo/elide) with Spring Boot

- Automatically configure elide and json:api/GraphQL controller.
- Integrated Spring Transaction.
- Integrated Spring Dependency Injection (optional).
- A convenience annotation, `@ElideCheck("expression")`, help you register elide check.
- A convenience annotation, `@ElideHook(lifeCycle = OnXXX.class)`, help you register elide function hook.
- Catch `org.hibernate.exception.ConstraintViolationException` return HTTP [422](https://tools.ietf.org/html/rfc4918#section-11.2).

## Usage
 
```xml
<dependency>
  <groupId>org.illyasviel.elide</groupId>
  <artifactId>elide-spring-boot-starter</artifactId>
  <version>1.4.0</version>
</dependency>
```

The GraphQL auto configuration takes effect when `elide-graphql` on the classpath.

```xml
<dependency>
  <groupId>com.yahoo.elide</groupId>
  <artifactId>elide-graphql</artifactId>
  <version>${elide.version}</version>
</dependency>
```

## Example

Check out the [elide-spring-boot-sample](elide-spring-boot-sample).

## Configuration

The following shows all the default properties.

```yaml
elide:
  prefix: "/api"
  default-page-size: 20
  max-page-size: 100
  spring-dependency-injection: true
  return-error-objects: false
  mvc:
    enable: true
    get: true
    post: true
    patch: true
    delete: true
    graphql: true
```
