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

package org.illyasviel.elide.spring.boot.domain;

import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.SharePermission;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author olOwOlo
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Entity
@Include(rootLevel = true)
@SharePermission
public class Book {

  private Integer id;
  private String name;
  private Short price;
  private Author author;
  private Integer uniqueNumber;

  @Id
  @GeneratedValue(generator = "book_g", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(name = "book_g", sequenceName = "book_sequence")
  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Short getPrice() {
    return price;
  }

  @ManyToOne
  public Author getAuthor() {
    return author;
  }

  @Column(unique = true)
  public Integer getUniqueNumber() {
    return uniqueNumber;
  }

}
