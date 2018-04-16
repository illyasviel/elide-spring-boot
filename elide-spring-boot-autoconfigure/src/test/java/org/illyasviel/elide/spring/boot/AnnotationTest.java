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

import com.yahoo.elide.Elide;
import com.yahoo.elide.security.checks.Check;
import java.util.Map;
import org.illyasviel.elide.spring.boot.check.RejectAll;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author olOwOlo
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TApplication.class)
public class AnnotationTest {

  @Autowired
  private Elide elide;
  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @Before
  public void before() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  public void testCheckAnnotation() {
    Map<String, Class<? extends Check>> checkMap = elide.getElideSettings()
        .getDictionary().getCheckMappings();
    assertThat(checkMap.get(RejectAll.INLINE_REJECT))
        .isEqualTo(org.illyasviel.elide.spring.boot.check.RejectAll.Inline.class);
    assertThat(checkMap.get(RejectAll.AT_COMMIT_REJECT))
        .isEqualTo(org.illyasviel.elide.spring.boot.check.RejectAll.AtCommit.class);
    assertThat(checkMap.containsKey(RejectAll.WRONG_CLASS_TYPE)).isEqualTo(false);
  }

  @Test
  public void testReadPermissionRejectAllCheck() throws Exception {
    String postString = "{ \"data\": { \"type\": \"rejectEntity\", \"attributes\": { \"name\": \"name\" } } }";
    mockMvc.perform(post("/api/rejectEntity")
        .contentType(JSON_API_CONTENT_TYPE)
        .content(postString)
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(content().contentType(JSON_API_RESPONSE))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/api/rejectEntity")
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(content().contentType(JSON_API_RESPONSE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }
}
