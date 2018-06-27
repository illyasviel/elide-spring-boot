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

package org.illyasviel.elide.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.illyasviel.elide.spring.boot.ElideIntegrationTest.JSON_API_CONTENT_TYPE;
import static org.illyasviel.elide.spring.boot.ElideIntegrationTest.JSON_API_RESPONSE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.illyasviel.elide.spring.boot.autoconfigure.ElideControllerAutoConfiguration;
import org.illyasviel.elide.spring.boot.autoconfigure.ElideControllerAutoConfiguration.ElideDeleteController;
import org.illyasviel.elide.spring.boot.autoconfigure.ElideControllerAutoConfiguration.ElideGetController;
import org.illyasviel.elide.spring.boot.autoconfigure.ElideControllerAutoConfiguration.ElidePatchController;
import org.illyasviel.elide.spring.boot.autoconfigure.ElideControllerAutoConfiguration.ElidePostController;
import org.illyasviel.elide.spring.boot.autoconfigure.ElideProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

/**
 * @author olOwOlo
 */
@Configuration
@EnableConfigurationProperties(ElideProperties.class)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TApplication.class)
@TestPropertySource(properties = {"elide.mvc.enable=true", "elide.mvc.get=true",
    "elide.mvc.post=true", "elide.mvc.patch=false", "elide.mvc.delete=false",
    "elide.return-error-objects=true", "elide.spring-dependency-injection=false"})
public class ElidePropertiesTest {

  @Autowired
  private ElideProperties elideProperties;
  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @Before
  public void before() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  public void testElideProperties() {
    assertThat(elideProperties.getPrefix()).isEqualTo("/api");
    assertThat(elideProperties.getDefaultPageSize()).isEqualTo(20);
    assertThat(elideProperties.getMaxPageSize()).isEqualTo(100);
    assertThat(elideProperties.isReturnErrorObjects()).isTrue();
    assertThat(elideProperties.isSpringDependencyInjection()).isFalse();
    assertThat(elideProperties.getMvc().isEnable()).isTrue();
    assertThat(elideProperties.getMvc().isGet()).isTrue();
    assertThat(elideProperties.getMvc().isPost()).isTrue();
    assertThat(elideProperties.getMvc().isPatch()).isFalse();
    assertThat(elideProperties.getMvc().isDelete()).isFalse();
    assertThat(elideProperties.getMvc().isGraphql()).isTrue();
  }

  @Test
  public void testMVCBean() {
    wac.getBean(ElideControllerAutoConfiguration.class);
  }

  @Test
  public void testGetBean() {
    wac.getBean(ElideGetController.class);
  }

  @Test
  public void testPostBean() {
    wac.getBean(ElidePostController.class);
  }

  @Test(expected = NoSuchBeanDefinitionException.class)
  public void testPatchBean() {
    wac.getBean(ElidePatchController.class);
  }

  @Test(expected = NoSuchBeanDefinitionException.class)
  public void testDeleteBean() {
    wac.getBean(ElideDeleteController.class);
  }

  @Transactional
  @Test(expected = NullPointerException.class)
  public void testDisableDI() throws Throwable {
    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

    try {
      String postBook = "{\"data\": {\"type\": \"account\",\"attributes\": {\"username\": \"username\",\"password\": \"password\"}}}";

      mockMvc.perform(post("/api/account")
          .contentType(JSON_API_CONTENT_TYPE)
          .content(postBook)
          .accept(JSON_API_CONTENT_TYPE))
          .andExpect(content().contentType(JSON_API_RESPONSE))
          .andExpect(status().isCreated());
    } catch (NestedServletException e) {
      throw e.getCause();
    }
  }

  @Test
  public void testErrorObjects() throws Exception {
    mockMvc.perform(get("/api/author/666")
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(content().contentType(JSON_API_RESPONSE))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].detail").value("InvalidObjectIdentifierException: Unknown identifier '666' for author"));
  }
}
