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

import static org.illyasviel.elide.spring.boot.ElideIntegrationTest.JSON_API_CONTENT_TYPE;
import static org.illyasviel.elide.spring.boot.ElideIntegrationTest.JSON_API_RESPONSE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author olOwOlo
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TApplication.class)
public class SpringDITest {

  @Autowired
  private WebApplicationContext wac;

  @Transactional
  @Test
  public void testDI() throws Exception {
    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

    String postBook = "{\"data\": {\"type\": \"account\",\"attributes\": {\"username\": \"username\",\"password\": \"password\"}}}";

    mockMvc.perform(post("/api/account")
        .contentType(JSON_API_CONTENT_TYPE)
        .content(postBook)
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(content().contentType(JSON_API_RESPONSE))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/api/account")
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(content().contentType(JSON_API_RESPONSE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].attributes.username").value("encoded username"))
        .andExpect(jsonPath("$.data[0].attributes.password").value("encoded password"));
  }
}
