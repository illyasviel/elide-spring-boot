/*
 * Copyright (c) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.illyasviel.elide.spring.boot.autoconfigure;

import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideResponse;
import java.security.Principal;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedHashMap;
import org.illyasviel.elide.spring.boot.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Elide Controller AutoConfiguration.
 * TODO all request mapping produces should without any media type parameters
 *
 * @author olOwOlo
 */
@Configuration
@EnableConfigurationProperties(ElideProperties.class)
@AutoConfigureAfter(ElideAutoConfiguration.class)
@ConditionalOnProperty(prefix = "elide.mvc", value = "enable",
    havingValue = "true", matchIfMissing = true)
public class ElideControllerAutoConfiguration {

  private static final Logger logger = LoggerFactory
      .getLogger(ElideControllerAutoConfiguration.class);

  static final String JSON_API_CONTENT_TYPE = "application/vnd.api+json";

  @Configuration
  @RestController
  @RequestMapping(produces = JSON_API_CONTENT_TYPE)
  @ConditionalOnProperty(prefix = "elide.mvc", value = "get",
      havingValue = "true", matchIfMissing = true)
  public static class ElideGetController {

    private final Elide elide;
    private final ElideProperties elideProperties;

    @Autowired
    public ElideGetController(Elide elide, ElideProperties elideProperties) {
      this.elide = elide;
      this.elideProperties = elideProperties;
    }

    /**
     * Elide [GET] controller.
     */
    @GetMapping(value = "/**")
    public ResponseEntity<String> elideGet(@RequestParam Map<String, String> allRequestParams,
        HttpServletRequest request, Principal authentication) {
      ElideResponse response = elide
          .get(getJsonApiPath(request, elideProperties.getPrefix()),
              new MultivaluedHashMap<>(allRequestParams), authentication);
      return ResponseEntity.status(response.getResponseCode()).body(response.getBody());
    }
  }

  @Configuration
  @RestController
  @RequestMapping(produces = JSON_API_CONTENT_TYPE)
  @ConditionalOnProperty(prefix = "elide.mvc", value = "post",
      havingValue = "true", matchIfMissing = true)
  public static class ElidePostController {

    private final Elide elide;
    private final ElideProperties elideProperties;

    @Autowired
    public ElidePostController(Elide elide, ElideProperties elideProperties) {
      this.elide = elide;
      this.elideProperties = elideProperties;
    }

    /**
     * Elide [POST] controller.
     */
    @PostMapping(value = "/**", consumes = JSON_API_CONTENT_TYPE)
    public ResponseEntity<String> elidePost(@RequestBody String body,
        HttpServletRequest request, Principal authentication) {
      ElideResponse response = elide
          .post(getJsonApiPath(request, elideProperties.getPrefix()), body, authentication);
      return ResponseEntity.status(response.getResponseCode()).body(response.getBody());
    }
  }

  @Configuration
  @RestController
  @RequestMapping(produces = JSON_API_CONTENT_TYPE)
  @ConditionalOnProperty(prefix = "elide.mvc", value = "patch",
      havingValue = "true", matchIfMissing = true)
  public static class ElidePatchController {

    private final Elide elide;
    private final ElideProperties elideProperties;

    @Autowired
    public ElidePatchController(Elide elide, ElideProperties elideProperties) {
      this.elide = elide;
      this.elideProperties = elideProperties;
    }

    /**
     * Elide [PATCH] controller.
     */
    @PatchMapping(value = "/**", consumes = JSON_API_CONTENT_TYPE)
    public ResponseEntity<String> elidePatch(@RequestBody String body,
        HttpServletRequest request, Principal authentication) {
      ElideResponse response = elide.patch(JSON_API_CONTENT_TYPE, JSON_API_CONTENT_TYPE,
          getJsonApiPath(request, elideProperties.getPrefix()), body, authentication);
      return ResponseEntity.status(response.getResponseCode()).body(response.getBody());
    }
  }


  @Configuration
  @RestController
  @RequestMapping(produces = JSON_API_CONTENT_TYPE)
  @ConditionalOnProperty(prefix = "elide.mvc", value = "delete",
      havingValue = "true", matchIfMissing = true)
  public static class ElideDeleteController {

    private final Elide elide;
    private final ElideProperties elideProperties;

    @Autowired
    public ElideDeleteController(Elide elide, ElideProperties elideProperties) {
      this.elide = elide;
      this.elideProperties = elideProperties;
    }

    /**
     * Elide [DELETE](relationships) controller.
     */
    @DeleteMapping(value = "/**", consumes = JSON_API_CONTENT_TYPE)
    public ResponseEntity<String> elideDeleteRelationship(@RequestBody String body,
        HttpServletRequest request, Principal authentication) {
      ElideResponse response = elide
          .delete(getJsonApiPath(request, elideProperties.getPrefix()), body, authentication);
      return ResponseEntity.status(response.getResponseCode()).body(response.getBody());
    }

    /**
     * Elide [DELETE](entity) controller.
     */
    @DeleteMapping(value = "/**")
    public ResponseEntity<String> elideDelete(HttpServletRequest request,
        Principal authentication) {
      ElideResponse response = elide
          .delete(getJsonApiPath(request, elideProperties.getPrefix()), null, authentication);
      return ResponseEntity.status(response.getResponseCode()).body(response.getBody());
    }
  }

  private static String getJsonApiPath(HttpServletRequest request, String prefix) {
    String pathname = (String) request
        .getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    if (pathname.startsWith(prefix + "/")) {
      logger.debug("[{}][{}] forward to elide.", request.getMethod(), pathname);
      return pathname.replaceFirst(prefix, "");
    } else {
      throw new ResourceNotFoundException();
    }
  }
}
