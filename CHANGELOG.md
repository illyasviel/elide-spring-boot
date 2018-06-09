<a name="1.3.0"></a>
# [1.3.0](https://github.com/illyasviel/elide-spring-boot/compare/v1.2.0...v1.3.0) (2018-06-09)


### Bug Fixes

* **controller:** avoid wildcard on the root path ([10c66ce](https://github.com/illyasviel/elide-spring-boot/commit/10c66ce))


### Features

* **dependency:** spring boot 2.0.2, elide 4.2.3 ([1720187](https://github.com/illyasviel/elide-spring-boot/commit/1720187))
* **property:** configure to return error objects ([30153c5](https://github.com/illyasviel/elide-spring-boot/commit/30153c5))



<a name="1.2.0"></a>
# [1.2.0](https://github.com/illyasviel/elide-spring-boot/compare/v1.1.0...v1.2.0) (2018-05-01)


### Features

* **annotation:** add `ElideHook` help register function hooks ([7356725](https://github.com/illyasviel/elide-spring-boot/commit/7356725))
* **property:** configure to disable spring dependency injection ([d4392bc](https://github.com/illyasviel/elide-spring-boot/commit/d4392bc))



<a name="1.1.0"></a>
# [1.1.0](https://github.com/illyasviel/elide-spring-boot/compare/v1.0.0...v1.1.0) (2018-04-17)


### Bug Fixes

* catch persistence and spring transaction exception ([57228bc](https://github.com/illyasviel/elide-spring-boot/commit/57228bc))


### Features

* catch ConstraintViolationException, return 422 ([14d46f3](https://github.com/illyasviel/elide-spring-boot/commit/14d46f3))
* **dependency:** spring boot 2.0.1, elide 4.2.2 ([a5614a2](https://github.com/illyasviel/elide-spring-boot/commit/a5614a2))
* custom default pageSize and maxPageSize ([2cdac53](https://github.com/illyasviel/elide-spring-boot/commit/2cdac53))
* custom url prefix ([f2d4334](https://github.com/illyasviel/elide-spring-boot/commit/f2d4334))
* spring dependency injection ([b9c3563](https://github.com/illyasviel/elide-spring-boot/commit/b9c3563))


### BREAKING CHANGES

* add default prefix "/api".



