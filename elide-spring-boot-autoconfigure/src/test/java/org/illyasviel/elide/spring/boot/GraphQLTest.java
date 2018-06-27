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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.illyasviel.elide.spring.boot.repository.AccountRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * GraphQLTest.
 *
 * @author olOwOlo
 */
@Transactional
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TApplication.class)
public class GraphQLTest {

  private final static ObjectMapper objectMapper = new ObjectMapper();

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private AccountRepository accountRepository;

  @Before
  public void before() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  public void testCreate() throws Exception {
    graphQLQuery(
        "mutation createAccount($name: String, $pw: String) { account(op: UPSERT, data: { username: $name, password: $pw }) { edges { node { id username password } } } }",
        ImmutableMap.of("name", "alice", "pw", "123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.account.edges[0].node.username").value("alice"))
        .andExpect(jsonPath("$.data.account.edges[0].node.password").value("123"));
  }

  @Sql(statements = "insert into account(id, username, password) values (233, 'alice', '123')")
  @Test
  public void testRead() throws Exception {
    graphQLQuery("query { account { edges { node { id username password } } } }")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.account.edges.length()").value(1))
        .andExpect(jsonPath("$.data.account.edges[0].node.id").value("233"))
        .andExpect(jsonPath("$.data.account.edges[0].node.username").value("alice"))
        .andExpect(jsonPath("$.data.account.edges[0].node.password").value("123"));
  }

  @Sql(statements = {
      "insert into account(id, username, password) values (233, 'alice', '123')",
      "delete from book",
      "insert into book(id, unique_number) values (666, 1)"
  })
  @Test
  public void testReadByArray() throws Exception {
    String accountQuery = toJsonQuery("query { account { edges { node { id username password } } } }", null);
    String bookQuery = toJsonQuery("query { book { edges { node { id uniqueNumber } } } }", null);

    mockMvc.perform(post("/api/graphql")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content("[" + accountQuery + "," + bookQuery + "]"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].data.account.edges[0].node.id").value("233"))
        .andExpect(jsonPath("$[1].data.book.edges[0].node.id").value("666"))
        .andExpect(jsonPath("$[1].data.book.edges[0].node.uniqueNumber").value(1));
  }

  @Sql(statements = "insert into account(id, username, password) values (233, 'alice', '123')")
  @Test
  public void testUpdate() throws Exception {
    graphQLQuery("mutation { account(op: UPDATE, data: { id: \"233\", username: \"bob\" }) { edges { node { id username } } } }")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.account.edges.length()").value(1))
        .andExpect(jsonPath("$.data.account.edges[0].node.id").value("233"))
        .andExpect(jsonPath("$.data.account.edges[0].node.username").value("bob"));
  }

  @Sql(statements = "insert into account(id, username, password) values (1, 'alice', '123'), (2, 'bob', '123'), (3, 'eve', '123')")
  @Test
  public void testDelete() throws Exception {
    graphQLQuery("mutation { account(op: DELETE, ids: [\"1\", \"2\"]) { edges { node { id username } } } }")
        .andExpect(status().isOk());

    assertThat(accountRepository.findAll().size()).isEqualTo(1);
  }

  @Test
  public void testInvalidJson() throws Exception {
    mockMvc.perform(post("/api/graphql")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content("Invalid Json"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testInvalidGraphQL() throws Exception {
    graphQLQuery("mutation create($some, $pw: String) { account() { } }")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errors").exists())
        .andExpect(jsonPath("$.errors[0].message").value("Invalid Syntax"));
  }

  @Sql(statements = {
      "delete from book",
      "insert into book(id, unique_number) values (666, 1)"
  })
  @Test
  public void testPersistenceException() throws Exception {
    graphQLQuery("mutation { book(op: UPSERT, data: { uniqueNumber: 1 }) { edges { node { id uniqueNumber } } } }")
        .andExpect(jsonPath("$.errors.length()").value(1));
  }

  private ResultActions graphQLQuery(String query) throws Exception {
    return graphQLQuery(query, null);
  }

  private ResultActions graphQLQuery(String query, Map<String, Object> variables) throws Exception {
    return mockMvc.perform(post("/api/graphql")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(toJsonQuery(query, variables)));
  }

  private String toJsonQuery(String query, Map<String, Object> variables) throws JsonProcessingException {
    return objectMapper.writeValueAsString(toJsonNode(query, variables));
  }

  private JsonNode toJsonNode(String query, Map<String, Object> variables) {
    ObjectNode graphQLNode = JsonNodeFactory.instance.objectNode();
    graphQLNode.put("query", query);
    if (variables != null) {
      graphQLNode.set("variables", objectMapper.valueToTree(variables));
    }
    return graphQLNode;
  }
}
