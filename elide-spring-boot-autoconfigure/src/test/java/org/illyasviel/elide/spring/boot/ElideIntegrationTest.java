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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(classes = TApplication.class)
public class ElideIntegrationTest {
  private static final Logger logger = LoggerFactory.getLogger(ElideIntegrationTest.class);

  final static String JSON_API_CONTENT_TYPE = "application/vnd.api+json";

  // TODO should without any media type parameters
  final static String JSON_API_RESPONSE = JSON_API_CONTENT_TYPE + ";charset=UTF-8";

  private static boolean isPrepareFinish = false;

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext wac;

  @Before
  public void before() throws Exception {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

    if (isPrepareFinish) {
      return;
    }
    isPrepareFinish = true;

    for (int i = 0; i < 5; i++) {
      String postAuthor = "{ \"data\": { \"type\": \"author\", \"attributes\": { \"firstName\": \"firstName\", \"lastName\": \"lastName\", \"age\": \"" + (i + 18) + "\" } } }";

      mockMvc.perform(post("/api/author")
          .contentType(JSON_API_CONTENT_TYPE)
          .content(postAuthor)
          .accept(JSON_API_CONTENT_TYPE))
          .andExpect(content().contentType(JSON_API_RESPONSE))
          .andExpect(status().isCreated());
      logger.debug("Insert an Author with age" + (i + 18));

      for (int j = 0; j < 10; j++) {
        String postBook = "{\"data\": {\"type\": \"book\",\"attributes\": {\"name\": \"bookName\",\"price\": \"" + (100 * i + j) + "\"},\"relationships\": {\"author\": {\"data\": {\"type\": \"author\",\"id\": \"" + (i + 1) + "\"}}}}}";

        mockMvc.perform(post("/api/book")
            .contentType(JSON_API_CONTENT_TYPE)
            .content(postBook)
            .accept(JSON_API_CONTENT_TYPE))
            .andExpect(content().contentType(JSON_API_RESPONSE))
            .andExpect(status().isCreated());
        logger.debug("Insert an Book with price" + (100 * i + j));
      }
    }
  }

  @Test
  public void test1APrepareData() {
    // do nothing, init data
  }

