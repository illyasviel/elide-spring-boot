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

import org.illyasviel.elide.spring.boot.autoconfigure.ElideControllerAutoConfiguration;
import org.illyasviel.elide.spring.boot.autoconfigure.ElideControllerAutoConfiguration.ElideDeleteController;
import org.illyasviel.elide.spring.boot.autoconfigure.ElideControllerAutoConfiguration.ElideGetController;
import org.illyasviel.elide.spring.boot.autoconfigure.ElideControllerAutoConfiguration.ElidePatchController;
import org.illyasviel.elide.spring.boot.autoconfigure.ElideControllerAutoConfiguration.ElidePostController;
import org.illyasviel.elide.spring.boot.autoconfigure.ElideProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author olOwOlo
 */
@Configuration
@EnableConfigurationProperties(ElideProperties.class)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TApplication.class)
@TestPropertySource(properties = {"elide.mvc.enable=true", "elide.mvc.get=true",
    "elide.mvc.post=false", "elide.mvc.patch=false", "elide.mvc.delete=false"})
public class ElidePropertiesTest {

  @Autowired
  private ElideProperties elideProperties;
  @Autowired
  private WebApplicationContext wac;

  @Test
  public void testElideProperties() {
    assertThat(elideProperties.getMvc().isEnable()).isTrue();
    assertThat(elideProperties.getPrefix()).isEqualTo("/api");
    assertThat(elideProperties.getDefaultPageSize()).isEqualTo(20);
    assertThat(elideProperties.getMaxPageSize()).isEqualTo(100);
    assertThat(elideProperties.isSpringDependencyInjection()).isTrue();
    assertThat(elideProperties.getMvc().isGet()).isTrue();
    assertThat(elideProperties.getMvc().isPost()).isFalse();
    assertThat(elideProperties.getMvc().isPatch()).isFalse();
    assertThat(elideProperties.getMvc().isDelete()).isFalse();
  }

  @Test
  public void testMVCBean() {
    assertThat(wac.getBean(ElideControllerAutoConfiguration.class));
  }

  @Test
  public void testGetBean() {
    assertThat(wac.getBean(ElideGetController.class));
  }

  @Test(expected = NoSuchBeanDefinitionException.class)
  public void testPostBean() {
    assertThat(wac.getBean(ElidePostController.class));
  }

  @Test(expected = NoSuchBeanDefinitionException.class)
  public void testPatchBean() {
    assertThat(wac.getBean(ElidePatchController.class));
  }

  @Test(expected = NoSuchBeanDefinitionException.class)
  public void testDeleteBean() {
    assertThat(wac.getBean(ElideDeleteController.class));
  }
}
