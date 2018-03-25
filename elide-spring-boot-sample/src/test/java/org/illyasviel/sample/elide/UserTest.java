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

package org.illyasviel.sample.elide;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
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
@SpringBootTest
public class UserTest {

  private static final String JSON_API_CONTENT_TYPE = "application/vnd.api+json";

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext wac;

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Transactional
  @Test
  public void test() throws Exception {
    // test create
    mockMvc.perform(post("/user")
        .content("{ \"data\": { \"type\": \"user\", \"attributes\": { \"password\": \"test\", \"username\": \"test\" }}}")
        .contentType(JSON_API_CONTENT_TYPE))
        .andExpect(status().isCreated());

    // test select
    mockMvc.perform(get("/user"))
        .andExpect(content().string("{\"data\":[{\"type\":\"user\",\"id\":\"1\",\"attributes\":{\"password\":\"test\",\"username\":\"test\"}}]}"))
        .andExpect(status().isOk());
    }
}