  @Test
  public void testRootEntityFormulaFetch() throws Exception {
    mockMvc.perform(get("/api/book?fields[book]=name,price&page[limit]=3&page[totals]")
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(content().contentType(JSON_API_RESPONSE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].attributes.name").value("bookName"))
        .andExpect(jsonPath("$.data.length()").value(3))
        .andExpect(jsonPath("$.meta.page.number").value(1))
        .andExpect(jsonPath("$.meta.page.limit").value(3))
        .andExpect(jsonPath("$.meta.page.totalRecords").value(50))
        .andExpect(jsonPath("$.meta.page.totalPages").value(17));
  }

  @Test
  public void testSubCollectionEntityFormulaFetch() throws Exception {
    mockMvc.perform(get("/api/book/1/author")
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(content().contentType(JSON_API_RESPONSE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.type").value("author"))
        .andExpect(jsonPath("$.data.attributes.age").value(18))
        .andExpect(jsonPath("$.data.relationships.books.data.length()").value(10));
  }

  @Test
  public void testRootEntityFormulaWithFilter() throws Exception {
    mockMvc.perform(get("/api/book?filter[book]=price>=400")
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(content().contentType(JSON_API_RESPONSE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].attributes.name").value("bookName"))
        .andExpect(jsonPath("$.data.length()").value(10));
  }

  @Test
  public void testSubCollectionEntityFormulaWithFilter() throws Exception {
    mockMvc.perform(get("/api/author/1/books?filter[book]=price>=8&page[limit]=1&page[totals]")
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(content().contentType(JSON_API_RESPONSE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].attributes.name").value("bookName"))
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.meta.page.number").value(1))
        .andExpect(jsonPath("$.meta.page.limit").value(1))
        .andExpect(jsonPath("$.meta.page.totalRecords").value(2))
        .andExpect(jsonPath("$.meta.page.totalPages").value(2));
  }

  @Test
  public void testRootEntityFormulaWithSorting() throws Exception {
    mockMvc.perform(get("/api/book?sort=-price&page[limit]=3")
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(content().contentType(JSON_API_RESPONSE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].attributes.name").value("bookName"))
        .andExpect(jsonPath("$.data[0].attributes.price").value(409))
        .andExpect(jsonPath("$.data.length()").value(3));
  }

  @Test
  public void testSubCollectionEntityFormulaWithSorting() throws Exception {
    mockMvc.perform(get("/api/author/1/books?sort=-price")
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(content().contentType(JSON_API_RESPONSE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].attributes.name").value("bookName"))
        .andExpect(jsonPath("$.data[0].attributes.price").value(9))
        .andExpect(jsonPath("$.data.length()").value(10));
  }

  @Transactional
  @Test
  public void testRootEntityUpdateAttributes() throws Exception {
    String patchBook = "{ \"data\": { \"type\": \"book\", \"id\": \"1\", \"attributes\": { \"name\": \"root name\" } } }";
    mockMvc.perform(patch("/api/book/1")
        .contentType(JSON_API_CONTENT_TYPE)
        .content(patchBook)
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/book/1")
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(content().contentType(JSON_API_RESPONSE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.attributes.name").value("root name"));
  }

  @Transactional
  @Test
  public void testSubEntityUpdateAttributes() throws Exception {
    String patchBook = "{ \"data\": { \"type\": \"book\", \"id\": \"1\", \"attributes\": { \"name\": \"sub name\" } } }";
    mockMvc.perform(patch("/api/author/1/books/1")
        .contentType(JSON_API_CONTENT_TYPE)
        .content(patchBook)
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/book/1")
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(content().contentType(JSON_API_RESPONSE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.attributes.name").value("sub name"));
  }

  @Transactional
  @Test
  public void testSubEntityUpdateRelationship() throws Exception {
    String patchBook = "{ \"data\": { \"type\": \"author\", \"id\": 2 } }";
    mockMvc.perform(patch("/api/author/1/books/1/relationships/author")
        .contentType(JSON_API_CONTENT_TYPE)
        .content(patchBook)
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/book/1")
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(content().contentType(JSON_API_RESPONSE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.relationships.author.data.id").value(2));
  }

  @Transactional
  @Test
  public void testRootEntityDeleteEntity() throws Exception {
    mockMvc.perform(delete("/api/book/1")
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/book/1")
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(content().contentType(JSON_API_RESPONSE))
        .andExpect(status().isNotFound());
  }

  @Transactional
  @Test
  public void testSubEntityDeleteEntity() throws Exception {
    mockMvc.perform(delete("/api/author/1/books/2")
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/book/2")
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(content().contentType(JSON_API_RESPONSE))
        .andExpect(status().isNotFound());
  }

  @Transactional
  @Test
  public void testSubEntityDeleteRelationship() throws Exception {
    String deleteBookRelation = "{ \"data\": { \"type\": \"author\", \"id\": \"1\" } }";
    mockMvc.perform(delete("/api/author/1/books/1/relationships/author")
        .contentType(JSON_API_CONTENT_TYPE)
        .content(deleteBookRelation)
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/book/1")
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(content().contentType(JSON_API_RESPONSE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.relationships.author.data").doesNotExist());
  }

  @Test
  public void testInvalidIdentifier() throws Exception {
    mockMvc.perform(delete("/api/author/1/books/99999")
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0]").isString());
  }

  @Transactional
  @Test
  public void testPersistenceException() throws Exception {
    String postBook = "{\"data\": {\"type\": \"book\",\"attributes\": {\"name\": \"bookName\",\"price\": \"100\",\"uniqueNumber\": \"1\"},\"relationships\": {\"author\": {\"data\": {\"type\": \"author\",\"id\": \"1\"}}}}}";

    mockMvc.perform(post("/api/book")
        .contentType(JSON_API_CONTENT_TYPE)
        .content(postBook)
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(content().contentType(JSON_API_RESPONSE))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/api/book")
        .contentType(JSON_API_CONTENT_TYPE)
        .content(postBook)
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(content().contentType(JSON_API_RESPONSE))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  public void testWrongPrefix() throws Exception {
    mockMvc.perform(get("/book")
        .accept(JSON_API_CONTENT_TYPE))
        .andExpect(status().isNotFound());
  }
}
